package org.jwebsocket.plugins.itemstorage.event;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javolution.util.FastList;
import org.jwebsocket.plugins.itemstorage.api.IItem;
import org.jwebsocket.plugins.itemstorage.api.IItemCollection;
import org.jwebsocket.plugins.itemstorage.api.IItemStorage;
import org.jwebsocket.plugins.itemstorage.api.IItemStorageListener;
import org.jwebsocket.plugins.itemstorage.collection.ItemCollection;

/**
 *
 * @author kyberneees
 */
public class ItemStorageEventManager {

	private static List<IItemStorageListener> mListeners = new FastList<IItemStorageListener>();
	private static ExecutorService mThreadPool = Executors.newFixedThreadPool(10);

	public static void addListener(IItemStorageListener aListener) {
		mListeners.add(aListener);
	}

	public static void removeListener(IItemStorageListener aListener) {
		mListeners.remove(aListener);
	}

	public static void onItemSaved(final String aUser, final IItem aItem, final IItemCollection aItemCollection) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					lListener.onItemSaved(aUser, aItem, aItemCollection);
				}
			});
		}
	}

	public static void onItemSaved(final IItem aItem, final IItemStorage aItemStorage) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					lListener.onItemSaved(aItem, aItemStorage);
				}
			});
		}
	}

	public static void onItemRemoved(final IItem aItem, final IItemStorage aItemStorage) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					lListener.onItemRemoved(aItem, aItemStorage);
				}
			});
		}
	}

	public static void onItemRemoved(final String aUser, final IItem aItem, final IItemCollection aItemCollection) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					lListener.onItemRemoved(aUser, aItem, aItemCollection);
				}
			});
		}
	}

	public static void onStorageCleaned(final IItemStorage aItemStorage) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					lListener.onStorageCleaned(aItemStorage);
				}
			});
		}
	}

	public static void onStorageCreated(final IItemStorage aItemStorage) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					lListener.onStorageCreated(aItemStorage);
				}
			});
		}
	}

	public static void onStorageRemoved(final String aStorageName) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			try {
				lListener.onStorageRemoved(aStorageName);
			} catch (Exception lEx) {
			}
		}
	}

	public static void onSubscription(final String aCollectionName, final String aSubscriber) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					lListener.onSubscription(aCollectionName, aSubscriber);
				}
			});
		}
	}

	public static void onUnsubscription(final String aCollectionName, final String aSubscriber, final String aUser) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					lListener.onUnsubscription(aCollectionName, aSubscriber, aUser);
				}
			});
		}
	}

	public static void onAuthorization(final String aCollectionName, final String aPublisher) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					lListener.onAuthorization(aCollectionName, aPublisher);
				}
			});
		}
	}

	public static void onCollectionRestarted(final String aCollectionName, final Set<String> aAffectedClients) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					lListener.onCollectionRestarted(aCollectionName, aAffectedClients);
				}
			});
		}
	}

	public static void onCollectionSaved(final ItemCollection aCollection) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			final IItemStorageListener lListener = lIt.next();
			mThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					lListener.onCollectionSaved(aCollection.getName(), aCollection.getSubcribers());
				}
			});
		}
	}

	public static void onBeforeSaveItem(IItem aItem, IItemStorage aStorage) throws Exception {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			IItemStorageListener lListener = lIt.next();
			lListener.onBeforeSaveItem(aItem, aStorage);
		}
	}

	public static void onBeforeCreateCollection(ItemCollection aCollection) {
		for (Iterator<IItemStorageListener> lIt = mListeners.iterator(); lIt.hasNext();) {
			IItemStorageListener lListener = lIt.next();
			lListener.onBeforeCreateCollection(aCollection);
		}
	}
}