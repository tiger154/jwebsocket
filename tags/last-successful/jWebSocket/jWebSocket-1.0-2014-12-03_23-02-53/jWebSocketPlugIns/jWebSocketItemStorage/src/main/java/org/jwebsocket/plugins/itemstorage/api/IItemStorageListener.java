//	---------------------------------------------------------------------------
//	jWebSocket - IItemStorageListener (Community Edition, CE)
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
package org.jwebsocket.plugins.itemstorage.api;

import java.util.List;
import java.util.Set;
import org.jwebsocket.plugins.itemstorage.collection.ItemCollection;

/**
 *
 * @author Rolando Santamaria Maso
 */
public interface IItemStorageListener {

	/**
	 * Called when an item has been saved on a collection
	 *
	 * @param aUser
	 * @param aItem
	 * @param aItemCollection
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
	 * @param aUser
	 */
	void onUnsubscription(String aCollectionName, String aSubscriber, String aUser);

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
	 * Called when a collection has been saved
	 *
	 * @param aCollectionName
	 * @param lSubscribers
	 */
	void onCollectionSaved(String aCollectionName, List<String> lSubscribers);

	/**
	 * Called before save an item on a storage
	 *
	 * @param aItem
	 * @param aItemStorage
	 * @throws java.lang.Exception
	 */
	public void onBeforeSaveItem(IItem aItem, IItemStorage aItemStorage) throws Exception;

	/**
	 * Called before create an item collection
	 *
	 * @param aCollection
	 */
	public void onBeforeCreateCollection(ItemCollection aCollection);
}
