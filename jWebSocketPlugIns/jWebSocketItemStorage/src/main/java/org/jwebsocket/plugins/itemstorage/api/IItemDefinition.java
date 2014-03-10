//	---------------------------------------------------------------------------
//	jWebSocket - IItemDefinition (Community Edition, CE)
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

import java.util.Map;
import org.jwebsocket.api.IMappable;
import org.jwebsocket.api.ITokenizable;

/**
 *
 * @author Rolando Santamaria Maso
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
