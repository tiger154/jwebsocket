//  ---------------------------------------------------------------------------
//  jWebSocket - PublisherStore (Community Edition, CE)
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

/**
 * Base interface that defines the store operations for publishers
 *
 * @author puran, kyberneees
 * @version $Id: PublisherStore.java 1270 2010-12-23 08:53:06Z fivefeetfurther $
 */
public interface PublisherStore {

	/**
	 * Returns the publisher information for the given publisher id
	 *
	 * @param id the publisher id to fetch
	 * @return the publisher object, null if the publisher doesn't exist.
	 */
	Publisher getPublisher(String id);

	/**
	 * Store the given publisher in the channel store
	 *
	 * @param publisher the publisher object
	 * @return {@code true} if insert successful
	 */
	boolean storePublisher(Publisher publisher);

	/**
	 * Removes the publisher from the store based on given id
	 *
	 *
	 * @param id
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
	 *
	 * @return the size value
	 */
	int getPublisherStoreSize();

	/**
	 * Indicates if the publisher store contains the given publisher identifier
	 *
	 * @param aPublisherId
	 * @return TRUE if the publisher exists, FALSE otherwise
	 */
	boolean hasPublisher(String aPublisherId);
}
