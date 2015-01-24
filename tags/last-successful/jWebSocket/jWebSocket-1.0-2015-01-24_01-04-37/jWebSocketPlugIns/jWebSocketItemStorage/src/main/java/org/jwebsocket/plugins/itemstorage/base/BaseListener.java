//	---------------------------------------------------------------------------
//	jWebSocket - BaseListener (Community Edition, CE)
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
 * @author Rolando Santamaria Maso
 */
public class BaseListener implements IItemStorageListener {

	/**
	 *
	 */
	protected IItemCollectionProvider mCollectionProvider;

	/**
	 *
	 */
	protected IItemFactory mItemFactory;

	/**
	 *
	 */
	public BaseListener() {
		mCollectionProvider = (IItemCollectionProvider) JWebSocketBeanFactory
				.getInstance(ItemStoragePlugIn.NS_ITEM_STORAGE)
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
	public void onUnsubscription(String aCollectionName, String aSubscriber, String aUser) {
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
