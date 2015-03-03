//	---------------------------------------------------------------------------
//	jWebSocket Channel Manager (Community Edition, CE)
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.IStorageProvider;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.channels.Channel.ChannelState;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.JMSManager;

/**
 * Manager class responsible for all the channel operations within the
 * jWebSocket server system.
 *
 * @author Alexander Schulze, Puran Singh
 * @version $Id: ChannelManager.java 1592 2011-02-20 00:49:48Z fivefeetfurther $
 */
public class ChannelManager implements IInitializable {

	/**
	 * logger
	 */
	private static final Logger mLog = Logging.getLogger();
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
	private ChannelStore mChannelStore;
	private SubscriberStore mSubscriberStore;
	private PublisherStore mPublisherStore;
	private final FastMap<String, Channel> mChannelCache = new FastMap<String, Channel>().shared();
	/**
	 * in-memory store maps
	 */
	// private final Map<String, Channel> mChannels = new ConcurrentHashMap<String, Channel>();
	private Map<String, Object> mChannelPluginSettings = null;
	/**
	 * Logger channel that publish all the logs in jWebSocket system
	 */
	private static final Channel mLoggerChannel = null;
	/**
	 * admin channel to monitor channel plug-in activity
	 */
	private static final Channel mAdminChannel = null;
	/**
	 * setting to check if new channel creation or registration is allowed
	 */
	private final IStorageProvider mStorageProvider;
	private boolean mAllowCreateSystemChannels = false;
	/**
	 *
	 */
	public static final String PLUGIN_STORAGES_PREFIX = "jws.channel.";
	/**
	 *
	 */
	public static final String CHANNELS_STORAGE = PLUGIN_STORAGES_PREFIX + "channels";
	/**
	 *
	 */
	public static final String CHANNEL_PUBLISHERS_STORAGE_PREFIX = PLUGIN_STORAGES_PREFIX + "channel.pub.";
	/**
	 *
	 */
	public static final String CHANNEL_SUBSCRIBERS_STORAGE_PREFIX = PLUGIN_STORAGES_PREFIX + "channel.sub.";
	/**
	 *
	 */
	public static final String PUBLISHERS_STORAGE_PREFIX = PLUGIN_STORAGES_PREFIX + "pub.";
	/**
	 *
	 */
	public static final String SUBSCRIBER_STORAGE_PREFIX = PLUGIN_STORAGES_PREFIX + "sub.";
	private final Map<String, Object> mSettings;
	private JMSManager mJMSManager;

	@Override
	public void initialize() throws Exception {
		mChannelStore = new BaseChannelStore(mStorageProvider.getStorage(CHANNELS_STORAGE), mStorageProvider);
		mSubscriberStore = new BaseSubscriberStore(mStorageProvider);
		mPublisherStore = new BasePublisherStore(mStorageProvider);
		mChannelPluginSettings = new ConcurrentHashMap<String, Object>(mSettings);

		Object lAllowCreateSystemChannels = mChannelPluginSettings.get(ALLOW_CREATE_SYSTEM_CHANNELS);
		if (lAllowCreateSystemChannels != null
				&& lAllowCreateSystemChannels.equals("true")) {
			mAllowCreateSystemChannels = true;
		}
		int lSuccess = 0;
		for (Object lKey : mSettings.keySet()) {
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
				Object lObj = mSettings.get(lOption);
				JSONObject lJSON = null;
				boolean lParseOk = false;
				if (lObj instanceof JSONObject) {
					lJSON = (JSONObject) lObj;
					lParseOk = true;
				} else {
					try {
						lJSON = new JSONObject((String) lObj);
						lParseOk = true;
					} catch (JSONException Ex) {
						// TODO: process exception here properly!
					}
				}
				if (lJSON == null) {
					mLog.error("JSON is 'null', settings for channel '"
							+ lChannelId + "' could not be parsed properly.");
				} else if (lParseOk) {

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
								(TokenServer) JWebSocketFactory.getServer(lServerId),
								mStorageProvider.getStorage(CHANNEL_SUBSCRIBERS_STORAGE_PREFIX + lChannelId),
								mStorageProvider.getStorage(CHANNEL_PUBLISHERS_STORAGE_PREFIX + lChannelId));

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
			mLog.debug(lSuccess + " channels successfully initialized.");
		}
	}

	@Override
	public void shutdown() throws Exception {
	}

	public void setJMSManager(JMSManager aJMSManager) {
		mJMSManager = aJMSManager;
		
		// JMS message registration
		if (null != mJMSManager) {
			try {
			mJMSManager.subscribe(new MessageListener() {
				@Override
				public void onMessage(Message aMessage) {
					try {
						mChannelCache.remove(aMessage.getStringProperty("channelId"));
					} catch (JMSException lEx) {
					}
				}
			}, "ns = '" + ChannelPlugIn.NS_CHANNELS + "' AND (msgType='channelRemoved' OR msgType='channelUpdated'");
			} catch (Exception lEx){
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "registering channels JMS listener"));
			}
		}
	}

	private ChannelManager(Map<String, Object> aSettings,
			IStorageProvider aStorageProvider) {
		mStorageProvider = aStorageProvider;
		mSettings = aSettings;
	}

	/**
	 *
	 * @return
	 */
	public IStorageProvider getStorageProvider() {
		return mStorageProvider;
	}

	/**
	 * Returns the channel registered in the jWebSocket system based on channel
	 * id it does a various lookup and then if it doesn't find anywhere from the
	 * memory it loads the channel from the database. If it doesn' find anything
	 * then it returns the null object
	 *
	 * @param aChannelId
	 * @return channel object, null if not found
	 * @throws Exception
	 */
	public Channel getChannel(String aChannelId) throws Exception {
		if (!mChannelCache.containsKey(aChannelId)) {
			mChannelCache.put(aChannelId, mChannelStore.getChannel(aChannelId));
		}

		return mChannelCache.get(aChannelId);
	}

	/**
	 *
	 * @param aChannelId
	 * @return
	 * @throws Exception
	 */
	public Channel removeChannel(String aChannelId) throws Exception {
		Channel lChannel = getChannel(aChannelId);
		if (lChannel != null) {
			for (String lPublisher : lChannel.getPublishers()) {
				mPublisherStore.removePublisher(lPublisher);
			}
			for (String lSubscriber : lChannel.getSubscribers()) {
				mSubscriberStore.removeSubscriber(lSubscriber);
			}

			lChannel.clearPublishers();
			lChannel.clearSubscribers();

			mChannelStore.removeChannel(aChannelId);
			mChannelCache.remove(aChannelId);
		}
		
		return lChannel;
	}

	/**
	 *
	 * @param aChannel
	 * @return
	 * @throws Exception
	 */
	public Channel removeChannel(Channel aChannel) throws Exception {
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
		mChannelCache.put(aChannel.getId(), aChannel);
		mChannelStore.storeChannel(aChannel);
	}

	/**
	 * Returns the registered subscriber object for the given subscriber id
	 *
	 * @param aSubscriberId the subscriber id
	 * @return the subscriber object
	 * @throws Exception
	 */
	public Subscriber getSubscriber(String aSubscriberId) throws Exception {
		return mSubscriberStore.getSubscriber(aSubscriberId);
	}

	/**
	 * Removes the given subscriber information from channel store
	 *
	 * @param aSubscriber the subscriber object
	 * @throws Exception
	 */
	public void removeSubscriber(Subscriber aSubscriber) throws Exception {
		mSubscriberStore.removeSubscriber(aSubscriber.getId());
	}

	/**
	 * Returns the registered publisher for the given publisher id
	 *
	 * @param aPublisherId the publisher id
	 * @return the publisher object
	 * @throws Exception
	 */
	public Publisher getPublisher(String aPublisherId) throws Exception {
		return mPublisherStore.getPublisher(aPublisherId);
	}

	/**
	 * Removes the publisher from the channel store permanently
	 *
	 * @param aPublisher the publisher to remove
	 * @throws Exception
	 */
	public void removePublisher(Publisher aPublisher) throws Exception {
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
	 * @throws Exception
	 */
	public Map<String, Channel> getChannels() throws Exception {
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
