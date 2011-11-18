//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.eventmodel.event;

import org.jwebsocket.api.IInitializable;
import java.util.Set;
import javolution.util.FastSet;
import org.jwebsocket.eventmodel.observable.Event;

/**
 *
 * @author kyberneees
 */
public class C2SEventDefinitionManager implements IInitializable {

	private Set<C2SEventDefinition> set = new FastSet<C2SEventDefinition>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown() {
	}

	/**
	 * @return The C2SEventDefinition collection 
	 */
	public Set<C2SEventDefinition> getSet() {
		return set;
	}

	/**
	 * @param set The C2SEventDefinition collection to set
	 */
	public void setSet(Set<C2SEventDefinition> set) {
		this.set.addAll(set);
	}

	/**
	 * Indicates if exists a C2SEventDefinition with a custom identifier
	 * 
	 * @param aEventId The C2SEventDefinition identifier
	 * @return <tt>TRUE</tt> if the C2SEventDefinition exists, <tt>FALSE</tt> otherwise
	 */
	public boolean hasDefinition(String aEventId) {
		for (C2SEventDefinition def : set) {
			if (def.getId().equals(aEventId)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Get a C2SEventDefinition using it identifier
	 *
	 * @param aEventId The C2SEventDefinition identifier
	 * @return The C2SEventDefinition 
	 * @throws Exception
	 */
	public C2SEventDefinition getDefinition(String aEventId) throws Exception {
		for (C2SEventDefinition def : set) {
			if (def.getId().equals(aEventId)) {
				return def;
			}
		}

		throw new IndexOutOfBoundsException("The event definition with id '" + aEventId + "' does not exists!");
	}

	/**
	 * Get the C2SEventDefinition identifier using it class
	 * 
	 * @param aEventClass The C2SEventDefinition class
	 * @return The C2SEventDefinition identifier
	 * @throws Exception
	 */
	public String getIdByClass(Class<? extends Event> aEventClass) throws Exception {
		for (C2SEventDefinition def : set) {
			if (def.getEventClass().equals(aEventClass)) {
				return def.getId();
			}
		}

		throw new IndexOutOfBoundsException("The event definition with class '" + aEventClass.toString() + "' does not exists!");
	}
}
