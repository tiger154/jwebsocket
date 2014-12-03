//	---------------------------------------------------------------------------
//	jWebSocket - MemoryItemCollectionProvider (Community Edition, CE)
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

import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.plugins.itemstorage.api.IItemCollection;
import org.jwebsocket.plugins.itemstorage.api.IItemCollectionProvider;
import org.jwebsocket.plugins.itemstorage.api.IItemStorageProvider;
import org.jwebsocket.plugins.itemstorage.collection.ItemCollection;
import org.jwebsocket.plugins.itemstorage.event.ItemStorageEventManager;
import org.springframework.util.Assert;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class MemoryItemCollectionProvider implements IItemCollectionProvider {

	private final IItemStorageProvider mItemStorageProvider;
	private static final Map<String, IItemCollection> mCollections = new FastMap<String, IItemCollection>();

	/**
	 *
	 * @param aItemStorageProvider
	 */
	public MemoryItemCollectionProvider(IItemStorageProvider aItemStorageProvider) {
		mItemStorageProvider = aItemStorageProvider;
		Assert.notNull(mItemStorageProvider, "Item storage provider cannot be null!");
	}

	@Override
	public IItemStorageProvider getItemStorageProvider() {
		return mItemStorageProvider;
	}

	@Override
	public IItemCollection getCollection(String aName, String aItemType) throws Exception {
		if (mCollections.containsKey(aName)) {
			return getCollection(aName);
		}

		Assert.isTrue(aName.matches(ItemCollection.COLLECTION_NAME_REGEXP),
				"The storage name is invalid!");
		ItemCollection lCollection = new ItemCollection(System.currentTimeMillis(), getItemStorageProvider()
				.getItemStorage(aName, aItemType), new MemoryClientCollection(), new MemoryClientCollection());
		saveCollection(lCollection);

		return lCollection;
	}

	@Override
	public IItemCollection getCollection(String aName) throws Exception {
		return mCollections.get(aName);
	}

	@Override
	public synchronized void removeCollection(String aName) throws Exception {
		mItemStorageProvider.removeItemStorage(aName);
		mCollections.remove(aName);
	}

	@Override
	public synchronized void saveCollection(IItemCollection aCollection) throws Exception {
		boolean lNew = false;

		// notify event
		if (!collectionExists(aCollection.getName())) {
			lNew = true;
			ItemStorageEventManager.onBeforeCreateCollection((ItemCollection) aCollection);
		}
		mCollections.put(aCollection.getName(), aCollection);

		// notify event
		if (!lNew) {
			ItemStorageEventManager.onCollectionSaved((ItemCollection) aCollection);
		}
	}

	@Override
	public List<String> collectionNames() {
		return new FastList<String>(mCollections.keySet());
	}

	@Override
	public List<String> collectionPublicNames(int aOffset, int aLength) {
		FastList<String> lNames = new FastList<String>();
		for (IItemCollection lCollection : mCollections.values()) {
			if (!lCollection.isPrivate()) {
				if (0 == aOffset && aLength > 0) {
					lNames.add(lCollection.getName());
					aLength--;

					if (0 == aLength) {
						break;
					}
				} else {
					aOffset--;
				}
			}
		}

		return lNames;
	}

	@Override
	public List<String> collectionNamesByOwner(String aOwner, int aOffset, int aLength) {
		FastList<String> lNames = new FastList<String>();
		for (IItemCollection lCollection : mCollections.values()) {
			if (lCollection.getOwner().equals(aOwner)) {
				if (0 == aOffset && aLength > 0) {
					lNames.add(lCollection.getName());
					aLength--;

					if (0 == aLength) {
						break;
					}
				} else {
					aOffset--;
				}
			}
		}

		return lNames;
	}

	@Override
	public Boolean collectionExists(String aCollectionName) {
		return mCollections.containsKey(aCollectionName);
	}

	@Override
	public void initialize() throws Exception {
	}

	@Override
	public void shutdown() throws Exception {
	}

	@Override
	public boolean isItemTypeInUse(String aItemType) {
		for (IItemCollection lCollection : mCollections.values()) {
			if (lCollection.getItemStorage().getItemType().equals(aItemType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public long size() {
		long lSize = 0;
		for (IItemCollection lCollection : mCollections.values()) {
			if (!lCollection.isPrivate()) {
				lSize++;
			}
		}
		return lSize;
	}

	@Override
	public long size(String aOwner) {
		long lSize = 0;
		for (IItemCollection lCollection : mCollections.values()) {
			if (lCollection.getOwner().equals(aOwner)) {
				lSize++;
			}
		}
		return lSize;
	}
}
