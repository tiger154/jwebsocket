//	---------------------------------------------------------------------------
//	jWebSocket - IC2SEventDefinitionManager (Community Edition, CE)
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
package org.jwebsocket.eventmodel.api;

import java.util.Set;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.observable.Event;

/**
 * Allows to store c2s event definitions in multiple storage sources
 *
 * @author Rolando Santamaria Maso
 */
public interface IC2SEventDefinitionManager extends IInitializable {

	/**
	 * Indicates if exists a C2SEventDefinition with a custom identifier
	 *
	 * @param aEventId The C2SEventDefinition identifier
	 * @return <tt>TRUE</tt> if the C2SEventDefinition exists, <tt>FALSE</tt>
	 * otherwise
	 */
	boolean hasDefinition(String aEventId);

	/**
	 * Get a C2SEventDefinition using it identifier
	 *
	 * @param aEventId The C2SEventDefinition identifier
	 * @return The C2SEventDefinition
	 * @throws Exception
	 */
	C2SEventDefinition getDefinition(String aEventId) throws Exception;

	/**
	 * Get the C2SEventDefinition identifier using it class
	 *
	 * @param aEventClass The C2SEventDefinition class
	 * @return The C2SEventDefinition identifier
	 * @throws Exception
	 */
	String getIdByClass(Class<? extends Event> aEventClass) throws Exception;

	/**
	 * Register multiple event definitions.
	 *
	 * Replace existing definitions if the type match
	 *
	 * @param aDefs The events definitions
	 * @throws Exception
	 */
	void registerDefinitions(Set<C2SEventDefinition> aDefs) throws Exception;

	/**
	 * Register a event definition.
	 *
	 * Replace existing definition if the type match
	 *
	 * @param aDef The event definition
	 * @throws Exception
	 */
	void registerDefinition(C2SEventDefinition aDef) throws Exception;

	/**
	 * Remove a event definition.
	 *
	 * @param aDef The event definition
	 * @throws Exception
	 */
	void removeDefinition(C2SEventDefinition aDef) throws Exception;
}
