//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.api;

import org.jwebsocket.api.IInitializable;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.observable.Event;

/**
 * Allows to store c2s event definitions in multiple storage sources
 * 
 * @author kyberneees
 */
public interface IC2SEventDefinitionManager extends IInitializable {

	/**
	 * Indicates if exists a C2SEventDefinition with a custom identifier
	 * 
	 * @param aEventId The C2SEventDefinition identifier
	 * @return <tt>TRUE</tt> if the C2SEventDefinition exists, <tt>FALSE</tt> otherwise
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
}
