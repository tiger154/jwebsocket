package org.jwebsocket.plugins.itemstorage.api;

import java.util.Map;
import org.jwebsocket.api.IMappable;
import org.jwebsocket.api.ITokenizable;

/**
 *
 * @author kyberneees
 */
public interface IItemDefinition extends ITokenizable, IMappable {

	/**
	 * The item type is a unique value. Represents the abstract item type i.e
	 * "contact"
	 *
	 * @return The item type.
	 */
	String getType();

	/**
	 * Set the item type
	 *
	 * @param aType
	 */
	void setType(String aType);

	/**
	 *
	 * @return The item primary key attribute name i.e "username"
	 */
	String getPrimaryKeyAttribute();

	/**
	 * Set the primary key attribute name.
	 *
	 * @param aAttribute
	 */
	void setPrimaryKeyAttribute(String aAttribute);

	/**
	 *
	 * @return The item attributes and their types
	 */
	Map<String, String> getAttributeTypes();

	/**
	 * Set the item attributes and their types
	 *
	 * @param aAttributes
	 */
	void setAttributeTypes(Map<String, String> aAttributes);

	/**
	 *
	 * @param aAttributeName
	 * @return TRUE if the item contains the given attribute
	 */
	Boolean containsAttribute(String aAttributeName);
	
	/**
	 * Validates the item definition argument's value before save
	 *
	 * @throws Exception
	 */
	void validate() throws Exception;
}
