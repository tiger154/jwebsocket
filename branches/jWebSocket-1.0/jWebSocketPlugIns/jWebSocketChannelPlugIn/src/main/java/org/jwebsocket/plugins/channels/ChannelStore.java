//  ---------------------------------------------------------------------------
//  jWebSocket - ChannelStore
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

import java.util.Map;

/**
 * Channel store interface that defines the operation for store/retrieval of
 * channel data.
 * 
 * @author puran
 * @version $Id: ChannelStore.java 1592 2011-02-20 00:49:48Z fivefeetfurther $
 */
public interface ChannelStore {

	/**
	 * Returns the channel from the data store based on channel key or id
	 * @param id the channel id
	 * @return the channel object
	 */
	Channel getChannel(String id);

	/**
	 * Returns all channels from the data store
	 * @return the channel object
	 */
	Map<String, Channel> getChannels();

	/**
	 * Store the given channel in the channel store
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
	 * Clears the channel store, use this method with care since it removes
	 * all the channel information from the store physically and cannot be
	 * rolled back.
	 */
	void clearChannels();

	/**
	 * Returns the size of the channel store
	 * @return the size value
	 */
	int getChannelStoreSize();
}
