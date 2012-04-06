//  ---------------------------------------------------------------------------
//  jWebSocket - ChannelPlugIn
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

import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.channels.Channel.ChannelState;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.context.ApplicationContext;

/**
 * Token based implementation of the channel plugin. It's based on a
 * publisher/subscriber model where channels can be either used to publish the
 * data by one or more registered publishers and subscribed by multiple
 * subscribers. The operation of channel is best handled by channel sub-protocol
 * that has to be followed by clients to publish data to the channel or
 * subscribe for receiving the data from the channels.
 * 
 ************************ PUBLISHER OPERATION***********************************
 * 
 * Token Type : <tt>publisher</tt> 
 * Namespace :  <tt>org.jwebsocket.plugins.channel</tt>
 * 
 * Token Key :  <tt>event</tt> 
 * Token Value : <tt>[authorize][publish][stop]</tt>
 * 
 * <tt>authorize</tt> event command is used for authorization of client before
 * publishing a data to the channel, publisher client has to authorize itself
 * using <tt>secretKey</tt>, <tt>accessKey</tt> and <tt>login</tt> which is
 * registered in the jWebSocket server system via configuration file or from
 * other jWebSocket components.
 * 
 * <tt>Token Request Includes:</tt>
 * 
 * Token Key    : <tt>channel<tt>
 * Token Value  : <tt>channel id to authorize for</tt>
 * 
 * Token Key    : <tt>secretKey<tt>
 * Token Value  : <tt>value of the secret key</tt>
 * 
 * Token Key    : <tt>accessKey<tt>
 * Token Value  : <tt>value of the access key</tt>
 * 
 * Token Key    : <tt>login<tt>
 * Token Value  : <tt>login name or id of the jWebSocket registered user</tt>
 * 
 * <tt>publish</tt>: publish event means publisher client has been authorized
 * and ready to publish the data. Data is received from the token string of key
 * <tt>data</tt>. If the channel registered is not started then it is started
 * when publish command is received for the first time.
 * 
 * <tt>Token Request Includes:</tt> 
 * 
 * Token Key    : <tt>channel<tt>
 * Token Value  : <tt>channel id to publish the data</tt>
 * 
 * Token Key    : <tt>data<tt>
 * Token Value  : <tt>data to publish to the channel</tt>
 * 
 * <tt>stop</tt>: stop event means proper shutdown of channel and no more data
 * will be received from the publisher.
 * 
 ************************ SUBSCRIBER OPERATION *****************************************
 * 
 * Token Type : <tt>subscriber</tt> Namespace :
 * <tt>org.jwebsocket.plugins.channel</tt>
 * 
 * Token Key : <tt>operation</tt> Token Value : <tt>[subscribe][unsubscribe]</tt>
 * 
 * <tt>subscribe</tt> subscribe event is to register the client as a subscriber
 * for the passed in channel and accessKey if the channel is private and needs
 * accessKey for subscription
 * 
 * <tt>Token Request Includes:</tt> Token Key : <tt>channel<tt>
 * Token Value  : <tt>channel id to publish the data</tt>
 * 
 * Token Key : <tt>accessKey<tt>
 * Token Value  : <tt>accessKey value required for subscription</tt>
 * 
 * <tt>unsubscribe</tt> removes the client from the channel so no data will be
 * broadcasted to the unsuscribed clients.
 * 
 * <tt>Token Request Includes:</tt> Token Key : <tt>channel<tt>
 * Token Value  : <tt>channel id to unsubscribe</tt>
 * 
 * @author puran, aschulze
 * @version $Id: ChannelPlugIn.java 1603 2011-02-28 16:48:50Z fivefeetfurther $
 */
public class ChannelPlugIn extends TokenPlugIn {

	/** logger */
	private static Logger mLog = Logging.getLogger();
	/** channel manager */
	private ChannelManager mChannelManager = null;
	/** name space for channels */
	private static final String NS_CHANNELS =
			JWebSocketServerConstants.NS_BASE + ".plugins.channels";
	/** empty string */
	private static final String EMPTY_STRING = "";
	/** channel plug-in handshake protocol operation values */
	private static final String AUTHORIZE = "authorize";
	private static final String PUBLISH = "publish";
	private static final String STOP_CHANNEL = "stopChannel";
	private static final String SUBSCRIBE = "subscribe";
	private static final String UNSUBSCRIBE = "unsubscribe";
	private static final String GET_CHANNELS = "getChannels";
	private static final String CREATE_CHANNEL = "createChannel";
	private static final String REMOVE_CHANNEL = "removeChannel";
	private static final String GET_SUBSCRIBERS = "getSubscribers";
	private static final String GET_SUBSCRIPTIONS = "getSubscriptions";
	/** channel plug-in handshake protocol parameters */
	private static final String DATA = "data";
	// private static final String EVENT = "event";
	private static final String ACCESSKEY = "accessKey";
	private static final String SECRETKEY = "secretKey";
	private static final String OWNER = "owner";
	private static final String CHANNEL = "channel";
	private static final String CONNECTED = "connected";
	private static ApplicationContext mBeanFactory;
	
	/**
	 * Constructor with plug-in config
	 *
	 * @param aConfiguration
	 *            the plug-in configuration for this PlugIn
	 */
	public ChannelPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating channel plug-in...");
		}
		// specify default name space
		this.setNamespace(NS_CHANNELS);

		try {
			mBeanFactory = getConfigBeanFactory();
			mChannelManager = (ChannelManager) mBeanFactory.getBean("channelManager");

			// give a success message to the administrator
			if (mLog.isInfoEnabled()) {
				mLog.info("Channel plug-in successfully instantiated.");
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " at Channel plug-in instantiation: " + lEx.getMessage());
		}
	}

	/**
	 * {@inheritDoc} When the engine starts perform the initialization of
	 * default and system channels and start it for accepting subscriptions.
	 */
	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Engine started, starting channels...");
		}
		try {
			mChannelManager.startChannels();
			Map lChannels = mChannelManager.getChannels();
			if (mLog.isInfoEnabled()) {
				if (lChannels != null) {
					mLog.info(lChannels.size() + " channels started.");
				} else {
					mLog.info("No channels configured to be started.");
				}
			}
		} catch (ChannelLifeCycleException lEx) {
			mLog.error("Failed to start channels: " + lEx.getMessage());
		}
	}

	/**
	 * {@inheritDoc} Stops the system channels and clean up all the taken
	 * resources by those channels.
	 */
	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Engine stopped, stopping channels...");
		}
		try {
			// if channel manager has started at all
			// (maybe engine didn't come up)
			if (mChannelManager != null) {
				mChannelManager.stopChannels();
				if (mLog.isInfoEnabled()) {
					mLog.info("Channels stopped.");
				}
			} else if (mLog.isInfoEnabled()) {
				mLog.info("Channels were not yet started, properly terminated.");
			}
		} catch (ChannelLifeCycleException lEx) {
			mLog.error("Error stopping channels", lEx);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		// call super connectorStarted
		super.connectorStarted(aConnector);
		// currently no further action required here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// unsubscribe from the channel, if subscribed
		Subscriber lSubscriber = mChannelManager.getSubscriber(aConnector.getId());
		if (lSubscriber != null) {
			for (String lChannelId : lSubscriber.getChannels()) {
				Channel lChannel = mChannelManager.getChannel(lChannelId);
				if (lChannel != null) {
					// remove subscriber from channel
					lChannel.unsubscribe(lSubscriber.getId());
					// and store channel
					mChannelManager.storeChannel(lChannel);
					// remove subscriber from subscriber store
					mChannelManager.removeSubscriber(lSubscriber);
				}
			}
		}
		Publisher lPublisher = mChannelManager.getPublisher(aConnector.getId());
		if (lPublisher != null) {
			for (String lChannelId : lPublisher.getChannels()) {
				Channel lChannel = mChannelManager.getChannel(lChannelId);
				if (lChannel != null) {
					// remove publisher from channel
					lChannel.removePublisher(lPublisher.getId());
					// and store channel
					mChannelManager.storeChannel(lChannel);
					// remove publisher from publisher store
					mChannelManager.removePublisher(lPublisher);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();
		if (lType != null && getNamespace().equals(lNS)) {
			if (SUBSCRIBE.equals(lType)) {
				subscribe(aConnector, aToken);
			} else if (UNSUBSCRIBE.equals(lType)) {
				unsubscribe(aConnector, aToken);
			} else if (GET_CHANNELS.equals(lType)) {
				getChannels(aConnector, aToken);
			} else if (AUTHORIZE.equals(lType)) {
				// perform the authorization
				authorize(aConnector, aToken);
			} else if (PUBLISH.equals(lType)) {
				// perform the authorization
				publish(aConnector, aToken);
			} else if (CREATE_CHANNEL.equals(lType)) {
				// perform the authorization
				createChannel(aConnector, aToken);
			} else if (REMOVE_CHANNEL.equals(lType)) {
				// perform the authorization
				removeChannel(aConnector, aToken);
			} else if (GET_SUBSCRIBERS.equals(lType)) {
				// return all subscribers for a given channel
				getSubscribers(aConnector, aToken);
			} else if (GET_SUBSCRIPTIONS.equals(lType)) {
				// return all subscriptions for a given client
				getSubscriptions(aConnector, aToken);
			} else if (STOP_CHANNEL.equals(lType)) {
				// return all subscriptions for a given client
				stopChannel(aConnector, aToken);
			} else {
				// ignore
			}
		}
	}

	/**
	 * Subscribes the connector to the channel given by the subscriber.
	 *
	 * @param aConnector
	 *            the connector for this client
	 * @param aToken
	 *            the request token object
	 */
	private void subscribe(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'subscribe'...");
		}
		// check if user is allowed to run 'subscribe' command
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_CHANNELS + ".subscribe")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		String lChannelId = aToken.getString(CHANNEL);
		String lAccessKey = aToken.getString(ACCESSKEY);

		// check for valid channel id
		if (lChannelId == null || EMPTY_STRING.equals(lChannelId)) {
			sendErrorToken(aConnector, aToken, -1,
					"No or invalid channel id passed");
			return;
		}
		// try to get channel
		Channel lChannel = mChannelManager.getChannel(lChannelId);

		// try to find the given channel in the channel manager
		// and check if the channel is up
		if (lChannel == null
				|| lChannel.getState() == Channel.ChannelState.STOPPED) {
			sendErrorToken(aConnector, aToken, -1,
					"Channel '" + lChannelId
					+ "' doesn't exist or is not started.");
			return;
		}

		String lChannelAccessKey = lChannel.getAccessKey();
		if (lChannel.isPrivate()) {
			// TODO: this should be obsolete in future, validate by channel class!
			// validation if private channel has access key
			if (lChannelAccessKey == null || EMPTY_STRING.equals(lChannelAccessKey)) {
				sendErrorToken(aConnector, aToken, -1,
						"No access key assigned to private channel '"
						+ lChannelId + "'");
				return;
			}
			if (lAccessKey == null || !lAccessKey.equals(lChannelAccessKey)) {
				sendErrorToken(aConnector, aToken, -1,
						"Invalid access key for private channel '"
						+ lChannelId + "'");
				return;
			}
		} else {
			// does public channel have an access key?
			if (lChannelAccessKey != null) {
				// if so check match
				if (!lChannelAccessKey.equals(lAccessKey)) {
					sendErrorToken(aConnector, aToken, -1,
							"Invalid access key for public channel '"
							+ lChannelId + "'");
					return;
				}
			} else {
				// if not also check if no access key was passed
				if (lAccessKey != null) {
					sendErrorToken(aConnector, aToken, -1,
							"Invalid access key for public channel '"
							+ lChannelId + "'");
					return;
				}
			}
		}

		// check if client is already a subscriber in the channel manager
		Subscriber lSubscriber = mChannelManager.getSubscriber(aConnector.getId());
		if (lSubscriber == null) {
			lSubscriber = new Subscriber(aConnector.getId());
		}

		// If client already subscribed to given channel, return error message
		Token lResponseToken = createResponse(aToken);
		if (lSubscriber.getChannels().contains(lChannelId)) {
			sendErrorToken(aConnector, aToken, -1,
					"Client already subscribed to channel '"
					+ lChannelId + "'");
			return;
		} else {
			// this adds the subscriber id the channel
			lChannel.subscribe(lSubscriber.getId());
			// this add the channel to the subscriber
			lSubscriber.addChannel(lChannel.getId());
			// this saves the subscriber
			mChannelManager.storeSubscriber(lSubscriber);
			// and this saves the channel
			mChannelManager.storeChannel(lChannel);
		}

		// return channelId for client's convenience
		lResponseToken.setString("channelId", lChannelId);
		// send the success response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	/**
	 * Method for subscribers to unsubscribe from the channel. If the unsubscribe
	 * operation is successful it sends the unsubscriber - ok response to the
	 * client.
	 *
	 * @param aConnector
	 *            the connector associated with the subscriber
	 * @param aToken
	 *            the token object
	 */
	private void unsubscribe(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'unsubscribe'...");
		}
		// check if user is allowed to run 'subscribe' command (no extra unsubscribe right!)
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_CHANNELS + ".subscribe")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		String lChannelId = aToken.getString(CHANNEL);
		// check for valid channel id
		if (lChannelId == null || EMPTY_STRING.equals(lChannelId)) {
			sendErrorToken(aConnector, aToken, -1,
					"No or invalid channel id passed");
			return;
		}

		Token lResponseToken = createResponse(aToken);
		// check if client exists as subscriber at channel manager
		Subscriber lSubscriber =
				mChannelManager.getSubscriber(aConnector.getId());
		if (lSubscriber != null) {
			Channel lChannel = mChannelManager.getChannel(lChannelId);
			if (lChannel != null) {
				// this removes the subscriber id from the channel
				lChannel.unsubscribe(lSubscriber.getId());
				// this add the channel to the subscriber
				lSubscriber.removeChannel(lChannel.getId());
				// this saves the subscriber
				mChannelManager.storeSubscriber(lSubscriber);
				// and this saves the channel
				mChannelManager.storeChannel(lChannel);
			} else {
				sendErrorToken(aConnector, aToken, -1,
						"Channel '" + lChannelId
						+ "' doesn't exist.");
				return;
			}
		} else {
			sendErrorToken(aConnector, aToken, -1,
					"Client not subscribed to channel '"
					+ lChannelId + "'.");
			return;
		}

		// return channelId for client's convenience
		lResponseToken.setString("channelId", lChannelId);
		// send the success response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	/**
	 * Returns all channels available to the client
	 *
	 * @param aConnector
	 *            the connector for this client
	 * @param aToken
	 *            the request token object
	 */
	private void getChannels(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getChannels'...");
		}
		// check if user is allowed to run 'subscribe' command (no extra unsubscribe right!)
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_CHANNELS + ".getChannels")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		// TODO: Here we probably have to introduce restrictions
		// not all clients should be allowed to retreive system or private channels
		Token lResponseToken = createResponse(aToken);

		List lChannels = new FastList();
		Map<String, Channel> lCMChannels = mChannelManager.getChannels();
		for (Map.Entry<String, Channel> lEntry : lCMChannels.entrySet()) {
			Map lItem = new FastMap();
			Channel lChannel = lEntry.getValue();
			if (!lChannel.isPrivate()) {
				lItem.put("id", lChannel.getId());
				lItem.put("name", lChannel.getName());
				lItem.put("isPrivate", lChannel.isPrivate());
				lItem.put("isSystem", lChannel.isSystem());
				// TODO: remove these two lines after tests
				// lItem.put("accessKey", lChannel.getAccessKey());
				// lItem.put("secretKey", lChannel.getSecretKey());
				// lItem.put("owner", lChannel.getOwner());
				lChannels.add(lItem);
			}
		}
		lResponseToken.setList("channels", lChannels);

		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	/**
	 * Authorize the publisher before publishing the data to the channel
	 * @param aConnector the connector associated with the publisher
	 * @param aToken the token received from the publisher client
	 * @param aChannelId the channel id
	 */
	private void authorize(WebSocketConnector aConnector, Token aToken) {
		String lChannelId = aToken.getString(CHANNEL);
		String lAccessKey = aToken.getString(ACCESSKEY);
		String lSecretKey = aToken.getString(SECRETKEY);
		/*
		String lLogin = aToken.getString("login");
		User lUser = SecurityFactory.getUser(lLogin);
		if (lUser == null) {
		sendErrorToken(aConnector, aToken, -1,
		"'" + aConnector.getId()
		+ "' Authorization failed for channel '"
		+ lChannelId
		+ "', channel owner is not registered in the jWebSocket server system");
		return;
		}
		 */
		if (lSecretKey == null || lAccessKey == null) {
			sendErrorToken(aConnector, aToken, -1,
					"'" + aConnector.getId()
					+ "' Authorization failed, access/secret key pair value is not correct");
			return;
		} else {
			Channel lChannel = mChannelManager.getChannel(lChannelId);
			if (lChannel == null) {
				sendErrorToken(aConnector, aToken, -1,
						"'" + aConnector.getId()
						+ "' channel not found for given channelId '"
						+ lChannelId + "'");
				return;
			}
			Publisher lPublisher = null;

			if (lChannel.getAccessKey().equals(lAccessKey)
					&& lChannel.getSecretKey().equals(lSecretKey)) {
				lPublisher = new Publisher(aConnector.getId());
				// store publisher
				mChannelManager.storePublisher(lPublisher);
			}

			if (lPublisher == null) {
				// couldn't authorize the publisher
				sendErrorToken(aConnector, aToken, -1,
						"'" + aConnector.getId()
						+ "': Authorization failed for channel '"
						+ lChannelId + "'");
			} else if (lChannel.getPublishers().contains(lPublisher.getId())) {
				// couldn't authorize the publisher
				sendErrorToken(aConnector, aToken, -1,
						"'" + aConnector.getId()
						+ "': Already authorized for channel '"
						+ lChannelId + "'");
			} else {
				// add the publisher to the channel
				lChannel.addPublisher(lPublisher.getId());
				// and store the channel including the new publisher
				mChannelManager.storeChannel(lChannel);

				Token lResponseToken = createResponse(aToken);
				// mChannelManager.publishToLoggerChannel(lResponseToken);

				// return channelId for client's convenience
				lResponseToken.setString("channelId", lChannelId);

				// send the success response
				sendToken(aConnector, aConnector, lResponseToken);
			}
		}
	}

	private void publish(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'publish'...");
		}
		// check if user is allowed to publish data on channels at all
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_CHANNELS + ".publish")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}
		String lChannelId = aToken.getString(CHANNEL);

		Channel lChannel = mChannelManager.getChannel(lChannelId);
		Publisher lPublisher = mChannelManager.getPublisher(aConnector.getId());

		if (lPublisher == null) {
			sendErrorToken(aConnector, aToken, -1,
					"Connector '" + aConnector.getId()
					+ "': access denied, publisher not authorized for channelId '"
					+ lChannelId + "'");
			return;
		}
		Token lResponseToken = createResponse(aToken);
		// return channelId for client's convenience
		lResponseToken.setString("channelId", lChannelId);
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);

		Token lToken = TokenFactory.createToken(NS_CHANNELS, DATA);
		String lData = aToken.getString(DATA);
		// mChannelManager.publishToLoggerChannel(lToken);
		lToken.setString("data", lData);
		lToken.setString("publisher", lPublisher.getId());
		// return channelId for client's convenience
		lToken.setString("channelId", lChannelId);
		lChannel.broadcastToken(lToken);
	}

	private void createChannel(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'createChannel'...");
		}
		// check if user is allowed to create a new channel
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_CHANNELS + ".createChannel")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}
		// TODO: To create system channels a special right needs to be assigned according to specification (manageSystemChannels)

		Token lResponseToken = createResponse(aToken);

		// get arguments from request
		String lChannelId = aToken.getString(CHANNEL);
		String lAccessKey = aToken.getString(ACCESSKEY);
		String lSecretKey = aToken.getString(SECRETKEY);
		String lName = aToken.getString("name");
		boolean lIsPrivate = aToken.getBoolean("isPrivate", false);
		boolean lIsSystem = aToken.getBoolean("isSystem", false);
		if (lName == null) {
			lName = lChannelId;
		}
		String lOwner = aToken.getString("owner");
		// TODO: introduce validation here
		if (lOwner == null) {
			lOwner = aConnector.getUsername();
		}
		// check if we have a valid user for the new channel (owner)
		if (lOwner == null) {
			sendErrorToken(aConnector, aToken, -1,
					"No owner for channel or not authenticated.");
			return;
		}
		// check if we have a valid user for the new channel (owner)
		if (lIsPrivate
				&& (lAccessKey == null || lAccessKey.isEmpty() || lSecretKey == null || lSecretKey.isEmpty())) {
			sendErrorToken(aConnector, aToken, -1,
					"For private channels both access key and secret key are mandatory.");
			return;
		}
		// check if channel already exists
		Channel lChannel = mChannelManager.getChannel(lChannelId);
		if (lChannel != null) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", "Channel with id '" + lChannelId + "' already exists.");
		} else {
			lChannel = new Channel(
					lChannelId, // String aId,
					lName, // String aName,
					lIsPrivate, // boolean aPrivateChannel,
					lIsSystem, // boolean aSystemChannel,
					lAccessKey, // String aAccessKey,
					lSecretKey, // String aSecretKey,
					lOwner, // String aOwner,
					// TODO: think about whether start/stop of channel is desired at all!
					ChannelState.INITIALIZED // ChannelState aState,
					);
			mChannelManager.storeChannel(lChannel);

			Token lChannelCreated = TokenFactory.createToken(NS_CHANNELS, BaseToken.TT_EVENT);
			lChannelCreated.setString("name", "channelCreated");
			lChannelCreated.setString("channelId", lChannelId);
			lChannelCreated.setString("channelName", lName);

			// TODO: make broadcast options optional here, not hardcoded!
			// TODO: maybe send on admin channel only?
			// don't broadcast private channel creation!
			if (!lChannel.isPrivate()) {
				broadcastToken(aConnector, lChannelCreated,
						new BroadcastOptions(BroadcastOptions.SENDER_INCLUDED, BroadcastOptions.RESPONSE_IGNORED));
			}
		}

		// return channelId for client's convenience
		lResponseToken.setString("channelId", lChannelId);
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void removeChannel(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'removeChannel'...");
		}
		// check if user is allowed to remove an existing channel
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_CHANNELS + ".removeChannel")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}
		Token lResponseToken = createResponse(aToken);

		// get arguments from request
		String lChannelId = aToken.getString(CHANNEL);
		String lAccessKey = aToken.getString(ACCESSKEY);
		String lSecretKey = aToken.getString(SECRETKEY);

		// TODO: These two fields will allow to overwrite current user authentication
		// String lOwner = aToken.getString(OWNER);
		// String lPassword =

		// check if channel already exists
		Channel lChannel = mChannelManager.getChannel(lChannelId);
		if (lChannel == null) {
			sendErrorToken(aConnector, aToken, -1,
					"Channel with id '" + lChannelId + "' does not exist.");
			return;
		}
		// check if it is a system channel which definitely cannot be removed
		// TODO: To remove system channels a special right needs to be assigned according to specification
		if (lChannel.isSystem()) {
			sendErrorToken(aConnector, aToken, -1,
					"System channel '" + lChannelId + "' cannot be removed.");
			return;
		}
		// check if it is the owner that tries to remove the channel
		String lUser = aConnector.getUsername();
		if (lUser == null || !lUser.equals(lChannel.getOwner())) {
			sendErrorToken(aConnector, aToken, -1,
					"Channel '" + lChannelId + "' can be removed by owner only.");
			return;
		}
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
		if (!(lAccessKeyMatch && lSecretKeyMatch)) {
			sendErrorToken(aConnector, aToken, -1,
					"Invalid or non-mtaching access key or secret key to remove channel '"
					+ lChannelId + "'.");
			return;
		}

		// TODO: Add condition to optionally suppress this broadcast
		if (true) {
			mChannelManager.removeChannel(lChannel);

			Token lChannelCreated = TokenFactory.createToken(NS_CHANNELS, BaseToken.TT_EVENT);
			lChannelCreated.setString("name", "channelRemoved");
			lChannelCreated.setString("channelId", lChannelId);
			lChannelCreated.setString("channelName", lChannel.getName());

			// TODO: make broadcast options optional here, not hardcoded!
			// TODO: maybe send on admin channel only?
			broadcastToken(aConnector, lChannelCreated, new BroadcastOptions(true, false));
		}

		// return channelId for client's convenience
		lResponseToken.setString("channelId", lChannelId);
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void getSubscribers(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getSubscribers'...");
		}
		// check if user is allowed to retrieve subscribers of a channel
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_CHANNELS + ".getSubscribers")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}
		String lChannelId = aToken.getString(CHANNEL);
		String lAccessKey = aToken.getString(ACCESSKEY);

		Channel lChannel = mChannelManager.getChannel(lChannelId);
		if (lChannel == null) {
			sendErrorToken(aConnector, aToken, -1,
					"No channel found with id '" + lChannelId + "'");
			return;
		}

		String lChannelAccessKey = lChannel.getAccessKey();
		if (lChannel.isPrivate()) {
			// TODO: this should be obsolete in future, validate by channel class!
			// validation if private channel has access key
			if (lChannelAccessKey == null || EMPTY_STRING.equals(lChannelAccessKey)) {
				sendErrorToken(aConnector, aToken, -1,
						"No access key assigned to private channel '"
						+ lChannelId + "'");
				return;
			}
			if (lAccessKey == null || !lAccessKey.equals(lChannelAccessKey)) {
				sendErrorToken(aConnector, aToken, -1,
						"Invalid access key for private channel '"
						+ lChannelId + "'");
				return;
			}
		} else {
			// does public channel have an access key?
			if (lChannelAccessKey != null) {
				// if so check match
				if (!lChannelAccessKey.equals(lAccessKey)) {
					sendErrorToken(aConnector, aToken, -1,
							"Invalid access key for public channel '"
							+ lChannelId + "'");
					return;
				}
			} else {
				// if not also check if no access key was passed
				if (lAccessKey != null) {
					sendErrorToken(aConnector, aToken, -1,
							"Invalid access key for public channel '"
							+ lChannelId + "'");
					return;
				}
			}
		}

		List<String> lChannelSubscribers = lChannel.getSubscribers();
		List<Map> lSubscribers = new FastList<Map>();
		if (null != lChannelSubscribers) {
			for (String lSubscriber : lChannelSubscribers) {
				Map<String, Object> lItem = new FastMap<String, Object>();
				lItem.put("id", lSubscriber);
				lSubscribers.add(lItem);
			}
		}
		Token lResponseToken = createResponse(aToken);
		// return channel id for client's convenience
		lResponseToken.setString("channel", lChannelId);
		lResponseToken.setList("subscribers", lSubscribers);

		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void getSubscriptions(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getSubscriptions'...");
		}
		// check if user is allowed to retrieve all its subscriptions
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_CHANNELS + ".getSubscriptions")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}
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
				}
				lSubscriptions.add(lItem);
			}
		}
		Token lResponseToken = createResponse(aToken);
		lResponseToken.setList("channels", lSubscriptions);

		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void stopChannel(WebSocketConnector aConnector, Token aToken) {
		String lChannelId = aToken.getString(CHANNEL);
		if (lChannelId == null || EMPTY_STRING.equals(lChannelId)) {
			sendErrorToken(aConnector, aToken, -1,
					"Channel value not specified.");
			return;
		}
		Publisher lPublisher = mChannelManager.getPublisher(aConnector.getId());
		Channel lChannel = mChannelManager.getChannel(lChannelId);
		if (lChannel == null) {
			sendErrorToken(aConnector, aToken, -1,
					"'" + aConnector.getId()
					+ "' channel not found for given channelId '"
					+ lChannelId + "'");
			return;
		}
		if (lPublisher == null) {
			sendErrorToken(aConnector, aToken, -1,
					"Connector: " + aConnector.getId()
					+ ": access denied, publisher not authorized for channelId '"
					+ lChannelId + "'");
			return;
		}
		try {
			lChannel.stop(lPublisher.getId());
			Token lSuccessToken = createResponse(aToken);
			sendTokenAsync(aConnector, aConnector, lSuccessToken);
		} catch (ChannelLifeCycleException lEx) {
			mLog.error("Error stopping channel '" + lChannelId
					+ "' from publisher "
					+ lPublisher.getId() + "'", lEx);

			// publish to logger channel
			Token lErrorToken = createResponse(aToken);
			lErrorToken.setInteger("code", -1);
			lErrorToken.setString("msg", "'" + aConnector.getId()
					+ "' Error stopping channel '" + lChannelId
					+ "' from publisher '" + lPublisher.getId() + "'");
			mChannelManager.publishToLoggerChannel(lErrorToken);
			sendTokenAsync(aConnector, aConnector, lErrorToken);
		}
	}
}
