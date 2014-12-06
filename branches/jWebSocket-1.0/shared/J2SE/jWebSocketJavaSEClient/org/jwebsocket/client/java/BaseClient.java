//	---------------------------------------------------------------------------
//	jWebSocket - BaseClient (Community Edition, CE)
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

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.api.IPacketDeliveryListener;
import org.jwebsocket.api.WebSocketBaseClientEvent;
import org.jwebsocket.api.WebSocketClient;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientFilter;
import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketStatus;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.ReliabilityOptions;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketFrameType;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.JWSTimerTask;
import org.jwebsocket.util.MessagingControl;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 *
 * @author Rolando Santamaria Maso
 */
public abstract class BaseClient implements WebSocketClient {

	/**
	 *
	 */
	public static final int RECEIVER_SHUTDOWN_TIMEOUT = 3000;
	/**
	 *
	 */
	protected volatile WebSocketStatus mStatus = WebSocketStatus.CLOSED;
	private final ScheduledThreadPoolExecutor mExecutor = new ScheduledThreadPoolExecutor(1);
	private final ExecutorService mListenersExecutor = Executors.newFixedThreadPool(1);
	private final Map<String, Object> mParams = new FastMap<String, Object>();
	/**
	 *
	 */
	protected int mVersion = JWebSocketCommonConstants.WS_VERSION_DEFAULT;
	private ReliabilityOptions mReliabilityOptions = null;
	private int mPingInterval = 20000;
	private ScheduledFuture mReconnectorTask = null;
	private Boolean mIsReconnecting = false;
	private final Object mReconnectLock = new Object();
	/**
	 * Connection URI
	 */
	protected URI mURI = null;
	/**
	 * list of the registered listeners
	 */
	private List<WebSocketClientListener> mListeners = new FastList<WebSocketClientListener>();
	/**
	 * list of the registered filters
	 */
	private List<WebSocketClientFilter> mFilters = new FastList<WebSocketClientFilter>();
	/**
	 * fragmentation model
	 */
	private Map<String, IPacketDeliveryListener> mPacketDeliveryListeners = new FastMap<String, IPacketDeliveryListener>().shared();
	private Integer mMaxFrameSize = 1048840;
	private Map<String, String> mFragments = new LinkedHashMap<String, String>();

	/**
	 *
	 * @return
	 */
	protected Map<String, IPacketDeliveryListener> getPacketDeliveryListeners() {
		return mPacketDeliveryListeners;
	}

	/**
	 * @return the ping interval
	 */
	@Override
	public int getPingInterval() {
		return mPingInterval;
	}
	
	@Override
	public URI getURI(){
		return mURI;
	}

	/**
	 *
	 * @param aInterval
	 */
	@Override
	public void setPingInterval(int aInterval) {
		mPingInterval = aInterval;
	}

	/**
	 * @return the mReliabilityOptions
	 */
	@Override
	public ReliabilityOptions getReliabilityOptions() {
		return mReliabilityOptions;
	}

	/**
	 * @param aReliabilityOptions the ReliabilityOptions to set
	 */
	@Override
	public void setReliabilityOptions(ReliabilityOptions aReliabilityOptions) {
		this.mReliabilityOptions = aReliabilityOptions;
	}

	@Override
	public Integer getMaxFrameSize() {
		return mMaxFrameSize;
	}

	/**
	 *
	 * @param aMaxFrameSize
	 */
	protected void setMaxFrameSize(Integer aMaxFrameSize) {
		this.mMaxFrameSize = aMaxFrameSize;
	}

	/**
	 *
	 * @return
	 */
	protected Map<String, String> getFragments() {
		return mFragments;
	}

	/**
	 *
	 * @return
	 */
	protected ScheduledThreadPoolExecutor getExecutor() {
		return mExecutor;
	}

	/**
	 *
	 * @return
	 */
	protected ExecutorService getListenersExecutor() {
		return mListenersExecutor;
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
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
	@Override
	public Object getParam(String aKey) {
		return mParams.get(aKey);
	}

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	@Override
	public void setParam(String aKey, Object aValue) {
		mParams.put(aKey, aValue);
	}

	@Override
	public void setVersion(int aVersion) {
		mVersion = aVersion;
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

	/**
	 *
	 * @param aMessage
	 * @param aListener
	 * @throws Exception
	 */
	protected void sendMessage(Token aMessage, IPacketDeliveryListener aListener) throws Exception {
		if (null != aListener) {
			aMessage.setBoolean(MessagingControl.PROPERTY_IS_ACK_REQUIRED, true);
			aMessage.setBoolean(MessagingControl.PROPERTY_IS_WRAPPED_MESSAGE, true);
			final String lMsgId = aMessage.getString(MessagingControl.PROPERTY_MESSAGE_ID);
			getPacketDeliveryListeners().put(lMsgId, aListener);

			// schedule the timer task
			try {
				Tools.getTimer().schedule(new JWSTimerTask() {
					@Override
					public void runTask() {
						Tools.getThreadPool().submit(new Runnable() {
							@Override
							public void run() {
								IPacketDeliveryListener lListener = getPacketDeliveryListeners().remove(lMsgId);
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
	 *
	 * @param aStatus
	 */
	@Override
	public void setStatus(WebSocketStatus aStatus) {
		mStatus = aStatus;
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

	@Override
	public void send(WebSocketPacket aPacket) throws WebSocketException {
		try {
			for (WebSocketClientFilter lFilter : getFilters()) {
				lFilter.filterPacketOut(aPacket);
			}
		} catch (Exception lEx) {
			throw new WebSocketException("Outbound filtering process exception!", lEx);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyOpened(final WebSocketClientEvent aEvent) {
		for (final WebSocketClientListener lListener : getListeners()) {
			getListenersExecutor().submit(new Runnable() {
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
	 *
	 * @param aDataPacket
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
							aDataPacket.setString(getFragments().remove(lFragmentationId) + lData);
						} else {
							if (!getFragments().containsKey(lFragmentationId)) {
								getFragments().put(lFragmentationId, lData);
							} else {
								getFragments().put(lFragmentationId, getFragments().get(lFragmentationId) + lData);
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
						setMaxFrameSize(lMessage.getInteger(MessagingControl.PROPERTY_DATA));
						// The end of the "max frame size" handshake indicates that the connection is finally opened
						WebSocketClientEvent lEvent = new WebSocketBaseClientEvent(this, EVENT_OPEN, "");
						// notify listeners that client has opened.
						setStatus(WebSocketStatus.OPEN);
						notifyOpened(lEvent);

						return;
					} else // processing packet delivery acknowledge from the client
					if (MessagingControl.NAME_MESSAGE_DELIVERY_ACKNOWLEDGE.equals(lName)) {
						IPacketDeliveryListener lListener = getPacketDeliveryListeners()
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
			for (WebSocketClientFilter lFilter : getFilters()) {
				lFilter.filterPacketIn(aDataPacket);
			}
		} catch (Exception lEx) {
			return;
		}

		// finally notify the listeners
		for (final WebSocketClientListener lListener : getListeners()) {
			final WebSocketPacket lPacket = aDataPacket;
			getListenersExecutor().submit(new Runnable() {
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
			getListenersExecutor().submit(new Runnable() {
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
	 * {@inheritDoc}
	 */
	@Override
	public void notifyClosed(final WebSocketClientEvent aEvent) {
		for (final WebSocketClientListener lListener : getListeners()) {
			getListenersExecutor().submit(new Runnable() {
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
			} catch (Exception lEx) {
				WebSocketClientEvent lEvent = new WebSocketBaseClientEvent(mEvent.getClient(), EVENT_CLOSE,
						lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage());
				notifyClosed(lEvent);
			}
		}
	}

	/**
	 *
	 */
	protected void abortReconnect() {
		synchronized (mReconnectLock) {
			// cancel running re-connect task
			if (null != mReconnectorTask) {
				mReconnectorTask.cancel(true);
			}
			// reset internal re-connection flag
			mIsReconnecting = false;
			mReconnectorTask = null;
			// clean up all potentially old references to inactive tasks
			getExecutor().purge();
		}
	}

	/**
	 *
	 * @param aEvent
	 */
	protected void checkReconnect(WebSocketClientEvent aEvent) {
		synchronized (mReconnectLock) {
			// first, purge all potentially old references to other tasks
			getExecutor().purge();
			// did we configure reliability options?
			// and is there now re-connection task already active?
			if (getReliabilityOptions() != null
					&& getReliabilityOptions().getReconnectDelay() > 0
					&& !mIsReconnecting) {
				// schedule a re-connect action after the re-connect delay
				mIsReconnecting = true;
				mReconnectorTask = getExecutor().schedule(
						new ReOpener(aEvent),
						getReliabilityOptions().getReconnectDelay(),
						TimeUnit.MILLISECONDS);
			}
		}
	}
}
