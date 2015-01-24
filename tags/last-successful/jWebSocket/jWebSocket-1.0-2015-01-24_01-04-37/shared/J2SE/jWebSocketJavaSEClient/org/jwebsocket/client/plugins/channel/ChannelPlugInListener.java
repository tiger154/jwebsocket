//	---------------------------------------------------------------------------
//	jWebSocket - ChannelPlugInListener (Community Edition, CE)
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

import org.jwebsocket.api.WebSocketClientTokenPlugInListener;
import org.jwebsocket.token.Token;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class ChannelPlugInListener implements WebSocketClientTokenPlugInListener {

	@Override
	public void processToken(Token aToken) {
		if ("event".equals(aToken.getType())) {
			if ("channelCreated".equals(aToken.getString("name"))) {
				OnChannelCreated(aToken);
			} else if ("channelRemoved".equals(aToken.getString("name"))) {
				OnChannelRemoved(aToken);
			} else if ("channelStarted".equals(aToken.getString("name"))) {
				OnChannelStarted(aToken);
			} else if ("channelStopped".equals(aToken.getString("name"))) {
				OnChannelStopped(aToken);
			} else if ("subscription".equals(aToken.getString("name"))) {
				OnChannelSubscription(aToken);
			} else if ("unsubscription".equals(aToken.getString("name"))) {
				OnChannelUnsubscription(aToken);
			}
		} else if ("getChannels".equals(aToken.getString("reqType"))) {
			OnChannelsReceived(aToken);
		} else if ("data".equals(aToken.getType())) {
			OnChannelBroadcast(aToken);
		}
	}

	/**
	 * Called when a channel has been created.
	 *
	 * @param aToken
	 */
	public void OnChannelCreated(Token aToken) {
	}

	/**
	 * Called when a channel has been removed.
	 *
	 * @param aToken
	 */
	public void OnChannelRemoved(Token aToken) {
	}

	/**
	 * Called when a channel has been started.
	 *
	 * @param aToken
	 */
	public void OnChannelStarted(Token aToken) {
	}

	/**
	 * Called when a channel has been stopped.
	 *
	 * @param aToken
	 */
	public void OnChannelStopped(Token aToken) {
	}

	/**
	 * Called when a channel receives a new subscription.
	 *
	 * @param aToken
	 */
	public void OnChannelSubscription(Token aToken) {
	}

	/**
	 * Called when a channel receives an un-subscription.
	 *
	 * @param aToken
	 */
	public void OnChannelUnsubscription(Token aToken) {
	}

	/**
	 * Called when the list of available channels is received.
	 *
	 * @param aToken
	 */
	public void OnChannelsReceived(Token aToken) {
	}

	/**
	 * Called when a channel broadcast data because of a publication.
	 *
	 * @param aToken
	 */
	public void OnChannelBroadcast(Token aToken) {
	}
}
