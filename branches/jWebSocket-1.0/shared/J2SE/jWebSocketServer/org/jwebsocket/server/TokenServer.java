//	---------------------------------------------------------------------------
//	jWebSocket - Token Server (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//      Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.server;

import static java.lang.Boolean.TRUE;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.*;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.connectors.InternalConnector;
import org.jwebsocket.filter.TokenFilterChain;
import org.jwebsocket.kit.*;
import org.jwebsocket.listener.WebSocketServerTokenEvent;
import org.jwebsocket.listener.WebSocketServerTokenListener;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.plugins.TokenPlugInChain;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.JMSManager;
import org.springframework.util.Assert;

/**
 * @author Alexander Schulze
 * @author jang
 * @author Rolando Betancourt Toucet
 */
public class TokenServer extends BaseServer {

	private static final Logger mLog = Logging.getLogger(TokenServer.class);
	// specify shared connector variables
	/**
	 *
	 */
	private volatile boolean mIsAlive = false;
	private static ExecutorService mCachedThreadPool;
	private final static int TIME_OUT_TERMINATION_THREAD = 10;
	private final int mCorePoolSize;
	private final int mMaximumPoolSize;
	private final int mKeepAliveTime;
	private final int mBlockingQueueSize;
	private JMSManager mJMSManager = null;
	protected IEventBus mEventBus;

	/**
	 *
	 * @param aServerConfig
	 */
	public TokenServer(ServerConfiguration aServerConfig) {
		super(aServerConfig);
		mPlugInChain = new TokenPlugInChain(this);
		mFilterChain = new TokenFilterChain(this);

		mCorePoolSize = aServerConfig.getThreadPoolConfig().getCorePoolSize();
		mMaximumPoolSize = aServerConfig.getThreadPoolConfig().getMaximumPoolSize();
		mKeepAliveTime = aServerConfig.getThreadPoolConfig().getKeepAliveTime();
		mBlockingQueueSize = aServerConfig.getThreadPoolConfig().getBlockingQueueSize();

		BaseToken.setExclLogField(aServerConfig.getSettings());
	}

	@Override
	public void startServer() throws WebSocketException {
		// Create the thread pool.
		mCachedThreadPool = new ThreadPoolExecutor(mCorePoolSize, mMaximumPoolSize, mKeepAliveTime, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(mBlockingQueueSize));
		mIsAlive = true;
		if (mLog.isInfoEnabled()) {
			mLog.info("Token server '" + getId() + "' started.");
		}
	}

	@Override
	public boolean isAlive() {
		// nothing special to do here.
		// Token server does not contain any thread or similar.
		return mIsAlive;
	}

	@Override
	public void stopServer() throws WebSocketException {
		mIsAlive = false;
		// Shutdown the thread pool
		if (mCachedThreadPool != null) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Shutting down token server threadPool.");
			}
			mCachedThreadPool.shutdown(); // Disable new tasks from being
			// submitted
			try {
				// Wait a while for existing tasks to terminate
				if (!mCachedThreadPool.awaitTermination(TIME_OUT_TERMINATION_THREAD, TimeUnit.SECONDS)) {
					mCachedThreadPool.shutdownNow();
					/*
					 * // Cancel currently // executing
					 * tasks // Wait a while for tasks to
					 * respond to being cancelled if
					 * (!mCachedThreadPool.awaitTermination(TIME_OUT_TERMINATION_THREAD,
					 * TimeUnit.SECONDS)) { mLog.error("Pool
					 * did not terminate");
					 * mCachedThreadPool.shutdownNow(); }
					 */
				}
			} catch (InterruptedException lEx) {
				// (Re-)Cancel if current thread also interrupted
				mCachedThreadPool.shutdownNow();
			}
		}
		if (mLog.isInfoEnabled()) {
			mLog.info("Token server '" + getId() + "' stopped.");
		}
	}

	/**
	 * removes a plug-in from the plug-in chain of the server.
	 *
	 * @param aPlugIn
	 */
	public void removePlugIn(WebSocketPlugIn aPlugIn) {
		mPlugInChain.removePlugIn(aPlugIn);
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing engine '" + aEngine.getId() + "' started...");
		}
		mPlugInChain.engineStarted(aEngine);
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing engine '" + aEngine.getId() + "' stopped...");
		}
		mPlugInChain.engineStopped(aEngine);
	}

	/**
	 * {@inheritDoc }
	 *
	 * @param aConnector
	 */
	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		if (aConnector.supportTokens()) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Processing connector '" + aConnector.getId() + "' started...");
			}
			// notify plugins that a connector has started,
			// i.e. a client was sconnected.
			mPlugInChain.connectorStarted(aConnector);
		}
		super.connectorStarted(aConnector);
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// notify plugins that a connector has stopped,
		// i.e. a client was disconnected.
		if (aConnector.supportTokens()) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Processing connector '"
						+ aConnector.getId() + "' stopped...");
			}
			mPlugInChain.connectorStopped(aConnector, aCloseReason);
		}
		super.connectorStopped(aConnector, aCloseReason);
	}

	/**
	 *
	 * @param aConnector
	 * @param aDataPacket
	 * @return
	 */
	public Token packetToToken(WebSocketConnector aConnector, WebSocketPacket aDataPacket) {
		String lFormat = aConnector.getHeader().getFormat();
		return TokenFactory.packetToToken(lFormat, aDataPacket);
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 * @return
	 */
	public WebSocketPacket tokenToPacket(WebSocketConnector aConnector, Token aToken) {
		String lFormat = aConnector.getHeader().getFormat();
		return TokenFactory.tokenToPacket(lFormat, aToken);
	}

	public void broadcastToSharedSession(String aSenderId, String aSessionId, Token aToken, boolean aSenderIncluded) {
		// getting shared session connectors
		Collection<WebSocketConnector> lConnectors = getSharedSessionConnectors(aSessionId).values();
		for (WebSocketConnector lConnector : lConnectors) {
			if (!aSenderIncluded && aSenderId.equals(lConnector.getId())) {
				continue;
			}

			sendToken(lConnector, aToken);
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void processFilteredToken(WebSocketConnector aConnector, Token aToken) {
		getPlugInChain().processToken(aConnector, aToken);
		// forward the token to the listener chain
		List<WebSocketServerListener> lListeners = getListeners();
		WebSocketServerTokenEvent lEvent = new WebSocketServerTokenEvent(aConnector, this);
		for (WebSocketServerListener lListener : lListeners) {
			if (lListener != null && lListener instanceof WebSocketServerTokenListener) {
				((WebSocketServerTokenListener) lListener).processToken(lEvent, aToken);
			}
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void processToken(WebSocketConnector aConnector, Token aToken) {
		// before forwarding the token to the plug-ins push it through filter
		// chain

		// TODO: Remove this temporary hack with final release 1.0
		// this was required to ensure upward compatibility from 0.10 to 0.11
		String lNS = aToken.getNS();
		if (lNS != null && lNS.startsWith("org.jWebSocket")) {
			aToken.setNS("org.jwebsocket" + lNS.substring(14));
		}

		FilterResponse lFilterResponse = getFilterChain().processTokenIn(aConnector, aToken);

		// only forward the token to the plug-in chain
		// if filter chain does not response "aborted"
		if (!lFilterResponse.isRejected()) {
			processFilteredToken(aConnector, aToken);
		}
	}

	@Override
	public void processPacket(WebSocketEngine aEngine,
			final WebSocketConnector aConnector, WebSocketPacket aDataPacket) {
		// is the data packet supposed to be interpreted as token?
		if (!WebSocketFrameType.BINARY.equals(aDataPacket.getFrameType())) {
			if (aConnector.supportTokens()) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Processing packet as token...");
				}

				final Token lToken = packetToToken(aConnector, aDataPacket);
				if (lToken != null) {
					boolean lRunReqInOwnThread
							= TRUE.equals(lToken.getBoolean("spawnThread"))
							|| "true".equals(lToken.getString("spawnThread"));
					// TODO: create list of running threads and close all properly
					// on shutdown
					if (lRunReqInOwnThread) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Processing threaded token '"
									+ Logging.getTokenStr(lToken)
									+ "' from '" + aConnector + "'...");
						}
						mCachedThreadPool.execute(new Runnable() {
							@Override
							public void run() {
								processToken(aConnector, lToken);
							}
						});
					} else {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Processing token '" + Logging.getTokenStr(lToken)
									+ "' from '" + aConnector + "'...");
						}
						processToken(aConnector, lToken);
					}
				} else {
					mLog.error("Packet '" + Logging.getTokenStr(aDataPacket.toString())
							+ "' could not be converted into token.");
				}
			} else {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Processing packet as custom packet...");
				}
			}
		}
		super.processPacket(aEngine, aConnector, aDataPacket);
	}

	/**
	 *
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	public void sendToken(WebSocketConnector aSource,
			WebSocketConnector aTarget, Token aToken) {
		sendTokenData(aSource, aTarget, aToken, false);
	}

	/**
	 *
	 * @param aTarget
	 * @param aToken
	 */
	public void sendToken(WebSocketConnector aTarget, Token aToken) {
		sendToken(null, aTarget, aToken);
	}

	/**
	 *
	 * @param aTarget
	 * @param aToken
	 * @param aListener
	 */
	public void sendTokenInTransaction(WebSocketConnector aTarget, Token aToken,
			IPacketDeliveryListener aListener) {
		sendTokenInTransaction(aTarget, aToken, aTarget.getMaxFrameSize(), aListener);
	}

	/**
	 * Sends a token fragmented
	 *
	 * @param aTarget
	 * @param aToken
	 * @param aFragmentSize
	 */
	public void sendTokenFragmented(WebSocketConnector aTarget, Token aToken, int aFragmentSize) {
		sendTokenInTransaction(aTarget, aToken, aFragmentSize, new IPacketDeliveryListener() {
			@Override
			public long getTimeout() {
				return 60 * 1000;
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
	 *
	 * @param aTarget
	 * @param aToken
	 * @param aFragmentSize
	 * @param aListener
	 */
	public void sendTokenInTransaction(WebSocketConnector aTarget, Token aToken,
			int aFragmentSize, IPacketDeliveryListener aListener) {
		Assert.notNull(aTarget, "The target connector argument cannot be null!");
		Assert.notNull(aToken, "The token argument cannot be null!");
		Assert.notNull(aListener, "The listener argument cannot be null!");

		if (aTarget.supportTokens()) {
			// before sending the token push it through filter chain
			FilterResponse lFilterResponse = getFilterChain().processTokenOut(null, aTarget, aToken);
			if (!lFilterResponse.isRejected()) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Sending token '" + Logging.getTokenStr(aToken)
							+ "' to '" + aTarget + "'...");
				}
				if (aTarget.isInternal()) {
					// tokenize before pass the token to the internal client in order 
					// to break object references
					Token lToken = JSONProcessor.packetToToken(JSONProcessor.tokenToPacket(aToken));
					// adressing the token to the internal client
					((InternalConnector) aTarget).handleIncomingToken(lToken);
					aListener.OnSuccess();
				} else {
					super.sendPacketInTransaction(
							aTarget,
							tokenToPacket(aTarget, aToken),
							aFragmentSize,
							aListener);
				}
			} else {
				aListener.OnFailure(new Exception("The token has been rejected by the filters!!"));
			}
		} else {
			aListener.OnFailure(new Exception("Target connector '"
					+ aTarget.getId()
					+ "' does not support tokens!"));
		}
	}

	class ChunkableListener implements IChunkableDeliveryListener {

		WebSocketConnector mTarget;
		Iterator<Token> mChunkableIterator;
		IChunkableDeliveryListener mOriginListener;
		long mSentTime;
		Token mCurrentChunk;
		String mNS, mType;
		Integer mFragmentSize;

		public ChunkableListener(WebSocketConnector aTarget, Token aCurrentChunk,
				Iterator<Token> aChunkableIterator, IChunkableDeliveryListener aOriginListener,
				long aSentTime, Integer aFragmentSize) {
			mTarget = aTarget;
			mChunkableIterator = aChunkableIterator;
			mOriginListener = aOriginListener;
			mSentTime = aSentTime;
			mCurrentChunk = aCurrentChunk;
			mNS = aCurrentChunk.getNS();
			mType = aCurrentChunk.getType();
			mFragmentSize = aFragmentSize;
		}

		@Override
		public long getTimeout() {
			long lTimeout = mSentTime + mOriginListener.getTimeout() - System.currentTimeMillis();
			if (lTimeout < 0) {
				lTimeout = 0;
			}

			return lTimeout;
		}

		@Override
		public void OnTimeout() {
			mOriginListener.OnTimeout();
		}

		@Override
		public void OnSuccess() {
			// notify chunk delivered
			OnChunkDelivered(mCurrentChunk);

			// process next chunks
			if (mChunkableIterator.hasNext()) {
				try {
					mCurrentChunk = mChunkableIterator.next();
					Assert.notNull(mCurrentChunk, "Iterator returned null on 'next' method call!");

					// setting chunk properties
					mCurrentChunk.setNS(mNS);
					mCurrentChunk.setType(mType);
					mCurrentChunk.setChunk(true);
					if (!mChunkableIterator.hasNext()) {
						mCurrentChunk.setLastChunk(true);
					}

					sendTokenInTransaction(mTarget, mCurrentChunk, mFragmentSize, this);
				} catch (Exception lEx) {
					mOriginListener.OnFailure(lEx);
				}
			} else {
				mOriginListener.OnSuccess();
			}
		}

		@Override
		public void OnFailure(Exception lEx) {
			mOriginListener.OnFailure(lEx);
		}

		@Override
		public void OnChunkDelivered(Token aToken) {
			mOriginListener.OnChunkDelivered(mCurrentChunk);
		}
	}

	/**
	 * @param aConnector
	 * @param aChunkable
	 */
	public void sendChunkable(WebSocketConnector aConnector, IChunkable aChunkable) {

		sendChunkable(aConnector, aChunkable, new IChunkableDeliveryListener() {
			@Override
			public void OnChunkDelivered(Token aToken) {
			}

			@Override
			public long getTimeout() {
				return 60 * 1000;
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
	 *
	 * @param aConnector
	 * @param aChunkable
	 * @param aListener
	 */
	public void sendChunkable(WebSocketConnector aConnector, IChunkable aChunkable,
			IChunkableDeliveryListener aListener) {
		try {
			if (0 > aChunkable.getMaxFrameSize()) {
				aChunkable.setMaxFrameSize(aConnector.getMaxFrameSize());
			}
			Iterator<Token> lChunksIterator = aChunkable.getChunksIterator();
			Assert.isTrue(lChunksIterator.hasNext(), "The chunks iterator is empty. No data to send!");
			Token lCurrentChunk = lChunksIterator.next();
			Assert.notNull(lCurrentChunk, "Iterator returned null on 'next' method call!");

			// setting chunk properties
			lCurrentChunk.setNS(aChunkable.getNS());
			lCurrentChunk.setType(aChunkable.getType());
			lCurrentChunk.setChunk(true);
			if (!lChunksIterator.hasNext()) {
				lCurrentChunk.setLastChunk(true);
			} else {
				lCurrentChunk.setLastChunk(false);
			}

			// sending chunks
			sendTokenInTransaction(aConnector, lCurrentChunk, aChunkable.getFragmentSize(), new ChunkableListener(
					aConnector,
					lCurrentChunk,
					lChunksIterator,
					aListener,
					System.currentTimeMillis(),
					aChunkable.getFragmentSize()));
		} catch (Exception lEx) {
			aListener.OnFailure(lEx);
		}
	}

	/**
	 *
	 * @param aTarget
	 * @param aToken
	 * @return
	 */
	public IOFuture sendTokenAsync(WebSocketConnector aTarget, Token aToken) {
		return sendTokenData(null, aTarget, aToken, true);
	}

	/**
	 *
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 * @return
	 */
	public IOFuture sendTokenAsync(WebSocketConnector aSource,
			WebSocketConnector aTarget, Token aToken) {
		return sendTokenData(aSource, aTarget, aToken, true);
	}

	/**
	 * Sends a token to a connector
	 *
	 * @param aConnectorId The target connector id
	 * @param aToken The token to be sent
	 */
	public void sendToken(String aConnectorId, Token aToken) {
		Iterator<WebSocketEngine> lIt = getEngines().values().iterator();
		while (lIt.hasNext()) {
			WebSocketConnector lConnector = lIt.next().getConnectorById(aConnectorId);

			if (null != lConnector) {
				sendToken(lConnector, aToken);
				return;
			}
		}

		mLog.warn("Target connector '" + aConnectorId + "' was not found.");
	}

	/**
	 *
	 * @param aEngineId
	 * @param aConnectorId
	 * @param aToken
	 */
	public void sendToken(String aEngineId, String aConnectorId, Token aToken) {
		// TODO: return meaningful result here.
		WebSocketConnector lTargetConnector = getConnector(aEngineId, aConnectorId);
		if (lTargetConnector != null) {
			if (lTargetConnector.supportTokens()) {
				// before sending the token push it through filter chain
				FilterResponse lFilterResponse = getFilterChain().processTokenOut(null, lTargetConnector, aToken);
				// only forward the token to the plug-in chain
				// if filter chain does not response "aborted"
				if (!lFilterResponse.isRejected()) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Sending token '" + Logging.getTokenStr(aToken)
								+ "' to '" + lTargetConnector + "'...");
					}
					sendPacketData(lTargetConnector, tokenToPacket(lTargetConnector, aToken), false);
				} else {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Unable to send the token. The token has been rejected by the filters!");
					}
				}
			} else {
				mLog.warn("Target connector '" + aConnectorId
						+ "' does not support tokens.");
			}
		} else {
			mLog.warn("Target connector '" + aConnectorId + "' was not found.");
		}
	}

	private IOFuture sendTokenData(WebSocketConnector aSource,
			WebSocketConnector aTarget, Token aToken, boolean aIsAsync) {
		if (null == aTarget) {
			mLog.warn("Trying to send token to removed or closed connector: "
					+ Logging.getTokenStr(aToken));
		} else if (aTarget.supportTokens()) {
			// before sending the token push it through filter chain
			FilterResponse lFilterResponse = getFilterChain().processTokenOut(
					aSource, aTarget, aToken);

			// only forward the token to the plug-in chain
			// if filter chain does not response "aborted"
			if (!lFilterResponse.isRejected()) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Sending token '" + Logging.getTokenStr(aToken)
							+ "' to '" + aTarget + "'...");
				}
				if (aTarget.isInternal()) {
					((InternalConnector) aTarget).handleIncomingToken(aToken);
				} else {
					WebSocketPacket lPacket = tokenToPacket(aTarget, aToken);
					return sendPacketData(aTarget, lPacket, aIsAsync);
				}

			} else {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Unable to send the token. The token has been rejected by the filters!");
				}
			}
		} else {
			mLog.warn("Target connector '" + aTarget.getId()
					+ "' does not support tokens.");
		}
		return null;
	}

	private IOFuture sendPacketData(WebSocketConnector aTarget,
			WebSocketPacket aDataPacket, boolean aIsAsync) {
		if (aIsAsync) {
			return super.sendPacketAsync(aTarget, aDataPacket);
		} else {
			super.sendPacket(aTarget, aDataPacket);
			return null;
		}
	}

	/**
	 * Broadcasts the passed token to all token based connectors of the
	 * underlying engines that belong to the specified group.
	 *
	 * @param aToken - token to broadcast
	 */
	public void broadcastGroup(Token aToken) {
		String lGroup = aToken.getString("group");
		// if the group is not specified in the token then noone gets the
		// message:
		if (lGroup == null || lGroup.length() <= 0) {
			mLog.debug("Token '" + aToken + "' has no group specified...");
			return;
		}
		broadcastFiltered(aToken, "group", lGroup);
	}

	/**
	 * Broadcasts the passed token to all token based connectors of the
	 * underlying engines that belong to the specified filter and its name.
	 *
	 * @param aToken
	 * @param aFilterID
	 * @param aFilterName
	 */
	public void broadcastFiltered(Token aToken, String aFilterID, String aFilterName) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Broadcasting token '" + aToken + "' to all token based "
					+ "connectors that belong to the filter '" + aFilterID
					+ "' called '" + aFilterName + "'...");
		}
		FastMap<String, Object> lFilter = new FastMap<String, Object>();
		lFilter.put(aFilterID, aFilterName);
		broadcastFiltered(aToken, lFilter);
	}

	/**
	 * Broadcasts the passed token to all token based connectors of the
	 * underlying engines that belong to the specified filters.
	 *
	 * @param aToken
	 * @param aFilter
	 */
	public void broadcastFiltered(Token aToken, FastMap<String, Object> aFilter) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Broadcasting token '" + aToken + "' to all token based "
					+ "connectors that belong to the filters...");
		}

		// before sending the token push it through filter chain
		FilterResponse lFilterResponse = getFilterChain().processTokenOut(
				null, null, aToken);

		// converting the token within the loop is removed in this method!
		WebSocketPacket lPacket;
		// lPackets maps protocol formats to appropriate converted packets:
		FastMap<String, WebSocketPacket> lPackets = new FastMap<String, WebSocketPacket>();
		String lFormat;
		for (WebSocketConnector lConnector : selectConnectors(aFilter).values()) {
			if (!lConnector.supportTokens()) {
				continue;
			}

			lFormat = lConnector.getHeader().getFormat();
			lPacket = lPackets.get(lFormat);
			// if there is no packet for this protocol format already, make one and
			// store it in the map
			if (lPacket == null) {
				lPacket = tokenToPacket(lConnector, aToken);
				lPackets.put(lFormat, lPacket);
			}
			sendPacket(lConnector, lPacket);
		}
	}

	/**
	 * Broadcasts the passed token to all token based connectors of the
	 * underlying engines.
	 *
	 * @param aToken
	 */
	public void broadcastToken(Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Broadcasting token '" + aToken + " to all token based connectors...");
		}

		// before sending the token push it through filter chain
		FilterResponse lFilterResponse = getFilterChain().processTokenOut(
				null, null, aToken);
		if (lFilterResponse.isRejected()) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Broadcasting token '" + aToken + " rejected by filters...");
			}
			return;
		}

		// converting the token within the loop is removed in this method!
		WebSocketPacket lPacket;
		// lPackets maps protocol formats to appropriate converted packets:
		FastMap<String, WebSocketPacket> lPackets = new FastMap<String, WebSocketPacket>();
		String lFormat;
		for (WebSocketConnector lConnector : selectTokenConnectors().values()) {
			lFormat = lConnector.getHeader().getFormat();
			lPacket = lPackets.get(lFormat);
			// if there is no packet for this protocol format already, make one and
			// store it in the map
			if (lPacket == null) {
				lPacket = tokenToPacket(lConnector, aToken);
				lPackets.put(lFormat, lPacket);
			}
			sendPacket(lConnector, lPacket);
		}
	}

	/**
	 * Broadcasts to all connector, except the sender (aSource).
	 *
	 * @param aSource
	 * @param aToken
	 */
	public void broadcastToken(WebSocketConnector aSource, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Broadcasting token '" + aToken + " to all token based connectors...");
		}

		// before sending the token push it through filter chain
		FilterResponse lFilterResponse = getFilterChain().processTokenOut(aSource, null, aToken);
		if (lFilterResponse.isRejected()) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Broadcasting token '" + aToken + " rejected by filters...");
			}
			return;
		}

		// converting the token within the loop is removed in this method!
		WebSocketPacket lPacket;
		// optimization: lPackets maps protocol formats to appropriate converted packets:
		// only needs to convert packet once per protocol!
		Map<String, WebSocketPacket> lPackets = new FastMap<String, WebSocketPacket>();
		String lFormat;
		// interate through all connectors of all engines
		for (WebSocketConnector lConnector : selectTokenConnectors().values()) {
			if (!lConnector.equals(aSource) /*
					 * &&
					 * WebSocketConnectorStatus.UP.equals(lConnector.getStatus())
					 */) {
				try {
					RequestHeader lHeader = lConnector.getHeader();
					if (null != lHeader) {
						lFormat = lHeader.getFormat();
						// try to get packet for protocol
						lPacket = lPackets.get(lFormat);
						// if there is no packet for this protocol format already, make one and
						// store it in the map
						if (null == lPacket) {
							lPacket = tokenToPacket(lConnector, aToken);
							lPackets.put(lFormat, lPacket);
						}
						lConnector.sendPacket(lPacket);
					}
				} catch (Exception ex) {
					mLog.error(Logging.getSimpleExceptionMessage(ex,
							"sending token to connection '" + lConnector.getId() + "'"));
				}
			}
		}
	}

	/**
	 * iterates through all connectors of all engines and sends the token to
	 * each connector. The token format is considered for each connection
	 * individually so that the application can broadcast a token to all kinds
	 * of clients.
	 *
	 * @param aSource
	 * @param aToken
	 * @param aBroadcastOptions
	 */
	public void broadcastToken(WebSocketConnector aSource, Token aToken, BroadcastOptions aBroadcastOptions) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Broadcasting token '" + aToken + " to all token based connectors...");
		}

		// before sending the token push it through filter chain
		FilterResponse lFilterResponse = getFilterChain().processTokenOut(aSource, null, aToken);

		// converting the token within the loop is removed in this method!
		WebSocketPacket lPacket;
		// lPackets maps protocol formats to appropriate converted packets:
		Map<String, WebSocketPacket> lPackets = new FastMap<String, WebSocketPacket>();
		String lFormat;
		for (WebSocketConnector lConnector : selectTokenConnectors().values()) {
			if (null == aSource || !aSource.equals(lConnector) || aBroadcastOptions.isSenderIncluded()) {
				RequestHeader lHeader = lConnector.getHeader();
				lFormat = lHeader.getFormat();
				lPacket = lPackets.get(lFormat);
				// if there is no packet for this protocol format already, make one and
				// store it in the map
				if (lPacket == null) {
					lPacket = tokenToPacket(lConnector, aToken);
					lPackets.put(lFormat, lPacket);
				}
				sendPacket(lConnector, lPacket);
			}
		}
	}

	/**
	 * creates a standard response
	 *
	 * @param aInToken
	 * @param aOutToken
	 */
	public void setResponseFields(Token aInToken, Token aOutToken) {
		Integer lTokenId = null;
		String lType = null;
		String lNS = null;
		if (aInToken != null) {
			lTokenId = aInToken.getInteger("utid", -1);
			lType = aInToken.getString("type");
			lNS = aInToken.getString("ns");
		}
		aOutToken.setType("response");

		// if code and msg are already part of outgoing token do not overwrite!
		aOutToken.setInteger("code", aOutToken.getInteger("code", 0));
		aOutToken.setString("msg", aOutToken.getString("msg", "ok"));

		if (lTokenId != null) {
			aOutToken.setInteger("utid", lTokenId);
		}
		if (lNS != null) {
			aOutToken.setString("ns", lNS);
		}
		if (lType != null) {
			aOutToken.setString("reqType", lType);
		}
	}

	/**
	 *
	 * @param aInToken
	 * @return
	 */
	public Token createResponse(Token aInToken) {
		Token lResToken = TokenFactory.createToken();
		setResponseFields(aInToken, lResToken);
		return lResToken;
	}

	/**
	 * creates an error token yet with a code and a message
	 *
	 * @param aInToken
	 * @param aCode
	 * @param aMessage
	 * @return
	 */
	public Token createErrorToken(Token aInToken, int aCode, String aMessage) {
		Token lResToken = createResponse(aInToken);
		lResToken.setInteger("code", aCode);
		lResToken.setString("msg", aMessage);
		return lResToken;
	}

	/**
	 * creates a response token with the standard "not authenticated" message.
	 *
	 * @param aInToken
	 * @return
	 */
	public Token createNotAuthToken(Token aInToken) {

		Token lResToken = createErrorToken(aInToken, -1, "not authenticated");
		/*
		 * Token lResToken = createResponse(aInToken);
		 * lResToken.setInteger("code", -1); lResToken.setString("msg",
		 * "not authenticated");
		 */
		return lResToken;
	}

	/**
	 * creates a response token with the standard ""access denied" message.
	 *
	 * @param aInToken
	 * @return
	 */
	public Token createAccessDenied(Token aInToken) {
		Token lResToken = createErrorToken(aInToken, -1, "access denied");
		/*
		 * Token lResToken = createResponse(aInToken);
		 * lResToken.setInteger("code", -1); lResToken.setString("msg",
		 * "access denied");
		 */
		return lResToken;
	}

	/**
	 * creates an error response token based on
	 *
	 * @param aConnector
	 * @param aInToken
	 * @param aErrCode
	 * @param aMessage
	 */
	public void sendErrorToken(WebSocketConnector aConnector, Token aInToken,
			int aErrCode, String aMessage) {
		Token lToken = createResponse(aInToken);
		lToken.setInteger("code", aErrCode);
		lToken.setString("msg", aMessage);
		sendToken(aConnector, lToken);
	}

	/**
	 * @return the mPlugInChain
	 */
	@Override
	public TokenPlugInChain getPlugInChain() {
		return (TokenPlugInChain) mPlugInChain;
	}

	/**
	 * @return the mFilterChain
	 */
	@Override
	public TokenFilterChain getFilterChain() {
		return (TokenFilterChain) mFilterChain;
	}

	/**
	 * Get the global JMSManager
	 *
	 * @return
	 */
	public JMSManager getJMSManager() {
		if (null == mJMSManager) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Loading JMSManager...");
			}
			mJMSManager = (JMSManager) JWebSocketBeanFactory.getInstance().getBean("jmsManager");
		}
		return mJMSManager;
	}

	/**
	 * Get the global IEventBus instance
	 *
	 * @return
	 */
	public IEventBus getEventBus() {
		if (null == mEventBus) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Loading event bus...");
			}
			mEventBus = (IEventBus) JWebSocketBeanFactory.getInstance().getBean("eventBus");
			mEventBus.setExceptionHandler(new IEventBus.IExceptionHandler() {

				@Override
				public void handle(Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "during EventBus handler invocation"), lEx);
				}
			});
		}
		return mEventBus;
	}
}
