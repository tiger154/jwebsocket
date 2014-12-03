//	---------------------------------------------------------------------------
//	jWebSocket - BaseListenerStore (Community Edition, CE)
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jwebsocket.plugins.jms.infra.impl.JmsListenerContainer;

/**
 * holds references to all available jms listener containers
 *
 * @author Johannes Smutny
 */
public class BaseListenerStore implements ListenerStore {

	/**
	 * logger object
	 */
	private final Map<DestinationIdentifier, JmsListenerContainer> mListeners
			= new ConcurrentHashMap<DestinationIdentifier, JmsListenerContainer>();

	@Override
	public JmsListenerContainer getListener(DestinationIdentifier aId) {
		return mListeners.get(aId);
	}

	@Override
	public void storeListener(DestinationIdentifier aId,
			JmsListenerContainer aListener) {
		mListeners.put(aId, aListener);
	}

	@Override
	public void removeListener(DestinationIdentifier aId) {
		mListeners.remove(aId);

	}

	@Override
	public void clearListeners() {
		mListeners.clear();
	}

	@Override
	public int getListenersStoreSize() {
		return mListeners.size();
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Collection<JmsListenerContainer> getAll() {
		return mListeners.values();
	}
}
