//	---------------------------------------------------------------------------
//	jWebSocket - TCP Connector (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.tcp;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import javax.net.ssl.SSLSocket;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketConnectorStatus;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.*;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.flashbridge.FlashBridgePlugIn;
import org.jwebsocket.util.JWSTimerTask;
import org.jwebsocket.util.Tools;
// import org.krakenapps.pcap.decoder.ethernet.MacAddress;
// import org.krakenapps.pcap.util.Arping;

/**
 * Implementation of the jWebSocket TCP socket connector.
 *
 * @author Alexander Schulze
 * @author jang
 * @author Rolando Santamaria Maso
 * @author Rolando Betancourt Toucet
 */
public class TCPConnector extends BaseConnector {

	private static final Logger mLog = Logging.getLogger();
	private InputStream mIn = null;
	private OutputStream mOut = null;
	private Socket mClientSocket = null;
	private static final int CONNECT_TIMEOUT = 10000; // in ms
	/**
	 *
	 */
	public static final String TCP_LOG = "TCP";
	/**
	 *
	 */
	public static final String SSL_LOG = "SSL";

	/**
	 *
	 */
	public static final String FLASH_POLICY_REQUEST = "policy-file-request";
	private String mLogInfo = TCP_LOG;
	private CloseReason mCloseReason = CloseReason.TIMEOUT;
	private Thread mClientThread = null;
	private TimeoutOutputStreamNIOWriter mOutputStreamNIOSender;
	private final boolean mKeepAlive;
	private final Integer mKeepAliveInterval;
	private final Integer mKeepAliveConnectorsTimeout;
	private S2CPingTimeoutTask mS2CPingTimeoutTask = null;
	private S2CPingIntervalTask mS2CPingIntervalTask = null;
	private long mPingStartedAt;
	private final Object mSyncPing = new Object();

	/**
	 * creates a new TCP connector for the passed engine using the passed client
	 * socket. Usually connectors are instantiated by their engine only, not by
	 * the application.
	 *
	 * @param aEngine
	 * @param aClientSocket
	 */
	public TCPConnector(WebSocketEngine aEngine, Socket aClientSocket) {
		super(aEngine);
		mClientSocket = aClientSocket;
		setSSL(mClientSocket instanceof SSLSocket);
		mLogInfo = isSSL() ? SSL_LOG : TCP_LOG;

		mKeepAlive = aEngine.getConfiguration().getKeepAliveConnectors();
		mKeepAliveInterval = aEngine.getConfiguration().getKeepAliveConnectorsInterval();
		mKeepAliveConnectorsTimeout = aEngine.getConfiguration().getKeepAliveConnectorsTimeout();

		try {
			mIn = mClientSocket.getInputStream();
			mOut = mClientSocket.getOutputStream();

			mOutputStreamNIOSender = new TimeoutOutputStreamNIOWriter(this, mIn, mOut);
		} catch (IOException lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " instantiating "
					+ getClass().getSimpleName() + ": "
					+ lEx.getMessage());
		}
	}

	/**
	 *
	 */
	public void init() {
		ClientProcessor lClientProc = new ClientProcessor(this);
		mClientThread = new Thread(lClientProc);
		mClientThread.start();
	}

	@Override
	public void startConnector() {
		int lPort = -1;
		int lTimeout = -1;
		try {
			lPort = mClientSocket.getPort();
			lTimeout = mClientSocket.getSoTimeout();
		} catch (SocketException lEx) {
			mLog.warn(Logging.getSimpleExceptionMessage(lEx,
					"getting client's socket port and default timeout."));
		}

		/*
		 InetAddress lAddr = mClientSocket.getInetAddress();
		 mLog.debug(
		 "InetAddress: HostAddress: " + lAddr.getHostAddress()
		 + ", HostName: " + lAddr.getHostName()
		 + ", CanonicalHostName: " + lAddr.getCanonicalHostName());
		 SocketAddress lRemoteSocketAddr = mClientSocket.getRemoteSocketAddress();
		 mLog.debug(
		 "RemoteSocketAddress: " + lRemoteSocketAddr.toString());

		 try {
		 InetAddress lTest = InetAddress.getByName(lAddr.getHostAddress());
		 String lHostName = lTest.getHostName();
		 mLog.debug(
		 "Hostname: " + lHostName);
		 } catch (Exception lEx) {
		 mLog.warn(Logging.getSimpleExceptionMessage(lEx,
		 "Obtaining remote host name"));
		 }

		 String lIP4 = lAddr.getHostAddress();
		 MacAddress lMAC;
		 try {
		 lMAC = Arping.query(InetAddress.getByName(lIP4), 2000);
		 // lMAC = Arping.query(lAddr, 2000);
		 // lMAC = Arping.query(lAddr., 2000);
		 mLog.debug("Client (IP: " + lIP4 + ") connected from MAC: " + lMAC.toString());
		 } catch (Exception lEx) {
		 mLog.warn(Logging.getSimpleExceptionMessage(lEx,
		 "obtaining client's MAC address (IP: " + lIP4 + ")."));
		 }
		 */
		String lNodeStr = getNodeId();
		if (lNodeStr != null) {
			lNodeStr = " (unid: " + lNodeStr + ")";
		} else {
			lNodeStr = "";
		}
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting " + mLogInfo + " connector" + lNodeStr + " '"
					+ getId() + "' on port "
					+ lPort + " with timeout "
					+ (lTimeout > 0 ? lTimeout + "ms" : "infinite") + "");
		}

		super.startConnector();

		if (mLog.isInfoEnabled()) {
			mLog.info("Started " + mLogInfo + " connector" + lNodeStr + " '"
					+ getId() + "' on port "
					+ lPort + " with timeout "
					+ (lTimeout > 0 ? lTimeout + "ms" : "infinite") + "");
		}
		if (mKeepAlive) {
			mS2CPingIntervalTask = new S2CPingIntervalTask();
			Tools.getTimer().scheduleAtFixedRate(mS2CPingIntervalTask, mKeepAliveInterval, mKeepAliveInterval);
		}
	}

	/**
	 * This closes all streams, the client socket and shuts down the tread.
	 *
	 * @param aCloseReason
	 */
	private void terminateConnector(CloseReason aCloseReason) {
		setStatus(WebSocketConnectorStatus.DOWN);
		int lPort = mClientSocket.getPort();
		try {
			mOut.close();
		} catch (IOException lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " while closing outbound stream for " + mLogInfo
					+ " connector (" + aCloseReason.name()
					+ ") on port " + lPort + ": " + lEx.getMessage());
		}
		try {
			mIn.close();
		} catch (IOException lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " while closing inbound stream for " + mLogInfo
					+ " connector (" + aCloseReason.name()
					+ ") on port " + lPort + ": " + lEx.getMessage());
		}
		try {
			if (!mClientSocket.isClosed()) {
				mClientSocket.close();
			}
		} catch (IOException lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " while closing socket " + mLogInfo
					+ " connector (" + aCloseReason.name()
					+ ") on port " + lPort + ": " + lEx.getMessage());
		}
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		try {
			mS2CPingIntervalTask.cancel();
		} catch (Exception lEx) {
			// If the task is not running will always come here
		}
		// supporting client "close" command
		String lClientCloseFlag = "connector_was_closed_by_client_demand";
		if (null != getVar(lClientCloseFlag)) {
			return;
		}
		if (aCloseReason.equals(CloseReason.CLIENT)) {
			setVar(lClientCloseFlag, true);
		}

		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Stopping " + mLogInfo
						+ " connector '"
						+ getId() + "' (" + aCloseReason.name() + ")...");
			}
			mCloseReason = aCloseReason;
			synchronized (getWriteLock()) {
				if (!isHixie() && !CloseReason.BROKEN.equals(aCloseReason)) {
					// Hybi specs demand that client must be notified
					// with CLOSE control message before disconnect

					// @TODO the close reason has to be notified to the client
					// following the WebSocket protocol specification for this
					// @see http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-17#page-45
					WebSocketPacket lClose
							= new RawPacket(WebSocketFrameType.CLOSE,
									WebSocketProtocolAbstraction.calcCloseData(
											CloseReason.CLIENT.getCode(),
											CloseReason.CLIENT.name()));

					WebSocketConnectorStatus lStatus = getStatus();
					// to ensure that the close packet can be sent at all!
					setStatus(WebSocketConnectorStatus.UP);
					sendPacket(lClose);
					setStatus(lStatus);
				}
				stopReader();
				terminateConnector(aCloseReason);
			}

			if (mLog.isInfoEnabled()) {
				mLog.info("Stopped " + mLogInfo
						+ " connector '"
						+ getId() + "' (" + mCloseReason
						+ ") on port " + mClientSocket.getPort() + ".");
			}
		} finally {
			super.stopConnector(aCloseReason);
		}
	}

	/**
	 *
	 */
	public void stopReader() {
		try {
			// force input stream to close to terminate reader thread
			if (!mClientSocket.isInputShutdown()
					&& !mClientSocket.isClosed()) {
				if (!(mClientSocket instanceof SSLSocket)) {
					mClientSocket.shutdownInput();
				}
			}
		} catch (IOException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "shutting down reader stream (" + getId() + ")"));
		}
		try {
			// force input stream to close to terminate reader thread
			mIn.close();
		} catch (IOException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "closing reader stream (" + getId() + ")"));
		}
	}

	/*
	 * this is called by the TimeoutOutputstreamWriter, the data is first
	 * written into a queue and then send by a watched thread
	 */
	/**
	 *
	 * @param aDataPacket
	 */
	public synchronized void _sendPacket(WebSocketPacket aDataPacket) {
		try {
			checkBeforeSend(aDataPacket);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "sending packet to '" + getId() + "' connector!"));
			return;
		}

		try {
			if (isHixie()) {
				sendHixie(aDataPacket);
			} else {
				sendHybi(getVersion(), aDataPacket);
			}
			mOut.flush();
		} catch (IOException lEx) {
			// in case a socket gets closed due to a timeout
			// in a write operation, this is not necessarily an error.
			// TODO: think how to eventually better deal with this.
			/*
			 * mLog.error(lEx.getClass().getSimpleName() + " sending data
			 * packet: " + lEx.getMessage());
			 */
		}
	}

	@Override
	public void sendPacket(WebSocketPacket aDataPacket) {
		mOutputStreamNIOSender.sendPacket(aDataPacket);
	}

	@Override
	public IOFuture sendPacketAsync(WebSocketPacket aDataPacket) {
		throw new UnsupportedOperationException("Underlying connector: "
				+ getClass().getName()
				+ " doesn't support asynchronous send operation");
	}

	private void sendHixie(WebSocketPacket aDataPacket) throws IOException {
		// exception handling is done in sendPacket method
		if (aDataPacket.getFrameType() == WebSocketFrameType.BINARY) {
			// each packet is enclosed in 0xFF<length><data>
			// TODO: for future use! Not yet finally spec'd in IETF drafts!
			mOut.write(0xFF);
			byte[] lBA = aDataPacket.getByteArray();
			// TODO: implement multi byte length!
			mOut.write(lBA.length);
			mOut.write(lBA);
			mOut.flush();
		} else {
			// each packet is enclosed in 0x00<data>0xFF
			mOut.write(0x00);
			mOut.write(aDataPacket.getByteArray());
			mOut.write(0xFF);
			mOut.flush();
		}
	}

	// TODO: implement fragmentation for packet sending
	private void sendHybi(int aVersion, WebSocketPacket aDataPacket) throws IOException {
		// exception handling is done in sendPacket method
		WebSocketFrameType lFrameType = aDataPacket.getFrameType();
		if (WebSocketFrameType.CLOSE.equals(lFrameType) // || WebSocketFrameType.PING.equals(lFrameType)
				// || WebSocketFrameType.PONG.equals(lFrameType)
				) {
			int lCode = -1;
			String lText = "[empty]";
			if (aDataPacket.getByteArray().length >= 2) {
				lCode = ((byte) aDataPacket.getByteArray()[0] << 8)
						| ((byte) aDataPacket.getByteArray()[1] & 0xFF);
			}
			if (aDataPacket.getByteArray().length >= 3) {
				lText = new String(Arrays.copyOfRange(aDataPacket.getByteArray(), 2, aDataPacket.getByteArray().length), "UTF-8");
			}
			if (aDataPacket.getByteArray().length > 126) {
				mLog.warn("Control frame with too long content detected (cut to 126 bytes): "
						+ aDataPacket.getFrameType().name()
						+ ", " + aDataPacket.getByteArray().length + " bytes"
						+ ", code: " + lCode
						+ ", text: '" + lText + "'");
				aDataPacket.setByteArray(Arrays.copyOf(aDataPacket.getByteArray(), 126));
			} else {
				mLog.debug("Sending control frame: "
						+ aDataPacket.getFrameType().name()
						+ ", " + aDataPacket.getByteArray().length + " bytes"
						+ ", code: " + lCode
						+ ", text: '" + lText + "'");
			}
		}
		byte[] lPacket = WebSocketProtocolAbstraction.rawToProtocolPacket(
				aVersion, aDataPacket, WebSocketProtocolAbstraction.UNMASKED);
		mOut.write(lPacket);
		mOut.flush();
	}

	@SuppressWarnings("SleepWhileInLoop")
	private RequestHeader processHandshake(Socket aClientSocket)
			throws UnsupportedEncodingException, IOException {

		InputStream lIn = aClientSocket.getInputStream();
		OutputStream lOut = aClientSocket.getOutputStream();

		ByteArrayOutputStream lBAOS = new ByteArrayOutputStream(4096);
		byte[] lBuff = new byte[4096];
		int lRead, lTotal = 0;
		long lStart = System.currentTimeMillis();
		do {
			lRead = lIn.read(lBuff, 0, lBuff.length);
			if (lRead != -1) {
				lTotal += lRead;
				lBAOS.write(lBuff, 0, lRead);
				String lAsStr = lBAOS.toString();
				if (null != lAsStr
						&& (lAsStr.indexOf("\r\n\r\n") > 0
						|| lAsStr.contains(FLASH_POLICY_REQUEST))) {
					break;
				}
			} else {
				try {
					Thread.sleep(20);
				} catch (InterruptedException ex) {
				}
			}
		} while (System.currentTimeMillis() - lStart < CONNECT_TIMEOUT);
		if (lTotal <= 0) {
			mLog.warn(mLogInfo + " connection "
					+ aClientSocket.getInetAddress() + ":"
					+ aClientSocket.getPort()
					+ " did not detect initial handshake (total bytes read: " + lTotal + ").");
			return null;
		}
		byte[] lReq = lBAOS.toByteArray();
		String lReqString = new String(lReq).replace("\r\n", "\\n");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Parsing handshake request: " + lReqString);
		}

		// maybe the request is a flash policy-file-request
		if (null != lReqString && lReqString.contains(FLASH_POLICY_REQUEST)) {
			mLog.warn("Client '"
					+ aClientSocket.getInetAddress() + ":"
					+ aClientSocket.getPort()
					+ "' requests for policy file ('"
					+ lReqString
					+ "'), check for FlashBridge plug-in.");
			String lCrossDomainXML = FlashBridgePlugIn.getCrossDomainXML();
			if (null != lCrossDomainXML && lCrossDomainXML.length() > 0) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Sending flash policy file to '"
							+ aClientSocket.getInetAddress() + ":"
							+ aClientSocket.getPort() + "'");
				}
				byte[] lBA = lCrossDomainXML.getBytes("US-ASCII");
				lOut.write(lBA);
				lOut.flush();
				RequestHeader lHeader = new RequestHeader();
				lHeader.put(RequestHeader.WS_PROTOCOL, lReqString);
				return lHeader;
			}
		}

		Map<String, Object> lReqMap = WebSocketHandshake.parseC2SRequest(
				lReq, aClientSocket instanceof SSLSocket);
		if (lReqMap == null) {
			return null;
		}

		EngineUtils.parseCookies(lReqMap);

		RequestHeader lHeader = EngineUtils.validateC2SRequest(
				getEngine().getConfiguration().getDomains(), lReqMap, mLog,
				aClientSocket);
		if (null == lHeader) {
			return null;
		}

		// generate the websocket handshake
		byte[] lBA = WebSocketHandshake.generateS2CResponse(lReqMap, lHeader);
		if (lBA == null) {
			if (mLog.isDebugEnabled()) {
				mLog.warn(mLogInfo + " connector detected illegal handshake.");
			}
			return null;
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Flushing handshake response: " + new String(lBA).replace("\r\n", "\\n"));
		}

		lOut.write(lBA);
		lOut.flush();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Handshake flushed.");
		}

		return lHeader;
	}

	private class S2CPingTimeoutTask extends JWSTimerTask {

		@Override
		public void runTask() {
			synchronized (mSyncPing) {
				// to allow a new ping timeout task to be created ...
				// set existing one to null
				mS2CPingTimeoutTask = null;

				if (getStatus() != WebSocketConnectorStatus.DOWN) {
					if (mLog.isDebugEnabled()) {
						mLog.debug(
								"Shutting down connector '"
								+ getId()
								+ "' because it didn't respond to a ping");
					}
					mCloseReason = CloseReason.BROKEN;
					try {
						// setStatus(WebSocketConnectorStatus.DOWN);
						mIn.close();
						// stopConnector(CloseReason.BROKEN);
					} catch (IOException ex) {
					}
				}
				// if the connector is shutdown ...
				// we also can cancel the S2C ping
				try {
					mS2CPingIntervalTask.cancel();
				} catch (Exception lEx) {
				}
			}
		}
	}

	private class S2CPingIntervalTask extends JWSTimerTask {

		@Override
		public void runTask() {
			synchronized (mSyncPing) {
				if (mLog.isDebugEnabled()) {
					// String lUsername = getUsername();
					mLog.debug("Sending S2C ping to connector '" + getId() + "'"
							// + " " + (null != lUsername ? "(" + lUsername + ")" : "[not authenticated]")
							+ "...");
				}
				WebSocketPacket lPing = new RawPacket("");
				lPing.setFrameType(WebSocketFrameType.PING);
				sendPacket(lPing);
				// The task needs to be cancelled right after we received a PONG packet
				if (null == mS2CPingTimeoutTask) {
					mPingStartedAt = new Date().getTime();
					mS2CPingTimeoutTask = new S2CPingTimeoutTask();
					Tools.getTimer().schedule(mS2CPingTimeoutTask, mKeepAliveConnectorsTimeout);
				}
			}
		}
	}

	private class ClientProcessor implements Runnable {

		private WebSocketConnector mConnector = null;

		/**
		 * Creates the new socket listener thread for this connector.
		 *
		 * @param aConnector
		 */
		public ClientProcessor(WebSocketConnector aConnector) {
			mConnector = aConnector;
		}

		@Override
		public void run() {
			WebSocketEngine lEngine = getEngine();
			Thread.currentThread().setName("jWebSocket " + mLogInfo + "-Connector " + getId());

			// start client listener loop
			setStatus(WebSocketConnectorStatus.UP);
			mCloseReason = CloseReason.SERVER;
			int lPort = mClientSocket.getPort();

			String lLogInfo = isSSL() ? "SSL" : "TCP";
			boolean lOk = false;
			try {
				boolean lTCPNoDelay = mClientSocket.getTcpNoDelay();
				// ensure that all packets are sent immediately w/o delay
				// to achieve better latency, no waiting and packaging.
				mClientSocket.setTcpNoDelay(true);
				mClientSocket.setSoTimeout(10 * 1000);

				RequestHeader lHeader = processHandshake(mClientSocket);
				if (null != lHeader) {
					String lSubProt = lHeader.getSubProtocol();
					if (null != lSubProt && lSubProt.contains(FLASH_POLICY_REQUEST)) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("WebSocket connection rejected, but flash policy file sent to '"
									+ mClientSocket.getInetAddress() + ":"
									+ mClientSocket.getPort() + "'.");
						}
					} else {
						setHeader(lHeader);
						int lTimeout = lHeader.getTimeout(getEngine().getConfiguration().getTimeout());
						mClientSocket.setSoTimeout(lTimeout);
						setVersion(lHeader.getVersion());
						setSubprot(lSubProt);
						if (mLog.isDebugEnabled()) {
							mLog.debug(lLogInfo + " client accepted on port "
									+ mClientSocket.getPort()
									+ " with timeout "
									+ (lTimeout > 0 ? lTimeout + "ms" : "infinite")
									+ " (TCPNoDelay was: " + lTCPNoDelay + ")"
									+ "...");
						}
						lOk = true;
					}
				} else {
					InetAddress lAddr = mClientSocket.getInetAddress();
					mLog.error(lLogInfo + " client not accepted on port "
							+ mClientSocket.getPort()
							+ " due to handshake issues. "
							+ (null != lAddr
							? lAddr.getHostAddress() + ", " + lAddr.getHostName()
							: "[no IP/hostname available]")
							+ ", headers: "
							+ (null != lHeader
							? lHeader.toString()
							: "[no headers passed]")
							+ ", invalid client" + (isSSL() ? " , SSL handshake error or certificate issue" : "")
							+ ", connection closed.");
				}
			} catch (IOException lEx) {
				if (lEx instanceof SocketException || lEx instanceof SocketTimeoutException) {
					mLog.warn(Logging.getSimpleExceptionMessage(lEx, "executing handshake"));
				} else {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "executing handshake"));
				}
			}
			try {
				if (!lOk) {
					// if header could not be parsed properly
					// immediately disconnect the client.
					mClientSocket.close();
					return;
				}
			} catch (IOException lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "closing " + lLogInfo + " socket"));
				return;
			}

			if (mLog.isDebugEnabled()) {
				mLog.debug("Starting " + lLogInfo + " connector...");
			}

			//Setting the session identifier in the connector's WebSocketSession instance
			mConnector.getSession().setSessionId(mConnector.getHeader().
					getCookies().get(mConnector.getHeader().getSessionCookieName()).toString());

			// registering connector
			getEngine().addConnector(mConnector);
			// start connector
			mConnector.startConnector();

			// readHixie and readHybi process potential exceptions already!
			try {
				if (isHixie()) {
					processHixie(lEngine);
				} else {
					processHybi(getVersion(), lEngine);
				}
			} finally {
				mConnector.stopConnector(mCloseReason);
			}
		}

		private void processHixie(WebSocketEngine aEngine) {
			ByteArrayOutputStream lBuff = new ByteArrayOutputStream();
			while (WebSocketConnectorStatus.UP == getStatus()) {
				try {
					int lIn = WebSocketProtocolAbstraction.read(mIn);
					// start of frame
					if (lIn == 0x00) {
						lBuff.reset();
						// end of frame
					} else if (lIn == 0xFF) {
						if (lBuff.size() > getMaxFrameSize()) {
							mLog.error(BaseEngine.getUnsupportedIncomingPacketSizeMsg(mConnector, lBuff.size()));
						} else {
							RawPacket lPacket = new RawPacket(lBuff.toByteArray());
							try {
								mConnector.processPacket(lPacket);
							} catch (Exception lEx) {
								mLog.error(lEx.getClass().getSimpleName()
										+ " in processPacket of connector "
										+ mConnector.getClass().getSimpleName()
										+ ": " + lEx.getMessage());
							}
						}
						lBuff.reset();
						// reading pending packets in the buffer (for high concurrency scenarios)
						if (mIn.available() > 0) {
							processHixie(aEngine);
						}
					} else if (lIn < 0) {
						mCloseReason = CloseReason.CLIENT;
						setStatus(WebSocketConnectorStatus.DOWN);
						// any other byte within or outside a frame
					} else {
						lBuff.write(lIn);
					}
				} catch (SocketTimeoutException lEx) {
					mLog.error(lEx.getClass().getSimpleName()
							+ " on timeout: " + lEx.getMessage());
					mCloseReason = CloseReason.TIMEOUT;
					setStatus(WebSocketConnectorStatus.DOWN);
				} catch (Exception lEx) {
					mLog.error(lEx.getClass().getSimpleName()
							+ " on other: " + lEx.getMessage());
					mCloseReason = CloseReason.SERVER;
					setStatus(WebSocketConnectorStatus.DOWN);
				}
			}
		}

		private void processHybi(int aVersion, WebSocketEngine aEngine) {

			String lFrom = getRemoteHost() + ":" + getRemotePort() + " (" + getId() + ")";
			while (WebSocketConnectorStatus.UP == getStatus()) {
				try {
					WebSocketPacket lPacket = WebSocketProtocolAbstraction.protocolToRawPacket(aVersion, mIn);
					if (lPacket == null) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Processing " + mLogInfo + " client 'disconnect' from " + lFrom + "...");
						}
						mCloseReason = CloseReason.CLIENT;
						setStatus(WebSocketConnectorStatus.DOWN);
					} else if (WebSocketFrameType.TEXT.equals(lPacket.getFrameType())
							|| WebSocketFrameType.BINARY.equals(lPacket.getFrameType())) {
						if (lPacket.size() > getMaxFrameSize()) {
							mLog.error(BaseEngine.getUnsupportedIncomingPacketSizeMsg(mConnector, lPacket.size()));
						} else {
							if (mLog.isDebugEnabled()) {
								mLog.debug("Processing '" + lPacket.getFrameType().toString().toLowerCase() + "' frame (" + mLogInfo + ") from " + lFrom + "...");
							}
							mConnector.processPacket(lPacket);
						}
						// reading pending packets in the buffer (for high concurrency scenarios)
						// if (mIn.available() > 0) {
						//	processHybi(aVersion, aEngine);
						// }
					} else if (WebSocketFrameType.PING.equals(lPacket.getFrameType())) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Processing 'ping' frame (" + mLogInfo + ") from " + lFrom + "...");
						}
						WebSocketPacket lPong = new RawPacket("");
						lPong.setFrameType(WebSocketFrameType.PONG);
						sendPacket(lPong);
					} else if (WebSocketFrameType.CLOSE.equals(lPacket.getFrameType())) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Processing 'close' frame (" + mLogInfo + ") from " + lFrom + "...");
						}
						mCloseReason = CloseReason.CLIENT;
						setStatus(WebSocketConnectorStatus.DOWN);

						// As per spec, server must respond to CLOSE with acknowledgment CLOSE (maybe
						// this should be handled higher up in the hierarchy?)
						WebSocketPacket lClose
								= new RawPacket(WebSocketFrameType.CLOSE,
										WebSocketProtocolAbstraction.calcCloseData(
												CloseReason.CLIENT.getCode(),
												CloseReason.CLIENT.name()));
						sendPacket(lClose);
						// the streams are closed in the run method
					} else if (WebSocketFrameType.FRAGMENT.equals(lPacket.getFrameType())) {
						// TODO: support fragments!
						mLog.warn("Frame type '" + lPacket.getFrameType() + "' not yet supported (" + mLogInfo + "), ignoring frame.");
					} else if (WebSocketFrameType.PONG.equals(lPacket.getFrameType())) {
						// TODO: shouldn't we enclose this in a synchronize(this)??
						if (mKeepAlive) {
							if (mLog.isDebugEnabled()) {
								mLog.debug("Received S2C pong from connector '"
										+ getId()
										+ "' "
										+ (null != getUsername() ? "(" + getUsername() + ")" : "[not authenticated]")
										+ " ("
										+ ((new Date()).getTime() - mPingStartedAt)
										+ "ms).");
							}
							synchronized (mSyncPing) {
								if (null != mS2CPingTimeoutTask) {
									try {
										mS2CPingTimeoutTask.cancel();
										mS2CPingTimeoutTask = null;
									} catch (Exception lEx) {
										// If the task is not running will always come here
									}
								}
							}
						} else {
							if (mLog.isDebugEnabled()) {
								mLog.warn("Pong received from connector '" + getId() + "' although no S2C keep-alive configured.");
							}
						}
						// do nothing
						// special support for IE issue
					} else {
						mLog.error("Unknown frame type '" + lPacket.getFrameType() + "' (" + mLogInfo + "), ignoring frame.");
					}
				} catch (SocketTimeoutException lEx) {
					if (mLog.isDebugEnabled()) {
						mLog.debug(lEx.getClass().getSimpleName() + " reading hybi (" + getId() + ", " + mLogInfo + "): " + lEx.getMessage());
					}
					mCloseReason = CloseReason.TIMEOUT;
					setStatus(WebSocketConnectorStatus.DOWN);
				} catch (Exception lEx) {
					if (mLog.isDebugEnabled() && WebSocketConnectorStatus.UP == getStatus()) {
						mLog.debug(lEx.getClass().getSimpleName() + " reading hybi (" + getId() + ", " + mLogInfo + "): " + lEx.getMessage());
					}
					if (!CloseReason.BROKEN.equals(mCloseReason)) {
						mCloseReason = CloseReason.SERVER;
					}
					setStatus(WebSocketConnectorStatus.DOWN);
				}
			} // while up, if exception occurs the status is set to DOWN
		}
	}

	@Override
	public String generateUID() {
		String lUID = mClientSocket.getInetAddress().getHostAddress()
				+ "@" + mClientSocket.getPort();
		return lUID;
	}

	@Override
	public int getRemotePort() {
		return mClientSocket.getPort();
	}

	@Override
	public InetAddress getRemoteHost() {
		return mClientSocket.getInetAddress();
	}

	@Override
	public String toString() {
		// TODO: Show proper IPV6 if used
		String lRes = getId() + " (" + getRemoteHost().getHostAddress()
				+ ":" + getRemotePort();
		String lUsername = getUsername();
		if (lUsername != null) {
			lRes += ", " + lUsername;
		}
		return lRes + ")";
	}
}
