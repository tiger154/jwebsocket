//	---------------------------------------------------------------------------
//	jWebSocket - ItemStoragePlugIn
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

import org.jwebsocket.api.WebSocketTokenClient;
import org.jwebsocket.client.plugins.BaseClientTokenPlugIn;
import org.jwebsocket.config.JWebSocketClientConstants;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.token.WebSocketResponseTokenListener;

/**
 *
 * @author kyberneees
 */
public class ItemStoragePlugIn extends BaseClientTokenPlugIn {

	public static String DEFAULT_NS = JWebSocketClientConstants.NS_BASE + ".plugins.itemstorage";

	public ItemStoragePlugIn(WebSocketTokenClient aClient, String aNS) {
		super(aClient, aNS);
	}

	public ItemStoragePlugIn(WebSocketTokenClient aClient) {
		super(aClient, DEFAULT_NS);
	}

	/**
	 * Creates an item collection
	 *
	 * @param aCollectionName The collection name
	 * @param aItemType The collection item type
	 * @param aSecretPwd The collection secret password
	 * @param aAccessPwd The collection access password
	 * @param aIsPrivate Indicates if the collection is private
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void createCollection(String aCollectionName, String aItemType, String aSecretPwd,
			String aAccessPwd, Boolean aIsPrivate, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "createCollection");

		lRequest.setString("collectionName", aCollectionName);
		lRequest.setString("itemType", aItemType);
		lRequest.setString("secretPassword", aSecretPwd);
		lRequest.setString("accessPassword", aAccessPwd);
		lRequest.setBoolean("isPrivate", aIsPrivate);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Removes an item collection
	 *
	 * @param aCollectionName The collection name
	 * @param aSecretPwd The collection secret password
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void removeCollection(String aCollectionName, String aSecretPwd,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "removeCollection");

		lRequest.setString("collectionName", aCollectionName);
		lRequest.setString("secretPassword", aSecretPwd);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Indicates if a collection exists
	 *
	 * @param aCollectionName The collection name
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void existsCollection(String aCollectionName,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "existsCollection");

		lRequest.setString("collectionName", aCollectionName);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Subscribes to an item collection
	 *
	 * @param aCollectionName The collection name
	 * @param aAccessPwd The collection secret password
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void subscribeCollection(String aCollectionName, String aAccessPwd,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "subscribe");

		lRequest.setString("collectionName", aCollectionName);
		lRequest.setString("accessPassword", aAccessPwd);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Authorize to an item collection
	 *
	 * @param aCollectionName The collection name
	 * @param aSecretPwd The collection secret password
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void authorizeCollection(String aCollectionName, String aSecretPwd,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "authorize");

		lRequest.setString("collectionName", aCollectionName);
		lRequest.setString("secretPassword", aSecretPwd);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Un-subscribes from an item collection
	 *
	 * @param aCollectionName The collection name
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void unsubscribeCollection(String aCollectionName,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "unsubscribe");

		lRequest.setString("collectionName", aCollectionName);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Clear an item collection
	 *
	 * @param aCollectionName The collection name
	 * @param aSecretPwd The collection secret password
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void clearCollection(String aCollectionName, String aSecretPwd,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "clearCollection");

		lRequest.setString("collectionName", aCollectionName);
		lRequest.setString("secretPassword", aSecretPwd);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Edit an item collection configuration
	 *
	 * @param aCollectionName The collection name
	 * @param aSecretPwd The collection secret password
	 * @param aNewSecretPwd The collection new secret password
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void editCollection(String aCollectionName, String aSecretPwd, String aNewSecretPwd,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "editCollection");

		lRequest.setString("collectionName", aCollectionName);
		lRequest.setString("secretPassword", aSecretPwd);
		lRequest.setString("newSecretPassword", aNewSecretPwd);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Edit an item collection configuration
	 *
	 * @param aCollectionName The collection name
	 * @param aSecretPwd The collection secret password
	 * @param aAccessPwd The collection new access password
	 * @param aIsPrivate The collection new is private value
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void editCollection(String aCollectionName, String aSecretPwd, String aAccessPwd, boolean aIsPrivate,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "editCollection");

		lRequest.setString("collectionName", aCollectionName);
		lRequest.setString("secretPassword", aSecretPwd);
		lRequest.setString("accessPassword", aAccessPwd);
		lRequest.setBoolean("isPrivate", aIsPrivate);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Edit an item collection configuration
	 *
	 * @param aCollectionName The collection name
	 * @param aSecretPwd The collection secret password
	 * @param aNewSecretPwd The collection new secret password
	 * @param aAccessPwd The collection new access password
	 * @param aIsPrivate The collection new is private value
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void editCollection(String aCollectionName, String aSecretPwd, String aNewSecretPwd,
			String aAccessPwd, boolean aIsPrivate,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "editCollection");

		lRequest.setString("collectionName", aCollectionName);
		lRequest.setString("secretPassword", aSecretPwd);
		lRequest.setString("newSecretPassword", aNewSecretPwd);
		lRequest.setString("accessPassword", aAccessPwd);
		lRequest.setBoolean("isPrivate", aIsPrivate);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Restarts an item collection
	 *
	 * @param aCollectionName The collection name
	 * @param aSecretPwd The collection secret password
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void restartCollection(String aCollectionName, String aSecretPwd,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "restartCollection");

		lRequest.setString("collectionName", aCollectionName);
		lRequest.setString("secretPassword", aSecretPwd);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Gets all public or active user collection names
	 *
	 * @param aUserOnly If TRUE, only the active user collection names are
	 * returned
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void getCollectionNames(boolean aUserOnly,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "getCollectionNames");

		lRequest.setBoolean("userOnly", aUserOnly);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Finds an item collection
	 *
	 * @param aCollectionName The collection name
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void findCollection(String aCollectionName,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "findCollection");

		lRequest.setString("collectionName", aCollectionName);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Saves an item on a collection
	 *
	 * @param aCollectionName The collection name
	 * @param aItem The item to be saved
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void saveItem(String aCollectionName, Token aItem,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "saveItem");

		lRequest.setString("collectionName", aCollectionName);
		lRequest.setToken("item", aItem);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Removes an item from a collection
	 *
	 * @param aCollectionName The collection name
	 * @param aPK The item primary key
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void removeItem(String aCollectionName, String aPK,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "removeItem");

		lRequest.setString("collectionName", aCollectionName);
		lRequest.setString("itemPK", aPK);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Finds an item by primary key on a collection
	 *
	 * @param aCollectionName The collection name
	 * @param aPK The item primary key
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void findItemByPK(String aCollectionName, String aPK,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "findItemByPK");

		lRequest.setString("collectionName", aCollectionName);
		lRequest.setString("itemPK", aPK);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Indicates if an item exists on a collection
	 *
	 * @param aCollectionName The collection name
	 * @param aPK The item primary key
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void existsItem(String aCollectionName, String aPK,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "existsItem");

		lRequest.setString("collectionName", aCollectionName);
		lRequest.setString("itemPK", aPK);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * List items from a collection
	 *
	 * @param aCollectionName The collection name
	 * @param aOffset The listing start position
	 * @param aLength The maximum number of items to be listed
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void listItems(String aCollectionName, int aOffset, int aLength,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "listItems");

		lRequest.setString("collectionName", aCollectionName);
		lRequest.setInteger("offset", aOffset);
		lRequest.setInteger("length", aLength);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * List the first 10 items from a collection
	 *
	 * @param aCollectionName The collection name
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void listItems(String aCollectionName,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		listItems(aCollectionName, 0, 10, aListener);
	}

	/**
	 * Finds an item definition
	 *
	 * @param aItemType The item type
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void findItemDefinition(String aItemType,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "findDefinition");

		lRequest.setString("itemType", aItemType);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Indicates if an item definition exists
	 *
	 * @param aItemType The item type
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void existsItemDefinition(String aItemType,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "existsDefinition");

		lRequest.setString("itemType", aItemType);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Lists item definitions
	 *
	 * @param aOffset The listing start position
	 * @param aLength The maximum number of item definitions to be listed
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void listItemDefinitions(int aOffset, int aLength,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "listDefinitions");

		lRequest.setInteger("offset", aOffset);
		lRequest.setInteger("length", aLength);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Lists the first 10 item definitions
	 *
	 * @param aListener The operation response listener
	 * @throws WebSocketException
	 */
	public void listItemDefinitions(WebSocketResponseTokenListener aListener) throws WebSocketException {
		listItemDefinitions(0, 10, aListener);
	}
}
