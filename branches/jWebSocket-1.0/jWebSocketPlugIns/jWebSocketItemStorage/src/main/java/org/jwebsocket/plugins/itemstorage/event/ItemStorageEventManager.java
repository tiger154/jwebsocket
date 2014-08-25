//	---------------------------------------------------------------------------
//	jWebSocket - ItemStorageEventManager (Community Edition, CE)
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
import java.util.Set;
import javolution.util.FastList;
import org.jwebsocket.plugins.itemstorage.api.IItem;
import org.jwebsocket.plugins.itemstorage.api.IItemCollection;
import org.jwebsocket.plugins.itemstorage.api.IItemStorage;
import org.jwebsocket.plugins.itemstorage.api.IItemStorageListener;
import org.jwebsocket.plugins.itemstorage.collection.ItemCollection;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class ItemStorageEventManager {

	private static final List<IItemStorageListener> mListeners = new FastList<IItemStorageListener>();

	/**
	 *
	 * @param aListener
	 */
	public static void addListener(IItemStorageListener aListener) {
		mListeners.add(aListener);
	}

	/**
	 *
	 * @param aListener
	 */
	public static void removeListener(IItemStorageListener aListener) {
		mListeners.remove(aListener);
	}

	/**
	 *
	 * @param aUser
	 * @param aItem
	 * @param aItemCollection
	 */
	public static void onItemSaved(final String aUser, final IItem aItem, final IItemCollection aItemCollection) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			Tools.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					lListener.onItemSaved(aUser, aItem, aItemCollection);
				}
			});
		}
	}

	/**
	 *
	 * @param aItem
	 * @param aItemStorage
	 */
	public static void onItemSaved(final IItem aItem, final IItemStorage aItemStorage) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			Tools.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					lListener.onItemSaved(aItem, aItemStorage);
				}
			});
		}
	}

	/**
	 *
	 * @param aItem
	 * @param aItemStorage
	 */
	public static void onItemRemoved(final IItem aItem, final IItemStorage aItemStorage) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			Tools.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					lListener.onItemRemoved(aItem, aItemStorage);
				}
			});
		}
	}

	/**
	 *
	 * @param aUser
	 * @param aItem
	 * @param aItemCollection
	 */
	public static void onItemRemoved(final String aUser, final IItem aItem, final IItemCollection aItemCollection) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			Tools.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					lListener.onItemRemoved(aUser, aItem, aItemCollection);
				}
			});
		}
	}

	/**
	 *
	 * @param aItemStorage
	 */
	public static void onStorageCleaned(final IItemStorage aItemStorage) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			Tools.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					lListener.onStorageCleaned(aItemStorage);
				}
			});
		}
	}

	/**
	 *
	 * @param aItemStorage
	 */
	public static void onStorageCreated(final IItemStorage aItemStorage) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			Tools.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					lListener.onStorageCreated(aItemStorage);
				}
			});
		}
	}

	/**
	 *
	 * @param aStorageName
	 */
	public static void onStorageRemoved(final String aStorageName) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			try {
				lListener.onStorageRemoved(aStorageName);
			} catch (Exception lEx) {
			}
		}
	}

	/**
	 *
	 * @param aCollectionName
	 * @param aSubscriber
	 */
	public static void onSubscription(final String aCollectionName, final String aSubscriber) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			Tools.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					lListener.onSubscription(aCollectionName, aSubscriber);
				}
			});
		}
	}

	/**
	 *
	 * @param aCollectionName
	 * @param aSubscriber
	 * @param aUser
	 */
	public static void onUnsubscription(final String aCollectionName, final String aSubscriber, final String aUser) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			Tools.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					lListener.onUnsubscription(aCollectionName, aSubscriber, aUser);
				}
			});
		}
	}

	/**
	 *
	 * @param aCollectionName
	 * @param aPublisher
	 */
	public static void onAuthorization(final String aCollectionName, final String aPublisher) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			Tools.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					lListener.onAuthorization(aCollectionName, aPublisher);
				}
			});
		}
	}

	/**
	 *
	 * @param aCollectionName
	 * @param aAffectedClients
	 */
	public static void onCollectionRestarted(final String aCollectionName, final Set<String> aAffectedClients) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			Tools.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					lListener.onCollectionRestarted(aCollectionName, aAffectedClients);
				}
			});
		}
	}

	/**
	 *
	 * @param aCollection
	 */
	public static void onCollectionSaved(final ItemCollection aCollection) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			Tools.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					try {
						lListener.onCollectionSaved(aCollection.getName(), aCollection.getSubcribers().getAll());
					} catch (Exception lEx) {
					}
				}
			});
		}
	}

	/**
	 *
	 * @param aItem
	 * @param aStorage
	 * @throws Exception
	 */
	public static void onBeforeSaveItem(IItem aItem, IItemStorage aStorage) throws Exception {
		for (IItemStorageListener lListener : mListeners) {
			lListener.onBeforeSaveItem(aItem, aStorage);
		}
	}

	/**
	 *
	 * @param aCollection
	 */
	public static void onBeforeCreateCollection(ItemCollection aCollection) {
		for (IItemStorageListener lListener : mListeners) {
			lListener.onBeforeCreateCollection(aCollection);
		}
	}
}
