//	---------------------------------------------------------------------------
//	jWebSocket - BaseWebSocketClient (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.jwebsocket.api.*;
import org.jwebsocket.client.token.WebSocketTokenClientEvent;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.*;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.HttpCookie;
import org.jwebsocket.util.MessagingControl;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 * Base {@code WebSocket} implementation based on
 * http://weberknecht.googlecode.com by Roderick Baier. This uses thread model
 * for handling WebSocket connection which is defined by the <tt>WebSocket</tt>
 * protocol specification. {@linkplain http://www.whatwg.org/specs/web-socket-protocol/}
 * {@linkplain http://www.w3.org/TR/websockets/}
 *
 * @author Roderick Baier
 * @author agali
 * @author puran
 * @author jang
 * @author aschulze
 * @author kyberneees
 * @author rbetancourt
 */
public class BaseWebSocketClient implements WebSocketClient {

	private static final int RECEIVER_SHUTDOWN_TIMEOUT = 3000;
	/**
	 * WebSocket connection URI
	 */
	private URI mURI = null;
	/**
	 * list of the registered listeners
	 */
	private List<WebSocketClientListener> mListeners = new FastList<WebSocketClientListener>();
	/**
	 * list of the registered filters
	 */
	private List<WebSocketClientFilter> mFilters = new FastList<WebSocketClientFilter>();
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
	protected volatile WebSocketStatus mStatus = WebSocketStatus.CLOSED;
	private List<WebSocketSubProtocol> mSubprotocols;
	private WebSocketSubProtocol mNegotiatedSubProtocol;
	private int mPingInterval = 20000;
	/**
	 *
	 */
	public static String EVENT_OPENING = "opening";
	/**
	 *
	 */
	public static String EVENT_OPEN = "open";
	/**
	 *
	 */
	public static String EVENT_CLOSE = "close";
	/**
	 *
	 */
	public static String DATA_CLOSE_ERROR = "error";
	/**
	 *
	 */
	public static String DATA_CLOSE_CLIENT = "client";
	/**
	 *
	 */
	public static String DATA_CLOSE_SERVER = "server";
	/**
	 *
	 */
	public static String DATA_CLOSE_SHUTDOWN = "shutdown";
	private static final String CR_CLIENT = "Client closed connection";
	private int mVersion = JWebSocketCommonConstants.WS_VERSION_DEFAULT;
	private WebSocketEncoding mEncoding = WebSocketEncoding.TEXT;
	private ReliabilityOptions mReliabilityOptions = null;
	private final ScheduledThreadPoolExecutor mExecutor = new ScheduledThreadPoolExecutor(1);
	private final ExecutorService mListenersExecutor = Executors.newFixedThreadPool(1);
	private final Map<String, Object> mParams = new FastMap<String, Object>();
	private final Object mWriteLock = new Object();
	private String mCloseReason = null;
	private ScheduledFuture mReconnectorTask = null;
	private Boolean mIsReconnecting = false;
	private final Object mReconnectLock = new Object();
	private Headers mHeaders = null;
	private Set<HttpCookie> mCookies = new FastSet<HttpCookie>();
	/**
	 * fragmentation model
	 */
	private Map<String, IPacketDeliveryListener> mPacketDeliveryListeners = new FastMap<String, IPacketDeliveryListener>().shared();
	private Integer mMaxFrameSize;
	private Map<String, String> mFragments = new LinkedHashMap<String, String>();

	/**
	 * Base constructor
	 */
	public BaseWebSocketClient() {
	}

	/**
	 *
	 * @param aStatus
	 * @throws Exception
	 */
	public void setStatus(WebSocketStatus aStatus) throws Exception {
		if (aStatus.equals(WebSocketStatus.AUTHENTICATED)) {
			this.mStatus = aStatus;
		} else {
			throw new Exception("The value '" + aStatus.name()
					+ "' cannot be assigned. Restricted to internal usage only!");
		}
	}

	@Override
	public Integer getMaxFrameSize() {
		return mMaxFrameSize;
	}

	@Override
	public void addFilter(WebSocketClientFilter aFilter) {
		mFilters.add(aFilter);
	}

	@Override
	public void removeFilter(WebSocketClientFilter aFilter) {
		mFilters.add(aFilter);
	}

	@Override
	public List<WebSocketClientFilter> getFilters() {
		return Collections.unmodifiableList(mFilters);
	}

	/**
	 * Constructor including reliability options
	 *
	 * @param aReliabilityOptions
	 */
	public BaseWebSocketClient(ReliabilityOptions aReliabilityOptions) {
		mReliabilityOptions = aReliabilityOptions;
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	public Object getParam(String aKey, Object aDefault) {
		Object lValue = mParams.get(aKey);
		if (null == lValue) {
			lValue = aDefault;
		}
		return lValue;
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	public Object getParam(String aKey) {
		return mParams.get(aKey);
	}

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	public void setParam(String aKey, Object aValue) {
		mParams.put(aKey, aValue);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aURI
	 */
	@Override
	public void open(String aURI) throws IsAlreadyConnectedException {
		open(JWebSocketCommonConstants.WS_VERSION_DEFAULT, aURI);
	}

	/**
	 * Make a sub protocol string for Sec-WebSocket-Protocol header. The result
	 * is something like this:
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
	public void open(int aVersion, String aURI, String aSubProtocols) throws IsAlreadyConnectedException {
		try {
			if (!isConnected()) {
				mAbortReconnect();

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
				} catch (Exception lEx) {
					// ignore exception here, will be processed afterwards
				}

				// registering new cookies from the server response
				List<String> lResponseCookies = (List) mHeaders.getField(Headers.SET_COOKIE);
				mCookies.addAll(HttpCookie.parse(mURI, lResponseCookies));

				if (!mHeaders.isValid()) {
					WebSocketClientEvent lEvent =
							new WebSocketBaseClientEvent(this, EVENT_CLOSE, "Handshake rejected.");
					notifyClosed(lEvent);
					mCheckReconnect(lEvent);
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
				// now set official status, may listeners ask for that
				mStatus = WebSocketStatus.OPEN;

				// notifying logic "opening" listeners notification
				// we consider that a client has finally openned when 
				// the "max frame size" handshake has completed 
				final WebSocketClientEvent lEvent = new WebSocketBaseClientEvent(this, EVENT_OPENING, null);
				for (final WebSocketClientListener lListener : getListeners()) {
					mListenersExecutor.submit(new Runnable() {
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
			WebSocketClientEvent lEvent =
					new WebSocketBaseClientEvent(this, EVENT_CLOSE, mCloseReason);
			notifyClosed(lEvent);
			mCheckReconnect(lEvent);
		}
	}

	@Override
	public void sendPacketInTransaction(WebSocketPacket aDataPacket, IPacketDeliveryListener aListener) {
		sendPacketInTransaction(aDataPacket, getMaxFrameSize(), aListener);
	}

	@Override
	public void sendPacketInTransaction(final WebSocketPacket aDataPacket,
			final Integer aFragmentSize, final IPacketDeliveryListener aListener) {
		// ommiting control frames
		if (WebSocketFrameType.BINARY.equals(aDataPacket.getFrameType())
				|| WebSocketFrameType.TEXT.equals(aDataPacket.getFrameType())) {

			try {
				Assert.isTrue(aDataPacket.size() <= getMaxFrameSize(),
						"The packet size exceeds the max frame size supported by the client!");

				Assert.isTrue(aFragmentSize > 0 && aFragmentSize <= getMaxFrameSize(), "Invalid 'fragment size' argument! "
						+ "Expected: fragment_size > 0 && fragment_size <= MAX_FRAME_SIZE");

				// processing fragmentation
				if (aFragmentSize < aDataPacket.size() && aFragmentSize < getMaxFrameSize()) {
					// first fragment is never the last
					boolean lIsLast = false;

					// unique fragmentation id, allows multiplexing
					final String lFragmentationId = MessagingControl.getFragmentationID();

					// creating a special fragment message
					Token lFragment = MessagingControl.buildFragmentMessage(
							"", lFragmentationId, lIsLast, Arrays
							.copyOfRange(aDataPacket.getByteArray(), 0, aFragmentSize));

					// sending fragments
					final long lSentTime = System.currentTimeMillis();
					sendMessage(lFragment, new IPacketDeliveryListener() {
						private int mBytesSent = 0;

						@Override
						public long getTimeout() {
							long lTimeout = lSentTime + aListener.getTimeout() - System.currentTimeMillis();
							if (lTimeout < 0) {
								lTimeout = 0;
							}

							return lTimeout;
						}

						@Override
						public void OnTimeout() {
							aListener.OnTimeout();
						}

						@Override
						public void OnSuccess() {
							// updating bytes sent
							mBytesSent += aFragmentSize;
							if (mBytesSent >= aDataPacket.size()) {
								// calling success if the packet was transmitted complete
								aListener.OnSuccess();
							} else {
								// prepare to sent a next fragment
								int lLength = (aFragmentSize + mBytesSent <= aDataPacket.size())
										? aFragmentSize
										: aDataPacket.size() - mBytesSent;

								byte[] lBytes = Arrays.copyOfRange(aDataPacket.getByteArray(), mBytesSent, mBytesSent + lLength);
								boolean lIsLast = (lLength + mBytesSent == aDataPacket.size()) ? true : false;

								// send next fragment
								Token lMessage = MessagingControl.buildFragmentMessage(
										"", lFragmentationId, lIsLast, lBytes);

								try {
									sendMessage(lMessage, this);
								} catch (Exception lEx) {
									aListener.OnFailure(lEx);
								}
							}
						}

						@Override
						public void OnFailure(Exception lEx) {
							aListener.OnFailure(lEx);
						}
					});

					// stop flow at this point
					return;
				}

				// normal send
				sendMessage(MessagingControl.buildMessage(
						"", aDataPacket.getByteArray()), aListener);

			} catch (Exception lEx) {
				aListener.OnFailure(lEx);
			}
		} else {
			aListener.OnFailure(new Exception("Control frames cannot be sent in transaction!"));
		}
	}

	private void sendMessage(Token aMessage, IPacketDeliveryListener aListener) throws Exception {
		if (null != aListener) {
			aMessage.setBoolean(MessagingControl.PROPERTY_IS_ACK_REQUIRED, true);
			aMessage.setBoolean(MessagingControl.PROPERTY_IS_WRAPPED_MESSAGE, true);
			final String lMsgId = aMessage.getString(MessagingControl.PROPERTY_MESSAGE_ID);
			mPacketDeliveryListeners.put(lMsgId, aListener);

			// schedule the timer task
			try {
				Tools.getTimer().schedule(new TimerTask() {
					@Override
					public void run() {
						Tools.getThreadPool().submit(new Runnable() {
							@Override
							public void run() {
								IPacketDeliveryListener lListener = mPacketDeliveryListeners.remove(lMsgId);
								if (null != lListener) {
									lListener.OnTimeout();
								}
							}
						});
					}
				}, aListener.getTimeout());
			} catch (IllegalStateException lEx) {
				// nothing, task was cancelled
			}
		}

		send(JSONProcessor.tokenToPacket(aMessage));
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
	 */
	@Override
	public void send(WebSocketPacket aDataPacket) throws WebSocketException {
		try {
			for (WebSocketClientFilter lFilter : mFilters) {
				lFilter.filterPacketOut(aDataPacket);
			}
		} catch (Exception lEx) {
			throw new WebSocketException("Outbound filtering process exception!", lEx);
		}

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
		// on an explicit close operation ...
		// cancel all potential re-connection tasks.
		mAbortReconnect();
		if (null != mReceiver) {
			mReceiver.quit();
		}

		if (!mStatus.isWritable()) {
			return;
		}
		setCloseReason(CR_CLIENT);
		try {
			sendCloseHandshake();
		} catch (Exception lEx) {
			// ignore that, connection is about to be terminated
		}
		try {
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
					WebSocketPacket lPacket = new RawPacket(WebSocketFrameType.CLOSE, "BYE");
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
	 * {@inheritDoc }
	 */
	@Override
	public boolean isConnected() {
		return mStatus.isConnected();
	}

	@Override
	public WebSocketStatus getStatus() {
		return mStatus;
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
	 * @return the client socket
	 */
	public Socket getConnectionSocket() {
		return mSocket;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addListener(WebSocketClientListener aListener) {
		mListeners.add(aListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeListener(WebSocketClientListener aListener) {
		mListeners.remove(aListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<WebSocketClientListener> getListeners() {
		return Collections.unmodifiableList(mListeners);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyOpened(final WebSocketClientEvent aEvent) {
		for (final WebSocketClientListener lListener : getListeners()) {
			mListenersExecutor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						lListener.processOpened(aEvent);
					} catch (Exception lEx) {
						// nothing, soppose to be catched internally
					}
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyPacket(final WebSocketClientEvent aEvent, WebSocketPacket aDataPacket) {
		// supporting the max frame size handshake
		if (!WebSocketFrameType.BINARY.equals(aDataPacket.getFrameType())) {
			Token lMessage = JSONProcessor.packetToToken(aDataPacket);

			// process only if is control message
			if (null != lMessage && lMessage.getBoolean(MessagingControl.PROPERTY_IS_WRAPPED_MESSAGE, false)) {
				if (MessagingControl.TYPE_MESSAGE.equals(lMessage.getString(MessagingControl.PROPERTY_TYPE))) {
					String lMsgId = lMessage.getString(MessagingControl.PROPERTY_MESSAGE_ID);

					// supporting message delivery notification if required
					if (lMessage.getBoolean(MessagingControl.PROPERTY_IS_ACK_REQUIRED, false)) {
						try {
							sendMessage(MessagingControl.buildAckMessage(lMsgId), null);
						} catch (Exception lEx) {
							throw new RuntimeException(lEx);
						}
					}

					// supporting fragmentation
					if (lMessage.getBoolean(MessagingControl.PROPERTY_IS_FRAGMENT, false)) {
						// getting the fragmentation key for session storage
						String lFragmentationId = "FRAGMENTS" + lMessage.getString(MessagingControl.PROPERTY_FRAGMENTATION_ID);
						// getting the data
						String lData = lMessage.getString(MessagingControl.PROPERTY_DATA);

						if (lMessage.getBoolean(MessagingControl.PROPERTY_IS_LAST_FRAGMENT, false)) {
							// setting new packet value
							aDataPacket.setString(mFragments.remove(lFragmentationId) + lData);
						} else {
							if (!mFragments.containsKey(lFragmentationId)) {
								mFragments.put(lFragmentationId, lData);
							} else {
								mFragments.put(lFragmentationId, mFragments.get(lFragmentationId) + lData);
							}

							return;
						}
					} else {
						// setting new packet value
						aDataPacket.setString(lMessage.getString(MessagingControl.PROPERTY_DATA));
					}

				} else if (MessagingControl.TYPE_INFO.equals(lMessage.getString(MessagingControl.PROPERTY_TYPE))) {
					String lName = lMessage.getString(MessagingControl.PROPERTY_NAME);

					// processing max frame size message
					if (MessagingControl.NAME_MAX_FRAME_SIZE.equals(lName)) {
						mMaxFrameSize = lMessage.getInteger(MessagingControl.PROPERTY_DATA);
						// The end of the "max frame size" handshake indicates that the connection is finally opened
						WebSocketClientEvent lEvent = new WebSocketBaseClientEvent(this, EVENT_OPEN, "");
						// notify listeners that client has opened.
						notifyOpened(lEvent);

						return;
					} else // processing packet delivery acknowledge from the client
					if (MessagingControl.NAME_MESSAGE_DELIVERY_ACKNOWLEDGE.equals(lName)) {
						IPacketDeliveryListener lListener = mPacketDeliveryListeners
								.remove(lMessage.getString(MessagingControl.PROPERTY_DATA));
						if (null != lListener) {
							lListener.OnSuccess();
						}
					}

					return;
				}
			}
		}

		try {
			for (WebSocketClientFilter lFilter : mFilters) {
				lFilter.filterPacketIn(aDataPacket);
			}
		} catch (Exception lEx) {
			return;
		}

		// finally notify the listeners
		for (final WebSocketClientListener lListener : getListeners()) {
			final WebSocketPacket lPacket = aDataPacket;
			mListenersExecutor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						lListener.processPacket(aEvent, lPacket);
					} catch (Exception lEx) {
						// nothing, soppose to be catched internally
					}
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyReconnecting(final WebSocketClientEvent aEvent) {
		for (final WebSocketClientListener lListener : getListeners()) {
			mListenersExecutor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						lListener.processReconnecting(aEvent);
					} catch (Exception lEx) {
						// nothing, soppose to be catched internally
					}
				}
			});
		}
	}

	/**
	 * @return the mReliabilityOptions
	 */
	public ReliabilityOptions getReliabilityOptions() {
		return mReliabilityOptions;
	}

	/**
	 * @param mReliabilityOptions the mReliabilityOptions to set
	 */
	public void setReliabilityOptions(ReliabilityOptions mReliabilityOptions) {
		this.mReliabilityOptions = mReliabilityOptions;
	}

	/**
	 * @return the mHeaders
	 */
	public Headers getHeaders() {
		return mHeaders;
	}

	/**
	 * @return the ping interval
	 */
	public int getPingInterval() {
		return mPingInterval;
	}

	/**
	 *
	 * @param aPingInterval
	 */
	public void setPingInterval(int aPingInterval) {
		mPingInterval = aPingInterval;
	}

	class ReOpener implements Runnable {

		private WebSocketClientEvent mEvent;

		public ReOpener(WebSocketClientEvent aEvent) {
			mEvent = aEvent;
		}

		@Override
		public void run() {
			mIsReconnecting = false;
			notifyReconnecting(mEvent);
			try {
				open(mURI.toString());
				// did we configure reliability options?
				/*
				 * if (mReliabilityOptions != null &&
				 * mReliabilityOptions.getReconnectDelay() > 0) {
				 * mExecutor.schedule( new ReOpener(aEvent),
				 * mReliabilityOptions.getReconnectDelay(),
				 * TimeUnit.MILLISECONDS); }
				 */
			} catch (Exception lEx) {
				WebSocketClientEvent lEvent =
						new WebSocketBaseClientEvent(mEvent.getClient(), EVENT_CLOSE,
						lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage());
				notifyClosed(lEvent);
			}
		}
	}

	private void mAbortReconnect() {
		synchronized (mReconnectLock) {
			// cancel running re-connect task
			if (null != mReconnectorTask) {
				mReconnectorTask.cancel(true);
			}
			// reset internal re-connection flag
			mIsReconnecting = false;
			mReconnectorTask = null;
			// clean up all potentially old references to inactive tasks
			mExecutor.purge();
		}
	}

	private void mCheckReconnect(WebSocketClientEvent aEvent) {
		synchronized (mReconnectLock) {
			// first, purge all potentially old references to other tasks
			mExecutor.purge();
			// did we configure reliability options?
			// and is there now re-connection task already active?
			if (mReliabilityOptions != null
					&& mReliabilityOptions.getReconnectDelay() > 0
					&& !mIsReconnecting) {
				// schedule a re-connect action after the re-connect delay
				mIsReconnecting = true;
				mReconnectorTask = mExecutor.schedule(
						new ReOpener(aEvent),
						mReliabilityOptions.getReconnectDelay(),
						TimeUnit.MILLISECONDS);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyClosed(final WebSocketClientEvent aEvent) {
		for (final WebSocketClientListener lListener : getListeners()) {
			mListenersExecutor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						lListener.processClosed(aEvent);
					} catch (Exception lEx) {
						// nothing, suppose to be catched internally
					}
				}
			});
		}
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

	@Override
	public void setVersion(int aVersion) {
		this.mVersion = aVersion;
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
			} catch (Exception lEx) {
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

			WebSocketClientEvent lEvent =
					new WebSocketBaseClientEvent(mClient, EVENT_CLOSE, mCloseReason);
			// notify listeners that client has closed
			notifyClosed(lEvent);

			quit();

			mPingSender.cancel();


			if (!CR_CLIENT.equals(mCloseReason)) {
				mCheckReconnect(lEvent);
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
						mListenersExecutor.submit(new Runnable() {
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
				} catch (Exception lEx) {
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
						mListenersExecutor.submit(new Runnable() {
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

		class PingSenderTask extends TimerTask {

			@Override
			public void run() {
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
