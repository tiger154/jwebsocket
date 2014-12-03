//	---------------------------------------------------------------------------
//	jWebSocket - ChannelListener (Community Edition, CE)
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

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.token.Token;

/**
 * Listener interface for the <tt>Channel</tt>.
 *
 * @author Alexander Schulze, Puran Singh, Rolando Santamaria Maso
 * @version $Id: ChannelListener.java 1592 2011-02-20 00:49:48Z fivefeetfurther
 * $
 */
public interface ChannelListener {

	/**
	 * Called when the channel is initialized.
	 *
	 * @param aChannel the started channel
	 */
	void channelInitialized(Channel aChannel);

	/**
	 * Called when the channel is started.
	 *
	 * @param aChannel the started channel
	 * @param aUser
	 */
	void channelStarted(Channel aChannel, String aUser);

	/**
	 * called when the channel is stopped for receiving/sending any data
	 *
	 * @param aChannel the stopped channel object
	 * @param aUser the user that stopped the channel
	 */
	void channelStopped(Channel aChannel, String aUser);

	/**
	 * called when the is removed
	 *
	 * @param aChannel the suspended channel
	 * @param aUser
	 */
	void channelRemoved(Channel aChannel, String aUser);

	/**
	 * called when a new subscriber subscribes the channel
	 *
	 * @param aChannel the channel
	 * @param aSubscriber the subscriber
	 */
	void subscribed(Channel aChannel, WebSocketConnector aSubscriber);

	/**
	 * called when someone unsuscribes the channel
	 *
	 * @param aChannel the channel
	 * @param aSubscriber the subscriber who unsuscribed
	 */
	void unsubscribed(Channel aChannel, WebSocketConnector aSubscriber);

	/**
	 * called when channel receives the data from the publisher.
	 *
	 * @param aChannel the channel object
	 * @param aToken the token data received that will be broadcasted to the
	 * subscribers
	 */
	void dataReceived(Channel aChannel, Token aToken);

	/**
	 * Called when channel is done with the broadcast of the data it received to
	 * all the subscribers.
	 *
	 * @param aChannel the channel object
	 * @param aToken the token data which was broadcasted
	 */
	void dataBroadcasted(Channel aChannel, Token aToken);
}
