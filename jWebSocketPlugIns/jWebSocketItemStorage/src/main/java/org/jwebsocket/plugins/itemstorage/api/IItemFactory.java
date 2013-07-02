//  ---------------------------------------------------------------------------
//  jWebSocket - IItemFactory (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
	Boolean supportsType(String aType) throws Exception;

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
	int size() throws Exception;
}
