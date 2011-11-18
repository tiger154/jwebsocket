//  ---------------------------------------------------------------------------
//  jWebSocket - SubscriberStore
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
package org.jwebsocket.plugins.jms;

/**
 * Base interface that defines the store operations for jms consumers
 * 
 * @author Johannes Smutny
 * 
 */
import java.util.Collection;

import org.springframework.jms.core.JmsTemplate;

public interface SenderStore {

	/**
	 * Returns the subscriber information for the given subscriber id
	 * 
	 * @param id
	 *            the subscriber id to fetch
	 * @return the subscriber object, null if the subscriber doesn't exist.
	 */
	JmsTemplate getSender(DestinationIdentifier aId);

	/**
	 * Store the given channel in the channel store
	 * 
	 * @param channel
	 *            the channel object
	 * @return {@code true} if insert successful
	 */
	void storeSender(DestinationIdentifier aId, JmsTemplate aListener);

	/**
	 * Removes the subscriber from the store based on given id
	 * 
	 * @param key
	 *            the key of the data to remove from the store
	 */
	void removeSender(DestinationIdentifier aId);

	/**
	 * Clears the subscriber store, use this method with care since it removes
	 * all the channel information from the store physically and cannot be
	 * rolled back.
	 */
	void clearSenders();

	/**
	 * Returns the size of the subscriber store
	 * 
	 * @return the size value
	 */
	int getSendersStoreSize();

	Collection<JmsTemplate> getAll();
}
