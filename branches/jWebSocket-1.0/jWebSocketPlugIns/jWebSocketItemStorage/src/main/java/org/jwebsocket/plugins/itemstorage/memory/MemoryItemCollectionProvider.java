package org.jwebsocket.plugins.itemstorage.memory;

import java.util.Iterator;
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
 * @author kyberneees
 */
public class MemoryItemCollectionProvider implements IItemCollectionProvider {

	private IItemStorageProvider mItemStorageProvider;
	private static Map<String, IItemCollection> mCollections = new FastMap<String, IItemCollection>();

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

		return new ItemCollection(System.currentTimeMillis(), getItemStorageProvider()
				.getItemStorage(aName, aItemType));
	}

	@Override
	public IItemCollection getCollection(String aName) throws Exception {
		return mCollections.get(aName);
	}

	@Override
	public void removeCollection(String aName) throws Exception {
		mItemStorageProvider.removeItemStorage(aName);
		mCollections.remove(aName);
	}

	@Override
	public void saveCollection(IItemCollection aCollection) throws Exception {
		aCollection.validate();
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
		for (Iterator<IItemCollection> lIt = mCollections.values().iterator(); lIt.hasNext();) {
			IItemCollection lCollection = lIt.next();
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
		for (Iterator<IItemCollection> lIt = mCollections.values().iterator(); lIt.hasNext();) {
			IItemCollection lCollection = lIt.next();
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
		for (Iterator<IItemCollection> lIt = mCollections.values().iterator(); lIt.hasNext();) {
			IItemCollection lCollection = lIt.next();
			if (lCollection.getItemStorage().getItemType().equals(aItemType)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public long size() {
		long lSize = 0;
		for (Iterator<IItemCollection> lIt = mCollections.values().iterator(); lIt.hasNext();) {
			IItemCollection lCollection = lIt.next();
			if (!lCollection.isPrivate()) {
				lSize++;
			}
		}

		return lSize;
	}

	@Override
	public long size(String aOwner) {
		long lSize = 0;
		for (Iterator<IItemCollection> lIt = mCollections.values().iterator(); lIt.hasNext();) {
			IItemCollection lCollection = lIt.next();
			if (lCollection.getOwner().equals(aOwner)) {
				lSize++;
			}
		}

		return lSize;
	}
}
