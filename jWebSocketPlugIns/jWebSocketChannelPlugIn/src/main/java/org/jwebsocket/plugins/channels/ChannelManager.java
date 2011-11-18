//  ---------------------------------------------------------------------------
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.channels.Channel.ChannelState;
import org.jwebsocket.token.Token;

/**
 * Manager class responsible for all the channel operations within the
 * jWebSocket server system.
 * 
 * @author puran, aschulze
 * @version $Id: ChannelManager.java 1592 2011-02-20 00:49:48Z fivefeetfurther $
 */
public class ChannelManager {

	/** logger */
	private static Logger mLog = Logging.getLogger(ChannelManager.class);
	/** id for the logger channel */
	private static final String LOGGER_CHANNEL_ID = "jws.logger.channel";
	/** id for the admin channel */
	private static final String ADMIN_CHANNEL_ID = "jws.admin.channel";
	/** settings key strings */
	private static final String USE_PERSISTENT_STORE = "use_persistent_store";
	private static final String ALLOW_NEW_CHANNELS = "allow_new_channels";
	/** persistent storage objects */
	private final ChannelStore mChannelStore;
	private final SubscriberStore mSubscriberStore;
	private final PublisherStore mPublisherStore;
	/** in-memory store maps */
	// private final Map<String, Channel> mChannels = new ConcurrentHashMap<String, Channel>();
	private Map<String, Object> mChannelPluginSettings = null;
	/**
	 * Logger channel that publish all the logs in jWebSocket system
	 */
	private static Channel mLoggerChannel = null;
	/**
	 * admin channel to monitor channel plugin activity
	 */
	private static Channel mAdminChannel = null;
	/** setting to check if new channel creation or registration is allowed */
	private static boolean mAllowNewChannels;

	private ChannelManager(Map aSettings,
			IBasicStorage aChannelStorage,
			IBasicStorage aSubscriberStorage,
			IBasicStorage aPublisherStorage) {

		mChannelStore = new BaseChannelStore(aChannelStorage);
		mSubscriberStore = new BaseSubscriberStore(aSubscriberStorage);
		mPublisherStore = new BasePublisherStore(aPublisherStorage);

		this.mChannelPluginSettings = new ConcurrentHashMap<String, Object>(aSettings);

		Object lAllowNewChannels = mChannelPluginSettings.get(ALLOW_NEW_CHANNELS);
		if (lAllowNewChannels != null && lAllowNewChannels.equals("true")) {
			mAllowNewChannels = true;
		}
		int lSuccess = 0;
		for (Object lKey : aSettings.keySet()) {
			String lOption = (String) lKey;
			if (lOption.startsWith("channel:")) {
				String lChannelId = lOption.substring(8);
				Object lObj = aSettings.get(lOption);
				JSONObject lJSON = null;
				boolean lParseOk = false;
				if (lObj instanceof JSONObject) {
					lJSON = (JSONObject) lObj;
					lParseOk = true;
				} else {
					try {
						lJSON = new JSONObject((String) lObj);
						lParseOk = true;
					} catch (Exception Ex) {
					}
				}
				if (lParseOk) {
					String lName = null;
					String lAccessKey = null;
					String lSecretKey = null;
					String lOwner = null;
					boolean lIsPrivate = false;
					boolean lIsSystem = false;
					try {
						lName = lJSON.getString("name");
					} catch (Exception lEx) {
					}
					try {
						lAccessKey = lJSON.getString("access_key");
					} catch (Exception lEx) {
					}
					try {
						lSecretKey = lJSON.getString("secret_key");
					} catch (Exception lEx) {
					}
					try {
						lOwner = lJSON.getString("owner");
					} catch (Exception lEx) {
					}
					try {
						lIsPrivate = lJSON.getBoolean("isPrivate");
					} catch (Exception lEx) {
					}
					try {
						lIsSystem = lJSON.getBoolean("isSystem");
					} catch (Exception lEx) {
					}

					if (mLog.isDebugEnabled()) {
						mLog.debug("Instantiating channel '"
								+ lChannelId + "' by configuration"
								+ " (private: "
								+ lIsPrivate
								+ ", system: " + lIsSystem + ")...");
					}

					Channel lChannel = new Channel(
							lChannelId, // String aId,
							lName, // String aName,
							lIsPrivate, // boolean aPrivateChannel,
							lIsSystem, // boolean aSystemChannel,
							lAccessKey, // String aAccessKey,
							lSecretKey, // String aSecretKey,
							lOwner, // String aOwner,
							ChannelState.INITIALIZED // ChannelState aState,
							);
					// put in channels map
					mChannelStore.storeChannel(lChannel);
					lSuccess++;
				}
			}
		}
		if (mLog.isDebugEnabled()) {
			mLog.debug(lSuccess + " channels successfully instantiated.");
		}
	}

	/**
	 * @param aSettings
	 * @return the static manager instance
	 */
	/*
	public static ChannelManager getChannelManager(Map<String, Object> aSettings) {
	return new ChannelManager(aSettings);
	}
	 */
	/**
	 * Starts the system channels within the jWebSocket system configured via
	 * jWebSocket.xml, Note that it doesn't insert the system channels to the
	 * channel store.
	 *
	 * @throws ChannelLifeCycleException
	 *             if exception starting the system channels
	 */
	public void startChannels() throws ChannelLifeCycleException {
		/*
		User lRoot = SecurityFactory.getRootUser();
		for (Channel lChannel : mChannels.values()) {
		try {
		lChannel.start(lRoot.getLoginname());
		} catch (Exception lEx) {
		mLog.error(lEx.getClass().getSimpleName()
		+ " on starting channel '" + lChannel.getId() + "': "
		+ lEx.getMessage());
		}
		}
		 */
	}

	/**
	 * Stops all the system channels running in the system and clears the system
	 * channels map
	 *
	 * @throws ChannelLifeCycleException
	 */
	public void stopChannels() throws ChannelLifeCycleException {
		/*
		User lRoot = SecurityFactory.getRootUser();
		for (Channel lChannel : mChannels.values()) {
		try {
		lChannel.stop(lRoot.getLoginname());
		} catch (Exception lEx) {
		mLog.error(lEx.getClass().getSimpleName()
		+ " on stopping channel '" + lChannel.getId() + "': "
		+ lEx.getMessage());
		}
		}
		 */
	}

	/**
	 * Returns the channel registered in the jWebSocket system based on channel
	 * id it does a various lookup and then if it doesn't find anywhere from the
	 * memory it loads the channel from the database. If it doesn' find anything
	 * then it returns the null object
	 *
	 * @param aChannelId
	 * @return channel object, null if not found
	 */
	public Channel getChannel(String aChannelId) {
		return mChannelStore.getChannel(aChannelId);
	}

	/**
	 *
	 * @param aChannelId
	 * @return
	 */
	public Channel removeChannel(String aChannelId) {
		Channel lChannel = getChannel(aChannelId);
		if (lChannel != null) {
			mChannelStore.removeChannel(aChannelId);
		}
		return lChannel;
	}

	/**
	 *
	 * @param aChannel
	 * @return
	 */
	public Channel removeChannel(Channel aChannel) {
		if (aChannel != null) {
			return removeChannel(aChannel.getId());
		}
		return null;
	}

	/**
	 * Adds the given channel to the list of channels maintained by the
	 * jWebSocket system.
	 *
	 * @param aChannel
	 *            the channel to store.
	 */
	public void storeChannel(Channel aChannel) {
		mChannelStore.storeChannel(aChannel);
	}

	/**
	 * Returns the registered subscriber object for the given subscriber id
	 *
	 * @param aSubscriberId
	 *            the subscriber id
	 * @return the subscriber object
	 */
	public Subscriber getSubscriber(String aSubscriberId) {
		return mSubscriberStore.getSubscriber(aSubscriberId);
	}

	/**
	 * Stores the registered subscriber information in the channel store
	 *
	 * @param aSubscriber
	 *            the subscriber to register
	 */
	public void storeSubscriber(Subscriber aSubscriber) {
		mSubscriberStore.storeSubscriber(aSubscriber);
	}

	/**
	 * Removes the given subscriber information from channel store
	 *
	 * @param aSubscriber
	 *            the subscriber object
	 */
	public void removeSubscriber(Subscriber aSubscriber) {
		mSubscriberStore.removeSubscriber(aSubscriber.getId());
	}

	/**
	 * Returns the registered publisher for the given publisher id
	 *
	 * @param aPublisherId
	 *            the publisher id
	 * @return the publisher object
	 */
	public Publisher getPublisher(String aPublisherId) {
		return mPublisherStore.getPublisher(aPublisherId);
	}

	/**
	 * Stores the given publisher to the channel store
	 *
	 *
	 * @param aPublisher
	 */
	public void storePublisher(Publisher aPublisher) {
		mPublisherStore.storePublisher(aPublisher);
	}

	/**
	 * Removes the publisher from the channel store permanently
	 *
	 * @param aPublisher
	 *            the publisher to remove
	 */
	public void removePublisher(Publisher aPublisher) {
		mPublisherStore.removePublisher(aPublisher.getId());
	}

	/**
	 * Returns the instance of the logger channel.If not initialized for some
	 * reason returns null.
	 *
	 * @return the logger channel
	 */
	public static Channel getLoggerChannel() {
		return mLoggerChannel;
	}

	/**
	 * Returns the instance of the admin channel. If not initialized for some
	 * reasons returns null.
	 *
	 * @return the admin channel
	 */
	public static Channel getAdminChannel() {
		return mAdminChannel;
	}

	/**
	 *
	 * @param aToken
	 */
	public void publishToLoggerChannel(Token aToken) {
		Channel lLoggerChannel = getLoggerChannel();
		// Added by Alex:
		if (lLoggerChannel != null) {
			lLoggerChannel.broadcastToken(aToken);
		}
	}

	/**
	 * @return the channels
	 */
	public Map<String, Channel> getChannels() {
		return mChannelStore.getChannels();
	}
}
