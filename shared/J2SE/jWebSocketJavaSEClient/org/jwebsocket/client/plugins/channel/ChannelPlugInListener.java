//	---------------------------------------------------------------------------
//	jWebSocket - ChannelPlugInListener
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

import org.jwebsocket.api.WebSocketClientTokenPlugInListener;
import org.jwebsocket.token.Token;

/**
 *
 * @author kyberneees
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
