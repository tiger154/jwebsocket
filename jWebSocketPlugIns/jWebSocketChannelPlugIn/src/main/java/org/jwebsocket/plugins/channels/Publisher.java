//  ---------------------------------------------------------------------------
//  jWebSocket - Channel Publisher (Community Edition, CE)
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

	/**
	 *
	 * @param aConnId
	 */
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
	 * Add the channel id to the list of channels this subscriber is subscribed
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
	 *
	 * @param aChannel the channel id to remove.
	 */
	public void removeChannel(String aChannel) {
		if (this.mChannels != null) {
			this.mChannels.remove(aChannel);
		}
	}
}
