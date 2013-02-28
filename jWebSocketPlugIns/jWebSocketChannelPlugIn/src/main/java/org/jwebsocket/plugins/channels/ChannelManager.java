//	---------------------------------------------------------------------------
//	jWebSocket Channel Manager (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
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
//  ---------------------------------------------------------------------------
package org.jwebsocket.plugins.channels;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.channels.Channel.ChannelState;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 * Manager class responsible for all the channel operations within the
 * jWebSocket server system.
 *
 * @author puran, aschulze
 * @version $Id: ChannelManager.java 1592 2011-02-20 00:49:48Z fivefeetfurther $
 */
public class ChannelManager {

	/**
	 * logger
	 */
	private static Logger mLog = Logging.getLogger(ChannelManager.class);
	/**
	 * id for the logger channel
	 */
	private static final String LOGGER_CHANNEL_ID = "jws.logger.channel";
	/**
	 * id for the admin channel
	 */
	private static final String ADMIN_CHANNEL_ID = "jws.admin.channel";
	/**
	 * settings key strings
	 */
	private static final String ALLOW_CREATE_SYSTEM_CHANNELS = "allowCreateSystemChannels";
	/**
	 * persistent storage objects
	 */
	private final ChannelStore mChannelStore;
	private final SubscriberStore mSubscriberStore;
	private final PublisherStore mPublisherStore;
	/**
	 * in-memory store maps
	 */
	// private final Map<String, Channel> mChannels = new ConcurrentHashMap<String, Channel>();
	private Map<String, Object> mChannelPluginSettings = null;
	/**
	 * Logger channel that publish all the logs in jWebSocket system
	 */
	private static Channel mLoggerChannel = null;
	/**
	 * admin channel to monitor channel plug-in activity
	 */
	private static Channel mAdminChannel = null;
	/**
	 * setting to check if new channel creation or registration is allowed
	 */
	private boolean mAllowCreateSystemChannels = false;

	private ChannelManager(Map aSettings,
			IBasicStorage aChannelStorage,
			IBasicStorage aSubscriberStorage,
			IBasicStorage aPublisherStorage) {

		mChannelStore = new BaseChannelStore(aChannelStorage);
		mSubscriberStore = new BaseSubscriberStore(aSubscriberStorage);
		mPublisherStore = new BasePublisherStore(aPublisherStorage);

		mChannelPluginSettings = new ConcurrentHashMap<String, Object>(aSettings);

		Object lAllowCreateSystemChannels = mChannelPluginSettings.get(ALLOW_CREATE_SYSTEM_CHANNELS);
		if (lAllowCreateSystemChannels != null
				&& lAllowCreateSystemChannels.equals("true")) {
			mAllowCreateSystemChannels = true;
		}
		int lSuccess = 0;
		for (Object lKey : aSettings.keySet()) {
			String lOption = (String) lKey;
			if (lOption.startsWith("channel:")) {
				String lChannelId = lOption.substring(8);

				if (mChannelStore.hasChannel(lChannelId)) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Channel '" + lChannelId + "' already exists. "
								+ "Loading from configuration ommited!");
					}
					// ommit this channel
					continue;
				}
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
					String lAccessKey;
					String lSecretKey;
					String lOwner;
					String lState;
					boolean lIsPrivate;
					boolean lIsSystem;
					String lServerId;
					try {
						lName = lJSON.getString(BaseChannelStore.NAME);
						lAccessKey = lJSON.getString(BaseChannelStore.ACCESS_KEY);
						lSecretKey = lJSON.getString(BaseChannelStore.SECRET_KEY);
						lOwner = lJSON.getString(BaseChannelStore.OWNER);
						lIsPrivate = lJSON.has(BaseChannelStore.PRIVATE) ? lJSON.getBoolean(BaseChannelStore.PRIVATE) : false;
						lIsSystem = lJSON.has(BaseChannelStore.SYSTEM) ? lJSON.getBoolean(BaseChannelStore.SYSTEM) : false;
						lState = lJSON.has(BaseChannelStore.STATE) ? lJSON.getString(BaseChannelStore.STATE) : ChannelState.CREATED.name();
						lServerId = lJSON.getString(BaseChannelStore.SERVER_ID);

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
								ChannelState.valueOf(lState), // ChannelState aState,
								(TokenServer) JWebSocketFactory.getServer(lServerId));

						// initializing channel if possible
						// the channel could be initialized
						if (lChannel.getState().equals(ChannelState.CREATED)) {
							lChannel.init();
						}

						// put in channels map
						mChannelStore.storeChannel(lChannel);

						lSuccess++;
					} catch (Exception lEx) {
						mLog.error(Logging.getSimpleExceptionMessage(lEx, "loading '" + lName
								+ "' channel"));
					}
				}
			}
		}
		if (mLog.isDebugEnabled()) {
			mLog.debug(lSuccess + " channels successfully instantiated.");
		}
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
	 * @param aChannel the channel to store.
	 */
	public void storeChannel(Channel aChannel) {
		mChannelStore.storeChannel(aChannel);
	}

	/**
	 * Returns the registered subscriber object for the given subscriber id
	 *
	 * @param aSubscriberId the subscriber id
	 * @return the subscriber object
	 */
	public Subscriber getSubscriber(String aSubscriberId) {
		return mSubscriberStore.getSubscriber(aSubscriberId);
	}

	/**
	 * Stores the registered subscriber information in the channel store
	 *
	 * @param aSubscriber the subscriber to register
	 */
	public void storeSubscriber(Subscriber aSubscriber) {
		mSubscriberStore.storeSubscriber(aSubscriber);
	}

	/**
	 * Removes the given subscriber information from channel store
	 *
	 * @param aSubscriber the subscriber object
	 */
	public void removeSubscriber(Subscriber aSubscriber) {
		mSubscriberStore.removeSubscriber(aSubscriber.getId());
	}

	/**
	 * Returns the registered publisher for the given publisher id
	 *
	 * @param aPublisherId the publisher id
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
	 * @param aPublisher the publisher to remove
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

	/**
	 *
	 * @return
	 */
	public boolean isAllowCreateSystemChannels() {
		return mAllowCreateSystemChannels;
	}

	/**
	 * Indicates if the subscriber store contains a subscriber with the given
	 * subscriber identifier
	 *
	 * @param aSubscriberId
	 * @return TRUE if the subscriber exists, FALSE otherwise
	 */
	public boolean hasSubscriber(String aSubscriberId) {
		return mSubscriberStore.hasSubscriber(aSubscriberId);
	}

	/**
	 * Indicates if the publisher store contains the given publisher identifier
	 *
	 * @param aPublisherId
	 * @return TRUE if the publisher exists, FALSE otherwise
	 */
	public boolean hasPublisher(String aPublisherId) {
		return mPublisherStore.hasPublisher(aPublisherId);
	}

	/**
	 * Indicates if the channel store contains a channel with the given channel
	 * identifier
	 *
	 * @param aChannelId
	 * @return TRUE if the channel exists, FALSE otherwise
	 */
	public boolean hasChannel(String aChannelId) {
		return mChannelStore.hasChannel(aChannelId);
	}
}
