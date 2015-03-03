//	---------------------------------------------------------------------------
//	jWebSocket - ItemStoragePlugInListener (Community Edition, CE)
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
package org.jwebsocket.client.plugins.itemstorage;

import org.jwebsocket.api.WebSocketClientTokenPlugInListener;
import org.jwebsocket.token.Token;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class ItemStoragePlugInListener implements WebSocketClientTokenPlugInListener {

	@Override
	public void processToken(Token aToken) {
		if ("event".equals(aToken.getType())) {
			if ("itemSaved".equals(aToken.getString("name"))) {
				OnItemSaved(aToken);
			} else if ("itemRemoved".equals(aToken.getString("name"))) {
				OnItemRemoved(aToken);
			} else if ("collectionCleaned".equals(aToken.getString("name"))) {
				OnCollectionCleaned(aToken);
			} else if ("collectionRestarted".equals(aToken.getString("name"))) {
				OnCollectionRestarted(aToken);
			} else if ("collectionSaved".equals(aToken.getString("name"))) {
				OnCollectionSaved(aToken);
			} else if ("collectionRemoved".equals(aToken.getString("name"))) {
				OnCollectionRemoved(aToken);
			} else if ("authorization".equals(aToken.getString("name"))) {
				OnCollectionAuthorization(aToken);
			} else if ("subscription".equals(aToken.getString("name"))) {
				OnCollectionSubscription(aToken);
			} else if ("unsubscription".equals(aToken.getString("name"))) {
				OnCollectionUnsubscription(aToken);
			}
		}
	}

	/**
	 * Called when an item has been saved on a subscribed collection
	 *
	 * @param aToken
	 */
	public void OnItemSaved(Token aToken) {
	}

	/**
	 * Called when an item has been removed on a subscribed collection
	 *
	 * @param aToken
	 */
	public void OnItemRemoved(Token aToken) {
	}

	/**
	 * Called when a subscribed collection has been cleaned
	 *
	 * @param aToken
	 */
	public void OnCollectionCleaned(Token aToken) {
	}

	/**
	 * Called when a subscribed collection has been restarted
	 *
	 * @param aToken
	 */
	public void OnCollectionRestarted(Token aToken) {
	}

	/**
	 * Called when a subscribed collection has been removed
	 *
	 * @param aToken
	 */
	public void OnCollectionRemoved(Token aToken) {
	}

	/**
	 * Called when a subscribed collection has been saved
	 *
	 * @param aToken
	 */
	public void OnCollectionSaved(Token aToken) {
	}

	/**
	 * Called when a client gets authorized to a subscribed collection
	 *
	 * @param aToken
	 */
	public void OnCollectionAuthorization(Token aToken) {
	}

	/**
	 * Called when a new client gets subscribed to a subscribed collection
	 *
	 * @param aToken
	 */
	public void OnCollectionSubscription(Token aToken) {
	}

	/**
	 * Called when a client gets un-subscribed from a subscribed collection
	 *
	 * @param aToken
	 */
	public void OnCollectionUnsubscription(Token aToken) {
	}
}
