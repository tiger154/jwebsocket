package org.jwebsocket.plugins.itemstorage.base;

import java.util.List;
import java.util.Set;
import org.jwebsocket.plugins.itemstorage.ItemStoragePlugIn;
import org.jwebsocket.plugins.itemstorage.api.IItem;
import org.jwebsocket.plugins.itemstorage.api.IItemCollection;
import org.jwebsocket.plugins.itemstorage.api.IItemCollectionProvider;
import org.jwebsocket.plugins.itemstorage.api.IItemFactory;
import org.jwebsocket.plugins.itemstorage.api.IItemStorage;
import org.jwebsocket.plugins.itemstorage.api.IItemStorageListener;
import org.jwebsocket.plugins.itemstorage.collection.ItemCollection;
import org.jwebsocket.spring.JWebSocketBeanFactory;

/**
 *
 * @author kyberneees
 */
public class BaseListener implements IItemStorageListener {

	protected IItemCollectionProvider mCollectionProvider;
	protected IItemFactory mItemFactory;

	public BaseListener() {
		mCollectionProvider = (IItemCollectionProvider) JWebSocketBeanFactory
				.getInstance(ItemStoragePlugIn.NS)
				.getBean("collectionProvider");
		mItemFactory = mCollectionProvider.getItemStorageProvider().getItemFactory();
	}

	@Override
	public void onItemSaved(String aUser, IItem aItem, IItemCollection aItemCollection) {
	}

	@Override
	public void onItemSaved(IItem aItem, IItemStorage aItemStorage) {
	}

	@Override
	public void onItemRemoved(String aUser, IItem aItem, IItemCollection aItemCollection) {
	}

	@Override
	public void onItemRemoved(IItem aItem, IItemStorage aItemStorage) {
	}

	@Override
	public void onStorageCleaned(IItemStorage aItemStorage) {
	}

	@Override
	public void onStorageCreated(IItemStorage aItemStorage) {
	}

	@Override
	public void onStorageRemoved(String aStorageName) {
	}

	@Override
	public void onSubscription(String aCollectionName, String aSubscriber) {
	}

	@Override
	public void onUnsubscription(String aCollectionName, String aSubscriber) {
	}

	@Override
	public void onAuthorization(String aCollectionName, String aPublisher) {
	}

	@Override
	public void onCollectionRestarted(String aCollectionName, Set<String> aAffectedClients) {
	}

	@Override
	public void onBeforeSaveItem(IItem aItem, IItemStorage aItemStorage) throws Exception {
	}

	@Override
	public void onBeforeCreateCollection(ItemCollection aCollection) {
	}

	@Override
	public void onCollectionSaved(String aCollectionName, List<String> aAffectedClients) {
	}
}
