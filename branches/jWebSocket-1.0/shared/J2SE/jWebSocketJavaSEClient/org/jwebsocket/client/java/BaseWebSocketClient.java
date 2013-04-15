//	---------------------------------------------------------------------------
//	jWebSocket - BaseWebSocketClient (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
import java.nio.ByteBuffer;
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
import org.jwebsocket.util.Fragmentation;
import org.jwebsocket.util.HttpCookie;
import org.jwebsocket.util.Tools;

/**
 * Base {@code WebSocket} implementation based on
 * http://weberknecht.googlecode.com by Roderick Baier. This uses thread model
 * for handling WebSocket connection which is defined by the
 * <tt>WebSocket</tt>
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
	private Map<Integer, IPacketDeliveryListener> mPacketDeliveryListeners = new FastMap<Integer, IPacketDeliveryListener>().shared();
	private Map<Integer, TimerTask> mPacketDeliveryTimerTasks = new FastMap<Integer, TimerTask>().shared();
	private final Object mPacketDeliveryListenersLock = new Object();
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
	public void open(String aURI) throws WebSocketException {
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
	 */
	public void open(int aVersion, String aURI) {
		String lSubProtocols = generateSubProtocolsHeaderValue();
		open(aVersion, aURI, lSubProtocols);
	}

	/**
	 *
	 * @param aVersion
	 * @param aURI
	 * @param aSubProtocols
	 */
	public void open(int aVersion, String aURI, String aSubProtocols) {
		try {
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
			Integer aFragmentSize, final IPacketDeliveryListener aListener) {
		// getting packet identifier
		final Integer lPacketId = Fragmentation.generateUID();

		// ommiting control frames
		if (WebSocketFrameType.BINARY.equals(aDataPacket.getFrameType())
				|| WebSocketFrameType.TEXT.equals(aDataPacket.getFrameType())) {

			String lPacketPrefix = lPacketId.toString() + Fragmentation.PACKET_ID_DELIMETER;

			try {
				if (!(lPacketPrefix.length() + aDataPacket.size() <= getMaxFrameSize())) {
					throw new Exception("The packet size exceeds the max frame size supported by the client! "
							+ "Consider that the packet has been prefixed with " + lPacketPrefix.length()
							+ " bytes for transaction.");

				}

				if (!(aFragmentSize > 0 && aFragmentSize <= getMaxFrameSize())) {
					throw new Exception("Invalid 'fragment size' argument! "
							+ "Expected: fragment_size > 0 && fragment_size <= MAX_FRAME_SIZE");
				}
				// processing fragmentation
				if (aFragmentSize < aDataPacket.size() && aFragmentSize < getMaxFrameSize()) {
					// first fragment is never the last
					boolean lIsLast = false;

					// fragmentation id, allows multiplexing
					int lFragmentationId = Fragmentation.generateUID();

					// creating a special packet for fragmentation
					WebSocketPacket lPacketFragmented = Fragmentation.createFragmentedPacket(
							Arrays.copyOfRange(aDataPacket.getByteArray(), 0, aFragmentSize),
							lFragmentationId, lIsLast);

					// checking the new packet size
					if (!(lPacketFragmented.size() + lPacketPrefix.length() <= getMaxFrameSize())) {
						throw new Exception("The packet size exceeds the max frame size supported by the client! "
								+ "Consider that the packet has been prefixed with "
								+ (lPacketFragmented.size() + lPacketPrefix.length() - aDataPacket.size())
								+ " bytes for fragmented transaction.");
					}

					// process fragmentation
					final BaseWebSocketClient lSender = this;
					sendPacketInTransaction(
							lPacketFragmented,
							// passing special listener for fragmentation
							Fragmentation.createListener(
							aDataPacket,
							aFragmentSize,
							aListener,
							System.currentTimeMillis(),
							lFragmentationId,
							new InFragmentationListenerSender() {
						@Override
						public void sendPacketInTransaction(WebSocketPacket aDataPacket, IPacketDeliveryListener aListener) {
							lSender.sendPacketInTransaction(aDataPacket, aListener);
						}
					}));
					return;
				}

				// preparing to send in transaction
				byte[] lPrefixBytes = lPacketPrefix.getBytes();
				// adding the prefix to the packet content
				ByteBuffer lBuffer = ByteBuffer.allocate(lPrefixBytes.length + aDataPacket.size());
				lBuffer.put(lPrefixBytes);
				lBuffer.put(aDataPacket.getByteArray());

				// setting the new packet content
				aDataPacket.setByteArray(lBuffer.array());
				lBuffer = null;

				// saving the delivery listener
				mPacketDeliveryListeners.put(lPacketId, aListener);

				// setting the timer task for timeout support
				TimerTask lTT = new TimerTask() {
					@Override
					public void run() {
						synchronized (mPacketDeliveryListenersLock) {
							if (mPacketDeliveryListeners.containsKey(lPacketId)) {
								// cleaning expired data and calling timeout
								mPacketDeliveryTimerTasks.remove(lPacketId);
								mPacketDeliveryListeners.remove(lPacketId).OnTimeout();
							}
						}
					}
				};

				// saving the timer task reference
				mPacketDeliveryTimerTasks.put(lPacketId, lTT);

				// sending the packet to the client
				send(aDataPacket);

				// schedule the timer task
				try {
					Tools.getTimer().schedule(lTT, aListener.getTimeout());
				} catch (IllegalStateException lEx) {
					// nothing, the task was cancelled
				}
			} catch (Exception lEx) {
				synchronized (mPacketDeliveryListenersLock) {
					if (mPacketDeliveryListeners.containsKey(lPacketId)) {
						// cleaning expired data and calling OnFailure 
						if (mPacketDeliveryTimerTasks.containsKey(lPacketId)) {
							mPacketDeliveryTimerTasks.remove(lPacketId).cancel();
						}
						mPacketDeliveryListeners.remove(lPacketId);
					}
				}
				aListener.OnFailure(lEx);
			}
		} else {
			aListener.OnFailure(new Exception("Control frames cannot be sent in transaction!"));
		}
	}

	@Override
	public void send(byte[] aData) throws WebSocketException {
		synchronized (mWriteLock) {
			if (isHixie()) {
				sendInternal(aData);
			} else {
				WebSocketPacket lPacket = new RawPacket(aData);
				lPacket.setFrameType(
						WebSocketProtocolAbstraction.encodingToFrameType(
						mNegotiatedSubProtocol.getEncoding()));
				sendInternal(
						WebSocketProtocolAbstraction.rawToProtocolPacket(
						mVersion, lPacket));
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
				sendInternal(WebSocketProtocolAbstraction.rawToProtocolPacket(mVersion, aDataPacket));
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
	public void notifyPacket(final WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
		// supporting the max frame size handshake
		String lData = aPacket.getString();
		if (null == mMaxFrameSize) {
			int lPos = lData.indexOf(Fragmentation.ARG_MAX_FRAME_SIZE);
			if (0 == lPos) {
				mMaxFrameSize = Integer.parseInt(lData.substring(Fragmentation.ARG_MAX_FRAME_SIZE.length()));

				// The end of the "max frame size" handshake indicates that the connection is finally opened
				WebSocketClientEvent lEvent = new WebSocketBaseClientEvent(this, EVENT_OPEN, "");
				// notify listeners that client has opened.
				notifyOpened(lEvent);

				return;
			}
		} else if (lData.length() > mMaxFrameSize) {
			// Data packet discarded. The packet size exceeds the max frame size supported by the client!
			return;
		}

		// processing packet delivery acknowledge from the server
		if (lData.length() <= (10 + Fragmentation.PACKET_DELIVERY_ACKNOWLEDGE_PREFIX.length())) {
			if (lData.startsWith(Fragmentation.PACKET_DELIVERY_ACKNOWLEDGE_PREFIX)) {
				try {
					// getting the delivered packet id
					Integer lPID = Integer.parseInt(lData.replace(Fragmentation.PACKET_DELIVERY_ACKNOWLEDGE_PREFIX, ""));

					synchronized (mPacketDeliveryListenersLock) {
						if (mPacketDeliveryListeners.containsKey(lPID)) {
							// canceling timeout timer task
							mPacketDeliveryTimerTasks.remove(lPID).cancel();
							// calling the success callback on the delivery listener
							mPacketDeliveryListeners.remove(lPID).OnSuccess();
						}
					}
				} catch (NumberFormatException lEx) {
				}
				// do not process the packet because it is a delivery acknowledge
				return;
			}
		}

		// supporting packet delivery acknowledge to the server
		int lPos = lData.indexOf(Fragmentation.PACKET_ID_DELIMETER);
		if (lPos >= 0 && lPos <= 10) {
			try {
				Integer lPacketId = Integer.parseInt(lData.substring(0, lPos));
				aPacket.setString(lData.substring(lPos + 1));

				// sending acknowledge packet
				send(new RawPacket(Fragmentation.PACKET_DELIVERY_ACKNOWLEDGE_PREFIX + lPacketId.toString()));
			} catch (Exception lEx) {
			}
		}

		// supporting fragmentation
		String lFragmentContent;
		String lKey;
		if (lData.startsWith(Fragmentation.PACKET_FRAGMENT_PREFIX)) {
			lPos = lData.indexOf(Fragmentation.PACKET_ID_DELIMETER);
			lKey = lData.substring(0, lPos);
			lFragmentContent = lData.substring(lPos + 1);

			// storing the packet fragment
			if (!mFragments.containsKey(lKey)) {
				mFragments.put(lKey, lFragmentContent);
			} else {
				mFragments.put(lKey, mFragments.get(lKey) + lFragmentContent);
			}

			// do not process fragment packets
			return;
		} else if (lData.startsWith(Fragmentation.PACKET_LAST_FRAGMENT_PREFIX)) {
			lPos = lData.indexOf(Fragmentation.PACKET_ID_DELIMETER);
			lKey = lData.substring(Fragmentation.PACKET_LAST_FRAGMENT_PREFIX.length(), lPos);
			lFragmentContent = lData.substring(lPos + 1);

			// getting the complete packet content
			aPacket.setString(mFragments.remove(Fragmentation.PACKET_FRAGMENT_PREFIX + lKey) + lFragmentContent);
		}

		try {
			for (WebSocketClientFilter lFilter : mFilters) {
				lFilter.filterPacketIn(aPacket);
			}
		} catch (Exception lEx) {
			return;
		}

		// finally notify the listeners
		for (final WebSocketClientListener lListener : getListeners()) {
			final WebSocketPacket lPacket = aPacket;
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
				sendPing();
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

						WebSocketClientEvent lWSCE = new WebSocketTokenClientEvent(mClient, null, null);
						RawPacket lPacket = new RawPacket(lBuff.toByteArray());

						lBuff.reset();
						notifyPacket(lWSCE, lPacket);
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
			WebSocketClientEvent lWSCE;
			WebSocketFrameType lFrameType;

			while (mIsRunning) {
				try {
					WebSocketPacket lPacket = WebSocketProtocolAbstraction.protocolToRawPacket(mVersion, mIS);
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
						WebSocketPacket lPong = new RawPacket(
								WebSocketFrameType.PONG, "");
						send(lPong);
					} else if (WebSocketFrameType.PONG == lFrameType) {
						// TODO: need to process connection management here!
					} else if (WebSocketFrameType.TEXT == lFrameType) {
						lWSCE = new WebSocketTokenClientEvent(mClient, null, null);
						notifyPacket(lWSCE, lPacket);
					}
				} catch (Exception lEx) {
					mIsRunning = false;
					setCloseReason(lEx.getClass().getName() + " in hybi processor: " + lEx.getMessage());
				}
			}
		}

		private void sendPing() {
			SendPing lSendPing = new SendPing();
			lSendPing.start();
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

		class SendPing extends Thread {

			@Override
			public void run() {
				try {
					Thread.currentThread().setName("jWebSocket-Client " + getId());
					// TODO: Don't we need a loop here?
					Thread.sleep(getPingInterval());
					WebSocketPacket lPing = new RawPacket(WebSocketFrameType.PING, "Hello");
					send(lPing);
				} catch (Exception lEx) {
				}
			}
			
			// TODO: What about shutting down the thread on application termination?
		}
	}
}
