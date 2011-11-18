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
package org.jwebsocket.plugins.channels;

/**
 * Base interface that defines the store operations for subscribers
 * @author puran
 * @version $Id: SubscriberStore.java 1275 2011-01-02 08:25:12Z fivefeetfurther $
 */
public interface SubscriberStore {

	/**
	 * Returns the subscriber information for the given subscriber id
	 * @param id the subscriber id to fetch
	 * @return the subscriber object, null if the subscriber doesn't exist.
	 */
	Subscriber getSubscriber(String id);

	/**
	 * Store the given channel in the channel store
	 * @param channel the channel object
	 * @return {@code true} if insert successful
	 */
	boolean storeSubscriber(Subscriber subscriber);

	/**
	 * Removes the subscriber from the store based on given id
	 * @param key the key of the data to remove from the store
	 */
	void removeSubscriber(String id);

	/**
	 * Clears the subscriber store, use this method with care since it removes
	 * all the channel information from the store physically and cannot be
	 * rolled back.
	 */
	void clearSubscribers();

	/**
	 * Returns the size of the subscriber store
	 * @return the size value
	 */
	int getSubscribersStoreSize();
}
