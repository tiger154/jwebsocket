//	---------------------------------------------------------------------------
//	jWebSocket - BaseSenderStore (Community Edition, CE)
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
import org.springframework.jms.core.JmsTemplate;

/**
 * holds references to all available JMS senders
 *
  * @author Johannes Smutny
*/
public class BaseSenderStore implements SenderStore {

	private Map<DestinationIdentifier, JmsTemplate> mListeners =
			new ConcurrentHashMap<DestinationIdentifier, JmsTemplate>();

	@Override
	public JmsTemplate getSender(DestinationIdentifier aId) {
		return mListeners.get(aId);
	}

	@Override
	public void storeSender(DestinationIdentifier aId, JmsTemplate aSender) {
		mListeners.put(aId, aSender);
	}

	@Override
	public void removeSender(DestinationIdentifier aId) {
		mListeners.remove(aId);

	}

	@Override
	public void clearSenders() {
		mListeners.clear();

	}

	@Override
	public int getSendersStoreSize() {
		return mListeners.size();
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Collection<JmsTemplate> getAll() {
		return mListeners.values();
	}
}
