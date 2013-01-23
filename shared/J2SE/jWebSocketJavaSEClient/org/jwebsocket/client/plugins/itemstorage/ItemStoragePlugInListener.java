//	---------------------------------------------------------------------------
//	jWebSocket - ItemStoragePlugInListener
//	Copyright (c) 2012 jWebSocket.org, Rolando Santamaria Maso, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.client.plugins.itemstorage;

import org.jwebsocket.api.WebSocketClientTokenPlugInListener;
import org.jwebsocket.token.Token;

/**
 *
 * @author kyberneees
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
