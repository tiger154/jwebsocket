//  ---------------------------------------------------------------------------
//  jWebSocket - Channel Publisher
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
import javolution.util.FastList;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.factory.JWebSocketFactory;

/**
 * Represents the single publisher connected to a particular channel
 * 
 * @author puran, aschulze
 * @version $Id: Publisher.java 1120 2010-10-24 06:03:08Z mailtopuran@gmail.com$
 */
public final class Publisher {

	private String mConnectionId;
	private List<String> mChannels = new FastList<String>();

	public Publisher(String aConnId) {
		this.mConnectionId = aConnId;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return mConnectionId;
	}

	/**
	 * @return the connector
	 */
	public WebSocketConnector getConnector() {
		WebSocketConnector lConnector =
				JWebSocketFactory.getTokenServer().getConnector(mConnectionId);
		return lConnector;
	}

	/**
	 * @return the channels
	 */
	public List<String> getChannels() {
		return mChannels;
	}

	/**
	 * Add the channel id to the list of channels this subscriber is
	 * subscribed
	 *
	 * @param aChannel
	 */
	public void addChannel(String aChannel) {
		if (this.mChannels != null) {
			this.mChannels.add(aChannel);
		}
	}

	/**
	 * Removes the channel from the subscriber list of channels
	 * @param aChannel the channel id to remove.
	 */
	public void removeChannel(String aChannel) {
		if (this.mChannels != null) {
			this.mChannels.remove(aChannel);
		}
	}


}
