package org.jwebsocket.plugins.itemstorage.api;

import java.util.List;
import java.util.Set;
import org.jwebsocket.api.IInitializable;

/**
 *
 * @author kyberneees
 */
public interface IItemFactory extends IInitializable {

	/**
	 * Get an IItem object prototype
	 *
	 * @param aType
	 * @return
	 * @throws Exception
	 */
	IItem getItemPrototype(String aType) throws Exception;

	/**
	 * Get an IItemDefinition prototype
	 *
	 * @return
	 */
	IItemDefinition getDefinitionPrototype();

	/**
	 * Get an item definition given the item type
	 *
	 * @param aType
	 * @return
	 */
	IItemDefinition getDefinition(String aType) throws Exception;

	/**
	 *
	 * @param aType
	 * @return TRUE if the item type exists, FALSE otherwise
	 */
	Boolean supportsType(String aType);

	/**
	 * Register a new item definition. If the definition exists, then it is
	 * replaced.
	 *
	 * @param aDefinition
	 */
	void registerDefinition(IItemDefinition aDefinition) throws Exception;

	/**
	 * Remove an item definition given the item type
	 *
	 * @param aType
	 * @return
	 */
	IItemDefinition removeDefinition(String aType) throws Exception;

	/**
	 * Register a list of item definitions
	 *
	 * @param aDefinitions
	 */
	void setDefinitions(Set<IItemDefinition> aDefinitions) throws Exception;

	/**
	 * List existing definitions
	 *
	 * @param aOffset
	 * @param aLength
	 * @return
	 */
	List<IItemDefinition> listDefinitions(int aOffset, int aLength) throws Exception;

	/**
	 *
	 * @return The number of existing item definitions
	 */
	int size();
}
