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

import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.*;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.tcp.EngineUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * <p>
 * Tcp engine that uses java non-blocking io api to bind to listening port and handle incoming/outgoing packets.
 * There's one 'selector' thread that is responsible only for handling socket operations. Therefore, every packet that
 * should be sent will be firstly queued into concurrent queue, which is continuously processed by selector thread.
 * Since the queue is concurrent, there's no blocking and a call to send method will return immediately.
 * </p>
 * <p>
 * All packets that are received from remote clients are processed in separate worker threads. This way it's possible to
 * handle many clients simultaneously with just a few threads. Add more worker threads to handle more clients.
 * </p>
 * <p>
 * Before making any changes to this source, note this: it is highly advisable to read from (or write to) a socket
 * only in selector thread. Ignoring this advice may result in strange consequences (threads locking or
 * spinning, depending on actual scenario).
 * </p>
 *
 * @author jang
 */
public class NioTcpEngine extends BaseEngine {

	private static Logger mLog = Logging.getLogger(NioTcpEngine.class);
	// TODO: move following constants to settings
	private static final int READ_BUFFER_SIZE = 2048;
	private static final int NUM_WORKERS = 3;
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

	public NioTcpEngine(EngineConfiguration aConfiguration) {
		super(aConfiguration);
	}

	@Override
	public void startEngine() throws WebSocketException {
		try {
			mPendingWrites = new ConcurrentHashMap<String, Queue<DataFuture>>();
			mPendingReads = new LinkedBlockingQueue<ReadBean>(READ_QUEUE_MAX_SIZE);
			mConnectorToChannelMap = new ConcurrentHashMap<String, SocketChannel>();
			mChannelToConnectorMap = new ConcurrentHashMap<SocketChannel, String>();
			mReadBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
			mSelector = Selector.open();
			mServerSocketChannel = ServerSocketChannel.open();
			mServerSocketChannel.configureBlocking(false);
			ServerSocket socket = mServerSocketChannel.socket();
			socket.bind(new InetSocketAddress(getConfiguration().getPort()));
			mServerSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);
			mIsRunning = true;

			// start worker threads
			mExecutorService = Executors.newFixedThreadPool(NUM_WORKERS);
			for (int lIdx = 0; lIdx < NUM_WORKERS; lIdx++) {
				// give an index to each worker thread
				mExecutorService.submit(new ReadWorker(lIdx));
			}

			// start selector thread
			new Thread(new SelectorThread()).start();
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
				mSelector.wakeup();
				mServerSocketChannel.close();
				mSelector.close();
				mPendingWrites.clear();
				// mPendingReads.notifyAll();
				mPendingReads.clear();
				mExecutorService.shutdown();
				mLog.info("NIO engine stopped.");
			} catch (IOException lIOEx) {
				throw new WebSocketException(lIOEx.getMessage(), lIOEx);
			}
		}
	}

	public void send(String aConnectorId, DataFuture aFuture) {
		if (mPendingWrites.containsKey(aConnectorId)) {
			mPendingWrites.get(aConnectorId).add(aFuture);
			// Wake up waiting selector.
			mSelector.wakeup();
		} else {
			mLog.debug("Discarding packet for unattached socket channel, remote client is: "
					+ getConnectors().get(aConnectorId).getRemoteHost());
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConn, CloseReason aCloseReason) {
		Queue<DataFuture> lQueue = mPendingWrites.remove(aConn.getId());
		if (lQueue != null) {
			lQueue.clear();
		}

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

		super.connectorStopped(aConn, aCloseReason);
	}

	/**
	 * Socket operations are permitted only via this thread. Strange behaviour will occur if anything is done to the
	 * socket outside of this thread.
	 */
	private class SelectorThread implements Runnable {

		@Override
		public void run() {
			Thread.currentThread().setName("jWebSocket NIO-Engine SelectorThread");

			engineStarted();

			while (mIsRunning && mSelector.isOpen()) {
				// check if there's anything to write to any of the clients
				for (String id : mPendingWrites.keySet()) {
					if (!mPendingWrites.get(id).isEmpty()) {
						mConnectorToChannelMap.get(id).keyFor(mSelector).interestOps(SelectionKey.OP_WRITE);
					}
				}

				try {
					// Waits for 500ms for any data from connected clients or for new client connections.
					// We could have indefinite wait (selector.wait()), but it is good to check for 'running' variable
					// fairly often.
					if (mSelector.select(500) > 0 && mIsRunning) {
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
		while (null != lQueue && !lQueue.isEmpty()) {
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
				throw lIOEx;
			}

			future.setSuccess();
			// remove the head element of the queue
			lQueue.poll();
		}

		if (lQueue.isEmpty()) {
			aKey.interestOps(SelectionKey.OP_READ);
		}
	}

	// this must be called only from selector thread
	private void accept(SelectionKey aKey) throws IOException {
		try {
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
			mLog.info("NIO Client accepted - remote ip: " + lConnector.getRemoteHost());
		} catch (IOException e) {
			mLog.warn("Could not accept new client connection");
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
			ReadBean lBean = new ReadBean();
			lBean.connectorId = lConnectorId;
			lBean.data = Arrays.copyOf(mReadBuffer.array(), lNumRead);
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

	private class ReadBean {

		String connectorId;
		byte[] data;
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
					final ReadBean lBean = mPendingReads.poll(200, TimeUnit.MILLISECONDS);
					if (lBean != null) {
						if (getConnectors().containsKey(lBean.connectorId)) {
							final NioTcpConnector lConnector = (NioTcpConnector) getConnectors().get(lBean.connectorId);
							if (lConnector.getWorkerId() > -1 && lConnector.getWorkerId() != hashCode()) {
								// another worker is right in the middle of packet processing for this connector
								lConnector.setDelayedPacketNotifier(new DelayedPacketNotifier() {

									@Override
									public void handleDelayedPacket() throws IOException {
										doRead(lConnector, lBean);
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
			aConnector.setWorkerId(hashCode());
			if (aConnector.isAfterHandshake()) {
				boolean lIsHixie = aConnector.isHixie();
				if (lIsHixie) {
					readHixie(aBean.data, aConnector);
				} else {
					// assume that #02 and #03 are the same regarding packet processing
					readHybi(aConnector.getVersion(), aBean.data, aConnector);
				}
			} else {
				// todo: consider ssl connections
				Map lHeaders = WebSocketHandshake.parseC2SRequest(aBean.data, false);
				byte[] lResponse = WebSocketHandshake.generateS2CResponse(lHeaders);
				RequestHeader lReqHeader = EngineUtils.validateC2SRequest(
						getConfiguration().getDomains(), lHeaders, mLog);
				if (lResponse == null || lReqHeader == null) {
					if (mLog.isDebugEnabled()) {
						mLog.warn("TCP-Engine detected illegal handshake.");
					}
					// disconnect the client
					clientDisconnect(aConnector);
				}

				send(aConnector.getId(), new DataFuture(aConnector, ByteBuffer.wrap(lResponse)));
				int lTimeout = lReqHeader.getTimeout(getSessionTimeout());
				if (lTimeout > 0) {
					mConnectorToChannelMap.get(aBean.connectorId).socket().setSoTimeout(lTimeout);
				}
				aConnector.handshakeValidated();
				aConnector.setHeader(lReqHeader);
				aConnector.startConnector();
			}
			aConnector.setWorkerId(-1);
		}
	}

	/**
	 *  One message may consist of one or more (fragmented message) protocol packets.
	 *  The spec is currently unclear whether control packets (ping, pong, close) may
	 *  be intermingled with fragmented packets of another message. For now I've
	 *  decided to not implement such packets 'swapping', and therefore reading fails
	 *  miserably if a client sends control packets during fragmented message read.
	 *  TODO: follow next spec drafts and add support for control packets inside fragmented message if needed.
	 *  <p>
	 *  Structure of packets conforms to the following scheme (copied from spec):
	 *  </p>
	 *  <pre>
	 *  0                   1                   2                   3
	 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 * +-+-+-+-+-------+-+-------------+-------------------------------+
	 * |M|R|R|R| opcode|R| Payload len |    Extended payload length    |
	 * |O|S|S|S|  (4)  |S|     (7)     |             (16/63)           |
	 * |R|V|V|V|       |V|             |   (if payload len==126/127)   |
	 * |E|1|2|3|       |4|             |                               |
	 * +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
	 * |     Extended payload length continued, if payload len == 127  |
	 * + - - - - - - - - - - - - - - - +-------------------------------+
	 * |                               |         Extension data        |
	 * +-------------------------------+ - - - - - - - - - - - - - - - +
	 * :                                                               :
	 * +---------------------------------------------------------------+
	 * :                       Application data                        :
	 * +---------------------------------------------------------------+
	 * </pre>
	 * RSVx bits are ignored (reserved for future use).
	 * TODO: add support for extension data, when extensions will be defined in the specs.
	 *
	 * <p>
	 * Read section 4.2 of the spec for detailed explanation.
	 * </p>
	 */
	private void readHybi(int aVersion, byte[] aBuffer, NioTcpConnector aConnector) throws IOException {
		try {
			if (aConnector.isPacketBufferEmpty()) {
				// begin normal packet read
				int lFlags = aBuffer[0];

				// determine fragmentation
				boolean lFragmented = (aVersion >= 4
						? (lFlags & 0x80) == 0x00
						: (lFlags & 0x80) == 0x80);
				boolean lMasked = true;
				int[] lMask = new int[4];

				// ignore upper 4 bits for now
				int lOpcode = lFlags & 0x0F;
				WebSocketFrameType lFrameType = WebSocketProtocolAbstraction.opcodeToFrameType(aVersion, lOpcode);

				// assume we start here
				int lPayloadStartIndex = 2;

				long lPayloadLen = aBuffer[1];
				lMasked = (lPayloadLen & 0x80) == 0x80;
				lPayloadLen &= 0x7F;

				if (lFrameType == WebSocketFrameType.INVALID) {
					// Could not determine packet type, ignore the packet.
					// Maybe we need a setting to decide, if such packets should abort the connection?
					mLog.trace("Dropping packet with unknown type: " + lOpcode);
				} else {
					aConnector.setPacketType(lFrameType);
					if (lPayloadLen == 126) {
						// following two bytes are actual payload length (16-bit unsigned integer)
						lPayloadLen = aBuffer[2] & 0xFF;
						lPayloadLen = (lPayloadLen << 8) | (aBuffer[3] & 0xFF);
						lPayloadStartIndex += 2;
					} else if (lPayloadLen == 127) {
						// following eight bytes are actual payload length (64-bit unsigned integer)
						lPayloadLen = aBuffer[2] & 0xFF;
						lPayloadLen = (lPayloadLen << 8) | (aBuffer[3] & 0xFF);
						lPayloadLen = (lPayloadLen << 8) | (aBuffer[4] & 0xFF);
						lPayloadLen = (lPayloadLen << 8) | (aBuffer[5] & 0xFF);
						lPayloadLen = (lPayloadLen << 8) | (aBuffer[6] & 0xFF);
						lPayloadLen = (lPayloadLen << 8) | (aBuffer[7] & 0xFF);
						lPayloadLen = (lPayloadLen << 8) | (aBuffer[8] & 0xFF);
						lPayloadLen = (lPayloadLen << 8) | (aBuffer[9] & 0xFF);
						lPayloadStartIndex += 8;
					}
					if (lMasked) {
						lMask[0] = aBuffer[lPayloadStartIndex + 0] & 0xFF;
						lMask[1] = aBuffer[lPayloadStartIndex + 1] & 0xFF;
						lMask[2] = aBuffer[lPayloadStartIndex + 2] & 0xFF;
						lMask[3] = aBuffer[lPayloadStartIndex + 3] & 0xFF;
						lPayloadStartIndex += 4;
					}

					if (lPayloadLen > 0) {
						if (lMasked) {
							int lMaskIdx = 0;
							int lBuffIdx = lPayloadStartIndex;
							long lCounter = lPayloadLen;
							while (lCounter-- > 0) {
								aBuffer[lBuffIdx] = (byte) (aBuffer[lBuffIdx] ^ lMask[lMaskIdx]);
								lBuffIdx++;
								lMaskIdx++;
								lMaskIdx &= 3;
							}
						}
						aConnector.setPayloadLength((int) lPayloadLen);
						mLog.debug(
								"aBuffer.length: " + aBuffer.length
								+ ", lPayloadStartIndex: " + lPayloadStartIndex);
						aConnector.extendPacketBuffer(aBuffer, lPayloadStartIndex, (int) lPayloadLen /* aBuffer.length - lPayloadStartIndex */);
					}
				}

				if (lFrameType == WebSocketFrameType.PING) {
					// As per spec, server must respond to PING with PONG (maybe
					// this should be handled higher up in the hierarchy?)
					WebSocketPacket lPong = new RawPacket(aConnector.getPacketBuffer());
					lPong.setFrameType(WebSocketFrameType.PONG);
					aConnector.sendPacket(lPong);
				} else if (lFrameType == WebSocketFrameType.CLOSE) {
					// As per spec, server must respond to CLOSE with acknowledgment CLOSE (maybe
					// this should be handled higher up in the hierarchy?)
					WebSocketPacket lClose = new RawPacket(aConnector.getPacketBuffer());
					lClose.setFrameType(WebSocketFrameType.CLOSE);
					aConnector.sendPacket(lClose);
					clientDisconnect(aConnector, CloseReason.CLIENT);
				}
			} else {
				aConnector.extendPacketBuffer(aBuffer, 0, aBuffer.length);
			}

			if (aConnector.isPacketBufferFull()) {
				// Packet was read, pass it forward.
				aConnector.flushPacketBuffer();
			}
		} catch (Exception e) {
			mLog.error("(other) " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			clientDisconnect(aConnector, CloseReason.SERVER);
		}
	}

	private void readHixie(byte[] buffer, NioTcpConnector connector) throws IOException {
		try {
			int start = 0;
			if (connector.isPacketBufferEmpty() && buffer[0] == 0x00) {
				// start of packet
				start = 1;
			}

			boolean stop = false;
			int count = buffer.length;
			for (int i = start; i < buffer.length; i++) {
				if (buffer[i] == (byte) 0xFF) {
					// end of packet
					count = i - start;
					stop = true;
					break;
				}
			}

			if (start + count > buffer.length) {
				// ignore -> broken packet (perhaps client disconnected in middle of sending
			} else {
				if (connector.isPacketBufferEmpty() && buffer.length == 1) {
					connector.extendPacketBuffer(buffer, 0, 0);
				} else {
					connector.extendPacketBuffer(buffer, start, count);
				}
			}

			if (stop) {
				connector.flushPacketBuffer();
			}
		} catch (Exception e) {
			mLog.error("Error while processing incoming packet", e);
			clientDisconnect(connector, CloseReason.SERVER);
		}
	}
}
