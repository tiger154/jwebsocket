//  ---------------------------------------------------------------------------
//  jWebSocket - BaseSubscriberStore
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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.jms.infra.impl.JmsListenerContainer;

/**
 * holds references to all available jms listener containers
 * 
 * @author Johannes Smutny
 */
public class BaseListenerStore implements ListenerStore {

	/** logger object */
	private Logger mLog = Logging.getLogger(getClass());
	private Map<DestinationIdentifier, JmsListenerContainer> mListeners = new ConcurrentHashMap<DestinationIdentifier, JmsListenerContainer>();

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

	@Override
	public Collection<JmsListenerContainer> getAll() {
		return mListeners.values();
	}

}
