//	---------------------------------------------------------------------------
//	jWebSocket - ItemStorageUtils for SMS Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.sms;

import java.util.Date;
import org.jwebsocket.plugins.itemstorage.ItemStoragePlugIn;
import org.jwebsocket.plugins.itemstorage.api.IItemCollection;
import org.jwebsocket.plugins.itemstorage.api.IItemCollectionProvider;
import org.jwebsocket.plugins.itemstorage.api.IItemFactory;
import org.jwebsocket.plugins.itemstorage.collection.ItemCollectionUtils;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.util.MapAppender;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class ItemStorageUtils {

	/**
	 *
	 * @param aSettings
	 * @return
	 * @throws Exception
	 */
	public static IItemCollection initialize(Settings aSettings) throws Exception {
		// getting the collection provider
		IItemCollectionProvider lCollectionProvider = (IItemCollectionProvider) JWebSocketBeanFactory
				.getInstance(ItemStoragePlugIn.NS_ITEM_STORAGE).getBean("collectionProvider");
		// getting the item definitions factory
		IItemFactory lItemFactory = lCollectionProvider.getItemStorageProvider().getItemFactory();
		// checking if collection already exists
		if (!lCollectionProvider.collectionExists(aSettings.getCollectionName())) {
			// check if definition already exists
			if (!lItemFactory.supportsType(aSettings.getCollectionType())) {
				// creating definition
				lItemFactory.registerDefinition(aSettings.getSMSItemDefinition());
			}
			// creating the collection
			IItemCollection lSMSCollection = lCollectionProvider
					.getCollection(aSettings.getCollectionName(), aSettings.getCollectionType());
			lSMSCollection.setAccessPassword(aSettings.getCollectionAccessPassword());
			lSMSCollection.setSecretPassword(aSettings.getCollectionSecretPassword());
			lSMSCollection.setOwner("root");
			// saving changes
			lCollectionProvider.saveCollection(lSMSCollection);
		}

		return lCollectionProvider.getCollection(aSettings.getCollectionName());
	}

	/**
	 * Save SMS
	 *
	 * @param mCollection
	 * @param aUser
	 * @param aMessage
	 * @param aFrom
	 * @param aTo
	 * @param aState
	 * @throws Exception
	 */
	public static void saveSMS(IItemCollection mCollection, String aUser, String aMessage,
			String aFrom, String aTo, String aState) throws Exception {
		ItemCollectionUtils.saveItem("root", mCollection, new MapAppender()
				.append("time", new Date().getTime())
				.append("length", aMessage.length())
				.append("from", aFrom)
				.append("to", aTo)
				.append("state", aState)
				.append("user", aUser)
				.append("message", aMessage)
				.getMap());
	}
}
