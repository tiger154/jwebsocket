//	---------------------------------------------------------------------------
//	jWebSocket - IItemStorage (Community Edition, CE)
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
import java.util.Map;
import java.util.Set;
import org.jwebsocket.api.IInitializable;

/**
 *
 * @author Rolando Santamaria Maso
 */
public interface IItemStorage extends IInitializable {

	/**
	 * An item storage uses an item factory to consult the item definitions
	 *
	 * @return The item factory used by the item storage
	 */
	IItemFactory getItemFactory();

	/**
	 *
	 * @return the IItemStorage name
	 */
	String getName();

	/**
	 *
	 * @return the type of the item supported by the storage
	 */
	String getItemType();

	/**
	 *
	 * @return A list containing the item's primary key
	 * @throws java.lang.Exception
	 */
	Set<String> getPKs() throws Exception;

	/**
	 * Save an item that changed his PK value
	 *
	 * @param aTargetPK
	 * @param aItem
	 * @throws java.lang.Exception
	 */
	void save(String aTargetPK, IItem aItem) throws Exception;

	/**
	 * Save an item
	 *
	 * @param aItem
	 * @throws Exception
	 */
	void save(IItem aItem) throws Exception;

	/**
	 * Remove an item by it primary key
	 *
	 * @param aPK
	 * @return
	 * @throws Exception
	 */
	IItem remove(String aPK) throws Exception;

	/**
	 * List all the items
	 *
	 * @return
	 * @throws Exception
	 */
	List<IItem> list() throws Exception;

	/**
	 * List items
	 *
	 * @param aOffset
	 * @return
	 * @throws java.lang.Exception
	 */
	List<IItem> list(int aOffset) throws Exception;

	/**
	 * List items
	 *
	 * @param aOffset
	 * @param aLength
	 * @return
	 * @throws java.lang.Exception
	 */
	List<IItem> list(int aOffset, int aLength) throws Exception;

	/**
	 * Find an item randomly
	 *
	 * @return
	 * @throws Exception
	 */
	IItem findRandom() throws Exception;

	/**
	 * Find an item by it primary key
	 *
	 * @param aPK
	 * @return
	 * @throws Exception
	 */
	IItem findByPK(String aPK) throws Exception;

	/**
	 * Find all the items that matches the key/value search criteria
	 *
	 * @param aAttribute
	 * @param aValue
	 * @return
	 * @throws Exception
	 */
	List<IItem> find(String aAttribute, Object aValue) throws Exception;

	/**
	 * Find all the items that matches the key/value search criteria
	 *
	 * @param aAttribute
	 * @param aValue
	 * @param aOffset
	 * @param aLength
	 * @return
	 * @throws Exception
	 */
	List<IItem> find(String aAttribute, Object aValue, int aOffset, int aLength) throws Exception;

	/**
	 * Find all the items that matches the key/value search criteria
	 *
	 * @param aAttribute
	 * @param aValue
	 * @param aOrderBy
	 * @param aOffset
	 * @param aLength
	 * @return
	 * @throws Exception
	 */
	List<IItem> find(String aAttribute, Object aValue, Map<String, Boolean> aOrderBy, int aOffset, int aLength) throws Exception;

	/**
	 * Find all the items that matches the keys/values search criteria
	 *
	 * @param aAttrsValues
	 * @param aOrderBy
	 * @param aOffset
	 * @param aLength
	 * @return
	 * @throws Exception
	 */
	List<IItem> find(Map<String, Object> aAttrsValues, Map<String, Boolean> aOrderBy, int aOffset, int aLength) throws Exception;

	/**
	 * Remove all the items of the storage
	 *
	 * @throws Exception
	 */
	void clear() throws Exception;

	/**
	 *
	 * @return The number of stored items
	 * @throws java.lang.Exception
	 */
	Integer size() throws Exception;

	/**
	 *
	 * @param aAttribute
	 * @param aValue
	 * @return The number of stored items that matches the criteria
	 * @throws java.lang.Exception
	 */
	Integer size(String aAttribute, Object aValue) throws Exception;

	/**
	 *
	 * @param aAttrsValues
	 * @return The number of stored items that matches the criteria
	 * @throws java.lang.Exception
	 */
	Integer size(Map<String, Object> aAttrsValues) throws Exception;

	/**
	 * Returns TRUE if an stored item matches the given PK, FALSE otherwise
	 *
	 * @param aPK
	 * @return
	 * @throws java.lang.Exception
	 */
	boolean exists(String aPK) throws Exception;
}
