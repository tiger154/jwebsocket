//  ---------------------------------------------------------------------------
//  jWebSocket - Channel Subscriber (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
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

/**
 * Class that represents the subscriber of a channel
 *
 * @author puran, aschulze
 * @version $Id: Subscriber.java 1592 2011-02-20 00:49:48Z fivefeetfurther $
 */
public class Subscriber {

	private String mConnectionId;
	private List<String> mChannels = new FastList<String>();

	/**
	 * Default constructor
	 *
	 * @param aConnectionId
	 */
	public Subscriber(String aConnectionId) {
		this.mConnectionId = aConnectionId;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return mConnectionId;
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
