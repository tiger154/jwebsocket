//	---------------------------------------------------------------------------
//	jWebSocket - Channel Publisher (Community Edition, CE)
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

import java.util.LinkedList;
import java.util.List;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.factory.JWebSocketFactory;

/**
 * Represents the single publisher connected to a particular channel
 *
 * @author Alexander Schulze, Puran Singh, Rolando Santamaria Maso
 * @version $Id: Publisher.java 1120 2010-10-24 06:03:08Z mailtopuran@gmail.com$
 */
public class Publisher {

	private final String mPublisherId;
	private final IBasicStorage<String, Object> mStorage;

	/**
	 *
	 * @param aId
	 * @param aStorage
	 */
	public Publisher(String aId, IBasicStorage<String, Object> aStorage) {
		mPublisherId = aId;
		mStorage = aStorage;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return mPublisherId;
	}

	/**
	 * @return the connector
	 */
	public WebSocketConnector getConnector() {
		WebSocketConnector lConnector =
				JWebSocketFactory.getTokenServer().getConnector(mPublisherId);
		return lConnector;
	}

	/**
	 * @return the channels
	 */
	public List<String> getChannels() {
		List<String> lList = new LinkedList<String>();
		lList.addAll(mStorage.keySet());

		return lList;
	}

	/**
	 *
	 * @param aChannel
	 * @return
	 */
	public Boolean inChannel(String aChannel) {
		return mStorage.containsKey(aChannel);
	}

	/**
	 * Add the channel id to the list of channels this subscriber is subscribed
	 *
	 * @param aChannel
	 */
	public void addChannel(String aChannel) {
		mStorage.put(aChannel, true);
	}

	/**
	 * Removes the channel from the subscriber list of channels
	 *
	 * @param aChannel the channel id to remove.
	 */
	public void removeChannel(String aChannel) {
		mStorage.remove(aChannel);
	}
}
