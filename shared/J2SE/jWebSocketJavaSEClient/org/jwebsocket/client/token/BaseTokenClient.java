//	---------------------------------------------------------------------------
//	jWebSocket - BaseTokenClient (Community Edition, CE)
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
package org.jwebsocket.client.token;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.codec.binary.Base64;
import org.jwebsocket.api.*;
import org.jwebsocket.client.java.JWebSocketWSClient;
import org.jwebsocket.config.JWebSocketClientConstants;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.ReliabilityOptions;
import org.jwebsocket.kit.WebSocketEncoding;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketFrameType;
import org.jwebsocket.kit.WebSocketSubProtocol;
import org.jwebsocket.packetProcessors.CSVProcessor;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.packetProcessors.XMLProcessor;
import org.jwebsocket.token.PendingResponseQueueItem;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.token.WebSocketResponseTokenListener;
import org.jwebsocket.util.Tools;

/**
 * Token based implementation of {@code WebSocketClient}
 *
 * @author Alexander Schulze
 * @author puran
 * @author jang
 * @author Rolando Santamaria Maso
 * @version $Id:$
 */
@Deprecated
public class BaseTokenClient implements WebSocketTokenClient {

	/**
	 * base name space for jWebSocket
	 */
	private final static String NS_BASE = "org.jwebsocket";
	/**
	 * token client protocols
	 */
	private final static String WELCOME = "welcome";
	private final static String LOGIN = "login";
	private final static String SPRING_LOGON = "logon";
	private final static String SPRING_LOGOFF = "logoff";
	private final static String GOODBYE = "goodBye";
	private final static String LOGOUT = "logout";
	/**
	 * token id
	 */
	private int CUR_TOKEN_ID = 0;
	/**
	 * sub protocol value
	 */
	private WebSocketSubProtocol mSubProt = null;
	// private String mSubProt;
	// private WebSocketEncoding mEncoding;
	private String mUsername = null;
	private String mClientId = null;
	private final Map<Integer, PendingResponseQueueItem> mPendingResponseQueue = new FastMap<Integer, PendingResponseQueueItem>().shared();
	private final ScheduledThreadPoolExecutor mResponseQueueExecutor = new ScheduledThreadPoolExecutor(1);
	private List<String> mEncodingFormats = new FastList<String>();
	private WebSocketClient mClient;

	/**
	 * Get the WebSocketClient implementation instance
	 *
	 * @return
	 */
	public WebSocketClient getWebSocketClient() {
		return mClient;
	}

	/**
	 *
	 * @param aClient
	 */
	public BaseTokenClient(WebSocketClient aClient) {
		this(JWebSocketCommonConstants.WS_SUBPROT_DEFAULT, JWebSocketCommonConstants.WS_ENCODING_DEFAULT, aClient);
	}

	/**
	 *
	 */
	public BaseTokenClient() {
		this(new JWebSocketWSClient());
	}

	/**
	 *
	 * @param aReliabilityOptions
	 */
	public BaseTokenClient(ReliabilityOptions aReliabilityOptions) {
		this(JWebSocketCommonConstants.WS_SUBPROT_DEFAULT, JWebSocketCommonConstants.WS_ENCODING_DEFAULT);
		setReliabilityOptions(aReliabilityOptions);
	}

	/**
	 *
	 * @param aSubProt
	 * @param aEncoding
	 */
	public BaseTokenClient(String aSubProt, WebSocketEncoding aEncoding) {
		this(aSubProt, aEncoding, new JWebSocketWSClient());
	}

	/**
	 *
	 * @param aSubProt
	 * @param aEncoding
	 * @param aClient
	 */
	public BaseTokenClient(String aSubProt, WebSocketEncoding aEncoding, WebSocketClient aClient) {
		mSubProt = new WebSocketSubProtocol(aSubProt, aEncoding);
		mClient = aClient;

		addSubProtocol(mSubProt);
		addListener(new TokenClientListener());

		mEncodingFormats.add("base64");
		mEncodingFormats.add("zipBase64");

		// registering encoding filter
		addFilter(new WebSocketClientTokenFilter() {
			@Override
			public void filterTokenIn(Token aToken) throws Exception {
				Map<String, String> lEnc = aToken.getMap("enc", null);
				if (null == lEnc) {
					return;
				}

				for (String lAttr : lEnc.keySet()) {
					String lFormat = lEnc.get(lAttr);
					String lValue = aToken.getString(lAttr);
					if (aToken.getBoolean("__binaryData") && "data".equals(lAttr)) {
						continue;
					}
					if (!mEncodingFormats.contains(lFormat)) {
						throw new Exception("Invalid encoding format '" + lFormat + "' received (not supported). Token cannot be received!");
					} else if ("base64".equals(lFormat)) {
						aToken.setString(lAttr, new String(Tools.base64Decode(lValue)));
					} else if ("zipBase64".equals(lFormat)) {
						aToken.setString(lAttr, new String(Tools.unzip(lValue.getBytes(), Boolean.TRUE)));
					}
				}
			}

			@Override
			public void filterTokenOut(Token aToken) throws Exception {
				Map<String, String> lEnc = aToken.getMap("enc", null);
				if (null == lEnc) {
					return;
				}

				for (String lAttr : lEnc.keySet()) {
					String lFormat = lEnc.get(lAttr);
					String lValue = aToken.getString(lAttr);

					if (!mEncodingFormats.contains(lFormat)) {
						throw new Exception("Invalid encoding format '" + lFormat + "' received (not supported). Token cannot be sent!");
					} else if ("base64".equals(lFormat)) {
						aToken.setString(lAttr, Tools.base64Encode(lValue.getBytes()));
					} else if ("zipBase64".equals(lFormat)) {
						aToken.setString(lAttr, new String(Tools.zip(lValue.getBytes(), Boolean.TRUE)));
					}
				}
			}

			@Override
			public void filterPacketIn(WebSocketPacket aPacket) throws Exception {
			}

			@Override
			public void filterPacketOut(WebSocketPacket aPacket) throws Exception {
			}
		});
	}

	/**
	 * Returns the client encoding formats
	 *
	 * @return
	 */
	public List<String> getEncodingFormats() {
		return Collections.unmodifiableList(mEncodingFormats);
	}

	@Override
	public void setReliabilityOptions(ReliabilityOptions aReliabilityOptions) {
		mClient.setReliabilityOptions(aReliabilityOptions);
	}

	@Override
	public ReliabilityOptions getReliabilityOptions() {
		return mClient.getReliabilityOptions();
	}

	@Override
	public void setStatus(WebSocketStatus aStatus) {
		mClient.setStatus(aStatus);
	}

	@Override
	public void open(String aURL) throws WebSocketException {
		mClient.open(aURL);
	}

	@Override
	public void addFilter(WebSocketClientFilter aFilter) {
		mClient.addFilter(aFilter);
	}

	@Override
	public void removeFilter(WebSocketClientFilter aFilter) {
		mClient.removeFilter(aFilter);
	}

	@Override
	public List<WebSocketClientFilter> getFilters() {
		return mClient.getFilters();
	}

	@Override
	public void send(byte[] aData) throws WebSocketException {
		mClient.send(aData);
	}

	@Override
	public void send(byte[] aData, WebSocketFrameType aFrameType) throws WebSocketException {
		mClient.send(aData, aFrameType);
	}

	@Override
	public void send(String aData, String aEncoding) throws WebSocketException {
		mClient.send(aData, aEncoding);
	}

	@Override
	public void send(WebSocketPacket aPacket) throws WebSocketException {
		mClient.send(aPacket);
	}

	@Override
	public boolean isConnected() {
		return mClient.isConnected();
	}

	@Override
	public WebSocketStatus getStatus() {
		return mClient.getStatus();
	}

	@Override
	public void notifyOpened(WebSocketClientEvent aEvent) {
		mClient.notifyOpened(aEvent);
	}

	@Override
	public void notifyPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
		mClient.notifyPacket(aEvent, aPacket);
	}

	@Override
	public void notifyClosed(WebSocketClientEvent aEvent) {
		mClient.notifyClosed(aEvent);
	}

	@Override
	public void notifyReconnecting(WebSocketClientEvent aEvent) {
		mClient.notifyReconnecting(aEvent);
	}

	@Override
	public void addListener(WebSocketClientListener aListener) {
		mClient.addListener(aListener);
	}

	@Override
	public void removeListener(WebSocketClientListener aListener) {
		mClient.removeListener(aListener);
	}

	@Override
	public List<WebSocketClientListener> getListeners() {
		return mClient.getListeners();
	}

	@Override
	public void addSubProtocol(WebSocketSubProtocol aSubProt) {
		mClient.addSubProtocol(aSubProt);
	}

	@Override
	public String getNegotiatedSubProtocol() {
		return mClient.getNegotiatedSubProtocol();
	}

	@Override
	public WebSocketEncoding getNegotiatedEncoding() {
		return mClient.getNegotiatedEncoding();
	}

	@Override
	public void setVersion(int aVersion) {
		mClient.setVersion(aVersion);
	}

	@Override
	public Integer getMaxFrameSize() {
		return mClient.getMaxFrameSize();
	}

	@Override
	public void sendPacketInTransaction(WebSocketPacket aDataPacket, Integer aFragmentSize, IPacketDeliveryListener aListener) {
		mClient.sendPacketInTransaction(aDataPacket, aFragmentSize, aListener);
	}

	@Override
	public void sendPacketInTransaction(WebSocketPacket aDataPacket, IPacketDeliveryListener aListener) {
		mClient.sendPacketInTransaction(aDataPacket, aListener);
	}

	@Override
	public URI getURI() {
		return mClient.getURI();
	}

	/**
	 * WebSocketClient listener implementation that receives the data packet and
	 * creates <tt>token</tt> objects
	 *
	 * @author Alexander Schulze
	 */
	class TokenClientListener implements WebSocketClientListener {

		/**
		 * {@inheritDoc} Initialize all the variables when the process starts
		 */
		@Override
		public void processOpening(WebSocketClientEvent aEvent) {
			// sending high level client headers
			Token lHeaders = TokenFactory.createToken(JWebSocketClientConstants.NS_SYSTEM, "header");
			lHeaders.setString("clientType", "native");
			lHeaders.setString("clientName", "JavaSEClient");
			lHeaders.setString("clientVersion", "1.0");
			lHeaders.setString("clientInfo", "Java Client");
			lHeaders.setString("jwsType", "Java");
			lHeaders.setString("jwsVersion", "1.0");
			lHeaders.setList(JWebSocketCommonConstants.ENCODING_FORMATS_VAR_KEY, mEncodingFormats);

			try {
				sendToken(lHeaders);
			} catch (Exception lEx) {
				// never happens
			}
		}

		/**
		 * {@inheritDoc} Initialize all the variables when the process starts
		 */
		@Override
		public void processOpened(WebSocketClientEvent aEvent) {
			mUsername = null;
			mClientId = null;
		}

		/**
		 * {@inheritDoc} This callback method is invoked by jWebSocket client
		 * after the data is received from low-level <tt>WebSocket</tt>
		 * connection. This method then generates the <tt>token</tt> objects
		 * using the data packets.
		 */
		@Override
		public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {

			Token lToken = packetToToken(aPacket);

			try {
				for (WebSocketClientFilter lFilter : getFilters()) {
					if (lFilter instanceof WebSocketClientTokenFilter) {
						((WebSocketClientTokenFilter) lFilter).filterTokenIn(lToken);
					}
				}
			} catch (Exception lEx) {
				// leave the flow here since the token does not
				// passed the filtering process
				return;
			}

			String lType = lToken.getType();
			String lReqType = lToken.getString("reqType");

			if (lType != null) {
				if (WELCOME.equals(lType)) {
					mClientId = lToken.getString("sourceId");
					String lUsername = lToken.getString("username");
					if (null != lUsername && !lUsername.equals("anonymous")) {
						mUsername = lUsername;
						setStatus(WebSocketStatus.AUTHENTICATED);
					}

					// synchronizing the c2s encoding formats
					List<String> lEncodingFormats = lToken.getList(JWebSocketCommonConstants.ENCODING_FORMATS_VAR_KEY);
					for (String lClientFormat : mEncodingFormats) {
						if (!lEncodingFormats.contains(lClientFormat)) {
							mEncodingFormats.remove(lClientFormat);
						}
					}

				} else if (GOODBYE.equals(lType)) {
					mUsername = null;
				}
			}
			if (lReqType != null) {
				if (LOGIN.equals(lReqType) || SPRING_LOGON.equals(lReqType)) {
					mUsername = lToken.getString("username");
					setStatus(WebSocketStatus.AUTHENTICATED);
				} else if (LOGOUT.equals(lReqType) || SPRING_LOGOFF.equals(lReqType)) {
					setStatus(WebSocketStatus.OPEN);
					mUsername = null;
				}
			}

			//Notifying pending OnResponse callbacks if exists
			synchronized (mPendingResponseQueue) {
				// check if the response token is part of the pending responses queue
				Integer lUTID = lToken.getInteger("utid");
				Integer lCode = lToken.getInteger("code");
				// is there unique token id available in the response
				// and is there a matching pending response at all?
				PendingResponseQueueItem lPRQI = (lUTID != null ? mPendingResponseQueue.get(lUTID) : null);
				if (lPRQI != null) {
					// if so start analyzing
					WebSocketResponseTokenListener lWSRTL = lPRQI.getListener();
					if (lWSRTL != null) {
						// fire on response
						lWSRTL.OnResponse(lToken);
						// usable response code available?
						if (lCode != null) {
							if (lCode == 0) {
								lWSRTL.OnSuccess(lToken);
							} else {
								lWSRTL.OnFailure(lToken);
							}
						}
					}
					// and drop the pending queue item
					mPendingResponseQueue.remove(lUTID);
				}
			}

			// notifying listeners
			for (WebSocketClientListener lListener : getListeners()) {
				if (lListener instanceof WebSocketClientTokenListener) {
					((WebSocketClientTokenListener) lListener).processToken(aEvent, lToken);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void processClosed(WebSocketClientEvent aEvent) {
			// clean up resources
			mUsername = null;
			mClientId = null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void processReconnecting(WebSocketClientEvent aEvent) {
		}
	}

	@Override
	public void addTokenClientListener(WebSocketClientTokenListener aTokenListener) {
		addListener(aTokenListener);
	}

	@Override
	public void removeTokenClientListener(WebSocketClientTokenListener aTokenListener) {
		removeListener(aTokenListener);
	}

	@Override
	public void close() {
		try {
			mClient.close();
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}
		mUsername = null;
		mClientId = null;
	}

	/**
	 * @return the active username
	 */
	@Override
	public String getUsername() {
		return mUsername;
	}

	/**
	 *
	 * @param aUsername
	 */
	public void setUsername(String aUsername) {
		this.mUsername = aUsername;
	}

	@Override
	public boolean isAuthenticated() {
		return (mUsername != null);
	}

	/**
	 * @return the connection identifier
	 */
	public String getClientId() {
		return mClientId;
	}

	/**
	 *
	 * @param aPacket
	 * @return
	 */
	public Token packetToToken(WebSocketPacket aPacket) {
		Token lToken = null;
		if (JWebSocketCommonConstants.WS_FORMAT_JSON.equals(mSubProt.getFormat())) {
			lToken = JSONProcessor.packetToToken(aPacket);
		} else if (JWebSocketCommonConstants.WS_FORMAT_CSV.equals(mSubProt.getFormat())) {
			lToken = CSVProcessor.packetToToken(aPacket);
		} else if (JWebSocketCommonConstants.WS_FORMAT_XML.equals(mSubProt.getFormat())) {
			lToken = XMLProcessor.packetToToken(aPacket);
		}

		return lToken;
	}

	/**
	 *
	 * @param aToken
	 * @return
	 */
	public WebSocketPacket tokenToPacket(Token aToken) {
		WebSocketPacket lPacket = null;

		if (JWebSocketCommonConstants.WS_FORMAT_JSON.equals(mSubProt.getFormat())) {
			lPacket = JSONProcessor.tokenToPacket(aToken);
		} else if (JWebSocketCommonConstants.WS_FORMAT_CSV.equals(mSubProt.getFormat())) {
			lPacket = CSVProcessor.tokenToPacket(aToken);
		} else if (JWebSocketCommonConstants.WS_FORMAT_XML.equals(mSubProt.getFormat())) {
			lPacket = XMLProcessor.tokenToPacket(aToken);
		}

		return lPacket;
	}

	/**
	 * {@inheritDoc }
	 *
	 * @throws org.jwebsocket.kit.WebSocketException
	 */
	@Override
	public void sendToken(Token aToken) throws WebSocketException {
		try {
			for (WebSocketClientFilter lFilter : getFilters()) {
				if (lFilter instanceof WebSocketClientTokenFilter) {
					((WebSocketClientTokenFilter) lFilter).filterTokenOut(aToken);
				}
			}
		} catch (Exception lEx) {
			// leave the flow here since the token does not
			// passed the filtering process
			return;
		}

		__setUTID(aToken);
		send(tokenToPacket(aToken));
	}

	private class ResponseTimeoutTimer implements Runnable {

		private Integer mUTID = 0;

		public ResponseTimeoutTimer(Integer aUTID) {
			mUTID = aUTID;
		}

		@Override
		public void run() {
			synchronized (mPendingResponseQueue) {
				PendingResponseQueueItem lPRQI = (mUTID != null ? mPendingResponseQueue.get(mUTID) : null);
				if (lPRQI != null) {
					// if so start analyzing
					WebSocketResponseTokenListener lWSRTL = lPRQI.getListener();
					if (lWSRTL != null) {
						// fire on response
						lWSRTL.OnTimeout(lPRQI.getToken());
					}
					// and drop the pending queue item
					mPendingResponseQueue.remove(mUTID);
				}
			}
		}
	}

	private void __processResponseListener(Token aToken, WebSocketResponseTokenListener aListener) {
		if (null == aListener) {
			return;
		}

		// supporting response listener
		PendingResponseQueueItem lPRQI = new PendingResponseQueueItem(aToken, aListener);
		int lUTID = CUR_TOKEN_ID + 1;
		mPendingResponseQueue.put(lUTID, lPRQI);
		ResponseTimeoutTimer lRTT = new ResponseTimeoutTimer(lUTID);
		mResponseQueueExecutor.schedule(lRTT, aListener.getTimeout(), TimeUnit.MILLISECONDS);
	}

	private void __setUTID(Token aToken) {
		// adding the utid attribute
		CUR_TOKEN_ID++;
		aToken.setInteger("utid", CUR_TOKEN_ID);
	}

	/**
	 * {@inheritDoc }
	 *
	 * @throws org.jwebsocket.kit.WebSocketException
	 */
	@Override
	public void sendToken(Token aToken, WebSocketResponseTokenListener aResponseListener) throws WebSocketException {
		__processResponseListener(aToken, aResponseListener);

		// sending the token
		sendToken(aToken);
	}

	/**
	 * {@inheritDoc }
	 *
	 * @throws org.jwebsocket.kit.WebSocketException
	 */
	@Override
	public void sendTokenInTransaction(Token aToken, WebSocketResponseTokenListener aResponseListener,
			IPacketDeliveryListener aDeliveryListener) throws WebSocketException {
		sendTokenInTransaction(aToken, getMaxFrameSize(), aResponseListener, aDeliveryListener);
	}

	/**
	 * {@inheritDoc }
	 *
	 * @throws org.jwebsocket.kit.WebSocketException
	 */
	@Override
	public void sendTokenInTransaction(Token aToken, int aFragmentSize,
			final WebSocketResponseTokenListener aResponseListener) throws WebSocketException {
		sendTokenInTransaction(aToken, aFragmentSize, aResponseListener, new IPacketDeliveryListener() {
			@Override
			public long getTimeout() {
				return aResponseListener.getTimeout();
			}

			@Override
			public void OnTimeout() {
			}

			@Override
			public void OnSuccess() {
			}

			@Override
			public void OnFailure(Exception lEx) {
			}
		});
	}

	/**
	 * {@inheritDoc }
	 *
	 * @throws org.jwebsocket.kit.WebSocketException
	 */
	@Override
	public void sendTokenInTransaction(Token aToken, int aFragmentSize, WebSocketResponseTokenListener aResponseListener,
			IPacketDeliveryListener aDeliveryListener) throws WebSocketException {
		__processResponseListener(aToken, aResponseListener);
		__setUTID(aToken);

		// sending the token as packet in transaction
		sendPacketInTransaction(tokenToPacket(aToken), aFragmentSize, aDeliveryListener);
	}

	class ChunkableListener implements IChunkableDeliveryListener {

		Iterator<Token> mChunkableIterator;
		IChunkableDeliveryListener mOriginChunkableListener;
		long mSentTime;
		Token mCurrentChunk;
		String mNS, mType;
		WebSocketResponseTokenListener mResponseListener;
		Integer mFragmentSize;

		public ChunkableListener(Token aCurrentChunk,
				Iterator<Token> aChunkableIterator, WebSocketResponseTokenListener aResponseListener,
				IChunkableDeliveryListener aOriginChunkableListener, long aSentTime, Integer aFragmentSize) {
			mChunkableIterator = aChunkableIterator;
			mOriginChunkableListener = aOriginChunkableListener;
			mSentTime = aSentTime;
			mCurrentChunk = aCurrentChunk;
			mNS = aCurrentChunk.getNS();
			mType = aCurrentChunk.getType();
			mFragmentSize = aFragmentSize;
		}

		@Override
		public long getTimeout() {
			long lTimeout = mSentTime + mOriginChunkableListener.getTimeout() - System.currentTimeMillis();
			if (lTimeout < 0) {
				lTimeout = 0;
			}

			return lTimeout;
		}

		@Override
		public void OnTimeout() {
			mOriginChunkableListener.OnTimeout();
		}

		@Override
		public void OnSuccess() {
			// notify chunk delivered
			OnChunkDelivered(mCurrentChunk);

			// process next chunks
			if (mChunkableIterator.hasNext()) {
				mCurrentChunk = mChunkableIterator.next();
				try {
					if (null == mCurrentChunk) {
						throw new Exception("Iterator returned null on 'next' method call!");
					}

					// setting chunk properties
					mCurrentChunk.setNS(mNS);
					mCurrentChunk.setType(mType);
					mCurrentChunk.setChunk(true);
					if (!mChunkableIterator.hasNext()) {
						mCurrentChunk.setLastChunk(true);
					}

					sendTokenInTransaction(mCurrentChunk, mFragmentSize, mResponseListener, this);
				} catch (Exception lEx) {
					mOriginChunkableListener.OnFailure(lEx);
				}
			} else {
				mOriginChunkableListener.OnSuccess();
			}
		}

		@Override
		public void OnFailure(Exception lEx) {
			mOriginChunkableListener.OnFailure(lEx);
		}

		@Override
		public void OnChunkDelivered(Token aToken) {
			mOriginChunkableListener.OnChunkDelivered(mCurrentChunk);
		}
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void sendChunkable(IChunkable aChunkable, final WebSocketResponseTokenListener aResponseListener) {
		sendChunkable(aChunkable, aResponseListener, new IChunkableDeliveryListener() {
			@Override
			public void OnChunkDelivered(Token aToken) {
			}

			@Override
			public long getTimeout() {
				return aResponseListener.getTimeout();
			}

			@Override
			public void OnTimeout() {
			}

			@Override
			public void OnSuccess() {
			}

			@Override
			public void OnFailure(Exception lEx) {
			}
		});
	}

	class InChunkingResponseListener implements WebSocketResponseTokenListener {

		WebSocketResponseTokenListener mOriginListener;
		long mSentTime;

		public InChunkingResponseListener(WebSocketResponseTokenListener aOriginListener, long aSentTime) {
			mOriginListener = aOriginListener;
			mSentTime = aSentTime;
		}

		@Override
		public long getTimeout() {
			// setting the timeout parameter appropiate value
			// since WebSocketResponseTokenListener timeout is the global processing timeout
			long lTimeout = mSentTime + mOriginListener.getTimeout() - System.currentTimeMillis();
			if (lTimeout < 0) {
				lTimeout = 0;
			}

			return lTimeout;
		}

		@Override
		public void setTimeout(long aTimeout) {
			mOriginListener.setTimeout(aTimeout);
		}

		@Override
		public void OnTimeout(Token aToken) {
			mOriginListener.OnTimeout(aToken);
		}

		@Override
		public void OnResponse(Token aToken) {
			mOriginListener.OnResponse(aToken);
		}

		@Override
		public void OnSuccess(Token aToken) {
			mOriginListener.OnSuccess(aToken);
		}

		@Override
		public void OnFailure(Token aToken) {
			mOriginListener.OnFailure(aToken);
		}
	}

	/**
	 * Send an IChunkable object to the server
	 *
	 * @param aChunkable
	 * @param aResponseListener
	 * @param aDeliveryListener
	 */
	@Override
	public void sendChunkable(IChunkable aChunkable, WebSocketResponseTokenListener aResponseListener,
			IChunkableDeliveryListener aDeliveryListener) {
		try {
			if (0 > aChunkable.getMaxFrameSize()) {
				aChunkable.setMaxFrameSize(getMaxFrameSize());
			}
			Iterator<Token> lChunksIterator = aChunkable.getChunksIterator();
			if (!lChunksIterator.hasNext()) {
				throw new Exception("The chunks iterator is empty. No data to send!");
			}
			Token lCurrentChunk = lChunksIterator.next();
			if (null == lCurrentChunk) {
				throw new Exception("Iterator returned null on 'next' method call!");
			}

			// setting chunk properties
			lCurrentChunk.setNS(aChunkable.getNS());
			lCurrentChunk.setType(aChunkable.getType());
			lCurrentChunk.setChunk(true);
			if (!lChunksIterator.hasNext()) {
				lCurrentChunk.setLastChunk(true);
			}

			// sending chunks
			long lSentTime = System.currentTimeMillis();
			sendTokenInTransaction(lCurrentChunk, aChunkable.getFragmentSize(),
					new InChunkingResponseListener(aResponseListener, lSentTime),
					new ChunkableListener(
							lCurrentChunk,
							lChunksIterator,
							aResponseListener,
							aDeliveryListener,
							lSentTime,
							aChunkable.getFragmentSize()));
		} catch (Exception lEx) {
			aDeliveryListener.OnFailure(lEx);
		}
	}
	private final static String NS_SYSTEM_PLUGIN = NS_BASE + ".plugins.system";

	@Override
	public void login(String aUsername, String aPassword) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "login");
		lToken.setString("username", aUsername);
		lToken.setString("password", aPassword);
		sendToken(lToken);
	}

	@Override
	public void logout() throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "logout");
		sendToken(lToken);
	}

	@Override
	public void ping(boolean aEcho) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "ping");
		lToken.setBoolean("echo", aEcho);
		sendToken(lToken);
	}

	@Override
	public void sendText(String aTarget, String aData) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "send");
		lToken.setString("targetId", aTarget);
		lToken.setString("sourceId", getClientId());
		lToken.setString("sender", getUsername());
		lToken.setString("data", aData);
		sendToken(lToken);
	}

	@Override
	public void broadcastToSharedSession(Token aToken) throws WebSocketException {
		broadcastToSharedSession(aToken, false, null);
	}

	@Override
	public void broadcastToSharedSession(Token aToken, boolean aSenderIncluded,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		aToken.setNS(NS_SYSTEM_PLUGIN);
		aToken.setType("broadcastToSharedSession");
		aToken.setBoolean("senderIncluded", aSenderIncluded);

		sendToken(aToken, aListener);
	}

	@Override
	public void broadcastText(String aData) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "broadcast");
		lToken.setString("sourceId", getClientId());
		lToken.setString("sender", getUsername());
		lToken.setString("data", aData);
		lToken.setBoolean("senderIncluded", false);
		lToken.setBoolean("responseRequested", true);
		sendToken(lToken);
	}
	private final static String NS_FILESYSTEM_PLUGIN = NS_BASE + ".plugins.filesystem";

	/**
	 *
	 * @param aData
	 * @param aFilename
	 * @param aScope
	 * @param aNotify
	 * @throws WebSocketException
	 */
	public void saveFile(byte[] aData, String aFilename, String aScope, Boolean aNotify) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_FILESYSTEM_PLUGIN, "save");
		lToken.setString("sourceId", getClientId());
		lToken.setString("sender", getUsername());
		lToken.setString("filename", aFilename);
		// TODO: set mimetype correctly according to file extension based on configuration in jWebSocket.xml
		lToken.setString("mimetype", "image/jpeg");
		lToken.setString("scope", aScope);
		lToken.setBoolean("notify", aNotify);

		// lToken.setString("data", Base64.encodeBase64String(aData));
		lToken.setString("data", Tools.base64Encode(aData));
		sendToken(lToken);
	}

	/**
	 *
	 * @param aHeader
	 * @param aData
	 * @param aFilename
	 * @param aTarget
	 * @throws WebSocketException
	 */
	public void sendFile(String aHeader, byte[] aData, String aFilename, String aTarget) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_FILESYSTEM_PLUGIN, "send");
		lToken.setString("sourceId", getClientId());
		lToken.setString("sender", getUsername());
		lToken.setString("filename", aFilename);
		// TODO: set mimetype correctly according to file extension based on configuration in jWebSocket.xml
		lToken.setString("mimetype", "image/jpeg");
		lToken.setString("unid", aTarget);

		lToken.setString("data", aHeader + new String(Base64.encodeBase64(aData)));
		sendToken(lToken);
	}

	/*
	 * functions of the Admin Plug-in
	 */
	private final static String NS_ADMIN_PLUGIN = NS_BASE + ".plugins.admin";

	@Override
	public void disconnect() throws WebSocketException {
	}

	/**
	 *
	 * @throws WebSocketException
	 */
	public void shutdown() throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_ADMIN_PLUGIN, "shutdown");
		sendToken(lToken);
	}

	@Override
	public void getConnections() throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_ADMIN_PLUGIN, "getConnections");
		sendToken(lToken);
	}

	/**
	 *
	 * @param aUsername
	 * @throws WebSocketException
	 */
	public void getUserRights(String aUsername) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_ADMIN_PLUGIN, "getUserRights");
		lToken.setString("username", aUsername);
		sendToken(lToken);
	}

	/**
	 *
	 * @param aUsername
	 * @throws WebSocketException
	 */
	public void getUserRoles(String aUsername) throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_ADMIN_PLUGIN, "getUserRoles");
		lToken.setString("username", aUsername);
		sendToken(lToken);
	}

	/**
	 * Retrieves a list of public entries stored in the server-side session
	 * storage of many clients.
	 *
	 * @param aClients
	 * @param aKeys
	 * @param aListener
	 * @throws Exception
	 */
	public void sessionGetMany(List<String> aClients, List<String> aKeys,
			WebSocketResponseTokenListener aListener) throws Exception {
		sessionGetMany(aClients, aKeys, false, aListener);
	}

	/**
	 * Retrieves a list of public entries stored in the server-side session
	 * storage of many clients.
	 *
	 * @param aClients
	 * @param aKeys
	 * @param aConnectionStorage
	 * @param aListener
	 * @throws Exception
	 */
	public void sessionGetMany(List<String> aClients, List<String> aKeys,
			boolean aConnectionStorage, WebSocketResponseTokenListener aListener) throws Exception {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "sessionGetMany");
		lToken.setList("clients", aClients);
		lToken.setList("keys", aKeys);
		lToken.setBoolean("connectionStorage", aConnectionStorage);

		sendToken(lToken, aListener);
	}

	/**
	 * Retrieves all the entries stored in the server-side session storage of a
	 * given client. A client can only get the public entries from others.
	 *
	 * @param aClientId
	 * @param aPublic
	 * @param aConnectionStorage
	 * @param aListener
	 * @throws Exception
	 */
	public void sessionGetAll(String aClientId, boolean aPublic,
			boolean aConnectionStorage, WebSocketResponseTokenListener aListener) throws Exception {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "sessionGetAll");
		lToken.setString("clientId", aClientId);
		lToken.setBoolean("public", aPublic);
		lToken.setBoolean("connectionStorage", aConnectionStorage);

		sendToken(lToken, aListener);
	}

	/**
	 * Retrieves all the entries stored in the server-side session storage of a
	 * given client. A client can only get the public entries from others.
	 *
	 * @param aClientId
	 * @param aPublic
	 * @param aListener
	 * @throws Exception
	 */
	public void sessionGetAll(String aClientId, boolean aPublic,
			WebSocketResponseTokenListener aListener) throws Exception {
		sessionGetAll(aClientId, aPublic, false, aListener);
	}

	/**
	 * Retrieves the list of entry keys stored in the server-side session
	 * storage of a given client. A client can only get the public entries from
	 * others.
	 *
	 * @param aClientId
	 * @param aPublic
	 * @param aConnectionStorage
	 * @param aListener
	 * @throws Exception
	 */
	public void sessionKeys(String aClientId, boolean aPublic,
			boolean aConnectionStorage, WebSocketResponseTokenListener aListener) throws Exception {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "sessionKeys");
		lToken.setString("clientId", aClientId);
		lToken.setBoolean("public", aPublic);
		lToken.setBoolean("connectionStorage", aConnectionStorage);

		sendToken(lToken, aListener);
	}

	/**
	 * Retrieves the list of entry keys stored in the server-side session
	 * storage of a given client. A client can only get the public entries from
	 * others.
	 *
	 * @param aClientId
	 * @param aPublic
	 * @param aListener
	 * @throws Exception
	 */
	public void sessionKeys(String aClientId, boolean aPublic,
			WebSocketResponseTokenListener aListener) throws Exception {
		sessionKeys(aClientId, aPublic, false, aListener);
	}

	/**
	 * Removes a server-side client session storage entry given the entry key.
	 *
	 * @param aKey
	 * @param aPublic
	 * @param aConnectionStorage
	 * @param aListener
	 * @throws Exception
	 */
	public void sessionRemove(String aKey, boolean aPublic,
			boolean aConnectionStorage, WebSocketResponseTokenListener aListener) throws Exception {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "sessionRemove");
		lToken.setString("key", aKey);
		lToken.setBoolean("public", aPublic);
		lToken.setBoolean("connectionStorage", aConnectionStorage);

		sendToken(lToken, aListener);
	}

	/**
	 * Removes a server-side client session storage entry given the entry key.
	 *
	 * @param aKey
	 * @param aPublic
	 * @param aListener
	 * @throws Exception
	 */
	public void sessionRemove(String aKey, boolean aPublic,
			WebSocketResponseTokenListener aListener) throws Exception {
		sessionRemove(aKey, aPublic, false, aListener);
	}

	/**
	 * Gets a server-side client session storage entry given the entry key.
	 *
	 * @param aClientId
	 * @param aKey
	 * @param aPublic
	 * @param aConnectionStorage
	 * @param aListener
	 * @throws Exception
	 */
	public void sessionGet(String aClientId, String aKey, boolean aPublic,
			boolean aConnectionStorage, WebSocketResponseTokenListener aListener) throws Exception {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "sessionGet");
		lToken.setString("key", aKey);
		lToken.setString("clientId", aClientId);
		lToken.setBoolean("public", aPublic);
		lToken.setBoolean("connectionStorage", aConnectionStorage);

		sendToken(lToken, aListener);
	}

	/**
	 * Gets a server-side client session storage entry given the entry key.
	 *
	 * @param aClientId
	 * @param aKey
	 * @param aPublic
	 * @param aListener
	 * @throws Exception
	 */
	public void sessionGet(String aClientId, String aKey, boolean aPublic,
			WebSocketResponseTokenListener aListener) throws Exception {
		sessionGet(aClientId, aKey, aPublic, false, aListener);
	}

	/**
	 * Indicates if the client server-side session storage contains a custom
	 * entry given the entry key.
	 *
	 * @param aClientId
	 * @param aKey
	 * @param aPublic
	 * @param aConnectionStorage
	 * @param aListener
	 * @throws Exception
	 */
	public void sessionHas(String aClientId, String aKey, boolean aPublic,
			boolean aConnectionStorage, WebSocketResponseTokenListener aListener) throws Exception {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "sessionHas");
		lToken.setString("key", aKey);
		lToken.setString("clientId", aClientId);
		lToken.setBoolean("public", aPublic);
		lToken.setBoolean("connectionStorage", aConnectionStorage);

		sendToken(lToken, aListener);
	}

	/**
	 * Indicates if the client server-side session storage contains a custom
	 * entry given the entry key.
	 *
	 * @param aClientId
	 * @param aKey
	 * @param aPublic
	 * @param aListener
	 * @throws Exception
	 */
	public void sessionHas(String aClientId, String aKey, boolean aPublic,
			WebSocketResponseTokenListener aListener) throws Exception {
		sessionHas(aClientId, aKey, aPublic, false, aListener);
	}

	/**
	 * Put key/value entry in the server-side client session storage.
	 *
	 * @param aKey
	 * @param aValue
	 * @param aPublic
	 * @param aListener
	 * @throws Exception
	 */
	public void sessionPut(String aKey, Object aValue, boolean aPublic,
			WebSocketResponseTokenListener aListener) throws Exception {
		sessionPut(aKey, aValue, aPublic, false, aListener);
	}

	/**
	 * Put key/value entry in the server-side client session storage.
	 *
	 * @param aKey
	 * @param aValue
	 * @param aPublic
	 * @param aConnectionStorage
	 * @param aListener
	 * @throws Exception
	 */
	public void sessionPut(String aKey, Object aValue, boolean aPublic, boolean aConnectionStorage,
			WebSocketResponseTokenListener aListener) throws Exception {
		Token lToken = TokenFactory.createToken(NS_SYSTEM_PLUGIN, "sessionPut");
		lToken.setString("key", aKey);
		lToken.getMap().put("value", aValue);
		lToken.setBoolean("public", aPublic);
		lToken.setBoolean("connectionStorage", aConnectionStorage);

		sendToken(lToken, aListener);
	}

	/**
	 * Sets server-side plug-ins configuration on the client session storage
	 *
	 * @param aNS
	 * @param aConfiguration
	 * @throws Exception
	 */
	public void setConfiguration(String aNS, Map<String, Object> aConfiguration) throws Exception {
		for (String lKey : aConfiguration.keySet()) {
			if (aConfiguration.get(lKey) instanceof Map) {
				setConfiguration(aNS + "." + lKey, (Map<String, Object>) aConfiguration.get(lKey));
			} else {
				this.sessionPut(aNS + "." + lKey, aConfiguration.get(lKey), false, null);
			}
		}
	}

	@Override
	public void setPingInterval(int aInterval) {
		mClient.setPingInterval(aInterval);
	}

	@Override
	public int getPingInterval() {
		return mClient.getPingInterval();
	}

	@Override
	public Object getParam(String aKey, Object aDefault) {
		return mClient.getParam(aKey, aDefault);
	}

	@Override
	public Object getParam(String aKey) {
		return mClient.getParam(aKey);
	}

	@Override
	public void setParam(String aKey, Object aValue) {
		mClient.setParam(aKey, aValue);
	}

	@Override
	public void open(int aVersion, String aURI) throws WebSocketException {
		mClient.open(aVersion, aURI);
	}

	@Override
	public void open(int aVersion, String aURI, String aSubProtocols) throws WebSocketException {
		mClient.open(aVersion, aURI, aSubProtocols);
	}
}
