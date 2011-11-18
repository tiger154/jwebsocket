//  ---------------------------------------------------------------------------
//  jWebSocket - Channel
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.plugins.channels;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;

import org.jwebsocket.async.IOFuture;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.security.Right;
import org.jwebsocket.security.Rights;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

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
 * <tt>AdminChannel<tt>
 * to stream the admin level read only information etc..
 * 
 * 2. Private Channel - These are the channels that can be registered, initialized 
 * and started by user at configuration time using <tt>jWebSocket.xml</tt> or
 * runtime. But to subscribe to this channel the user or client should have
 * valid <tt>api_key</tt> or rights.
 * 
 * 3. Public Channel - Same as private channel except anyone can subscribe to
 * this channel without the use of <tt>access_key</tt> or irrespective of the
 * roles and rights.
 * 
 * Also <tt>FastList</tt> has been used for the list of subscribers,
 * publishers and channel listeners for the concurrent access. Although it is
 * expensive but considering the fact that number of traversal for broadcasting
 * data or callback on listeners on events would be more than insertion and
 * removal.
 * 
 * @author puran, aschulze
 * @version $Id: Channel.java 1592 2011-02-20 00:49:48Z fivefeetfurther $
 */
public final class Channel implements ChannelLifeCycle {

	/** logger */
	private static Logger mLog = Logging.getLogger(Channel.class);
	private String mId;
	private String mName;
	private boolean mIsPrivate;
	private boolean mIsSystem;
	private String mSecretKey;
	private String mAccessKey;
	private String mOwner;
	private volatile boolean mAuthenticated = false;
	private List<String> mSubscribers;
	private List<String> mPublishers;
	private ChannelState mState = ChannelState.STOPPED;
	private List<ChannelListener> mChannelListeners;

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
		SUSPENDED(3);
		private int value;

		ChannelState(int value) {
			this.value = value;
		}

		/**
		 *
		 * @return
		 */
		public int getValue() {
			return value;
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
	 * @param aCreatedDate
	 */
	public Channel(String aId, String aName,
			boolean aIsPrivate, boolean aIsSystem,
			String aAccessKey, String aSecretKey, String aOwner,
			ChannelState aState) {
		this.mId = aId;
		this.mName = aName;
		this.mIsPrivate = aIsPrivate;
		this.mIsSystem = aIsSystem;
		this.mAccessKey = aAccessKey;
		this.mSecretKey = aSecretKey;
		this.mOwner = aOwner;
		this.mState = aState;
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
	 * returns if the channel is a private channel.
	 * Private channels are not listed by getChannel requests and
	 * require an access-key.
	 * @return
	 */
	public boolean isPrivate() {
		return mIsPrivate;
	}

	/**
	 * returns if the channel is a system channel.
	 * System channels cannot be removed from clients.
	 * @return the systemChannel
	 */
	public boolean isSystem() {
		return mIsSystem;
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
		return (mSubscribers != null
				? Collections.unmodifiableList(mSubscribers)
				: null);
	}

	/**
	 * Set the subscribers to this channel. Note that this method simply
	 * replaces the existing list of subscribers.
	 *
	 * @param aSubscribers
	 *            the list of subscribers
	 */
	public void setSubscribers(List<String> aSubscribers) {
		this.mSubscribers = aSubscribers;
	}

	/**
	 * @return the publishers who is currently publishing to this channel
	 */
	public List<String> getPublishers() {
		return (mPublishers != null
				? Collections.unmodifiableList(mPublishers)
				: null);
	}

	/**
	 * @param aPublishers
	 *            the publishers to set
	 */
	public void setPublishers(List<String> aPublishers) {
		this.mPublishers = aPublishers;
	}

	/**
	 * Add the publisher to the list of publishers.
	 *
	 * @param aPublisher
	 *            the publisher to add
	 */
	public void addPublisher(String aPublisher) {
		if (this.mPublishers == null) {
			this.mPublishers = new FastList<String>();
		}
		this.mPublishers.add(aPublisher);
	}

	/**
	 * Removes a publisher from the list of publishers.
	 *
	 * @param aPublisher
	 *            the publisher to add
	 */
	public void removePublisher(String aPublisher) {
		if (this.mPublishers == null) {
			this.mPublishers.remove(aPublisher);
		}
	}

	/**
	 * Subscribe to this channel
	 *
	 * @param aSubscriber
	 *            the subscriber which wants to subscribe
	 * @param aChannelManager
	 */
	public void subscribe(String aSubscriber) {
		// create new subscribers if needed
		if (this.mSubscribers == null) {
			this.mSubscribers = new FastList<String>();
		}
		if (!mSubscribers.contains(aSubscriber)) {
			mSubscribers.add(aSubscriber);
			// persist the subscriber
			/*
			if (mChannelListeners != null) {
			for (ChannelListener lListener : mChannelListeners) {
			try {
			lListener.subscribed(this, aSubscriber);
			} catch (Exception es) {
			// trap for any exception so that if any of the
			// listener implementation fails or throws exception
			// we continue with others.
			}
			}
			}
			 */
		}
	}

	/**
	 * Unsubscribe from this channel, and updates the channel store information
	 *
	 * @param aSubscriber
	 *            the subscriber to unsubscribe
	 * @param aChannelManager
	 *            the channel manager
	 */
	public void unsubscribe(String aSubscriber) {
		if (this.mSubscribers == null) {
			return;
		}
		if (mSubscribers.contains(aSubscriber)) {
			mSubscribers.remove(aSubscriber);
			/*
			if (mChannelListeners != null) {
			for (ChannelListener listener : mChannelListeners) {
			listener.unsubscribed(this, aSubscriber);
			}
			}
			 */
		}
	}

	/**
	 * Sends the data to the given subscriber. Note that this send operation
	 * will block the current thread until the send operation is complete. for
	 * asynchronous send operation use <tt>sendAsync</tt> method.
	 *
	 * @param aToken
	 *            the token data to send
	 * @param aSubscriber
	 *            the target subscriber
	 */
	public void send(Token aToken, Subscriber aSubscriber) {
		aSubscriber.sendToken(aToken);
	}

	/**
	 * Sends the data to the given target subscriber asynchronously.
	 *
	 * @param aToken
	 *            the token data to send
	 * @param aSubscriber
	 * @return the future object to keep track of send operation
	 */
	public IOFuture sendAsync(Token aToken, Subscriber aSubscriber) {
		return aSubscriber.sendTokenAsync(aToken);
	}

	/**
	 * broadcasts data to the subscribers asynchronously. It performs the
	 * concurrent broadcast to all the subscribers and wait for the all the
	 * broadcast task to complete only for 1 second maximum.
	 *
	 * @param aToken
	 *            the token data for the subscribers
	 */
	public void broadcastTokenAsync(final Token aToken) {
		// If no subscribers exist do nothing!
		if (mSubscribers != null && mSubscribers.size() > 0) {
			ExecutorService lExecutor = Executors.newCachedThreadPool();
			for (final String lSubscriber : mSubscribers) {
				lExecutor.submit(new Runnable() {

					@Override
					public void run() {
						TokenServer lTS = JWebSocketFactory.getTokenServer();
						WebSocketConnector lConnector = lTS.getConnector(lSubscriber);
						if (lConnector != null) {
							lTS.sendTokenAsync(lConnector, aToken);
						} else {
							mLog.warn("Trying to asynchronously broadcast token to unknown subscriber '" 
									+ lSubscriber + "' " + aToken.toString() + ".");
						}
					}
				});
			}
			// TODO: aopprove this weird async implementation
			try {
				lExecutor.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void broadcastToken(final Token aToken) {
		// If no subscribers exist do nothing!
		// TODO: synchronize mSubScribers!
		if (mSubscribers != null && mSubscribers.size() > 0) {
			TokenServer lTS = JWebSocketFactory.getTokenServer();
			for (final String lSubscriber : mSubscribers) {
				WebSocketConnector lConnector = lTS.getConnector(lSubscriber);
				if (lConnector != null) {
					lTS.sendToken(lConnector, aToken);
				} else {
					mLog.warn("Trying to broadcast token to unknown subscriber '" 
							+ lSubscriber + "' " + aToken.toString() + ".");
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
	 * @param aChannelListener
	 *            the channel listener to register
	 */
	public void registerListener(ChannelListener aChannelListener) {
		if (mChannelListeners == null) {
			mChannelListeners = new FastList<ChannelListener>();
		}
		mChannelListeners.add(aChannelListener);
	}

	/**
	 *
	 * @param aChannelListener
	 */
	public void removeListener(ChannelListener aChannelListener) {
		if (mChannelListeners != null) {
			mChannelListeners.remove(aChannelListener);
		}
	}

	/**
	 *
	 */
	@Override
	public void init() {
		this.mState = ChannelState.INITIALIZED;
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
					"Channel '" + this.getName() + "' is already started");
		}
		if (!SecurityFactory.isValidUser(aUser)) {
			throw new ChannelLifeCycleException(
					"Cannot start the channel '"
					+ this.getName()
					+ "' for invalid user login '" + aUser + "'");
		} else {
			Rights lRights = SecurityFactory.getUserRights(aUser);
			Right lRight = lRights.get("org.jwebsocket.plugins.channels.start");
			if (lRight == null) {
				throw new ChannelLifeCycleException(
						"User '" + aUser
						+ "' is not granted to start the channel");
			} else {
				// verify the owner
				if (aUser != null && !aUser.equals(getOwner())) {
					throw new ChannelLifeCycleException(
							"User '" + aUser + "' is not the owner of this channel"
							+ ", only owner of the channel can start.");
				}
				this.mAuthenticated = true;
			}
		}
		this.mState = ChannelState.STARTED;
		final Channel lChannel = this;
		if (mChannelListeners != null) {
			ExecutorService lPool = Executors.newCachedThreadPool();
			for (final ChannelListener lListener : mChannelListeners) {
				lPool.submit(new Runnable() {

					@Override
					public void run() {
						lListener.channelStarted(lChannel, aUser);
					}
				});
			}
			lPool.shutdown();
		}
	}

	/**
	 *
	 * @param aUser
	 * @throws ChannelLifeCycleException
	 */
	@Override
	public void suspend(final String aUser) throws ChannelLifeCycleException {
		if (this.mState == ChannelState.SUSPENDED) {
			throw new ChannelLifeCycleException("Channel '"
					+ this.getName()
					+ "' is already suspended");
		}
		if (!SecurityFactory.isValidUser(aUser) && !mAuthenticated) {
			throw new ChannelLifeCycleException("Cannot suspend the channel '"
					+ this.getName()
					+ "'] for invalid user login '" + aUser + "'");
		} else {
			Rights rights = SecurityFactory.getUserRights(aUser);
			Right right = rights.get("org.jwebsocket.plugins.channels.suspend");
			if (right == null) {
				throw new ChannelLifeCycleException(
						"User '" + aUser
						+ "' is not granted to suspend the channel");
			} else {
				// verify the owner
				if (aUser != null && !aUser.equals(getOwner())) {
					throw new ChannelLifeCycleException(
							"User '" + aUser + "' is not the owner of this channel"
							+ ", only owner of the channel can suspend.");
				}
			}
		}
		this.mState = ChannelState.SUSPENDED;
		final Channel channel = this;
		if (mChannelListeners != null) {
			ExecutorService pool = Executors.newCachedThreadPool();
			for (final ChannelListener listener : mChannelListeners) {
				pool.submit(new Runnable() {

					@Override
					public void run() {
						listener.channelSuspended(channel, aUser);
					}
				});
			}
			pool.shutdown();
		}
	}

	/**
	 *
	 * @param aUser
	 * @throws ChannelLifeCycleException
	 */
	@Override
	public void stop(final String aUser) throws ChannelLifeCycleException {
		if (this.mState == ChannelState.STOPPED) {
			throw new ChannelLifeCycleException(
					"Channel '" + getName() + "' is already stopped");
		}
		if (!SecurityFactory.isValidUser(aUser) && !mAuthenticated) {
			throw new ChannelLifeCycleException(
					"Cannot stop the channel '" + this.getName()
					+ "' for invalid user login '" + aUser + "'");
		} else {
			Rights rights = SecurityFactory.getUserRights(aUser);
			Right right = rights.get("org.jwebsocket.plugins.channels.stop");
			if (right == null) {
				throw new ChannelLifeCycleException(
						"User '" + aUser
						+ "' is not granted to stop the channel");
			} else {
				// verify the owner
				if (aUser != null && !aUser.equals(getOwner())) {
					throw new ChannelLifeCycleException(
							"User '" + aUser + "' is not the owner of this channel"
							+ ", only owner of the channel can stop");
				}
			}
		}
		if (this.mState == ChannelState.INITIALIZED || this.mState == ChannelState.STARTED) {
			this.mState = ChannelState.STOPPED;
		}
		final Channel channel = this;
		if (mChannelListeners != null) {
			ExecutorService pool = Executors.newCachedThreadPool();
			for (final ChannelListener listener : mChannelListeners) {
				pool.submit(new Runnable() {

					@Override
					public void run() {
						listener.channelStopped(channel, aUser);
					}
				});
			}
			pool.shutdown();
		}
	}

	/**
	 * @param aId
	 *            the id to set
	 */
	public void setId(String aId) {
		this.mId = aId;
	}

	/**
	 * @param aSecretKey
	 *            the secretKey to set
	 */
	public void setSecretKey(String aSecretKey) {
		this.mSecretKey = aSecretKey;
	}

	/**
	 * @param aAccessKey
	 *            the accessKey to set
	 */
	public void setAccessKey(String aAccessKey) {
		this.mAccessKey = aAccessKey;
	}

	/**
	 * @param aOwner
	 *            the owner to set
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
}
