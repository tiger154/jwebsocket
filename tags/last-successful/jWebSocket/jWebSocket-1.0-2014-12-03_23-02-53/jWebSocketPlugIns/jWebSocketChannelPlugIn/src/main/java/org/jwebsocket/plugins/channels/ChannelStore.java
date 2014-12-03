//	---------------------------------------------------------------------------
//	jWebSocket - ChannelStore (Community Edition, CE)
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

import java.util.Map;

/**
 * Channel store interface that defines the operation for store/retrieval of
 * channel data.
 *
 * @author Rolando Santamaria Maso, Puran Singh
 */
public interface ChannelStore {

	/**
	 * Returns the channel from the data store based on channel key or id
	 *
	 * @param id the channel id
	 * @return the channel object
	 * @throws Exception
	 */
	Channel getChannel(String id) throws Exception;

	/**
	 * Returns all channels from the data store
	 *
	 * @return the channel object
	 * @throws Exception
	 */
	Map<String, Channel> getChannels() throws Exception;

	/**
	 * Store the given channel in the channel store
	 *
	 * @param channel the channel object
	 * @return {@code true} if insert successful
	 */
	boolean storeChannel(Channel channel);

	/**
	 * Removes the channel from the channel store based on given id
	 *
	 * @param id
	 */
	void removeChannel(String id);

	/**
	 * Clears the channel store, use this method with care since it removes all
	 * the channel information from the store physically and cannot be rolled
	 * back.
	 */
	void clearChannels();

	/**
	 * Returns the size of the channel store
	 *
	 * @return the size value
	 */
	int getChannelStoreSize();

	/**
	 * Indicates if the channel store contains a channel with the given channel
	 * identifier
	 *
	 * @param aChannelId
	 * @return TRUE if the channel exists, FALSE otherwise
	 */
	boolean hasChannel(String aChannelId);
}
