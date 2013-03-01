//  ---------------------------------------------------------------------------
//  jWebSocket - SubscriberStore (Community Edition, CE)
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

/**
 * Base interface that defines the store operations for subscribers
 *
 * @author puran, kyberneees
 * @version $Id: SubscriberStore.java 1275 2011-01-02 08:25:12Z fivefeetfurther
 * $
 */
public interface SubscriberStore {

	/**
	 * Returns the subscriber information for the given subscriber id
	 *
	 * @param id the subscriber id to fetch
	 * @return the subscriber object, null if the subscriber doesn't exist.
	 */
	Subscriber getSubscriber(String id);

	/**
	 * Store the given channel in the channel store
	 *
	 * @param channel the channel object
	 * @return {@code true} if insert successful
	 */
	boolean storeSubscriber(Subscriber subscriber);

	/**
	 * Removes the subscriber from the store based on given id
	 *
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
	 *
	 * @return the size value
	 */
	int getSubscribersStoreSize();

	/**
	 * Indicates if the subscriber store contains a subscriber with the given
	 * subscriber identifier
	 *
	 * @return TRUE if the subscriber exists, FALSE otherwise
	 */
	boolean hasSubscriber(String aSubscriberId);
}
