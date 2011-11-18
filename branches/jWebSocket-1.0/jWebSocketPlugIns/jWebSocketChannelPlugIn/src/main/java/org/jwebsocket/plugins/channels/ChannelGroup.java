//  ---------------------------------------------------------------------------
//  jWebSocket - ChannelGroup
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

import java.util.Collections;
import java.util.Map;
import javolution.util.FastMap;

/**
 * maintains a group of channels, which e.g. can be used watch lists or similar.
 * @author aschulze
 */
public class ChannelGroup {

	private String mId = null;
	private Map<String, Channel> mChannels = null;

	/**
	 *
	 */
	public ChannelGroup(String aId) {
		mChannels = new FastMap<String, Channel>();
	}

	/**
	 * adds a new channel to an existing channel group.
	 * @param aChannel
	 * @return
	 */
	public boolean addChannel(Channel aChannel) {
		if (aChannel != null) {
			mChannels.put(aChannel.getId(), aChannel);
			return true;
		}
		return false;
	}

	/**
	 * checks if the given channel does exist in the channel group.
	 * @param aChannel
	 * @return
	 */
	public boolean containsChannel(Channel aChannel) {
		return mChannels.containsValue(aChannel);
	}

	/**
	 * checks if a channel with the given id does exist in the channel group.
	 * @param aChannelId
	 * @return
	 */
	public boolean containsChannel(String aChannelId) {
		return mChannels.containsKey(aChannelId);
	}

	/**
	 * returns the channel with the given channel id or <tt>null</tt>
	 * if the channel does not exist in the channel group.
	 * @param aChannelId
	 * @return
	 */
	public Channel getChannel(String aChannelId) {
		return mChannels.get(aChannelId);
	}

	/**
	 * removes a channel from an existing channel group.
	 * @param aChannel
	 * @return
	 */
	public boolean removeChannel(Channel aChannel) {
		if (aChannel != null) {
			mChannels.remove(aChannel.getId());
			return true;
		}
		return false;
	}

	/**
	 * removes a channel from an existing channel group.
	 * @param aChannelId
	 * @return
	 */
	public boolean removeChannel(String aChannelId) {
		if (aChannelId != null) {
			mChannels.remove(aChannelId);
			return true;
		}
		return false;
	}

	/**
	 * returns the unmodifiable map of channels in the channel group.
	 * @return
	 */
	public Map<String, Channel> getChannels() {
		return Collections.unmodifiableMap(mChannels);
	}

	/**
	 * returns the id of the channel group.
	 * @return the mId
	 */
	public String getId() {
		return mId;
	}
}
