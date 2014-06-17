//	---------------------------------------------------------------------------
//	jWebSocket - Channel (Community Edition, CE)
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
package org.jwebsocket.plugins.channels;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 * Channel class represents the data channel which is used by the
 * <tt>Publisher</tt> to publish the data and the number of <tt>Subscriber</tt>
 * 's can subscribe to the given channel to receive the data stream through the
 * channel as soon as it is available to the channel via publisher.
 *
 * Channel can be of 3 types:
 *
 * 1. System Channel - The channels which are and can only be initialized and
 * started by the jWebSocket components and are used by it for providing system
 * level information are called system channel. Examples can be
 * <tt>LoggerChannel</tt> for streaming server logs to the client,
 * <tt>AdminChannel<tt> to stream the admin level read only information etc..
 *
 * 2. Private Channel - These are the channels that can be registered,
 * initialized and started by user at configuration time using
 * <tt>jWebSocket.xml</tt> or runtime. But to subscribe to this channel the user
 * or client should have valid <tt>api_key</tt> or rights.
 *
 * 3. Public Channel - Same as private channel except anyone can subscribe to
 * this channel without the use of <tt>access_key</tt> or irrespective of the
 * roles and rights.
 *
 * Also <tt>FastList</tt> has been used for the list of subscribers, publishers
 * and channel listeners for the concurrent access. Although it is expensive but
 * considering the fact that number of traversal for broadcasting data or
 * callback on listeners on events would be more than insertion and removal.
 *
 * @author Alexander Schulze, Puran Singh
 * @version $Id: Channel.java 1592 2011-02-20 00:49:48Z fivefeetfurther $
 */
public final class Channel implements ChannelLifeCycle {

	private static final Logger mLog = Logging.getLogger(Channel.class);
	private String mId;
	private String mName;
	private boolean mIsPrivate;
	private boolean mIsSystem;
	private String mSecretKey;
	private String mAccessKey;
	private String mOwner;
	private volatile boolean mAuthenticated = false;
	private final IBasicStorage<String, Object> mSubscribers;
	private final IBasicStorage<String, Object> mPublishers;
	private ChannelState mState = ChannelState.CREATED;
	private final List<ChannelListener> mChannelListeners;
	private static final Map<String, List<ChannelListener>> mListeners = new FastMap<String, List<ChannelListener>>().shared();
	private final TokenServer mServer;

	/**
	 *
	 */
	public enum ChannelState {

		/**
		 *
		 */
		STOPPED(0),
		/**
		 *
		 */
		INITIALIZED(1),
		/**
		 *
		 */
		STARTED(2),
		/**
		 *
		 */
		CREATED(3);
		private final int mValue;

		ChannelState(int aValue) {
			mValue = aValue;
		}

		/**
		 *
		 * @return
		 */
		public int getValue() {
			return mValue;
		}
	}

	/**
	 * Initialize the new channel but it doesn't start.
	 *
	 * @param aId
	 * @param aName
	 * @param aIsPrivate
	 * @param aOwner
	 * @param aIsSystem
	 * @param aAccessKey
	 * @param aSecretKey
	 * @param aState
	 * @param aServer
	 * @param aSubscribers
	 * @param aPublishers
	 */
	public Channel(String aId, String aName,
			boolean aIsPrivate, boolean aIsSystem,
			String aAccessKey, String aSecretKey, String aOwner,
			ChannelState aState, TokenServer aServer,
			IBasicStorage<String, Object> aSubscribers,
			IBasicStorage<String, Object> aPublishers) {

		mId = aId;
		mName = aName;
		mIsPrivate = aIsPrivate;
		mIsSystem = aIsSystem;
		mAccessKey = aAccessKey;
		mSecretKey = aSecretKey;
		mOwner = aOwner;
		mState = aState;
		mServer = aServer;
		mPublishers = aPublishers;
		mSubscribers = aSubscribers;

		if (!mListeners.containsKey(aId)) {
			mListeners.put(aId, new FastList<ChannelListener>());
		}
		mChannelListeners = mListeners.get(aId);

		// register core channel listener
		if (mChannelListeners.isEmpty()) {
			registerListener(new ChannelListener() {
				
				@Override
				public void channelStarted(Channel aChannel, String aUser) {
					Token lEvent = TokenFactory.createToken(ChannelPlugIn.NS_CHANNELS, BaseToken.TT_EVENT);
					lEvent.setString("name", "channelStarted");
					lEvent.setString("user", aUser);
					lEvent.setString("channelName", aChannel.getName());
					lEvent.setString("channelId", aChannel.getId());

					aChannel.broadcastToken(lEvent);
				}

				@Override
				public void channelStopped(Channel aChannel, String aUser) {
					Token lEvent = TokenFactory.createToken(ChannelPlugIn.NS_CHANNELS, BaseToken.TT_EVENT);
					lEvent.setString("name", "channelStopped");
					lEvent.setString("user", aUser);
					lEvent.setString("channelName", aChannel.getName());
					lEvent.setString("channelId", aChannel.getId());

					aChannel.broadcastToken(lEvent);
				}

				@Override
				public void channelRemoved(Channel aChannel, String aUser) {
					// already supporting by broadcasting
				}

				@Override
				public void subscribed(Channel aChannel, WebSocketConnector aConnector) {
					Token lEvent = TokenFactory.createToken(ChannelPlugIn.NS_CHANNELS, BaseToken.TT_EVENT);
					lEvent.setString("name", "subscription");
					lEvent.setString("subscriber", aConnector.getId());
					lEvent.setBoolean("isPublisher", aChannel.getPublishers().contains(aConnector.getId()));
					lEvent.setString("user", aConnector.getUsername());
					lEvent.setString("channelName", aChannel.getName());
					lEvent.setString("channelId", aChannel.getId());
					lEvent.setString("state", aChannel.getState().name());
					lEvent.setBoolean("isSystem", aChannel.isSystem());
					lEvent.setBoolean("isPrivate", aChannel.isPrivate());

					aChannel.broadcastToken(lEvent);
				}

				@Override
				public void unsubscribed(Channel aChannel, WebSocketConnector aConnector) {
					Token lEvent = TokenFactory.createToken(ChannelPlugIn.NS_CHANNELS, BaseToken.TT_EVENT);
					lEvent.setString("name", "unsubscription");
					lEvent.setString("subscriber", aConnector.getId());
					lEvent.setBoolean("isPublisher", aChannel.getPublishers().contains(aConnector.getId()));
					lEvent.setString("channelName", aChannel.getName());
					lEvent.setString("channelId", aChannel.getId());
					lEvent.setString("state", aChannel.getState().name());
					lEvent.setBoolean("isSystem", aChannel.isSystem());
					lEvent.setBoolean("isPrivate", aChannel.isPrivate());
					lEvent.setString("user", aConnector.getUsername());

					aChannel.broadcastToken(lEvent);
				}

				@Override
				public void dataReceived(Channel aChannel, Token aToken) {
				}

				@Override
				public void dataBroadcasted(Channel aChannel, Token aToken) {
				}

				@Override
				public void channelInitialized(Channel aChannel) {
				}
			});
		}
	}

	/**
	 * Returns the channel unique id.
	 *
	 * @return the id
	 */
	public String getId() {
		return mId;
	}

	/**
	 *
	 * @return The TokenServer used by the channel
	 */
	public TokenServer getServer() {
		return mServer;
	}

	/**
	 *
	 * @return
	 */
	public String getName() {
		return mName;
	}

	/**
	 *
	 * @return
	 */
	public int getSubscriberCount() {
		return mSubscribers.size();
	}

	/**
	 * returns if the channel is a private channel. Private channels are not
	 * listed by getChannel requests and require an access-key.
	 *
	 * @return
	 */
	public boolean isPrivate() {
		return mIsPrivate;
	}

	/**
	 *
	 * @param aIsPrivate
	 */
	public void setPrivate(boolean aIsPrivate) {
		this.mIsPrivate = aIsPrivate;
	}

	/**
	 * returns if the channel is a system channel. System channels cannot be
	 * removed from clients.
	 *
	 * @return the systemChannel
	 */
	public boolean isSystem() {
		return mIsSystem;
	}

	/**
	 *
	 * @param aIsSystem
	 */
	public void setSystem(boolean aIsSystem) {
		this.mIsSystem = aIsSystem;
	}

	/**
	 * @return the secretKey
	 */
	public String getSecretKey() {
		return mSecretKey;
	}

	/**
	 * @return the accessKey
	 */
	public String getAccessKey() {
		return mAccessKey;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return mOwner;
	}

	/**
	 * Returns the unmodifiable list of all the subscribers to this channel
	 *
	 * @return the list of subscribers
	 */
	public List<String> getSubscribers() {
		List<String> lList = new LinkedList<String>();
		lList.addAll(mSubscribers.keySet());

		return lList;
	}

	/**
	 * @return the publishers who is currently publishing to this channel
	 */
	public List<String> getPublishers() {
		List<String> lList = new LinkedList<String>();
		lList.addAll(mPublishers.keySet());

		return lList;
	}

	/**
	 * @param aPublishers the publishers to set
	 */
	public void setPublishers(List<String> aPublishers) {
		for (String lPub : aPublishers) {
			addPublisher(lPub);
		}
	}

	/**
	 * Add the publisher to the list of publishers.
	 *
	 * @param aPublisher the publisher to add
	 */
	public void addPublisher(String aPublisher) {
		mPublishers.put(aPublisher, true);
	}

	/**
	 *
	 */
	public void clearPublishers() {
		mPublishers.clear();
	}

	/**
	 *
	 */
	public void clearSubscribers() {
		mSubscribers.clear();
	}

	/**
	 * Removes a publisher from the list of publishers.
	 *
	 * @param aPublisher the publisher to add
	 */
	public void removePublisher(String aPublisher) {
		this.mPublishers.remove(aPublisher);
	}

	/**
	 * Subscribe to this channel
	 *
	 * @param aSubscriber the subscriber which wants to subscribe
	 * @param aConnector the connector which wants to subscribe
	 */
	public void subscribe(String aSubscriber, WebSocketConnector aConnector) {
		if (!mSubscribers.containsKey(aSubscriber)) {
			mSubscribers.put(aSubscriber, true);

			// listeners notification
			final Channel lChannel = this;
			final WebSocketConnector lSubscriber = aConnector;
			if (mChannelListeners != null) {
				for (final ChannelListener lListener : mChannelListeners) {
					Tools.getThreadPool().submit(new Runnable() {
						@Override
						public void run() {
							lListener.subscribed(lChannel, lSubscriber);
						}
					});
				}
			}
		}
	}

	/**
	 * Unsubscribe from this channel, and updates the channel store information
	 *
	 * @param aSubscriber the subscriber to unsubscribe
	 * @param aConnector
	 */
	public void unsubscribe(String aSubscriber, WebSocketConnector aConnector) {
		if (mSubscribers.containsKey(aSubscriber)) {
			mSubscribers.remove(aSubscriber);

			// listeners notification
			final Channel lChannel = this;

//			final String lSubscriber = aSubscriber;
			final WebSocketConnector lConnector = aConnector;
			if (mChannelListeners != null) {
				for (final ChannelListener lListener : mChannelListeners) {
					Tools.getThreadPool().submit(new Runnable() {
						@Override
						public void run() {
							lListener.unsubscribed(lChannel, lConnector);
						}
					});
				}
			}
		}
	}

	/**
	 * Sends the data to the given subscriber. Note that this send operation
	 * will block the current thread until the send operation is complete. for
	 * asynchronous send operation use <tt>sendAsync</tt> method.
	 *
	 * @param aToken the token data to send
	 * @param aSubscriber the target subscriber
	 */
	public void send(Token aToken, Subscriber aSubscriber) {
		WebSocketConnector lConnector = mServer.getConnector(aSubscriber.getId());
		mServer.sendToken(lConnector, aToken);
	}

	/**
	 * Sends the data to the given target subscriber asynchronously.
	 *
	 * @param aToken the token data to send
	 * @param aSubscriber
	 * @return the future object to keep track of send operation
	 */
	public IOFuture sendAsync(Token aToken, Subscriber aSubscriber) {
		WebSocketConnector lConnector = mServer.getConnector(aSubscriber.getId());
		return mServer.sendTokenAsync(lConnector, aToken);
	}

	/**
	 * broadcasts data to the subscribers asynchronously. It performs the
	 * concurrent broadcast to all the subscribers and wait for the all the
	 * broadcast task to complete only for 1 second maximum.
	 *
	 * @param aToken the token data for the subscribers
	 */
	public void broadcastTokenAsync(final Token aToken) {
		// If no subscribers exist do nothing!
		if (mSubscribers != null && mSubscribers.size() > 0) {

			Iterator<String> lSubscribers = mSubscribers.keySet().iterator();
			while (lSubscribers.hasNext()) {
				final String lSubscriber = lSubscribers.next();
				Tools.getThreadPool().submit(new Runnable() {
					@Override
					public void run() {
						WebSocketConnector lConnector = mServer.getConnector(lSubscriber);
						// if the connector does not exists at this point, then is has been stopped before
						if (lConnector != null) {
							mServer.sendTokenAsync(lConnector, aToken);
						}
					}
				});
			}
		}
	}

	/**
	 *
	 * @param aToken
	 */
	public void broadcastToken(final Token aToken) {
		if (mSubscribers != null && mSubscribers.size() > 0) {
			Iterator<String> lSubscribers = mSubscribers.keySet().iterator();
			while (lSubscribers.hasNext()) {
				final String lSubscriber = lSubscribers.next();
				WebSocketConnector lConnector = mServer.getConnector(lSubscriber);
				// if the connector does not exists at this point, then is has been stopped before
				if (lConnector != null) {
					mServer.sendToken(lConnector, aToken);
				}
			}
		}
	}

	/**
	 * Returns the channel state
	 *
	 * @return the state
	 */
	public ChannelState getState() {
		return mState;
	}

	/**
	 * Register the channel listener to the list of listeners
	 *
	 * @param aChannelListener the channel listener to register
	 */
	public void registerListener(ChannelListener aChannelListener) {
		mChannelListeners.add(aChannelListener);
	}

	/**
	 *
	 * @param aChannelListener
	 */
	public void removeListener(ChannelListener aChannelListener) {
		mChannelListeners.remove(aChannelListener);
	}

	/**
	 * Initialize the channel
	 *
	 * @throws ChannelLifeCycleException
	 */
	@Override
	public void init() throws ChannelLifeCycleException {
		if (!mState.equals(ChannelState.CREATED)) {
			throw new ChannelLifeCycleException("Channel initialization failed. "
					+ "The channel '" + getName() + "' require to be in CREATED state!");
		}

		// setting the state mValue
		this.mState = ChannelState.INITIALIZED;

		// listeners notification
		final Channel lChannel = this;
		if (mChannelListeners != null) {
			for (final ChannelListener lListener : mChannelListeners) {
				Tools.getThreadPool().submit(new Runnable() {
					@Override
					public void run() {
						lListener.channelInitialized(lChannel);
					}
				});
			}
		}
	}

	/**
	 *
	 * @param aUser
	 * @throws ChannelLifeCycleException
	 */
	@Override
	public void start(final String aUser) throws ChannelLifeCycleException {
		if (this.mState == ChannelState.STARTED) {
			throw new ChannelLifeCycleException(
					"Channel '" + this.getName() + "' is started already!");
		}

		// verify the owner
		if (!getOwner().equals(aUser)) {
			throw new ChannelLifeCycleException(
					"User '" + aUser + "' is not the owner of this channel"
					+ ", only owner of the channel can start.");
		}

		this.mState = ChannelState.STARTED;
		final Channel lChannel = this;
		if (mChannelListeners != null) {
			for (final ChannelListener lListener : mChannelListeners) {
				Tools.getThreadPool().submit(new Runnable() {
					@Override
					public void run() {

						lListener.channelStarted(lChannel, aUser);
					}
				});
			}
		}
	}

	/**
	 *
	 * @param aUser
	 * @throws ChannelLifeCycleException
	 */
	@Override
	public void stop(final String aUser) throws ChannelLifeCycleException {
		if (!mState.equals(ChannelState.STARTED)) {
			throw new ChannelLifeCycleException(
					"Channel '" + getName() + "' is not started yet!");
		}

		// verify the owner
		if (!getOwner().equals(aUser)) {
			throw new ChannelLifeCycleException(
					"User '" + aUser + "' is not the owner of this channel"
					+ ", only owner of the channel can stop");
		}

		// setting the new state mValue
		mState = ChannelState.STOPPED;

		// listeners notification
		final Channel channel = this;
		if (mChannelListeners != null) {
			for (final ChannelListener listener : mChannelListeners) {
				Tools.getThreadPool().submit(new Runnable() {
					@Override
					public void run() {
						listener.channelStopped(channel, aUser);
					}
				});
			}
		}
	}

	/**
	 * @param aId the id to set
	 */
	public void setId(String aId) {
		this.mId = aId;
	}

	/**
	 * @param aSecretKey the secretKey to set
	 */
	public void setSecretKey(String aSecretKey) {
		this.mSecretKey = aSecretKey;
	}

	/**
	 * @param aAccessKey the accessKey to set
	 */
	public void setAccessKey(String aAccessKey) {
		this.mAccessKey = aAccessKey;
	}

	/**
	 * @param aOwner the owner to set
	 */
	public void setOwner(String aOwner) {
		this.mOwner = aOwner;
	}

	/**
	 *
	 * @return
	 */
	public boolean isAuthenticated() {
		return mAuthenticated;
	}

	/**
	 *
	 * @param aName
	 */
	public void setName(String aName) {
		this.mName = aName;
	}
}
