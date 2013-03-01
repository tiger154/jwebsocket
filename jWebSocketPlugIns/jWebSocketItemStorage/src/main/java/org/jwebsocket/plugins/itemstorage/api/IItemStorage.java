package org.jwebsocket.plugins.itemstorage.api;

import java.util.List;
import java.util.Set;
import org.jwebsocket.api.IInitializable;

/**
 *
 * @author kyberneees
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
	 */
	Set<String> getPKs() throws Exception;

	/**
	 * Save an item that changed his PK value
	 *
	 * @param aTargetPK
	 * @param aItem
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
	 */
	List<IItem> list(int aOffset) throws Exception;

	/**
	 * List items
	 *
	 * @param aOffset
	 * @param aLength
	 * @return
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
	 * @return
	 * @throws Exception
	 */
	List<IItem> find(String aAttribute, Object aValue, int aOffset, int aLength) throws Exception;

	/**
	 * Remove all the items of the storage
	 *
	 * @throws Exception
	 */
	void clear() throws Exception;

	/**
	 *
	 * @return The number of stored items
	 */
	Integer size();
	
	/**
	 *
	 * @return The number of stored items that matches the criteria
	 */
	Integer size(String aAttribute, Object aValue) throws Exception;

	/**
	 * Returns TRUE if an stored item matches the given PK, FALSE otherwise
	 *
	 * @param aPK
	 * @return
	 */
	boolean exists(String aPK);
}
