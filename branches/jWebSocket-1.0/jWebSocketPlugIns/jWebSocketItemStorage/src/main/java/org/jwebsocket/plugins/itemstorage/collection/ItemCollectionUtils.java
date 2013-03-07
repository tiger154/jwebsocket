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
import org.jwebsocket.plugins.itemstorage.item.ItemDefinition;
import org.springframework.util.Assert;

/**
 * This utility class wraps collection operations that are supposed to be called
 * out of the WebSocket API
 *
 * @author kyberneees
 */
public class ItemCollectionUtils {

	public static Set<String> restartCollection(IItemCollectionProvider aProvider, IItemCollection aCollection) throws Exception {
		Set<String> lAffectedClients = aCollection.restart();
		ItemStorageEventManager.onCollectionRestarted(aCollection.getName(), lAffectedClients);

		return lAffectedClients;
	}

	public static void subscribeCollection(IItemCollectionProvider aProvider, IItemCollection aCollection, String aSubscriber) throws Exception {
		aCollection.getSubcribers().add(aSubscriber);
		ItemStorageEventManager.onSubscription(aCollection.getName(), aSubscriber);
	}

	public static void unsubscribeCollection(IItemCollectionProvider aProvider, IItemCollection aCollection, String aSubscriber, String aUser) throws Exception {
		aCollection.getSubcribers().remove(aSubscriber);
		ItemStorageEventManager.onUnsubscription(aCollection.getName(), aSubscriber, aUser);
	}

	public static void authorizeCollection(IItemCollectionProvider aProvider, IItemCollection aCollection, String aPublisher) throws Exception {
		aCollection.getPublishers().add(aPublisher);
		ItemStorageEventManager.onAuthorization(aCollection.getName(), aPublisher);
	}

	public static IItem saveItem(String aUser, IItemFactory aItemFactory, IItemCollection aCollection, Map<String, Object> aData) throws Exception {
		// getting the item definition
		IItemDefinition lDef = aItemFactory.getDefinition(aCollection.getItemStorage().getItemType());

		// required to change the primary key value of an item
		String lTargetPK = (String) aData.remove(ItemDefinition.ATTR_INTERNAL_TARGET_PK);
		if (null == lTargetPK) {
			// target PK is the PK
			lTargetPK = (String) aData.get(lDef.getPrimaryKeyAttribute());
		}

		IItem lItem = null;
		boolean lIsNew = false;

		if (null != lTargetPK) {
			lItem = aCollection.getItemStorage().findByPK(lTargetPK);
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
		aCollection.getItemStorage().save(lTargetPK, lItem);

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
