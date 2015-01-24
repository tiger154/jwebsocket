//	---------------------------------------------------------------------------
//	jWebSocket - ChannelPlugIn (Community Edition, CE)
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
package org.jwebsocket.client.plugins.channel;

import java.util.Map;
import org.jwebsocket.api.WebSocketTokenClient;
import org.jwebsocket.client.plugins.BaseClientTokenPlugIn;
import org.jwebsocket.config.JWebSocketClientConstants;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.token.WebSocketResponseTokenListener;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class ChannelPlugIn extends BaseClientTokenPlugIn {

	/**
	 *
	 * @param aClient
	 * @param aNS
	 */
	public ChannelPlugIn(WebSocketTokenClient aClient, String aNS) {
		super(aClient, aNS);
	}

	/**
	 *
	 * @param aClient
	 */
	public ChannelPlugIn(WebSocketTokenClient aClient) {
		super(aClient, JWebSocketClientConstants.NS_CHANNELS);
	}

	/**
	 * Subscribes to an existing channel
	 *
	 * @param aChannelId
	 * @param aAccessKey
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void subscribe(String aChannelId, String aAccessKey, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "subscribe");
		lRequest.setString("channel", aChannelId);
		lRequest.setString("accessKey", aAccessKey);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Un-subscribes from a previous subscribed channel
	 *
	 * @param aChannelId
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void unsubscribe(String aChannelId, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "unsubscribe");
		lRequest.setString("channel", aChannelId);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Authorizes to an existing channel
	 *
	 * @param aChannelId
	 * @param aAccessKey
	 * @param aSecretKey
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void authorize(String aChannelId, String aAccessKey, String aSecretKey, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "authorize");
		lRequest.setString("channel", aChannelId);
		lRequest.setString("accessKey", aAccessKey);
		lRequest.setString("secretKey", aSecretKey);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Publishes data on a previous authorized channel
	 *
	 * @param aChannelId
	 * @param aData
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void publish(String aChannelId, String aData, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "publish");
		lRequest.setString("channel", aChannelId);
		lRequest.setString("data", aData);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Publishes data on a previous authorized channel
	 *
	 * @param aChannelId
	 * @param aData
	 * @param aDataMap
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void publish(String aChannelId, String aData, Map aDataMap, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "publish");
		lRequest.setString("channel", aChannelId);
		lRequest.setString("data", aData);
		lRequest.setMap("map", aDataMap);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Publishes data on a previous authorized channel
	 *
	 * @param aChannelId
	 * @param aMap
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void publish(String aChannelId, Map aMap, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "publish");
		lRequest.setString("channel", aChannelId);
		lRequest.setMap("map", aMap);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Creates a channel.
	 *
	 * @param aId
	 * @param aName
	 * @param aIsPrivate
	 * @param aIsSystem
	 * @param aAccessKey
	 * @param aSecretKey
	 * @param aOwner
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void create(String aId, String aName, boolean aIsPrivate, boolean aIsSystem, String aAccessKey, String aSecretKey, String aOwner, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "createChannel");
		lRequest.setString("channel", aId);
		lRequest.setString("name", aName);
		lRequest.setBoolean("isPrivate", aIsPrivate);
		lRequest.setBoolean("isSystem", aIsSystem);

		if (null != aAccessKey && !"".equals(aAccessKey)) {
			aAccessKey = Tools.getMD5(aAccessKey);
		}
		lRequest.setString("accessKey", aAccessKey);

		if (null != aSecretKey && !"".equals(aSecretKey)) {
			aSecretKey = Tools.getMD5(aSecretKey);
		}
		lRequest.setString("secretKey", aSecretKey);
		lRequest.setString("owner", aOwner);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Modify a channel
	 *
	 * @param aId
	 * @param aSecretKey The current secret key value
	 * @param aNewSecretKey The channel new secret key value
	 * @param aAccessKey The channel new access key value
	 * @param aOwner The channel new owner value
	 * @param aIsSystem The channel new 'isSystem' value
	 * @param aIsPrivate The channel new 'isPrivate' value
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void modify(String aId, String aSecretKey, String aNewSecretKey, String aAccessKey,
			String aOwner, boolean aIsSystem, boolean aIsPrivate, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "modifyChannel");
		lRequest.setString("channel", aId);
		lRequest.setBoolean("isPrivate", aIsPrivate);
		lRequest.setBoolean("isSystem", aIsSystem);

		if (null != aNewSecretKey && !"".equals(aNewSecretKey)) {
			aNewSecretKey = Tools.getMD5(aNewSecretKey);
		}
		if (null != aAccessKey && !"".equals(aAccessKey)) {
			aAccessKey = Tools.getMD5(aAccessKey);
		}
		lRequest.setString("accessKey", aAccessKey);
		lRequest.setString("newSecretKey", aNewSecretKey);

		if (null != aSecretKey && !"".equals(aSecretKey)) {
			aSecretKey = Tools.getMD5(aSecretKey);
		}
		lRequest.setString("secretKey", aSecretKey);
		lRequest.setString("owner", aOwner);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Create a channel.
	 *
	 * @param aId
	 * @param aName
	 * @param aIsPrivate
	 * @param aIsSystem
	 * @param aAccessKey
	 * @param aSecretKey
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void create(String aId, String aName, boolean aIsPrivate, boolean aIsSystem, String aAccessKey, String aSecretKey, WebSocketResponseTokenListener aListener) throws WebSocketException {
		create(aId, aName, aIsPrivate, aIsSystem, aAccessKey, aSecretKey, null, aListener);
	}

	/**
	 * Removes an existing channel.
	 *
	 * @param aChannelId
	 * @param aAccessKey
	 * @param aSecretKey
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void remove(String aChannelId, String aAccessKey, String aSecretKey, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "removeChannel");
		lRequest.setString("channel", aChannelId);
		lRequest.setString("accessKey", aAccessKey);
		lRequest.setString("secretKey", aSecretKey);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Gets subscribers from an existing channel.
	 *
	 * @param aChannelId
	 * @param aAccessKey
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void getSubscribers(String aChannelId, String aAccessKey, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "getSubscribers");
		lRequest.setString("channel", aChannelId);
		lRequest.setString("accessKey", aAccessKey);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Gets the client current subscriptions.
	 *
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void getSubscriptions(WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "getSubscriptions");

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Gets the list of available channels.
	 *
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void getChannels(WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "getChannels");

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Stops an existing started channel (authorization required).
	 *
	 * @param aChannelId
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void stop(String aChannelId, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "stop");
		lRequest.setString("channel", aChannelId);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Starts an existing stopped channel (authorization required)
	 *
	 * @param aChannelId
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void start(String aChannelId, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "start");
		lRequest.setString("channel", aChannelId);

		getTokenClient().sendToken(lRequest, aListener);
	}
}
