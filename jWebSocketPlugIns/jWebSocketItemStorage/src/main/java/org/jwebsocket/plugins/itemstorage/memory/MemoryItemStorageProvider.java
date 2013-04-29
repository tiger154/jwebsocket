package org.jwebsocket.plugins.itemstorage.memory;

import org.jwebsocket.plugins.itemstorage.api.IItemFactory;
import org.jwebsocket.plugins.itemstorage.api.IItemStorage;
import org.jwebsocket.plugins.itemstorage.api.IItemStorageProvider;
import org.jwebsocket.plugins.itemstorage.collection.ItemCollection;
import org.jwebsocket.plugins.itemstorage.event.ItemStorageEventManager;
import org.springframework.util.Assert;

/**
 * IItemStorageProvider implementation for RAM Memory
 *
 * @author kyberneees
 */
public class MemoryItemStorageProvider implements IItemStorageProvider {

	private IItemFactory mItemFactory;

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
