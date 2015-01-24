//	---------------------------------------------------------------------------
//	jWebSocket - ListenerStore (Community Edition, CE)
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
package org.jwebsocket.plugins.jms;

import java.util.Collection;
import org.jwebsocket.plugins.jms.infra.impl.JmsListenerContainer;

/**
 * Base interface that defines the store operations for JMS consumers
 *
 * @author Johannes Smutny
 */
public interface ListenerStore {

	/**
	 *
	 * @return
	 */
	Collection<JmsListenerContainer> getAll();

	/**
	 * Returns the subscriber information for the given subscriber id
	 *
	 * @param aId
	 * @return the subscriber object, null if the subscriber doesn't exist.
	 */
	JmsListenerContainer getListener(
			DestinationIdentifier aId);

	/**
	 * Store the given channel in the channel store
	 *
	 * @param aId
	 * @param aListener
	 */
	void storeListener(DestinationIdentifier aId,
			JmsListenerContainer aListener);

	/**
	 * Removes the subscriber from the store based on given id
	 *
	 *
	 * @param aId
	 */
	void removeListener(DestinationIdentifier aId);

	/**
	 * Clears the subscriber store, use this method with care since it removes
	 * all the channel information from the store physically and cannot be
	 * rolled back.
	 */
	void clearListeners();

	/**
	 * Returns the size of the subscriber store
	 *
	 * @return the size value
	 */
	int getListenersStoreSize();
}
