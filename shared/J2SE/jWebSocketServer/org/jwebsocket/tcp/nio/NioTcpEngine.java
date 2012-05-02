//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket NIO Engine
//	Copyright (c) 2011 Innotrade GmbH, jWebSocket.org, Author: Jan Gnezda
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.tcp.nio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.*;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.tcp.EngineUtils;
import org.jwebsocket.util.Tools;

/**
 * <p> Tcp engine that uses java non-blocking io api to bind to listening port
 * and handle incoming/outgoing packets. There's one 'selector' thread that is
 * responsible only for handling socket operations. Therefore, every packet that
 * should be sent will be firstly queued into concurrent queue, which is
 * continuously processed by selector thread. Since the queue is concurrent,
 * there's no blocking and a call to send method will return immediately. </p>
 * <p> All packets that are received from remote clients are processed in
 * separate worker threads. This way it's possible to handle many clients
 * simultaneously with just a few threads. Add more worker threads to handle
 * more clients. </p> <p> Before making any changes to this source, note this:
 * it is highly advisable to read from (or write to) a socket only in selector
 * thread. Ignoring this advice may result in strange consequences (threads
 * locking or spinning, depending on actual scenario). </p>
 *
 * @author jang
 * @author kyberneees (bug fixes, session identifier cookie support and
 * performance inprovements)
 */
public class NioTcpEngine extends BaseEngine {

	private static Logger mLog = Logging.getLogger();
	private static final String NUM_WORKERS = "workers";
	private static final int DEFAULT_NUM_WORKERS = 100;
	private static final int READ_QUEUE_MAX_SIZE = Integer.MAX_VALUE;
	private Selector mSelector;
	private ServerSocketChannel mServerSocketChannel;
	private boolean mIsRunning;
	private Map<String, Queue<DataFuture>> mPendingWrites; // <connector id, data queue>
	private BlockingQueue<ReadBean> mPendingReads;
	//worker threads
	private ExecutorService mExecutorService;
	//convenience maps
	private Map<String, SocketChannel> mConnectorToChannelMap; // <connector id, socket channel>
	private Map<SocketChannel, String> mChannelToConnectorMap; // <socket channel, connector id>
	private ByteBuffer mReadBuffer;
	private Thread mSelectorThread;
	private DelayedPacketsQueue mDelayedPacketsQueue;

	public NioTcpEngine(EngineConfiguration aConfiguration) {
		super(aConfiguration);
	}

	@Override
	public void startEngine() throws WebSocketException {
		try {
			mDelayedPacketsQueue = new DelayedPacketsQueue();
			mPendingWrites = new ConcurrentHashMap<String, Queue<DataFuture>>();
			mPendingReads = new LinkedBlockingQueue<ReadBean>(READ_QUEUE_MAX_SIZE);
			mConnectorToChannelMap = new ConcurrentHashMap<String, SocketChannel>();
			mChannelToConnectorMap = new ConcurrentHashMap<SocketChannel, String>();
			mReadBuffer = ByteBuffer.allocate(getConfiguration().getMaxFramesize());
			mSelector = Selector.open();
			mServerSocketChannel = ServerSocketChannel.open();
			mServerSocketChannel.configureBlocking(false);
			ServerSocket socket = mServerSocketChannel.socket();
			socket.bind(new InetSocketAddress(getConfiguration().getPort()));
			mServerSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);
			mIsRunning = true;

			// start worker threads
			Integer lNumWorkers = DEFAULT_NUM_WORKERS;
			if (getConfiguration().getSettings().containsKey(NUM_WORKERS)) {
				lNumWorkers = Integer.parseInt(getConfiguration().
						getSettings().
						get(NUM_WORKERS).
						toString());
			}
			mExecutorService = Executors.newFixedThreadPool(lNumWorkers);
			for (int lIdx = 0; lIdx < lNumWorkers; lIdx++) {
				// give an index to each worker thread
				mExecutorService.submit(new ReadWorker(lIdx));
			}

			// start selector thread
			mSelectorThread = new Thread(new SelectorThread());
			mSelectorThread.start();
		} catch (IOException e) {
			throw new WebSocketException(e.getMessage(), e);
		}
	}

	@Override
	public void stopEngine(CloseReason aCloseReason) throws WebSocketException {
		super.stopEngine(aCloseReason);
		if (mSelector != null) {
			try {
				mIsRunning = false;
				mSelectorThread.join();
				mSelector.wakeup();
				mServerSocketChannel.close();
				mSelector.close();
				mPendingWrites.clear();
				mPendingReads.clear();
				mDelayedPacketsQueue.clear();
				mExecutorService.shutdown();
				mLog.info("NIO engine stopped.");
			} catch (InterruptedException lEx) {
				throw new WebSocketException(lEx.getMessage(), lEx);
			} catch (IOException lEx) {
				throw new WebSocketException(lEx.getMessage(), lEx);
			}
		}
	}

	public void send(String aConnectorId, DataFuture aFuture) {
		if (mPendingWrites.containsKey(aConnectorId)) {
			mPendingWrites.get(aConnectorId).add(aFuture);
			// Wake up waiting selector.
			mSelector.wakeup();
		} else {
			aFuture.setFailure(new Exception("Discarding packet for unattached socket channel..."));
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConn, CloseReason aCloseReason) {
		mPendingWrites.remove(aConn.getId());

		if (mConnectorToChannelMap.containsKey(aConn.getId())) {
			SocketChannel lChannel = mConnectorToChannelMap.remove(aConn.getId());
			try {
				lChannel.socket().close();
				lChannel.close();
			} catch (Exception lEx) {
				mLog.error(lEx.getClass().getSimpleName() + " (connectorStopped): " + lEx.getMessage());
			}
			mChannelToConnectorMap.remove(lChannel);
		}

		if (((NioTcpConnector) aConn).isAfterHandshake()) {
			super.connectorStopped(aConn, aCloseReason);
		}
	}

	/**
	 * Socket operations are permitted only via this thread. Strange behaviour
	 * will occur if anything is done to the socket outside of this thread.
	 */
	private class SelectorThread implements Runnable {

		@Override
		public void run() {
			Thread.currentThread().setName("jWebSocket NIO-Engine SelectorThread");

			engineStarted();

			while (mIsRunning && mSelector.isOpen()) {
				for (Iterator<String> lIterator = mPendingWrites.keySet().iterator(); lIterator.hasNext();) {
					String lConnectorId = lIterator.next();
					try {
						SelectionKey lKey = mConnectorToChannelMap.get(lConnectorId).keyFor(mSelector);
						if (!mPendingWrites.get(lConnectorId).isEmpty() && lKey.isValid()) {
							lKey.interestOps(SelectionKey.OP_WRITE);
						}
					} catch (Exception ex) {
						// ignore, key was cancelled an instant after isValid() returned true,
						// most probably the client disconnected just at the wrong moment
					}
				}

				try {
					// Waits for 100ms for any data from connected clients or for new client connections.
					// We could have indefinite wait (selector.wait()), but it is good to check for 'running' variable
					// fairly often.
					if (mSelector.select(100) > 0 && mIsRunning) {
						Iterator<SelectionKey> lKeys = mSelector.selectedKeys().iterator();
						while (lKeys.hasNext()) {
							SelectionKey lKey = lKeys.next();
							lKeys.remove();
							if (lKey.isValid()) {
								try {
									if (lKey.isAcceptable()) {
										//accept new client connection
										accept(lKey);
									} else {
										if (lKey.isReadable()) {
											read(lKey);
										}
										if (lKey.isWritable()) {
											write(lKey);
										}
									}
								} catch (CancelledKeyException lCKEx) {
									// ignore, key was cancelled an instant after isValid() returned true,
									// most probably the client disconnected just at the wrong moment
								}
							}
						}
					} else {
						// nothing happened, continue looping ...
						mLog.trace("No data on listen port in 500ms timeout ...");
					}
				} catch (Exception lEx) {
					// something happened during socket operation (select, read or write), just log it
					mLog.error("Error during socket operation", lEx);
				}
			}
			engineStopped();
		}
	}

	// this must be called only from selector thread
	private void write(SelectionKey aKey) throws IOException {
		SocketChannel lSocketChannel = (SocketChannel) aKey.channel();
		Queue<DataFuture> lQueue = mPendingWrites.get(mChannelToConnectorMap.get(lSocketChannel));

		while (!lQueue.isEmpty()) {
			DataFuture future = lQueue.peek();
			try {
				ByteBuffer lData = future.getData();
				lSocketChannel.write(lData);
				if (lData.remaining() > 0) {
					// socket's buffer is full, stop writing for now and leave the remaining
					// data in queue for another round of writing
					break;
				}
			} catch (IOException lIOEx) {
				future.setFailure(lIOEx);
				// don't throw exception here
				// pending close packets are maybe in reading queue
				// some connectors could be not stopped yet
				//throw lIOEx;
			}

			future.setSuccess();
			// remove the head element of the queue
			lQueue.poll();
		}

		aKey.interestOps(SelectionKey.OP_READ);
	}

	// this must be called only from selector thread
	private void accept(SelectionKey aKey) throws IOException {
		try {
			if (getConnectors().size() == getConfiguration().getMaxConnections()
					&& getConfiguration().getOnMaxConnectionStrategy().equals("close")) {
				aKey.channel().close();
				aKey.cancel();
				mLog.info("NIO client (" + ((ServerSocketChannel) aKey.channel()).socket().getInetAddress()
						+ ") not accepted due to max connections reached. Connection closed!");
			} else {
				SocketChannel lSocketChannel = ((ServerSocketChannel) aKey.channel()).accept();
				lSocketChannel.configureBlocking(false);
				lSocketChannel.register(mSelector, SelectionKey.OP_READ);
				WebSocketConnector lConnector = new NioTcpConnector(
						this, lSocketChannel.socket().getInetAddress(),
						lSocketChannel.socket().getPort());
				getConnectors().put(lConnector.getId(), lConnector);
				mPendingWrites.put(lConnector.getId(), new ConcurrentLinkedQueue<DataFuture>());
				mConnectorToChannelMap.put(lConnector.getId(), lSocketChannel);
				mChannelToConnectorMap.put(lSocketChannel, lConnector.getId());
				mLog.info("NIO client started - remote ip: " + lConnector.getRemoteHost());
			}
		} catch (IOException e) {
			mLog.warn("Could not start new client connection");
			throw e;
		}
	}

	// this must be called only from selector thread
	private void read(SelectionKey aKey) throws IOException {
		SocketChannel lSocketChannel = (SocketChannel) aKey.channel();
		mReadBuffer.clear();

		int lNumRead;
		try {
			lNumRead = lSocketChannel.read(mReadBuffer);
		} catch (IOException lIOEx) {
			// remote client probably disconnected uncleanly ?
			clientDisconnect(aKey);
			return;
		}

		if (lNumRead == -1) {
			// read channel closed, connection has ended
			clientDisconnect(aKey);
			return;
		}

		if (lNumRead > 0 && mChannelToConnectorMap.containsKey(lSocketChannel)) {
			String lConnectorId = mChannelToConnectorMap.get(lSocketChannel);
			ReadBean lBean = new ReadBean(lConnectorId, Arrays.copyOf(mReadBuffer.array(), lNumRead));
			boolean lAccepted = mPendingReads.offer(lBean);
			if (!lAccepted) {
				// Read queue is full, discard the packet.
				// This may happen under continuous heavy load (plugins cannot process packets in time) or
				// if all worker threads are locked up (perhaps a rogue plugin is blocking packet processing).
				mLog.warn("Engine read queue is full, discarding incoming packet");
			}
		}
	}

	private void clientDisconnect(SelectionKey aKey) throws IOException {
		clientDisconnect(aKey, CloseReason.CLIENT);
	}

	private void clientDisconnect(SelectionKey aKey, CloseReason aReason) throws IOException {
		SocketChannel lChannel = (SocketChannel) aKey.channel();
		aKey.cancel();
		aKey.channel().close();
		if (mChannelToConnectorMap.containsKey(lChannel)) {
			String lId = mChannelToConnectorMap.remove(lChannel);
			if (lId != null) {
				mConnectorToChannelMap.remove(lId);

				connectorStopped(getConnectors().get(lId), aReason);
			}
		}
	}

	private void clientDisconnect(WebSocketConnector aConnector) throws IOException {
		clientDisconnect(aConnector, CloseReason.CLIENT);
	}

	private void clientDisconnect(WebSocketConnector aConnector,
			CloseReason aReason) throws IOException {
		if (mConnectorToChannelMap.containsKey(aConnector.getId())) {
			clientDisconnect(mConnectorToChannelMap.get(aConnector.getId()).keyFor(mSelector), aReason);
		}
	}

	private class ReadWorker implements Runnable {

		int mId = -1;

		public ReadWorker(int aId) {
			super();
			mId = aId;
		}

		@Override
		public void run() {
			Thread.currentThread().setName("jWebSocket NIO-Engine ReadWorker " + this.mId);
			while (mIsRunning) {
				try {
					IDelayedPacketNotifier mDelayedPacket = mDelayedPacketsQueue.pop();
					if (null != mDelayedPacket) {
						mDelayedPacket.handleDelayedPacket();
						continue;
					}

					final ReadBean lBean = mPendingReads.poll(200, TimeUnit.MILLISECONDS);
					if (lBean != null) {
						if (getConnectors().containsKey(lBean.getConnectorId())) {

							final NioTcpConnector lConnector = (NioTcpConnector) getConnectors().get(lBean.getConnectorId());
							if (lConnector.getWorkerId() > -1) {
								// another worker is right in the middle of packet processing for this connector
								// putting packets in a queue for high concurrency scenarios
								mDelayedPacketsQueue.addDelayedPacket(new IDelayedPacketNotifier() {

									@Override
									public void handleDelayedPacket() throws IOException {
										doRead(getConnector(), getBean());
									}

									@Override
									public NioTcpConnector getConnector() {
										return lConnector;
									}

									@Override
									public ReadBean getBean() {
										return lBean;
									}
								});
							} else {
								doRead(lConnector, lBean);
							}
						} else {
							// connector was already closed ...
							mLog.debug("Discarding incoming packet, because there's no connector to process it");
						}
					}
				} catch (InterruptedException e) {
					// Ignore this exception -- waiting was interrupted, probably during engine stop ...
					break;
				} catch (Exception e) {
					// uncaught exception during packet processing - kill the worker (todo: think about worker restart)
					mLog.error("Unexpected exception during incoming packet processing", e);
					break;
				}
			}
		}

		private void doRead(NioTcpConnector aConnector, ReadBean aBean) throws IOException {
			// settign the worker identifier
			aConnector.setWorkerId(hashCode());

			if (aConnector.isAfterHandshake()) {
				boolean lIsHixie = aConnector.isHixie();
				if (lIsHixie) {
					readHixie(aBean.getData(), aConnector);
				} else {
					// assume that #02 and #03 are the same regarding packet processing
					readHybi(aConnector.getVersion(), aBean.getData(), aConnector);
				}
			} else {
				// checking if "max connnections" value has been reached
				if (getConnectors().size() > getConfiguration().getMaxConnections()) {
					if (getConfiguration().getOnMaxConnectionStrategy().equals("reject")) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("NIO client not accepted due to max connections reached."
									+ " Connection rejected!");
						}
						clientDisconnect(aConnector, CloseReason.SERVER_REJECT_CONNECTION);
					} else {
						if (mLog.isDebugEnabled()) {
							mLog.debug("NIO client not accepted due to max connections reached."
									+ " Connection redirected!");
						}
						clientDisconnect(aConnector, CloseReason.SERVER_REDIRECT_CONNECTION);
					}
				} else {

					// todo: consider ssl connections
					Map lReqMap = WebSocketHandshake.parseC2SRequest(aBean.getData(), false);

					EngineUtils.parseCookies(lReqMap);
					//Setting the session identifier cookie if not present previously
					if (!((Map) lReqMap.get(RequestHeader.WS_COOKIES)).containsKey(JWebSocketCommonConstants.SESSIONID_COOKIE_NAME)) {
						((Map) lReqMap.get(RequestHeader.WS_COOKIES)).put(JWebSocketCommonConstants.SESSIONID_COOKIE_NAME, Tools.getMD5(UUID.randomUUID().toString()));
					}

					byte[] lResponse = WebSocketHandshake.generateS2CResponse(lReqMap);
					RequestHeader lReqHeader = EngineUtils.validateC2SRequest(
							getConfiguration().getDomains(), lReqMap, mLog);
					if (lResponse == null || lReqHeader == null) {
						if (mLog.isDebugEnabled()) {
							mLog.warn("TCP-Engine detected illegal handshake.");
						}
						// disconnect the client
						clientDisconnect(aConnector);
					}

					//Setting the session identifier
					aConnector.getSession().setSessionId(lReqHeader.getCookies().get(JWebSocketCommonConstants.SESSIONID_COOKIE_NAME).toString());

					send(aConnector.getId(), new DataFuture(aConnector, ByteBuffer.wrap(lResponse)));
					int lTimeout = lReqHeader.getTimeout(getSessionTimeout());
					if (lTimeout > 0) {
						mConnectorToChannelMap.get(aBean.getConnectorId()).socket().setSoTimeout(lTimeout);
					}
					aConnector.handshakeValidated();
					aConnector.setHeader(lReqHeader);
					aConnector.startConnector();
					aConnector.releaseWorker();
				}
			}
		}
	}

	private void readHybi(int aVersion, byte[] aBuffer, NioTcpConnector aConnector) throws IOException {
		try {
			WebSocketPacket lRawPacket;
			//if (aConnector.isPacketBufferEmpty()) {
			lRawPacket = WebSocketProtocolAbstraction.protocolToRawPacket(aVersion, new ByteArrayInputStream(aBuffer));
			if (lRawPacket.getFrameType() == WebSocketFrameType.PING) {
				// As per spec, server must respond to PING with PONG (maybe
				// this should be handled higher up in the hierarchy?)
				WebSocketPacket lPong = new RawPacket(lRawPacket.getByteArray());
				lPong.setFrameType(WebSocketFrameType.PONG);
				aConnector.sendPacket(lPong);
			} else if (lRawPacket.getFrameType() == WebSocketFrameType.CLOSE) {
				// As per spec, server must respond to CLOSE with acknowledgment CLOSE (maybe
				// this should be handled higher up in the hierarchy?)
				WebSocketPacket lClose = new RawPacket(lRawPacket.getByteArray());
				lClose.setFrameType(WebSocketFrameType.CLOSE);
				aConnector.sendPacket(lClose);
				clientDisconnect(aConnector, CloseReason.CLIENT);
			} else if (lRawPacket.getFrameType() == WebSocketFrameType.TEXT) {
				aConnector.flushPacket(lRawPacket);
			} else if (lRawPacket.getFrameType() == WebSocketFrameType.INVALID) {
				mLog.debug(getClass().getSimpleName() + ": Discarding invalid incoming packet... ");
			} else if (lRawPacket.getFrameType() == WebSocketFrameType.FRAGMENT
					|| lRawPacket.getFrameType() == WebSocketFrameType.BINARY) {
				mLog.debug(getClass().getSimpleName() + ": Discarding unsupported ('"
						+ lRawPacket.getFrameType().toString() + "')incoming packet... ");
			}
		} catch (Exception e) {
			mLog.error("(other) " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			clientDisconnect(aConnector, CloseReason.SERVER);
		}
	}

	private void readHixie(byte[] aBuffer, NioTcpConnector lConnector) throws IOException {
		ByteArrayInputStream lIn = new ByteArrayInputStream(aBuffer);
		ByteArrayOutputStream lBuff = new ByteArrayOutputStream();

		while (true) {
			try {
				int lByte = WebSocketProtocolAbstraction.read(lIn);
				// start of frame
				if (lByte == 0x00) {
					lBuff.reset();
					// end of frame
				} else if (lByte == 0xFF) {
					RawPacket lPacket = new RawPacket(lBuff.toByteArray());
					try {
						lConnector.flushPacket(lPacket);
					} catch (Exception lEx) {
						mLog.error(lEx.getClass().getSimpleName()
								+ " in processPacket of connector "
								+ lConnector.getClass().getSimpleName()
								+ ": " + lEx.getMessage());
					}
					break;
				} else {
					lBuff.write(lByte);
				}
			} catch (Exception lEx) {
				mLog.error("Error while processing incoming packet", lEx);
				clientDisconnect(lConnector, CloseReason.SERVER);
				break;
			}
		}
	}
}
