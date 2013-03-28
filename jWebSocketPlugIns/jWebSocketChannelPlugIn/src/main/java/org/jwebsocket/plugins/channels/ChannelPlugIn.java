//  ---------------------------------------------------------------------------
//  jWebSocket Channel Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.channels;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.plugins.annotations.Role;
import org.jwebsocket.plugins.channels.Channel.ChannelState;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * Token based implementation of the channel plugin. It's based on a
 * publisher/subscriber model where channels can be either used to publish the
 * data by one or more registered publishers and subscribed by multiple
 * subscribers. The operation of channel is best handled by channel sub-protocol
 * that has to be followed by clients to publish data to the channel or
 * subscribe for receiving the data from the channels.
 *
 ************************ PUBLISHER
 * OPERATION***********************************
 *
 * Token Type : <tt>publisher</tt> Namespace :
 * <tt>org.jwebsocket.plugins.channel</tt>
 *
 * Token Key : <tt>event</tt> Token Value : <tt>[authorize][publish][stop]</tt>
 *
 * <tt>authorize</tt> event command is used for authorization of client before
 * publishing a data to the channel, publisher client has to authorize itself
 * using <tt>secretKey</tt>, <tt>accessKey</tt> and <tt>login</tt> which is
 * registered in the jWebSocket server system via configuration file or from
 * other jWebSocket components.
 *
 * <tt>Token Request Includes:</tt>
 *
 * Token Key : <tt>channel<tt> Token Value : <tt>channel id to authorize
 * for</tt>
 *
 * Token Key : <tt>secretKey<tt> Token Value : <tt>value of the secret key</tt>
 *
 * Token Key : <tt>accessKey<tt> Token Value : <tt>value of the access key</tt>
 *
 * Token Key : <tt>login<tt> Token Value : <tt>login name or id of the
 * jWebSocket registered user</tt>
 *
 * <tt>publish</tt>: publish event means publisher client has been authorized
 * and ready to publish the data. Data is received from the token string of key
 * <tt>data</tt>. If the channel registered is not started then it is started
 * when publish command is received for the first time.
 *
 * <tt>Token Request Includes:</tt>
 *
 * Token Key : <tt>channel<tt> Token Value : <tt>channel id to publish the
 * data</tt>
 *
 * Token Key : <tt>data<tt> Token Value : <tt>data to publish to the
 * channel</tt>
 *
 * <tt>stop</tt>: stop event means proper shutdown of channel and no more data
 * will be received from the publisher.
 *
 ************************ SUBSCRIBER OPERATION
 * *****************************************
 *
 * Token Type : <tt>subscriber</tt> Namespace :
 * <tt>org.jwebsocket.plugins.channel</tt>
 *
 * Token Key : <tt>operation</tt> Token Value :
 * <tt>[subscribe][unsubscribe]</tt>
 *
 * <tt>subscribe</tt> subscribe event is to register the client as a subscriber
 * for the passed in channel and accessKey if the channel is private and needs
 * accessKey for subscription
 *
 * <tt>Token Request Includes:</tt> Token Key : <tt>channel<tt> Token Value :
 * <tt>channel id to publish the data</tt>
 *
 * Token Key : <tt>accessKey<tt> Token Value : <tt>accessKey value required for
 * subscription</tt>
 *
 * <tt>unsubscribe</tt> removes the client from the channel so no data will be
 * broadcasted to the unsuscribed clients.
 *
 * <tt>Token Request Includes:</tt> Token Key : <tt>channel<tt> Token Value :
 * <tt>channel id to unsubscribe</tt>
 *
 * @author puran, aschulze, kyberneees
 * @version $Id: ChannelPlugIn.java 1603 2011-02-28 16:48:50Z fivefeetfurther $
 */
public class ChannelPlugIn extends ActionPlugIn {

	private static Logger mLog = Logging.getLogger();
	private ChannelManager mChannelManager = null;
	/**
	 *
	 */
	public static final String NS_CHANNELS =
			JWebSocketServerConstants.NS_BASE + ".plugins.channels";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ChannelPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket ChannelPlugIn - Community Edition";
	/**
	 * empty string
	 */
	private static final String EMPTY_STRING = "";
	/**
	 * channel plug-in handshake protocol operation values
	 */
	private static final String AUTHORIZE = "authorize";
	private static final String PUBLISH = "publish";
	private static final String STOP_CHANNEL = "stop";
	private static final String START_CHANNEL = "start";
	private static final String SUBSCRIBE = "subscribe";
	private static final String UNSUBSCRIBE = "unsubscribe";
	private static final String GET_CHANNELS = "getChannels";
	private static final String CREATE_CHANNEL = "createChannel";
	private static final String REMOVE_CHANNEL = "removeChannel";
	private static final String GET_SUBSCRIBERS = "getSubscribers";
	private static final String GET_SUBSCRIPTIONS = "getSubscriptions";
	/**
	 * channel plug-in handshake protocol parameters
	 */
	private static final String DATA = "data";
	private static final String MAP = "map";
	// private static final String EVENT = "event";
	private static final String ACCESSKEY = "accessKey";
	private static final String SECRETKEY = "secretKey";
	private static final String OWNER = "owner";
	private static final String CHANNEL = "channel";
	private static ApplicationContext mBeanFactory;

	/**
	 * Constructor with plug-in configuration
	 *
	 * @param aConfiguration the plug-in configuration for this PlugIn
	 */
	public ChannelPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating channel plug-in...");
		}
		// specify default name space
		this.setNamespace(NS_CHANNELS);

		try {
			mBeanFactory = getConfigBeanFactory(NS_CHANNELS);
			mChannelManager = (ChannelManager) mBeanFactory.getBean("channelManager");

			// give a success message to the administrator
			if (mLog.isInfoEnabled()) {
				mLog.info("Channel plug-in successfully instantiated.");
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " at Channel plug-in instantiation: " + lEx.getMessage());
		}
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getVendor() {
		return VENDOR;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getLicense() {
		return LICENSE;
	}

	@Override
	public String getNamespace() {
		return NS_CHANNELS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		super.connectorStarted(aConnector);
		// currently no further action required here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		try {
			// unsubscribe from the channel, if subscribed
			Subscriber lSubscriber = mChannelManager.getSubscriber(aConnector.getId());
			for (Iterator<String> lIt = lSubscriber.getChannels().iterator(); lIt.hasNext();) {
				String lChannelId = lIt.next();
				Channel lChannel = mChannelManager.getChannel(lChannelId);
				if (lChannel != null) {
					// remove subscriber from channel
					lChannel.unsubscribe(lSubscriber.getId(), aConnector);
				}
			}
			// remove subscriber from subscriber store
			mChannelManager.removeSubscriber(lSubscriber);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "processing 'unsubscribe'..."));
		}

		try {
			Publisher lPublisher = mChannelManager.getPublisher(aConnector.getId());
			for (Iterator<String> lIt = lPublisher.getChannels().iterator(); lIt.hasNext();) {
				String lChannelId = lIt.next();
				Channel lChannel = mChannelManager.getChannel(lChannelId);
				if (lChannel != null) {
					// remove publisher from channel
					lChannel.removePublisher(lPublisher.getId());
				}
			}
			// remove publisher from publisher store
			mChannelManager.removePublisher(lPublisher);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "processing 'removePublisher'..."));
		}
	}

	/**
	 * Subscribes the connector to the channel given by the subscriber.
	 *
	 * @param aConnector the connector for this client
	 * @param aToken the request token object
	 */
	@Role(name = NS_CHANNELS + ".subscribe")
	public void subscribeAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lChannelId = aToken.getString(CHANNEL);
		String lAccessKey = aToken.getString(ACCESSKEY);

		// check for valid channel id
		Assert.isTrue(lChannelId != null && !EMPTY_STRING.equals(lChannelId), "No or invalid channel id passed!");
		// try to get channel
		Channel lChannel = mChannelManager.getChannel(lChannelId);

		// try to find the given channel in the channel manager
		// and check if the channel is up
		Assert.isTrue(null != lChannel && Channel.ChannelState.STOPPED != lChannel.getState(),
				"Channel '" + lChannelId
				+ "' doesn't exist or is not started.");

		String lChannelAccessKey = lChannel.getAccessKey();

		Assert.isTrue((lChannelAccessKey == null ? lAccessKey == null : lChannelAccessKey.equals(lAccessKey)),
				"Invalid given channel access key!");

		Subscriber lSubscriber = mChannelManager.getSubscriber(aConnector.getId());


		Assert.isTrue(!lSubscriber.inChannel(lChannelId), "Client already subscribed to channel '"
				+ lChannelId + "'!");

		// this adds the subscriber id the channel
		lChannel.subscribe(lSubscriber.getId(), aConnector);
		// this add the channel to the subscriber
		lSubscriber.addChannel(lChannel.getId());

		Token lResponseToken = createResponse(aToken);
		// return channelId for client's convenience
		lResponseToken.setString("channelId", lChannelId);
		// send the success response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	/**
	 * Method for subscribers to unsubscribe from the channel. If the
	 * unsubscribe operation is successful it sends the unsubscriber - ok
	 * response to the client.
	 *
	 * @param aConnector the connector associated with the subscriber
	 * @param aToken the token object
	 */
	@Role(name = NS_CHANNELS + ".subscribe")
	public void unsubscribeAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lChannelId = aToken.getString(CHANNEL);
		// check for valid channel id
		Assert.isTrue(lChannelId != null && !EMPTY_STRING.equals(lChannelId), "No or invalid channel id passed!");

		Subscriber lSubscriber = mChannelManager.getSubscriber(aConnector.getId());
		Assert.isTrue(lSubscriber.inChannel(lChannelId), "Client not subscribed to channel '"
				+ lChannelId + "'!");

		Channel lChannel = mChannelManager.getChannel(lChannelId);
		Assert.notNull(lChannel, "Channel '" + lChannelId + "' doesn't exists!");

		// this removes the subscriber id from the channel
		lChannel.unsubscribe(lSubscriber.getId(), aConnector);
		// this add the channel to the subscriber
		lSubscriber.removeChannel(lChannel.getId());

		Token lResponseToken = createResponse(aToken);
		// return channelId for client's convenience
		lResponseToken.setString("channelId", lChannelId);
		// send the success response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	/**
	 * Returns all channels available to the client
	 *
	 * @param aConnector the connector for this client
	 * @param aToken the request token object
	 */
	@Role(name = NS_CHANNELS + ".getChannels")
	public void getChannelsAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		// TODO: Here we probably have to introduce restrictions
		// not all clients should be allowed to retreive system channels
		Token lResponseToken = createResponse(aToken);

		List lChannels = new FastList();
		Map<String, Channel> lCMChannels = mChannelManager.getChannels();
		for (Map.Entry<String, Channel> lEntry : lCMChannels.entrySet()) {
			Map lItem = new FastMap();
			Channel lChannel = lEntry.getValue();
			if (!lChannel.isPrivate() || lChannel.getOwner().equals(aConnector.getUsername())) {
				lItem.put("id", lChannel.getId());
				lItem.put("name", lChannel.getName());
				lItem.put("isPrivate", lChannel.isPrivate());
				lItem.put("isSystem", lChannel.isSystem());
				lItem.put("state", lChannel.getState().name());

				lChannels.add(lItem);
			}
		}

		lResponseToken.setList("channels", lChannels);
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	/**
	 * Authorize the publisher before publishing the data to the channel
	 *
	 * @param aConnector the connector associated with the publisher
	 * @param aToken the token received from the publisher client
	 * @param aChannelId the channel id
	 */
	public void authorizeAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lChannelId = aToken.getString(CHANNEL);
		Assert.isTrue(lChannelId != null && !EMPTY_STRING.equals(lChannelId), "No or invalid channel id passed!");

		String lAccessKey = aToken.getString(ACCESSKEY);
		String lSecretKey = aToken.getString(SECRETKEY);

		Assert.notNull(lSecretKey, "The 'access key' argument can't be null!");
		Assert.notNull(lSecretKey, "The 'secret key' argument can't be null!");

		Channel lChannel = mChannelManager.getChannel(lChannelId);
		Assert.notNull(lChannel, "Channel '" + lChannelId + "' doesn't exists!");

		Publisher lPublisher = null;

		if (lChannel.getAccessKey().equals(lAccessKey) && lChannel.getSecretKey().equals(lSecretKey)) {
			lPublisher = mChannelManager.getPublisher(aConnector.getId());

			Assert.isTrue(!lPublisher.inChannel(lChannelId), "Already authorized for channel '"
					+ lChannelId + "'");

			lPublisher.addChannel(lChannelId);
		} else {
			throw new Exception("Authorization failed for channel '"
					+ lChannelId + "'!");
		}

		// add the publisher to the channel
		lChannel.addPublisher(lPublisher.getId());

		Token lResponseToken = createResponse(aToken);
		// mChannelManager.publishToLoggerChannel(lResponseToken);

		// return channelId for client's convenience
		lResponseToken.setString("channelId", lChannelId);

		// send the success response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	@Role(name = NS_CHANNELS + ".publish")
	public void publishAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lChannelId = aToken.getString(CHANNEL);
		Assert.isTrue(lChannelId != null && !EMPTY_STRING.equals(lChannelId), "No or invalid channel id passed!");

		Channel lChannel = mChannelManager.getChannel(lChannelId);
		Assert.notNull(lChannel, "Channel '" + lChannelId + "' doesn't exists!");

		// check if the channel is started
		Assert.isTrue(lChannel.getState().equals(ChannelState.STARTED),
				"Cannot publish data in a non-started channel. Channel '"
				+ lChannel.getName() + "' is not started!");

		Publisher lPublisher = mChannelManager.getPublisher(aConnector.getId());
		Assert.isTrue(lPublisher.inChannel(lChannelId), "Not authorized in the target channel yet!");

		Token lResponseToken = createResponse(aToken);
		// return channelId for client's convenience
		lResponseToken.setString("channelId", lChannelId);
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);

		Token lToken = TokenFactory.createToken(NS_CHANNELS, DATA);
		String lData = aToken.getString(DATA);
		Map lMap = aToken.getMap(MAP);
		// mChannelManager.publishToLoggerChannel(lToken);
		if (null != lData) {
			lToken.setString("data", lData);
		}
		if (null != lMap) {
			lToken.setMap("map", lMap);
		}
		lToken.setString("publisher", lPublisher.getId());
		lToken.setString("user", aConnector.getUsername());
		lToken.setString("channelId", lChannelId);

		// broadcast the token in the channel
		lChannel.broadcastToken(lToken);
	}

	@Role(name = NS_CHANNELS + ".createChannel")
	public void createChannelAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		// get arguments from request
		String lChannelId = aToken.getString(CHANNEL);
		Assert.isTrue(lChannelId != null && !EMPTY_STRING.equals(lChannelId), "No or invalid channel id passed!");

		String lAccessKey = aToken.getString(ACCESSKEY);
		String lSecretKey = aToken.getString(SECRETKEY);
		String lName = aToken.getString("name");
		boolean lIsPrivate = aToken.getBoolean("isPrivate", false);
		boolean lIsSystem = aToken.getBoolean("isSystem", false);
		if (lName == null) {
			lName = lChannelId;
		}
		String lOwner = aToken.getString(OWNER);
		// TODO: introduce validation here
		if (lOwner == null) {
			lOwner = aConnector.getUsername();
		}

		if (lIsPrivate
				&& (lAccessKey == null || lAccessKey.isEmpty() || lSecretKey == null || lSecretKey.isEmpty())) {
			throw new Exception("For private channels both access key and secret key are mandatory.");
		}

		if (lIsSystem) {
			Assert.isTrue(mChannelManager.isAllowCreateSystemChannels(), "Not allowed to create system channels from a client.");
		}
		Assert.isTrue(!mChannelManager.hasChannel(lChannelId), "Channel '" + lChannelId + "' already exists!");

		// creating channel instance
		Channel lChannel = new Channel(
				lChannelId,
				lName,
				lIsPrivate,
				lIsSystem,
				lAccessKey,
				lSecretKey,
				lOwner,
				ChannelState.CREATED,
				getServer(),
				mChannelManager.getStorageProvider().getStorage(ChannelManager.CHANNEL_SUBSCRIBERS_STORAGE_PREFIX + lChannelId),
				mChannelManager.getStorageProvider().getStorage(ChannelManager.CHANNEL_PUBLISHERS_STORAGE_PREFIX + lChannelId));

		if (mLog.isDebugEnabled()) {
			mLog.debug("Initializing channel '" + lChannel.getId() + "'...");
		}
		// initializing channel
		lChannel.init();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting channel '" + lChannel.getId() + "'...");
		}
		// starting channel
		lChannel.start(lOwner);

		// storing channel
		mChannelManager.storeChannel(lChannel);

		// TODO: make broadcast options optional here, not hardcoded!
		// TODO: maybe send on admin channel only?
		// don't broadcast private channel creation!
		if (!lChannel.isPrivate()) {
			// creating channelCreated event
			Token lChannelCreated = TokenFactory.createToken(NS_CHANNELS, BaseToken.TT_EVENT);
			lChannelCreated.setString("name", "channelCreated");
			lChannelCreated.setString("channelId", lChannelId);
			lChannelCreated.setString("channelName", lName);
			lChannelCreated.setBoolean("isPrivate", lIsPrivate);
			lChannelCreated.setBoolean("isSystem", lIsSystem);
			lChannelCreated.setString("state", lChannel.getState().name());
			lChannelCreated.setString("user", lOwner);
			
			broadcastToken(aConnector, lChannelCreated,
					new BroadcastOptions(BroadcastOptions.SENDER_INCLUDED, BroadcastOptions.RESPONSE_IGNORED));
		}

		Token lResponseToken = createResponse(aToken);
		// return channelId for client's convenience
		lResponseToken.setString("channelId", lChannelId);
		lResponseToken.setBoolean("isPrivate", lChannel.isPrivate());

		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	@Role(name = NS_CHANNELS + ".removeChannel")
	public void removeChannelAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		Token lResponseToken = createResponse(aToken);

		// get arguments from request
		String lChannelId = aToken.getString(CHANNEL);
		Assert.isTrue(lChannelId != null && !EMPTY_STRING.equals(lChannelId), "No or invalid channel id passed!");

		String lAccessKey = aToken.getString(ACCESSKEY);
		String lSecretKey = aToken.getString(SECRETKEY);

		// TODO: These two fields will allow to overwrite current user authentication
		// String lOwner = aToken.getString(OWNER);
		// String lPassword =

		// check if channel already exists
		Channel lChannel = mChannelManager.getChannel(lChannelId);
		Assert.notNull(lChannel, "Channel '" + lChannelId + "' doesn't exists!");


		// check if it is a system channel which definitely cannot be removed
		// TODO: To remove system channels a special right needs to be assigned according to specification
		Assert.isTrue(!lChannel.isSystem(), "System channel '" + lChannelId + "' cannot be removed.");

		// check if it is the owner that tries to remove the channel
		String lUser = aConnector.getUsername();
		Assert.isTrue((lUser == null ? lChannel.getOwner() == null : lUser.equals(lChannel.getOwner())),
				"Channel '" + lChannelId + "' can be removed by owner only.");

		String lChannelAccessKey = lChannel.getAccessKey();
		String lChannelSecretKey = lChannel.getSecretKey();
		// check if access key and secret key match
		boolean lAccessKeyMatch =
				(lAccessKey == null || lAccessKey.isEmpty())
				&& (lChannelAccessKey == null || lChannelAccessKey.isEmpty())
				|| (lAccessKey != null && lAccessKey.equals(lChannelAccessKey));
		boolean lSecretKeyMatch =
				(lSecretKey == null || lSecretKey.isEmpty())
				&& (lChannelSecretKey == null || lChannelSecretKey.isEmpty())
				|| (lSecretKey != null && lSecretKey.equals(lChannelSecretKey));

		// check if both access key an secret key match
		Assert.isTrue(lAccessKeyMatch && lSecretKeyMatch, "Invalid or non-mtaching "
				+ "access key or secret key to remove channel '" + lChannelId + "'.");

		mChannelManager.removeChannel(lChannel);

		Token lChannelCreated = TokenFactory.createToken(NS_CHANNELS, BaseToken.TT_EVENT);
		lChannelCreated.setString("name", "channelRemoved");
		lChannelCreated.setString("channelId", lChannelId);
		lChannelCreated.setString("channelName", lChannel.getName());
		lChannelCreated.setString("user", aConnector.getUsername());

		// TODO: make broadcast options optional here, not hardcoded!
		// TODO: maybe send on admin channel only?
		broadcastToken(aConnector, lChannelCreated, new BroadcastOptions(true, false));

		// return channelId for client's convenience
		lResponseToken.setString("channelId", lChannelId);

		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	@Role(name = NS_CHANNELS + ".getSubscribers")
	public void getSubscribersAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lChannelId = aToken.getString(CHANNEL);
		Assert.isTrue(lChannelId != null && !EMPTY_STRING.equals(lChannelId), "No or invalid channel id passed!");

		String lAccessKey = aToken.getString(ACCESSKEY);

		Channel lChannel = mChannelManager.getChannel(lChannelId);
		Assert.notNull(lChannel, "Channel '" + lChannelId + "' doesn't exists!");

		String lChannelAccessKey = lChannel.getAccessKey();
		Assert.isTrue((lChannelAccessKey == null ? lAccessKey == null : lChannelAccessKey.equals(lAccessKey)),
				"Invalid channel '" + lChannelId + "' access key!");

		List<String> lChannelSubscribers = lChannel.getSubscribers();
		List<Map> lSubscribers = new FastList<Map>();
		if (null != lChannelSubscribers) {
			for (String lSubscriber : lChannelSubscribers) {
				//TODO: Some subscribers remain in the list after connector disconnected
				if (getConnector(lSubscriber) != null) {
					Map<String, Object> lItem = new HashMap<String, Object>();
					lItem.put("id", lSubscriber);
					lItem.put("user", getConnector(lSubscriber).getUsername());
					lSubscribers.add(lItem);
				}
			}
		}
		Token lResponseToken = createResponse(aToken);
		// return channel id for client's convenience
		lResponseToken.setString("channel", lChannelId);
		lResponseToken.setList("subscribers", lSubscribers);

		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	@Role(name = NS_CHANNELS + ".getSubscriptions")
	public void getSubscriptionsAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		Subscriber lSubscriber = mChannelManager.getSubscriber(aConnector.getId());
		List<Map> lSubscriptions = new FastList<Map>();

		if (null != lSubscriber) {
			for (String lChannelId : lSubscriber.getChannels()) {
				Map lItem = new FastMap();
				Channel lChannel = mChannelManager.getChannel(lChannelId);
				if (lChannel != null) {
					lItem.put("id", lChannelId);
					lItem.put("name", lChannel.getName());
					lItem.put("isPrivate", lChannel.isPrivate());
					lItem.put("isSystem", lChannel.isSystem());
					lItem.put("state", lChannel.getState().name());
				}
				lSubscriptions.add(lItem);
			}
		}
		Token lResponseToken = createResponse(aToken);
		lResponseToken.setList("channels", lSubscriptions);

		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	@Role(name = NS_CHANNELS + ".stop")
	public void stopChannelAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lChannelId = aToken.getString(CHANNEL);
		Assert.isTrue(lChannelId != null && !EMPTY_STRING.equals(lChannelId), "No or invalid channel id passed!");

		Publisher lPublisher = mChannelManager.getPublisher(aConnector.getId());
		Channel lChannel = mChannelManager.getChannel(lChannelId);
		Assert.notNull(lChannel, "Channel '" + lChannelId + "' doesn't exists!");

		Assert.isTrue(lPublisher.inChannel(lChannelId), "Access denied! Client not autorized on channel '"
				+ lChannelId + "'");

		lChannel.stop(aConnector.getUsername());
		mChannelManager.storeChannel(lChannel);

		sendToken(aConnector, aConnector, createResponse(aToken));
	}

	@Role(name = NS_CHANNELS + ".start")
	public void startChannelAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lChannelId = aToken.getString(CHANNEL);
		Assert.isTrue(lChannelId != null && !EMPTY_STRING.equals(lChannelId), "No or invalid channel id passed!");

		Publisher lPublisher = mChannelManager.getPublisher(aConnector.getId());
		Channel lChannel = mChannelManager.getChannel(lChannelId);
		Assert.notNull(lChannel, "Channel '" + lChannelId + "' doesn't exists!");

		Assert.isTrue(lPublisher.inChannel(lChannelId), "Access denied! Client not autorized on channel '"
				+ lChannelId + "'");

		lChannel.start(aConnector.getUsername());
		mChannelManager.storeChannel(lChannel);

		sendToken(aConnector, aConnector, createResponse(aToken));
	}
}
