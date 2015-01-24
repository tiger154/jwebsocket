//	---------------------------------------------------------------------------
//	jWebSocket - ListenersRegistrator (Community Edition, CE)
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
package org.jwebsocket.plugins.itemstorage.event;

import java.util.Iterator;
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.plugins.itemstorage.api.IItemStorageListener;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class ListenersRegistrator {

	private List<IItemStorageListener> mListeners = new FastList<IItemStorageListener>();

	/**
	 *
	 * @return
	 */
	public List<IItemStorageListener> getListeners() {
		return mListeners;
	}

	/**
	 *
	 * @param aListeners
	 */
	public void setListeners(List<IItemStorageListener> aListeners) {
		mListeners = aListeners;
	}

	/**
	 *
	 */
	public void registerAll() {
		for (IItemStorageListener lListener : mListeners) {
			ItemStorageEventManager.addListener(lListener);
		}
	}
}
