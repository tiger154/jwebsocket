//	---------------------------------------------------------------------------
//	jWebSocket - IItemCollectionProvider (Community Edition, CE)
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
package org.jwebsocket.plugins.itemstorage.api;

import java.util.List;
import org.jwebsocket.api.IInitializable;

/**
 *
 * @author Rolando Santamaria Maso
 */
public interface IItemCollectionProvider extends IInitializable {

	/**
	 * The item storage provider is used by the collection provider to obtain
	 * the item storage instance that is required by the collections
	 *
	 * @return The item storage provider
	 */
	IItemStorageProvider getItemStorageProvider();

	/**
	 * Get a collection by the given name
	 *
	 * @param aName
	 * @param aItemType
	 * @return
	 * @throws Exception .
	 */
	IItemCollection getCollection(String aName, String aItemType) throws Exception;

	/**
	 * Get a collection by the given name
	 *
	 * @param aName
	 * @return
	 * @throws Exception
	 */
	IItemCollection getCollection(String aName) throws Exception;

	/**
	 * Remove a collection by the given name
	 *
	 * @param aName
	 * @throws Exception
	 */
	void removeCollection(String aName) throws Exception;

	/**
	 * Save the changes on a given collection instance
	 *
	 * @param aCollection
	 * @throws Exception
	 */
	void saveCollection(IItemCollection aCollection) throws Exception;

	/**
	 *
	 * @return A list containing the name of all existing collections
	 * @throws java.lang.Exception
	 */
	List<String> collectionNames() throws Exception;

	/**
	 *
	 * @param aOffset
	 * @param aLength
	 * @return A list containing the name of all existing public collections
	 * @throws java.lang.Exception
	 */
	List<String> collectionPublicNames(int aOffset, int aLength) throws Exception;

	/**
	 *
	 * @param aOwner
	 * @param aOffset
	 * @param aLength
	 * @return A list containing the name of the given owner collections
	 * @throws java.lang.Exception
	 */
	List<String> collectionNamesByOwner(String aOwner, int aOffset, int aLength) throws Exception;

	/**
	 *
	 * @param aCollectionName
	 * @return TRUE if the collection exists, FALSE otherwise
	 * @throws java.lang.Exception
	 */
	Boolean collectionExists(String aCollectionName) throws Exception;

	/**
	 * Returns TRUE if the given item type is being used by a collection (with
	 * size > 0), FALSE otherwise.
	 *
	 * @param aItemType
	 * @return
	 * @throws java.lang.Exception
	 */
	boolean isItemTypeInUse(String aItemType) throws Exception;

	/**
	 *
	 * @return The number of existing collections
	 * @throws java.lang.Exception
	 */
	long size() throws Exception;

	/**
	 *
	 * @param aOwner
	 * @return The number of existing collections
	 * @throws java.lang.Exception
	 */
	long size(String aOwner) throws Exception;
}
