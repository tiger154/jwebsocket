//	---------------------------------------------------------------------------
//	jWebSocket - BaseWebSocketClient (Community Edition, CE)
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
package org.jwebsocket.client.java;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javolution.util.FastSet;
import org.jwebsocket.api.*;
import org.jwebsocket.client.token.WebSocketTokenClientEvent;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.ReliabilityOptions;
import org.jwebsocket.kit.*;
import org.jwebsocket.util.HttpCookie;
import org.jwebsocket.util.JWSTimerTask;
import org.jwebsocket.util.Tools;

/**
 * Base {@code WebSocket} implementation based on http://weberknecht.googlecode.com by Roderick
 * Baier. This uses thread model for handling WebSocket connection which is defined by the
 * <tt>WebSocket</tt>
 * protocol specification. {@linkplain http://www.whatwg.org/specs/web-socket-protocol/}
 * {@linkplain http://www.w3.org/TR/websockets/}
 *
 * @author Roderick Baier
 * @author agali
 * @author puran
 * @author jang
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 * @author Rolando Betancourt Toucet
 */
@Deprecated
public class BaseWebSocketClient extends BaseClient {

	/**
	 * TCP socket
	 */
	private Socket mSocket = null;
	/**
	 * IO streams
	 */
	private InputStream mIn = null;
	private OutputStream mOut = null;
	/**
	 * Data receiver
	 */
	private WebSocketReceiver mReceiver = null;
	/**
	 * represents the WebSocket status
	 */
	private List<WebSocketSubProtocol> mSubprotocols;
	private WebSocketSubProtocol mNegotiatedSubProtocol;
	private static final String CR_CLIENT = "Client closed connection";
	private final WebSocketEncoding mEncoding = WebSocketEncoding.TEXT;
	private final Object mWriteLock = new Object();
	private String mCloseReason = null;
	private Headers mHeaders = null;
	private final Set<HttpCookie> mCookies = new FastSet<HttpCookie>();

	/**
	 * Base constructor
	 */
	public BaseWebSocketClient() {
	}

	/**
	 * Constructor including reliability options
	 *
	 * @param aReliabilityOptions
	 */
	public BaseWebSocketClient(ReliabilityOptions aReliabilityOptions) {
		setReliabilityOptions(aReliabilityOptions);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aURI
	 * @throws org.jwebsocket.kit.IsAlreadyConnectedException
	 */
	@Override
	public void open(String aURI) throws IsAlreadyConnectedException {
		open(JWebSocketCommonConstants.WS_VERSION_DEFAULT, aURI);
	}

	/**
	 * Make a sub protocol string for Sec-WebSocket-Protocol header. The result is something like
	 * this:
	 * <pre>
	 * org.jwebsocket.json org.websocket.text org.jwebsocket.binary
	 * </pre>
	 *
	 * @return sub protocol list in one string
	 */
	private String generateSubProtocolsHeaderValue() {
		if (mSubprotocols == null || mSubprotocols.size() <= 0) {
			return JWebSocketCommonConstants.WS_SUBPROT_DEFAULT;
		} else {
			StringBuilder lBuff = new StringBuilder();
			for (WebSocketSubProtocol lProt : mSubprotocols) {
				lBuff.append(lProt.getSubProt()).append(' ');
			}
			return lBuff.toString().trim();
		}
	}

	/**
	 *
	 * @param aVersion
	 * @param aURI
	 * @throws IsAlreadyConnectedException
	 */
	@Override
	public void open(int aVersion, String aURI) throws IsAlreadyConnectedException {
		String lSubProtocols = generateSubProtocolsHeaderValue();
		open(aVersion, aURI, lSubProtocols);
	}

	/**
	 *
	 * @param aVersion
	 * @param aURI
	 * @param aSubProtocols
	 * @throws IsAlreadyConnectedException
	 */
	@Override
	public void open(int aVersion, String aURI, String aSubProtocols) throws IsAlreadyConnectedException {
		try {
			if (!isConnected()) {
				abortReconnect();

				// set default close reason in case 
				// connection could not be established.
				mCloseReason = "Connection could not be established.";

				mVersion = aVersion;
				mURI = new URI(aURI);
				// the WebSocket Handshake here generates the initial client side Handshake only
				WebSocketHandshake lHandshake = new WebSocketHandshake(mVersion, mURI, aSubProtocols);
				// close current socket if still connected 
				// to avoid open connections on server
				if (mSocket != null && mSocket.isConnected()) {
					mSocket.close();
				}
				mSocket = createSocket();
				// don't gather packages here, reduce latency
				mSocket.setTcpNoDelay(true);
				mIn = mSocket.getInputStream();
				mOut = mSocket.getOutputStream();

				// pass session cookie, if already was set for this client instance
				byte[] lBA;
				List<HttpCookie> lTempCookies = new ArrayList();
				if (!mCookies.isEmpty()) {
					for (HttpCookie lCookie : mCookies) {
						if (HttpCookie.isValid(mURI, lCookie)) {
							// Cookie is valid
							lTempCookies.add(lCookie);
						}
					}
				}

				lBA = lHandshake.generateC2SRequest(lTempCookies);
				mOut.write(lBA);

				mStatus = WebSocketStatus.CONNECTING;

				mHeaders = new Headers();
				try {
					mHeaders.readFromStream(aVersion, mIn);
				} catch (WebSocketException lEx) {
					// ignore exception here, will be processed afterwards
				}

				// registering new cookies from the server response
				List<String> lResponseCookies = (List) mHeaders.getField(Headers.SET_COOKIE);
				mCookies.addAll(HttpCookie.parse(mURI, lResponseCookies));

				if (!mHeaders.isValid()) {
					WebSocketClientEvent lEvent = new WebSocketBaseClientEvent(this, EVENT_CLOSE, "Handshake rejected.");
					notifyClosed(lEvent);
					checkReconnect(lEvent);
					return;
				}

				// parse negotiated sub protocol
				String lProtocol = (String) mHeaders.getField(Headers.SEC_WEBSOCKET_PROTOCOL);
				if (lProtocol != null) {
					mNegotiatedSubProtocol = new WebSocketSubProtocol(lProtocol, mEncoding);
				} else {
					// just default to 'jwebsocket.org.json' and 'text'
					mNegotiatedSubProtocol = new WebSocketSubProtocol(
							JWebSocketCommonConstants.WS_SUBPROT_DEFAULT,
							JWebSocketCommonConstants.WS_ENCODING_DEFAULT);
				}
				// create new thread to receive the data from the new client
				mReceiver = new WebSocketReceiver(this, mIn);
				// and start the receiver thread for the port
				mReceiver.start();

				// notifying logic "opening" listeners notification
				// we consider that a client has finally openned when 
				// the "max frame size" handshake has completed 
				mStatus = WebSocketStatus.OPEN;
				final WebSocketClientEvent lEvent = new WebSocketBaseClientEvent(this, EVENT_OPENING, null);
				for (final WebSocketClientListener lListener : getListeners()) {
					getListenersExecutor().submit(new Runnable() {
						@Override
						public void run() {
							try {
								lListener.processOpening(lEvent);
							} catch (Exception lEx) {
								// nothing, soppose to be catched internally
							}
						}
					});
				}

				// reset close reason to be specified by next reason
				mCloseReason = null;
			} else {
				throw new IsAlreadyConnectedException("The Client is already connected");
			}

		} catch (IsAlreadyConnectedException lex) {
			throw new IsAlreadyConnectedException(lex.getMessage());
		} catch (Exception lEx) {
			WebSocketClientEvent lEvent = new WebSocketBaseClientEvent(this, EVENT_CLOSE, mCloseReason);
			notifyClosed(lEvent);
			checkReconnect(lEvent);
		}
	}

	@Override
	public void send(byte[] aData) throws WebSocketException {
		synchronized (mWriteLock) {
			if (isHixie()) {
				sendInternal(aData);
			} else {
				WebSocketPacket lPacket = new RawPacket(aData);

				sendInternal(
						WebSocketProtocolAbstraction.rawToProtocolPacket(
								mVersion, lPacket, WebSocketProtocolAbstraction.MASKED));
			}
		}
	}

	@Override
	public void send(byte[] aData, WebSocketFrameType aFrameType) throws WebSocketException {
		synchronized (mWriteLock) {
			if (isHixie()) {
				sendInternal(aData);
			} else {
				WebSocketPacket lPacket = new RawPacket(aData);
				lPacket.setFrameType(aFrameType);

				sendInternal(
						WebSocketProtocolAbstraction.rawToProtocolPacket(
								mVersion, lPacket, WebSocketProtocolAbstraction.MASKED));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aData
	 * @param aEncoding
	 * @throws org.jwebsocket.kit.WebSocketException
	 */
	@Override
	public void send(String aData, String aEncoding) throws WebSocketException {
		byte[] lData;
		try {
			lData = aData.getBytes(aEncoding);
		} catch (UnsupportedEncodingException lEx) {
			throw new WebSocketException(
					"Encoding exception while sending the data:"
					+ lEx.getMessage(), lEx);
		}
		send(lData);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aDataPacket
	 * @throws org.jwebsocket.kit.WebSocketException
	 */
	@Override
	public void send(WebSocketPacket aDataPacket) throws WebSocketException {
		super.send(aDataPacket);

		synchronized (mWriteLock) {
			if (isHixie()) {
				sendInternal(aDataPacket.getByteArray());
			} else {
				sendInternal(WebSocketProtocolAbstraction.rawToProtocolPacket(
						mVersion, aDataPacket, WebSocketProtocolAbstraction.MASKED));
			}
		}
	}

	private void sendInternal(byte[] aData) throws WebSocketException {
		if (!mStatus.isWritable()) {
			throw new WebSocketException("Error while sending binary data: not connected");
		}
		try {
			if (isHixie()) {
				if (WebSocketEncoding.BINARY.equals(mNegotiatedSubProtocol.getEncoding())) {
					mOut.write(0x80);
					// what if frame is longer than 255 characters (8bit?) Refer to IETF spec!
					// won't fix since hixie is far outdated!
					mOut.write(aData.length);
					mOut.write(aData);
				} else {
					mOut.write(0x00);
					mOut.write(aData);
					mOut.write(0xff);
				}
			} else {
				mOut.write(aData);
			}
			mOut.flush();
		} catch (IOException lEx) {
			terminateReceiverThread();
			throw new WebSocketException("Error while sending socket data: ", lEx);
		}
	}

	private void terminateReceiverThread() throws WebSocketException {
		mReceiver.quit();
		try {
			mReceiver.join(RECEIVER_SHUTDOWN_TIMEOUT);
		} catch (InterruptedException lEx) {
			throw new WebSocketException(
					"Receiver thread did not stop within "
					+ RECEIVER_SHUTDOWN_TIMEOUT + " ms", lEx);
		}
		mReceiver = null;
	}

	private void setCloseReason(String aCloseReason) {
		if (null == mCloseReason) {
			mCloseReason = aCloseReason;
		}
	}

	@Override
	public synchronized void close() {
		try {
			// on an explicit close operation ...
			// cancel all potential re-connection tasks.
			abortReconnect();
			if (null != mReceiver) {
				mReceiver.quit();
			}

			if (!mStatus.isWritable()) {
				return;
			}

			setCloseReason(CR_CLIENT);
			sendCloseHandshake();

			// stopping the receiver thread stops the entire client
			terminateReceiverThread();
		} catch (Exception lEx) {
			// ignore that, connection is about to be terminated
		}
	}

	private void sendCloseHandshake() throws WebSocketException {
		if (!mStatus.isClosable()) {
			throw new WebSocketException("Error while sending close handshake: not connected");
		}
		synchronized (mWriteLock) {
			try {
				if (isHixie()) {
					// old hixie close handshake
					mOut.write(0xff00);
					mOut.flush();
				} else {
					WebSocketPacket lPacket
							= new RawPacket(WebSocketFrameType.CLOSE,
									WebSocketProtocolAbstraction.calcCloseData(CloseReason.CLIENT.getCode(), CloseReason.CLIENT.name()));
					send(lPacket);
				}
			} catch (IOException lIOEx) {
				throw new WebSocketException("Error while sending close handshake", lIOEx);
			}
		}
	}

	private Socket createSocket() throws WebSocketException {
		String lScheme = mURI.getScheme();
		String lHost = mURI.getHost();
		int lPort = mURI.getPort();

		mSocket = null;

		if (lScheme != null && lScheme.equals("ws")) {
			if (lPort == -1) {
				lPort = 80;
			}
			try {
				mSocket = new Socket(lHost, lPort);
			} catch (UnknownHostException lUHEx) {
				throw new WebSocketException("Unknown host: " + lHost,
						WebSocketExceptionType.UNKNOWN_HOST, lUHEx);
			} catch (IOException lIOEx) {
				throw new WebSocketException("Error while creating socket to " + mURI,
						WebSocketExceptionType.UNABLE_TO_CONNECT, lIOEx);
			}
		} else if (lScheme != null && lScheme.equals("wss")) {
			if (lPort == -1) {
				lPort = 443;
			}
			try {
				try {
					// TODO: Make acceptance of unsigned certificates optional!
					// This methodology is used to accept unsigned certficates
					// on the SSL server. Be careful with this in production environments!

					// Create a trust manager to accept unsigned certificates
					TrustManager[] lTrustManager = new TrustManager[]{
						new X509TrustManager() {
							@Override
							public java.security.cert.X509Certificate[] getAcceptedIssuers() {
								return null;
							}

							@Override
							public void checkClientTrusted(
									java.security.cert.X509Certificate[] aCerts, String aAuthType) {
							}

							@Override
							public void checkServerTrusted(
									java.security.cert.X509Certificate[] aCerts, String aAuthType) {
							}
						}
					};
					// Use this trustmanager to not reject unsigned certificates
					SSLContext lSSLContext = SSLContext.getInstance("TLS");
					lSSLContext.init(null, lTrustManager, new java.security.SecureRandom());
					mSocket = (SSLSocket) lSSLContext.getSocketFactory().createSocket(lHost, lPort);
				} catch (NoSuchAlgorithmException lNSAEx) {
					throw new RuntimeException("Unable to initialize SSL context", lNSAEx);
				} catch (KeyManagementException lKMEx) {
					throw new RuntimeException("Unable to initialize SSL context", lKMEx);
				}
			} catch (UnknownHostException lUHEx) {
				throw new WebSocketException("Unknown host: " + lHost,
						WebSocketExceptionType.UNKNOWN_HOST, lUHEx);
			} catch (IOException lIOEx) {
				throw new WebSocketException("Error while creating secure socket to " + mURI,
						WebSocketExceptionType.UNABLE_TO_CONNECT_SSL, lIOEx);
			} catch (Exception lEx) {
				throw new WebSocketException(lEx.getClass().getSimpleName() + " while creating secure socket to " + mURI, lEx);
			}
		} else {
			throw new WebSocketException("Unsupported protocol: " + lScheme,
					WebSocketExceptionType.PROTOCOL_NOT_SUPPORTED);
		}

		return mSocket;
	}

	/**
	 * @return the client socket
	 */
	public Socket getConnectionSocket() {
		return mSocket;
	}

	/**
	 * {@inheritDoc }
	 *
	 * @return
	 */
	public WebSocketStatus getConnectionStatus() {
		return mStatus;
	}

	/**
	 * @return the mHeaders
	 */
	public Headers getHeaders() {
		return mHeaders;
	}

	@Override
	public void addSubProtocol(WebSocketSubProtocol aSubProt) {
		if (mSubprotocols == null) {
			mSubprotocols = new ArrayList<WebSocketSubProtocol>(3);
		}
		mSubprotocols.add(aSubProt);
	}

	@Override
	public String getNegotiatedSubProtocol() {
		return mNegotiatedSubProtocol == null ? null : mNegotiatedSubProtocol.getSubProt();
	}

	@Override
	public WebSocketEncoding getNegotiatedEncoding() {
		return mNegotiatedSubProtocol == null ? null : mNegotiatedSubProtocol.getEncoding();
	}

	private boolean isHixie() {
		return WebSocketProtocolAbstraction.isHixieVersion(mVersion);
	}

	class WebSocketReceiver extends Thread {

		private WebSocketClient mClient = null;
		private InputStream mIS = null;
		private volatile boolean mIsRunning = false;
		private PingSenderTask mPingSender = new PingSenderTask();

		public WebSocketReceiver(WebSocketClient aClient, InputStream aInput) {
			mClient = aClient;
			mIS = aInput;
		}

		@Override
		public void run() {
			Thread.currentThread().setName("jWebSocket-Client " + getId());

			mIsRunning = true;

			// the hixie and hybi processors handle potential exceptions
			if (isHixie()) {
				processHixie();
			} else {
				// initiating the ping thread for this connection
				mPingSender = new PingSenderTask();
				Tools.getTimer().scheduleAtFixedRate(mPingSender, getPingInterval(), getPingInterval());
				processHybi();
			}

			// set status AFTER close frame was sent, otherwise sending
			// close frame leads to an exception.
			mStatus = WebSocketStatus.CLOSING;
			String lExMsg = "";
			try {
				// shutdown methods are not implemented for SSL sockets
				if (!(mSocket instanceof SSLSocket)) {
					if (!mSocket.isOutputShutdown()) {
						mSocket.shutdownInput();
					}
				}
			} catch (IOException lEx) {
				lExMsg += "Shutdown input: " + lEx.getMessage() + ", ";
			}
			try {
				// shutdown methods are not implemented for SSL sockets
				if (!(mSocket instanceof SSLSocket)) {
					if (!mSocket.isOutputShutdown()) {
						mSocket.shutdownOutput();
					}
				}
			} catch (IOException lIOEx) {
				lExMsg += "Shutdown output: " + lIOEx.getMessage() + ", ";
			}
			try {
				if (!mSocket.isClosed()) {
					mSocket.close();
				}
			} catch (IOException lIOEx) {
				lExMsg += "Socket close: " + lIOEx.getMessage() + ", ";
			}

			// now the connection is really closed
			// set the status accordingly
			mStatus = WebSocketStatus.CLOSED;

			WebSocketClientEvent lEvent = new WebSocketBaseClientEvent(mClient, EVENT_CLOSE, mCloseReason);
			// notify listeners that client has closed
			notifyClosed(lEvent);

			quit();

			mPingSender.cancel();

			if (!CR_CLIENT.equals(mCloseReason)) {
				checkReconnect(lEvent);
			}
		}

		private void processHixie() {
			boolean lFrameStart = false;
			ByteArrayOutputStream lBuff = new ByteArrayOutputStream();
			while (mIsRunning) {
				try {
					int lB = mIS.read();
					// TODO: support binary frames
					if (lB == 0x00) {
						lFrameStart = true;
					} else if (lB == 0xff && lFrameStart == true) {
						lFrameStart = false;

						final WebSocketClientEvent lWSCE = new WebSocketTokenClientEvent(mClient, null, null);
						final RawPacket lPacket = new RawPacket(lBuff.toByteArray());
						lBuff.reset();
						getListenersExecutor().submit(new Runnable() {
							@Override
							public void run() {
								notifyPacket(lWSCE, lPacket);
							}
						});
					} else if (lFrameStart == true) {
						lBuff.write(lB);
					} else if (lB == -1) {
						setCloseReason("Inbound stream terminated");
						mIsRunning = false;
					}
				} catch (IOException lEx) {
					mIsRunning = false;
					setCloseReason(lEx.getClass().getName() + " in hybi processor: " + lEx.getMessage());
				}
			}
		}

		private void processHybi() {
			WebSocketFrameType lFrameType;

			while (mIsRunning) {
				try {
					final WebSocketPacket lPacket = WebSocketProtocolAbstraction.protocolToRawPacket(mVersion, mIS);
					lFrameType = (lPacket != null ? lPacket.getFrameType() : WebSocketFrameType.INVALID);
					if (null == lFrameType) {
						if (mIsRunning) {
							setCloseReason("Connection broken");
						} else {
							setCloseReason("Client terminated");
						}
						mIsRunning = false;
					} else if (WebSocketFrameType.INVALID == lFrameType) {
						mIsRunning = false;
						setCloseReason("Invalid hybi frame type detected");
					} else if (WebSocketFrameType.CLOSE == lFrameType) {
						mIsRunning = false;
						setCloseReason("Server closed connection");
					} else if (WebSocketFrameType.PING == lFrameType) {
						sendPong(new RawPacket(WebSocketFrameType.PONG, ""));
					} else if (WebSocketFrameType.PONG == lFrameType) {
						mStatus = WebSocketStatus.OPEN;
					} else if (WebSocketFrameType.TEXT == lFrameType
							|| WebSocketFrameType.BINARY == lFrameType) {
						final WebSocketClientEvent lWSCE = new WebSocketTokenClientEvent(mClient, null, null);
						getListenersExecutor().submit(new Runnable() {
							@Override
							public void run() {
								notifyPacket(lWSCE, lPacket);
							}
						});
					}
				} catch (Exception lEx) {
					mIsRunning = false;
					setCloseReason(lEx.getClass().getName() + " in hybi processor: " + lEx.getMessage());
				}
			}
		}

		private void sendPong(WebSocketPacket aDataPacket) throws WebSocketException {
			sendInternal(WebSocketProtocolAbstraction.rawToProtocolPacket(
					mVersion, aDataPacket, WebSocketProtocolAbstraction.MASKED));
		}

		public void quit() {
			// ensure that reader loops are not continued
			mIsRunning = false;
			try {
				mIS.close();
			} catch (IOException ex) {
				// just to force client reader to stop
			}
		}

		public boolean isRunning() {
			return mIsRunning;
		}

		class PingSenderTask extends JWSTimerTask {

			@Override
			public void runTask() {
				while (isAlive()) {
					try {
						Thread.currentThread().setName("jWebSocket-Client " + getId() + " PingPong processor");
						Thread.sleep(getPingInterval());

						WebSocketPacket lPing = new RawPacket(WebSocketFrameType.PING, "Hello");
						sendInternal(WebSocketProtocolAbstraction.rawToProtocolPacket(
								mVersion, lPing, WebSocketProtocolAbstraction.MASKED));
					} catch (Exception lEx) {
					}
				}
			}
		}
	}
}
