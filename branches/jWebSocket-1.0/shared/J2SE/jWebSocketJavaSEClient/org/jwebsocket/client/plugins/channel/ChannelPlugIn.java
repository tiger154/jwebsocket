//	---------------------------------------------------------------------------
//	jWebSocket - ChannelPlugIn
//	Copyright (c) 2012 jWebSocket.org, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
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

/**
 *
 * @author kyberneees
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
		lRequest.setString("accessKey", aAccessKey);
		lRequest.setString("secretKey", aSecretKey);
		lRequest.setString("owner", aOwner);

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
