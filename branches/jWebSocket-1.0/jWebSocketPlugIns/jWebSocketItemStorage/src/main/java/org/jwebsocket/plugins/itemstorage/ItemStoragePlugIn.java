//	---------------------------------------------------------------------------
//	jWebSocket - ItemStoragePlugIn (Community Edition, CE)
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
package org.jwebsocket.plugins.itemstorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPlugInChain;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.plugins.annotations.Role;
import org.jwebsocket.plugins.itemstorage.api.IItem;
import org.jwebsocket.plugins.itemstorage.api.IItemCollection;
import org.jwebsocket.plugins.itemstorage.api.IItemCollectionProvider;
import org.jwebsocket.plugins.itemstorage.api.IItemFactory;
import org.jwebsocket.plugins.itemstorage.api.IItemStorage;
import org.jwebsocket.plugins.itemstorage.api.ILogsManager;
import org.jwebsocket.plugins.itemstorage.base.BaseListener;
import org.jwebsocket.plugins.itemstorage.base.BaseLogsManager;
import org.jwebsocket.plugins.itemstorage.collection.ItemCollection;
import org.jwebsocket.plugins.itemstorage.collection.ItemCollectionUtils;
import org.jwebsocket.plugins.itemstorage.event.ItemStorageEventManager;
import org.jwebsocket.plugins.itemstorage.event.ListenersRegistrator;
import org.jwebsocket.plugins.itemstorage.item.ItemDefinition;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.ConnectionManager;
import org.jwebsocket.util.Tools;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;

/**
 * The item storage plug-in allows to clients and internal applications the
 * management of item collections. The plug-in provides support for almost all
 * "storage" requirements on cloud platforms or services. This plug-in in
 * combination with the FileSystem and Channels plug-in guarantee major common
 * cloud platforms requirements.
 *
 * @author Rolando Santamaria Maso
 */
public class ItemStoragePlugIn extends ActionPlugIn {

	/**
	 * PlugIn logger
	 */
	Logger mLog = Logging.getLogger();
	/**
	 * PlugIn constants
	 */
	public static final String ATTR_COLLECTION_NAME = "collectionName";
	/**
	 *
	 */
	public static final String ATTR_ITEM_PK = "itemPK";
	/**
	 *
	 */
	public static final String ATTR_ITEM_TYPE = "itemType";
	/**
	 *
	 */
	public static final String ATTR_ACCESS_PASSWORD
			= ItemCollection.ATTR_ACCESS_PASSWORD;
	/**
	 *
	 */
	public static final String ATTR_SECRET_PASSWORD
			= ItemCollection.ATTR_SECRET_PASSWORD;
	/**
	 *
	 */
	public static final String ATTR_NEW_SECRET_PASSWORD = "newSecretPassword";
	/**
	 *
	 */
	public static final String ATTR_ITEM = "item";
	/**
	 *
	 */
	public static final String ATTR_OFFSET = "offset";
	/**
	 *
	 */
	public static final String ATTR_LENGTH = "length";
	/**
	 *
	 */
	public static final String ATTR_PRIVATE = ItemCollection.ATTR_IS_PRIVATE;
	/**
	 *
	 */
	public static final String ATTR_CAPPED = ItemCollection.ATTR_CAPPED;
	/**
	 *
	 */
	public static final String ATTR_CAPACITY = ItemCollection.ATTR_CAPACITY;
	/**
	 *
	 */
	public static final String ATTR_EXISTS = "exists";
	/**
	 *
	 */
	public static final String NS_ITEM_STORAGE
			= JWebSocketServerConstants.NS_BASE + ".plugins.itemstorage";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ItemStoragePlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket ItemStoragePlugIn - Community Edition";
	/**
	 * PlugIn dependencies
	 */
	protected IItemCollectionProvider mCollectionProvider;
	/**
	 *
	 */
	protected IItemFactory mItemFactory;
	/**
	 *
	 */
	protected ILogsManager mLogsManager;
	/**
	 *
	 */
	protected int mFragmentSize;
	/**
	 *
	 */
	protected BeanFactory mBeanFactory;

	@Override
	public void setPlugInChain(WebSocketPlugInChain aPlugInChain) {
		super.setPlugInChain(aPlugInChain);

	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getVendor() {
		return VENDOR;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getLicense() {
		return LICENSE;
	}

	@Override
	public String getNamespace() {
		return NS_ITEM_STORAGE;
	}

	/**
	 *
	 * @param aConfiguration
	 */
	public ItemStoragePlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(NS_ITEM_STORAGE);

		mFragmentSize = Integer.parseInt(aConfiguration.getString("fragmentSize"));
		mBeanFactory = getConfigBeanFactory(NS_ITEM_STORAGE);

		// check database connection if exists
		ConnectionManager lConnManager = (ConnectionManager) JWebSocketBeanFactory.getInstance()
				.getBean(JWebSocketServerConstants.CONNECTION_MANAGER_BEAN_ID);
		if (lConnManager.containsConnection(NS_ITEM_STORAGE)) {
			Assert.isTrue(lConnManager.isValid(NS_ITEM_STORAGE),
					"ItemStorage database connection is not valid!");
		}

		mCollectionProvider = (IItemCollectionProvider) mBeanFactory.getBean("collectionProvider");
		mLogsManager = new BaseLogsManager();
		mItemFactory = mCollectionProvider.getItemStorageProvider().getItemFactory();

		// registering extension listeners
		mBeanFactory.getBean(ListenersRegistrator.class).registerAll();

		if (mLog.isInfoEnabled()) {
			mLog.info("ItemStorage plug-in successfully instantiated.");
		}

		final IItemCollectionProvider lCollectionProvider = mCollectionProvider;
		ItemStorageEventManager.addListener(new BaseListener() {
			@Override
			public void onBeforeSaveItem(IItem aItem, IItemStorage aItemStorage) throws Exception {
				if (lCollectionProvider.collectionExists(aItemStorage.getName())) {
					IItemCollection lCollection = lCollectionProvider.getCollection(aItemStorage.getName());
					if (lCollection.getCapacity() > 0 && !aItemStorage.exists(aItem.getPK())) { // item is new in collection
						if (aItemStorage.size() >= lCollection.getCapacity()) {
							if (lCollection.isCapped()) {
								// if the collection is capped remove the first element
								aItemStorage.remove(aItemStorage.list(0, 1).get(0).getPK());
							}
						}
						Assert.isTrue(aItemStorage.size() < lCollection.getCapacity(),
								"The item collection is full of capacity, no new items can be saved!");
					}
				}
			}

			@Override
			public void onItemSaved(String aUser, IItem aItem, IItemCollection aItemCollection) {
				notifyGenericEvent("itemSaved", aItemCollection.getName(), aUser, aItem);

				try {
					// log action
					Map<String, Object> lLog = BaseLogsManager.createActionPrototype(
							BaseLogsManager.ETYPE_ITEM,
							aItem.getPK(), "save", aUser, aItem.getUpdate().toString());
					lLog.put(BaseLogsManager.ATTR_COLLECTION, aItemCollection.getName());
					mLogsManager.logAction(lLog);
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "logging itemSaved action..."));
				}
			}

			@Override
			public void onItemRemoved(String aUser, IItem aItem, IItemCollection aItemCollection) {
				notifyGenericEvent("itemRemoved", aItemCollection.getName(), aUser, aItem);

				try {
					mLogsManager.clearItemLogs(aItemCollection.getName(), aItem.getPK());
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "cleaning item(" + aItem.getPK() + ") logs..."));
				}
			}

			@Override
			public void onStorageCleaned(IItemStorage aItemStorage) {
				// ensure that the storage represents a collection
				try {
					if (lCollectionProvider.collectionExists(aItemStorage.getName())) {
						notifyGenericEvent("collectionCleaned", aItemStorage.getName());

						try {
							mLogsManager.clearItemLogs(aItemStorage.getName());
						} catch (Exception lEx) {
							mLog.error(Logging.getSimpleExceptionMessage(lEx,
									"cleaning collection(" + aItemStorage.getName() + ") items logs..."));
						}
					}
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx,
							"processing (" + aItemStorage.getName() + ") + storage cleaned..."));
				}
			}

			@Override
			public void onStorageRemoved(String aStorageName) {
				// ensure that the storage represents a collection
				try {
					if (lCollectionProvider.collectionExists(aStorageName)) {
						notifyGenericEvent("collectionRemoved", aStorageName);

						try {
							mLogsManager.clearItemLogs(aStorageName);
							mLogsManager.clearCollectionLogs(aStorageName);
						} catch (Exception lEx) {
							mLog.error(Logging.getSimpleExceptionMessage(lEx,
									"cleaning collection(" + aStorageName + ") + items logs..."));
						}
					}
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx,
							"processing (" + aStorageName + ") + storage removed..."));
				}
			}

			@Override
			public void onAuthorization(String aCollectionName, String aPublisher) {
				String lUser = getConnector(aPublisher).getUsername();
				notifyGenericEvent("authorization", aCollectionName, lUser);

				try {
					// log action
					Map<String, Object> lLog = BaseLogsManager.createActionPrototype(
							BaseLogsManager.ETYPE_COLLECTION,
							aCollectionName, "authorize", lUser);
					mLogsManager.logAction(lLog);
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "logging authorization action..."));
				}
			}

			@Override
			public void onSubscription(String aCollectionName, String aSubscriber) {
				String lUser = getConnector(aSubscriber).getUsername();
				notifyGenericEvent("subscription", aCollectionName, lUser);

				try {
					// log action
					Map<String, Object> lLog = BaseLogsManager.createActionPrototype(
							BaseLogsManager.ETYPE_COLLECTION,
							aCollectionName, "subscribe", lUser);
					mLogsManager.logAction(lLog);
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "logging subscription action..."));
				}
			}

			@Override
			public void onUnsubscription(String aCollectionName, String aSubscriber, String aUser) {
				notifyGenericEvent("unsubscription", aCollectionName, aUser);

				try {
					// log action
					Map<String, Object> lLog = BaseLogsManager.createActionPrototype(
							BaseLogsManager.ETYPE_COLLECTION,
							aCollectionName, "unsubscribe", aUser);
					mLogsManager.logAction(lLog);
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "logging unsubscription action..."));
				}
			}

			@Override
			public void onCollectionRestarted(String aCollectionName, Set<String> aAffectedClients) {
				String lEventName = "collectionRestarted";
				Token lEvent = TokenFactory.createToken(NS_ITEM_STORAGE, "event");
				lEvent.setString("name", lEventName);
				lEvent.setString(ATTR_COLLECTION_NAME, aCollectionName);
				for (String lClient : aAffectedClients) {
					WebSocketConnector lConnector = getConnector(lClient);
					if (null != lConnector) {
						// checking per user events notification configuration
						String lExcludedEvents = (String) getConfigParam(lConnector, "events.exclude", "");
						if (lExcludedEvents.contains("," + lEventName)) {
							continue;
						}

						sendTokenFragmented(lConnector, lEvent, mFragmentSize);
					}
				}
			}

			@Override
			public void onCollectionSaved(String aCollectionName, List<String> aSubscribers) {
				String lEventName = "collectionSaved";
				Token lEvent = TokenFactory.createToken(NS_ITEM_STORAGE, "event");
				lEvent.setString("name", lEventName);
				lEvent.setString(ATTR_COLLECTION_NAME, aCollectionName);
				for (String lClient : aSubscribers) {
					WebSocketConnector lConnector = getConnector(lClient);
					if (null != lConnector) {
						// checking per user events notification configuration
						String lExcludedEvents = (String) getConfigParam(lConnector, "events.exclude", "");
						if (lExcludedEvents.contains("," + lEventName)) {
							continue;
						}

						sendTokenFragmented(lConnector, lEvent, mFragmentSize);
					}
				}
			}
		});
	}

	/**
	 *
	 * @param aEventName
	 * @param aCollectionName
	 * @param aParam
	 */
	protected void notifyGenericEvent(String aEventName, String aCollectionName, Object... aParam) {
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Notifying '" + aEventName + "' event ...");
			}

			IItemCollection lCollection = mCollectionProvider.getCollection(aCollectionName);
			Token lEvent = TokenFactory.createToken(NS_ITEM_STORAGE, "event");
			lEvent.setString("name", aEventName);
			lEvent.setString(ATTR_COLLECTION_NAME, aCollectionName);
			if (aEventName.startsWith(ATTR_ITEM) || "authorization".equals(aEventName) || aEventName.endsWith("subscription")) {
				lEvent.setString("user", aParam[0].toString());
			}

			for (String lSubscriber : lCollection.getSubcribers().getAll()) {
				WebSocketConnector lConnector = getConnector(lSubscriber);
				if (null != lConnector) {
					// checking per user events notification configuration
					String lExcludedEvents = (String) getConfigParam(lConnector, "events.exclude", "");
					if (lExcludedEvents.contains("," + aEventName)) {
						continue;
					}

					if (aEventName.startsWith("item")) {
						Boolean lItemUpdateOnly = (Boolean) getConfigParam(lConnector, "events.itemUpdateOnly", false);
						if (lItemUpdateOnly) {
							lEvent.setMap("item", ((IItem) aParam[1]).getUpdate());
						} else {
							lEvent.setToken("item", ((IItem) aParam[1]));
						}
					}

					sendTokenFragmented(lConnector, lEvent, mFragmentSize);
				}
			}
		} catch (Exception lEx) {
		}
	}

	@Override
	public void systemStarting() throws Exception {
	}

	@Override
	public void systemStopped() throws Exception {
	}

	@Override
	public void processLogoff(WebSocketConnector aConnector) {
		unsubscribeFromAll(aConnector);
	}

	/**
	 * Un-subscribe the client from all subscribed collections
	 *
	 * @param aConnector
	 */
	void unsubscribeFromAll(WebSocketConnector aConnector) {
		try {
			for (String lCollectionName : mCollectionProvider.collectionNames()) {
				try {
					IItemCollection lCollection = mCollectionProvider.getCollection(lCollectionName);
					String lClient = aConnector.getId();

					// removing subscriber
					if (lCollection.getSubcribers().contains(lClient)) {
						ItemCollectionUtils.unsubscribeCollection(mCollectionProvider,
								lCollection,
								lClient,
								aConnector.getUsername());
					}

					// removing publisher
					if (lCollection.getPublishers().contains(lClient)) {
						lCollection.getPublishers().remove(lClient);
						mCollectionProvider.saveCollection(lCollection);
					}
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx,
							"unsubscribing  connector..."));
				}
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx,
					"processing unsubscribe from all..."));
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		unsubscribeFromAll(aConnector);
	}

	/**
	 * Create an item collection
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".write_collection")
	public void createCollectionAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);
		String lType = aToken.getString(ATTR_ITEM_TYPE);
		String lSecretPassword = aToken.getString(ATTR_SECRET_PASSWORD);
		String lAccessPassword = aToken.getString(ATTR_ACCESS_PASSWORD);
		Boolean lIsPrivate = aToken.getBoolean(ATTR_PRIVATE, true);
		Boolean lIsCapped = aToken.getBoolean(ATTR_CAPPED, false);
		Integer lCapacity = aToken.getInteger(ATTR_CAPACITY, 0);

		Assert.isTrue(!mCollectionProvider.collectionExists(lCollectionName),
				"A collection with the same name already exists!");

		IItemCollection lCollection = mCollectionProvider.getCollection(lCollectionName, lType);
		lCollection.setAccessPassword(lAccessPassword);
		lCollection.setSecretPassword(lSecretPassword);
		lCollection.setPrivate(lIsPrivate);
		lCollection.setOwner(aConnector.getUsername());
		if (lCapacity > 0) {
			lCollection.setCapacity(lCapacity);
			lCollection.setCapped(lIsCapped);
		}

		mCollectionProvider.saveCollection(lCollection);

		// log action
		HashMap lMapCollection = new HashMap();
		lCollection.toMap(lMapCollection);
		lMapCollection.remove(ItemCollection.ATTR_ACCESS_PASSWORD);
		lMapCollection.remove(ItemCollection.ATTR_SECRET_PASSWORD);
		Map<String, Object> lLog = BaseLogsManager.createActionPrototype(
				BaseLogsManager.ETYPE_COLLECTION,
				lCollectionName, "save", aConnector.getUsername(), lMapCollection.toString());
		mLogsManager.logAction(lLog);

		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Restart a collection. When a collection is restarted all subscribers and
	 * publishers are removed.
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".write_collection")
	public void restartCollectionAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);
		String lSecretPwd = aToken.getString(ATTR_SECRET_PASSWORD, "");

		Assert.isTrue(mCollectionProvider.collectionExists(lCollectionName),
				"A collection with name '" + lCollectionName + "' does not exists!");
		IItemCollection lCollection = mCollectionProvider.getCollection(lCollectionName);

		Assert.isTrue(lCollection.getOwner().equals(aConnector.getUsername()),
				"The collection can be removed by owner only!");
		Assert.isTrue((lCollection.getSecretPassword() == null
				? "".equals(lSecretPwd)
				: lCollection.getSecretPassword().equals(Tools.getMD5(lSecretPwd))),
				"The given collection secret password is not correct!");

		Integer lUsers = ItemCollectionUtils.restartCollection(mCollectionProvider, lCollection).size();

		Map<String, Object> lLog = BaseLogsManager.createActionPrototype(
				BaseLogsManager.ETYPE_COLLECTION,
				lCollectionName, "restart", aConnector.getUsername(), lUsers + " users");
		mLogsManager.logAction(lLog);

		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Remove an item collection
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".write_collection")
	public void removeCollectionAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);
		String lSecretPwd = aToken.getString(ATTR_SECRET_PASSWORD, "");

		Assert.isTrue(mCollectionProvider.collectionExists(lCollectionName),
				"A collection with name '" + lCollectionName + "' does not exists!");
		IItemCollection lCollection = mCollectionProvider.getCollection(lCollectionName);

		Assert.isTrue(lCollection.getOwner().equals(aConnector.getUsername()),
				"The collection can be removed by owner only!");
		Assert.isTrue((lCollection.getSecretPassword() == null
				? "".equals(lSecretPwd)
				: lCollection.getSecretPassword().equals(Tools.getMD5(lSecretPwd))),
				"The given collection secret password is not correct!");

		mCollectionProvider.removeCollection(lCollectionName);

		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Clear collection items
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".write_item")
	public void clearCollectionAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);
		String lSecretPwd = aToken.getString(ATTR_SECRET_PASSWORD, "");

		Assert.isTrue(mCollectionProvider.collectionExists(lCollectionName),
				"A collection with name '" + lCollectionName + "' does not exists!");

		IItemCollection lCollection = mCollectionProvider.getCollection(lCollectionName);

		Assert.isTrue((lCollection.getSecretPassword() == null
				? "".equals(lSecretPwd)
				: lCollection.getSecretPassword().equals(Tools.getMD5(lSecretPwd))),
				"The given collection secret password is not correct!");

		Integer lExistingItems = lCollection.getItemStorage().size();
		lCollection.getItemStorage().clear();

		// log action
		Map<String, Object> lLog = BaseLogsManager.createActionPrototype(
				BaseLogsManager.ETYPE_COLLECTION,
				lCollectionName, "clear", aConnector.getUsername(), lExistingItems + " items");
		mLogsManager.logAction(lLog);

		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Indicates if a collection exists
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".read_collection")
	public void existsCollectionAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);

		Token lResponse = createResponse(aToken);
		lResponse.setBoolean(ATTR_EXISTS, mCollectionProvider.collectionExists(lCollectionName));

		sendToken(aConnector, lResponse);
	}

	/**
	 * Edit a collection configuration
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".write_collection")
	public void editCollectionAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);
		String lSecretPwd = aToken.getString(ATTR_SECRET_PASSWORD, "");

		Assert.isTrue(mCollectionProvider.collectionExists(lCollectionName),
				"A collection with name '" + lCollectionName + "' does not exists!");

		IItemCollection lCollection = mCollectionProvider.getCollection(lCollectionName);

		Assert.isTrue(lCollection.getOwner().equals(aConnector.getUsername()),
				"The collection can be edited by owner only!");

		Assert.isTrue((lCollection.getSecretPassword() == null
				? "".equals(lSecretPwd)
				: lCollection.getSecretPassword().equals(Tools.getMD5(lSecretPwd))),
				"The given collection secret password is not correct!");

		if (aToken.getMap().containsKey(ATTR_NEW_SECRET_PASSWORD)) {
			String lNewSecretPassword = aToken.getString(ATTR_NEW_SECRET_PASSWORD);
			lCollection.setSecretPassword(lNewSecretPassword);
		}
		if (aToken.getMap().containsKey(ATTR_ACCESS_PASSWORD)) {
			String lAccessPassword = aToken.getString(ATTR_ACCESS_PASSWORD);
			lCollection.setAccessPassword(lAccessPassword);
		}
		if (aToken.getMap().containsKey(ATTR_PRIVATE)) {
			Boolean lPrivate = aToken.getBoolean(ATTR_PRIVATE);
			lCollection.setPrivate(lPrivate);
		}
		if (aToken.getMap().containsKey(ATTR_CAPACITY)) {
			Integer lCapacity = aToken.getInteger(ATTR_CAPACITY);
			lCollection.setCapacity(lCapacity);
		}
		if (aToken.getMap().containsKey(ATTR_CAPPED)) {
			Boolean lIsCapped = aToken.getBoolean(ATTR_CAPPED);
			lCollection.setCapped(lIsCapped);
		}

		// saving collection changes
		mCollectionProvider.saveCollection(lCollection);

		// log action
		HashMap lMapCollection = new HashMap();
		lCollection.toMap(lMapCollection);
		lMapCollection.remove(ItemCollection.ATTR_ACCESS_PASSWORD);
		lMapCollection.remove(ItemCollection.ATTR_SECRET_PASSWORD);
		Map<String, Object> lLog = BaseLogsManager.createActionPrototype(
				BaseLogsManager.ETYPE_COLLECTION,
				lCollectionName, "save", aConnector.getUsername(), lMapCollection.toString());
		mLogsManager.logAction(lLog);

		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Get collection names
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".read_collection")
	public void getCollectionNamesAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		int lOffset = aToken.getInteger(ATTR_OFFSET, 0);
		int lLength = aToken.getInteger(ATTR_LENGTH, 10);

		Token lResponse = createResponse(aToken);

		if (aToken.getBoolean("userOnly", false)) {
			lResponse.setList("data", mCollectionProvider.collectionNamesByOwner(aConnector.getUsername(), lOffset, lLength));
			lResponse.setLong("total", mCollectionProvider.size(aConnector.getUsername()));
		} else {
			lResponse.setList("data", mCollectionProvider.collectionPublicNames(lOffset, lLength));
			lResponse.setLong("total", mCollectionProvider.size());
		}

		sendTokenFragmented(aConnector, lResponse, mFragmentSize);
	}

	/**
	 * Find a collection and get public information from it
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".read_collection")
	public void findCollectionAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);
		Token lResponse = createResponse(aToken);
		Map<String, Object> lInfo = null;

		IItemCollection lCollection;
		if (mCollectionProvider.collectionExists(lCollectionName)) {
			lCollection = mCollectionProvider.getCollection(lCollectionName);

			lInfo = new HashMap<String, Object>();
			// getting collection info
			lInfo.put(ItemCollection.ATTR_NAME, lCollectionName);
			lInfo.put(ItemDefinition.ATTR_TYPE, lCollection.getItemStorage().getItemType());
			lInfo.put(ItemCollection.ATTR_IS_PRIVATE, lCollection.isPrivate());
			lInfo.put(ItemCollection.ATTR_IS_SYSTEM, lCollection.isSystem());
			lInfo.put(ItemCollection.ATTR_CREATED_AT, lCollection.createdAt());
			lInfo.put(ItemCollection.ATTR_OWNER, lCollection.getOwner());
			lInfo.put("size", lCollection.getItemStorage().size());
			lInfo.put(ItemCollection.ATTR_SUBSCRIBERS, lCollection.getSubcribers().size());
			lInfo.put(ItemCollection.ATTR_PUBLISHERS, lCollection.getPublishers().size());
			lInfo.put(ItemCollection.ATTR_CAPACITY, lCollection.getCapacity());
			lInfo.put(ItemCollection.ATTR_CAPPED, lCollection.isCapped());
			lInfo.put("authorized", lCollection.getPublishers().contains(aConnector.getId()));
			lInfo.put("subscribed", lCollection.getSubcribers().contains(aConnector.getId()));
		}

		// setting info in the response
		lResponse.setMap("data", lInfo);
		sendTokenFragmented(aConnector, lResponse, mFragmentSize);
	}

	/**
	 * Subscribe to a collection
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".read_collection")
	public void subscribeAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);
		Assert.isTrue(mCollectionProvider.collectionExists(lCollectionName),
				"A collection with name '" + lCollectionName + "' does not exists!");

		String lAccessPassword = aToken.getString(ATTR_ACCESS_PASSWORD);
		Assert.notNull(lAccessPassword, "The collection access password argument is missing!");

		IItemCollection lCollection = mCollectionProvider.getCollection(lCollectionName);

		Assert.isTrue((lCollection.getAccessPassword() == null
				? "".equals(lAccessPassword)
				: lCollection.getAccessPassword().equals(Tools.getMD5(lAccessPassword))),
				"The given collection access password is not correct!");

		Assert.isTrue(!lCollection.getSubcribers().contains(aConnector.getId()),
				"The client is subscribed already!");

		ItemCollectionUtils.subscribeCollection(mCollectionProvider, lCollection, aConnector.getId());

		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Un-subscribe from a collection
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".read_collection")
	public void unsubscribeAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);
		Assert.isTrue(mCollectionProvider.collectionExists(lCollectionName),
				"A collection with name '" + lCollectionName + "' does not exists!");

		IItemCollection lCollection = mCollectionProvider.getCollection(lCollectionName);

		Assert.isTrue(lCollection.getSubcribers().contains(aConnector.getId()),
				"The client is not subscribed!");

		ItemCollectionUtils.unsubscribeCollection(mCollectionProvider,
				lCollection,
				aConnector.getId(),
				aConnector.getUsername());

		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Authorize to a collection
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".read_collection")
	public void authorizeAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);
		Assert.isTrue(mCollectionProvider.collectionExists(lCollectionName),
				"A collection with name '" + lCollectionName + "' does not exists!");

		String lSecretPwd = aToken.getString(ATTR_SECRET_PASSWORD);
		Assert.notNull(lSecretPwd, "The collection secret password argument is missing!");

		IItemCollection lCollection = mCollectionProvider.getCollection(lCollectionName);

		Assert.isTrue((lCollection.getSecretPassword() == null
				? "".equals(lSecretPwd)
				: lCollection.getSecretPassword().equals(Tools.getMD5(lSecretPwd))),
				"The given collection secret password is not correct!");

		Assert.isTrue(!lCollection.getPublishers().contains(aConnector.getId()),
				"The client is authorized already!");

		ItemCollectionUtils.authorizeCollection(mCollectionProvider, lCollection, aConnector.getId());

		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Save an item to a collection
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".write_item")
	public void saveItemAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);
		Assert.isTrue(mCollectionProvider.collectionExists(lCollectionName),
				"A collection with name '" + lCollectionName + "' does not exists!");

		IItemCollection lCollection = mCollectionProvider.getCollection(lCollectionName);

		Assert.isTrue(lCollection.getPublishers().contains(aConnector.getId()),
				"The client is not authorized on the target collection!");

		Map<String, Object> lData = aToken.getMap(ATTR_ITEM);
		Assert.notNull(lData, "The item argument is missing!");

		IItem lSaved = ItemCollectionUtils.saveItem(aConnector.getUsername(), lCollection, lData);

		Token lResponse = createResponse(aToken);
		lResponse.setString(ATTR_ITEM_PK, lSaved.getPK());
		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Remove an item from a collection
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".write_item")
	public void removeItemAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);
		Assert.isTrue(mCollectionProvider.collectionExists(lCollectionName),
				"A collection with name '" + lCollectionName + "' does not exists!");

		IItemCollection lCollection = mCollectionProvider.getCollection(lCollectionName);

		Assert.isTrue(lCollection.getPublishers().contains(aConnector.getId()),
				"The client is not authorized on the target collection!");

		String lPK = aToken.getString(ATTR_ITEM_PK);
		Assert.notNull(lPK, "The item PK argument is missing!");

		ItemCollectionUtils.removeItem(aConnector.getUsername(), lCollection, lPK);

		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Find an item by it ATTR_PK on a collection
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".read_item")
	public void findItemByPKAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);
		Assert.isTrue(mCollectionProvider.collectionExists(lCollectionName),
				"A collection with name '" + lCollectionName + "' does not exists!");

		IItemCollection lCollection = mCollectionProvider.getCollection(lCollectionName);

		Assert.isTrue(lCollection.getSubcribers().contains(aConnector.getId()),
				"The client is not subscribed on the target collection!");

		String lPK = aToken.getString(ATTR_ITEM_PK);
		Assert.notNull(lPK, "The item PK argument is missing!");

		Token lResponse = createResponse(aToken);
		IItem lItem = lCollection.getItemStorage().findByPK(lPK);
		lResponse.setToken("data", lItem);

		sendTokenFragmented(aConnector, lResponse, mFragmentSize);
	}

	/**
	 * Indicates if an item exists on a target collection
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".read_item")
	public void existsItemAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);
		Assert.isTrue(mCollectionProvider.collectionExists(lCollectionName),
				"A collection with name '" + lCollectionName + "' does not exists!");

		IItemCollection lCollection = mCollectionProvider.getCollection(lCollectionName);

		Assert.isTrue(lCollection.getSubcribers().contains(aConnector.getId()),
				"The client is not subscribed on the target collection!");

		String lPK = aToken.getString(ATTR_ITEM_PK);
		Assert.notNull(lPK, "The item PK argument is missing!");

		Token lResponse = createResponse(aToken);
		lResponse.setBoolean(ATTR_EXISTS, lCollection.getItemStorage().exists(lPK));

		sendToken(aConnector, lResponse);
	}

	/**
	 * List all items on a collection
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".read_item")
	public void listItemsAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lCollectionName = aToken.getString(ATTR_COLLECTION_NAME);
		Assert.isTrue(mCollectionProvider.collectionExists(lCollectionName),
				"A collection with name '" + lCollectionName + "' does not exists!");

		IItemCollection lCollection = mCollectionProvider.getCollection(lCollectionName);

		Assert.isTrue(lCollection.getSubcribers().contains(aConnector.getId()),
				"The client is not subscribed on the target collection!");

		int lOffset = aToken.getInteger(ATTR_OFFSET, 0);
		int lLength = aToken.getInteger(ATTR_LENGTH, 10);

		Token lResponse = createResponse(aToken);

		lResponse.setList("data", lCollection.getItemStorage().list(lOffset, lLength));
		lResponse.setInteger("total", lCollection.getItemStorage().size());

		sendTokenFragmented(aConnector, lResponse, mFragmentSize);
	}

	/**
	 * Indicates if an item definition exists
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".read_definition")
	public void existsDefinitionAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lItemType = aToken.getString(ATTR_ITEM_TYPE);

		Token lResponse = createResponse(aToken);
		lResponse.setBoolean(ATTR_EXISTS, mItemFactory.supportsType(lItemType));

		sendToken(aConnector, lResponse);
	}

	/**
	 * List all item definitions
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".read_definition")
	public void listDefinitionsAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		int lOffset = aToken.getInteger(ATTR_OFFSET, 0);
		int lLength = aToken.getInteger(ATTR_LENGTH, 10);

		Token lResponse = createResponse(aToken);
		lResponse.setList("data", mItemFactory.listDefinitions(lOffset, lLength));
		lResponse.setInteger("total", mItemFactory.size());

		sendTokenFragmented(aConnector, lResponse, mFragmentSize);
	}

	/**
	 * List all item definitions
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS_ITEM_STORAGE + ".read_definition")
	public void findDefinitionAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lItemType = aToken.getString(ATTR_ITEM_TYPE);

		Token lResponse = createResponse(aToken);
		lResponse.setToken("data", mItemFactory.getDefinition(lItemType));

		sendToken(aConnector, lResponse);
	}
}
