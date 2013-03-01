package org.jwebsocket.plugins.itemstorage.api;

import java.util.Map;
import org.jwebsocket.api.IMappable;
import org.jwebsocket.api.ITokenizable;

/**
 *
 * @author kyberneees
 */
public interface IItem extends ITokenizable, IMappable {

	/**
	 *
	 * @return The item type
	 */
	String getType();

	/**
	 *
	 * @return The item primary key
	 */
	String getPK();

	/**
	 * Set an item attribute (key/value mechanism)
	 *
	 * @param aAttribute
	 * @param aValue
	 * @return
	 */
	IItem set(String aAttribute, Object aValue);

	/**
	 * Set multiple item attributes from a map collection
	 *
	 * @param aMap
	 * @return
	 */
	IItem setAll(Map<String, Object> aMap);

	/**
	 * Get an item attribute
	 *
	 * @param aAttribute
	 * @return The item attribute value or null of doesn't exists
	 */
	Object get(String aAttribute);

	/**
	 *
	 * @return The item attributes map
	 */
	Map<String, Object> getAttributes();

	/**
	 * The item definition is a map like structure that contains information of
	 * the item, include primary key and data types.
	 *
	 * @return The item definition
	 */
	IItemDefinition getDefinition();

	/**
	 * Validates the item argument's value before save
	 *
	 * @throws Exception
	 */
	void validate() throws Exception;

	/**
	 *
	 * @return The item updating data
	 */
	Map<String, Object> getUpdate();

	/**
	 * Sets the item updating data
	 *
	 * @param aUpdate
	 */
	void setUpdate(Map<String, Object> aUpdate);

}
