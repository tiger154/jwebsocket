//	---------------------------------------------------------------------------
//	jWebSocket - MemoryItemStorageProvider (Community Edition, CE)
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
package org.jwebsocket.plugins.itemstorage.memory;

import org.jwebsocket.plugins.itemstorage.api.IItemFactory;
import org.jwebsocket.plugins.itemstorage.api.IItemStorage;
import org.jwebsocket.plugins.itemstorage.api.IItemStorageProvider;
import org.jwebsocket.plugins.itemstorage.event.ItemStorageEventManager;
import org.springframework.util.Assert;

/**
 * IItemStorageProvider implementation for RAM Memory
 *
 * @author Rolando Santamaria Maso
 */
public class MemoryItemStorageProvider implements IItemStorageProvider {

	private final IItemFactory mItemFactory;

	/**
	 *
	 * @param aItemFactory
	 */
	public MemoryItemStorageProvider(IItemFactory aItemFactory) {
		mItemFactory = aItemFactory;
		Assert.notNull(mItemFactory, "Item factory cannot be null!");
	}

	@Override
	public IItemStorage getItemStorage(String aName, String aType) throws Exception {
		Assert.notNull(aName, "The storage name cannot be null!");
		Assert.notNull(aType, "The storage type cannot be null!");

		boolean lIsNew = false;
		if (!MemoryItemStorage.getContainer().containsKey(aName)) {
			lIsNew = true;
		}

		IItemStorage lStorage = new MemoryItemStorage(aName, aType, mItemFactory);
		lStorage.initialize();

		if (lIsNew) {
			ItemStorageEventManager.onStorageCreated(lStorage);
		}

		return lStorage;
	}

	@Override
	public void removeItemStorage(String aName) throws Exception {
		MemoryItemStorage.release(aName);
		ItemStorageEventManager.onStorageRemoved(aName);
	}

	@Override
	public IItemFactory getItemFactory() {
		return mItemFactory;
	}

	@Override
	public void initialize() throws Exception {
	}

	@Override
	public void shutdown() throws Exception {
	}
}
