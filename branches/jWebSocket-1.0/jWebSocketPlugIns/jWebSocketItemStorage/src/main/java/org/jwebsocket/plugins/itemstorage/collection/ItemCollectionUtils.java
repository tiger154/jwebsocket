package org.jwebsocket.plugins.itemstorage.collection;

import java.util.Map;
import java.util.Set;
import org.jwebsocket.plugins.itemstorage.api.IItem;
import org.jwebsocket.plugins.itemstorage.api.IItemCollection;
import org.jwebsocket.plugins.itemstorage.api.IItemCollectionProvider;
import org.jwebsocket.plugins.itemstorage.api.IItemDefinition;
import org.jwebsocket.plugins.itemstorage.api.IItemFactory;
import org.jwebsocket.plugins.itemstorage.event.ItemStorageEventManager;
import org.jwebsocket.plugins.itemstorage.item.Item;
import org.springframework.util.Assert;

/**
 * This utility class wraps collection operations that are supposed to be called out of the WebSocket API
 *
 * @author kyberneees
 */
public class ItemCollectionUtils {

	public static void restartCollection(IItemCollectionProvider aProvider, IItemCollection aCollection) throws Exception {
		Set<String> lAffectedClients = aCollection.restart();
		aProvider.saveCollection(aCollection);
		ItemStorageEventManager.onCollectionRestarted(aCollection.getName(), lAffectedClients);
	}

	public static void subscribeCollection(IItemCollectionProvider aProvider, IItemCollection aCollection, String aSubscriber) throws Exception {
		aCollection.getSubcribers().add(aSubscriber);
		aProvider.saveCollection(aCollection);
		ItemStorageEventManager.onSubscription(aCollection.getName(), aSubscriber);
	}

	public static void unsubscribeCollection(IItemCollectionProvider aProvider, IItemCollection aCollection, String aSubscriber, String aUser) throws Exception {
		aCollection.getSubcribers().remove(aSubscriber);
		aProvider.saveCollection(aCollection);
		ItemStorageEventManager.onUnsubscription(aCollection.getName(), aSubscriber, aUser);
	}

	public static void authorizeCollection(IItemCollectionProvider aProvider, IItemCollection aCollection, String aPublisher) throws Exception {
		aCollection.getPublishers().add(aPublisher);
		aProvider.saveCollection(aCollection);
		ItemStorageEventManager.onAuthorization(aCollection.getName(), aPublisher);
	}

	public static IItem saveItem(String aUser, IItemFactory aItemFactory, IItemCollection aCollection, Map<String, Object> aData) throws Exception {
		// getting the item definition
		IItemDefinition lDef = aItemFactory.getDefinition(aCollection.getItemStorage().getItemType());
		// getting the item pk if exists
		String lPK = (String) aData.get(lDef.getPrimaryKeyAttribute());

		IItem lItem = null;
		boolean lIsNew = false;

		if (null != lPK) {
			lItem = aCollection.getItemStorage().findByPK(lPK);
		}
		if (null == lItem) {
			lIsNew = true;
			lItem = aItemFactory.getItemPrototype(lDef.getType());
		}

		// adjusting JSON types (special support for JavaScript)
		Item.adjustJSONTypes(aData, lItem.getDefinition());
		// setting the data on the item
		lItem.setAll(aData);
		// saving
		aCollection.getItemStorage().save(lItem);

		// getting item updates
		if (lIsNew) {
			lItem.setUpdate(lItem.getAttributes());
		} else {
			lItem.setUpdate(aData);
		}

		// notifying event
		ItemStorageEventManager.onItemSaved(aUser, lItem, aCollection);

		return lItem;
	}

	public static IItem removeItem(String aUser, IItemCollection aCollection, String aItemPK) throws Exception {
		IItem lItem = aCollection.getItemStorage().findByPK(aItemPK);
		Assert.notNull(lItem, "Item not found!");

		aCollection.getItemStorage().remove(aItemPK);
		ItemStorageEventManager.onItemRemoved(aUser, lItem, aCollection);

		return lItem;
	}
}
