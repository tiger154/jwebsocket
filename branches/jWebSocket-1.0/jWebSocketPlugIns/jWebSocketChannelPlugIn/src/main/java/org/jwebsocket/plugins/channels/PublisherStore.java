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

/**
 * Base interface that defines the store operations for publishers
 * @author puran
 * @version $Id: PublisherStore.java 1270 2010-12-23 08:53:06Z fivefeetfurther $
 */
public interface PublisherStore {

	/**
	 * Returns the publisher information for the given publisher id
	 * @param id the publisher id to fetch
	 * @return the publisher object, null if the publisher doesn't exist.
	 */
	Publisher getPublisher(String id);

	/**
	 * Store the given publisher in the channel store
	 * @param publisher the publisher object
	 * @return {@code true} if insert successful
	 */
	boolean storePublisher(Publisher publisher);

	/**
	 * Removes the publisher from the store based on given id
	 * @param key the key of the data to remove from the store
	 */
	void removePublisher(String id);

	/**
	 * Clears the publisher store, use this method with care since it removes
	 * all the publishers information from the store physically and cannot be
	 * rolled back.
	 */
	void clearPublishers();

	/**
	 * Returns the size of the publisher store
	 * @return the size value
	 */
	int getPublisherStoreSize();
}
