package org.jwebsocket.plugins.itemstorage.api;

import java.util.Set;
import org.jwebsocket.plugins.itemstorage.collection.ItemCollection;

/**
 *
 * @author kyberneees
 */
public interface IItemStorageListener {

	/**
	 * Called when an item has been saved on a collection
	 *
	 * @param aUser
	 * @param aItem
	 * @param aItemStorage
	 */
	void onItemSaved(String aUser, IItem aItem, IItemCollection aItemCollection);

	/**
	 * Called when an item has been saved on a storage
	 *
	 * @param aItem
	 * @param aItemStorage
	 */
	void onItemSaved(IItem aItem, IItemStorage aItemStorage);

	/**
	 * Called when an item has been removed on a collection
	 *
	 * @param aUser
	 * @param aItem
	 * @param aItemCollection
	 */
	void onItemRemoved(String aUser, IItem aItem, IItemCollection aItemCollection);

	/**
	 * Called when an item has been removed on a storage
	 *
	 * @param aItem
	 * @param aItemStorage
	 */
	void onItemRemoved(IItem aItem, IItemStorage aItemStorage);

	/**
	 * Called when a storage has been cleaned
	 *
	 * @param aItemStorage
	 */
	void onStorageCleaned(IItemStorage aItemStorage);

	/**
	 * Called when a storage has been created
	 *
	 * @param aItemStorage
	 */
	void onStorageCreated(IItemStorage aItemStorage);

	/**
	 * Called when a storage has been removed
	 *
	 * @param aStorageName
	 */
	void onStorageRemoved(String aStorageName);

	/**
	 * Called when a new client is subscribed on a collection
	 *
	 * @param aCollectionName
	 * @param aSubscriber
	 */
	void onSubscription(String aCollectionName, String aSubscriber);

	/**
	 * Called when a new client is unsubscripted on a collection
	 *
	 * @param aCollectionName
	 * @param aSubscriber
	 */
	void onUnsubscription(String aCollectionName, String aSubscriber);

	/**
	 * Called when a new client is authorized on a collection
	 *
	 * @param aCollectionName
	 * @param aPublisher
	 */
	void onAuthorization(String aCollectionName, String aPublisher);

	/**
	 * Called when a collection has been restarted
	 *
	 * @param aCollectionName
	 * @param aAffectedClients
	 */
	void onCollectionRestarted(String aCollectionName, Set<String> aAffectedClients);

	/**
	 * Called before save an item on a storage
	 *
	 * @param aItem
	 * @param aItemStorage
	 */
	public void onBeforeSaveItem(IItem aItem, IItemStorage aItemStorage) throws Exception;

	/**
	 * Called before create an item collection
	 *
	 * @param aCollection
	 */
	public void onBeforeCreateCollection(ItemCollection aCollection);
}
