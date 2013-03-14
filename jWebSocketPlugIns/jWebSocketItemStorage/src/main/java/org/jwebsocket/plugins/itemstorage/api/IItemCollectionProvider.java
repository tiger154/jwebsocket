package org.jwebsocket.plugins.itemstorage.api;

import java.util.List;
import org.jwebsocket.api.IInitializable;

/**
 *
 * @author kyberneees
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
	 */
	List<String> collectionNames() throws Exception;;

	/**
	 *
	 * @return A list containing the name of all existing public collections
	 */
	List<String> collectionPublicNames(int aOffset, int aLength) throws Exception;;

	/**
	 *
	 * @param aOwner
	 * @return A list containing the name of the given owner collections
	 */
	List<String> collectionNamesByOwner(String aOwner, int aOffset, int aLength) throws Exception;;

	/**
	 *
	 * @return TRUE if the collection exists, FALSE otherwise
	 */
	Boolean collectionExists(String aCollectionName) throws Exception;;

	/**
	 * Returns TRUE if the given item type is being used by a collection (with
	 * size > 0), FALSE otherwise.
	 *
	 * @param aItemType
	 * @return
	 */
	boolean isItemTypeInUse(String aItemType) throws Exception;;

	/**
	 *
	 * @return The number of existing collections
	 */
	long size() throws Exception;;

	/**
	 *
	 * @return The number of existing collections
	 */
	long size(String aOwner) throws Exception;;
}
