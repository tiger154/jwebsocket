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

import java.util.Set;
import javolution.util.FastSet;
import org.jwebsocket.eventmodel.api.IC2SEventDefinitionManager;
import org.jwebsocket.eventmodel.observable.Event;

/**
 *
 * @author kyberneees
 */
public class C2SEventDefinitionManager implements IC2SEventDefinitionManager {

	private Set<C2SEventDefinition> mDefinitions = new FastSet();

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
	public Set<C2SEventDefinition> getDefinitions() {
		return mDefinitions;
	}

	/**
	 * @param aSet The C2SEventDefinition collection to set
	 */
	public void setDefinitions(Set<C2SEventDefinition> aSet) {
		this.mDefinitions.addAll(aSet);
	}

	/**
	 * {@inheritDoc } 
	 */
	@Override
	public boolean hasDefinition(String aEventId) {
		for (C2SEventDefinition lDef : mDefinitions) {
			if (lDef.getId().equals(aEventId)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc } 
	 */
	@Override
	public C2SEventDefinition getDefinition(String aEventId) throws Exception {
		for (C2SEventDefinition lDef : mDefinitions) {
			if (lDef.getId().equals(aEventId)) {
				return lDef;
			}
		}

		throw new IndexOutOfBoundsException("The event definition with id '" + aEventId + "' does not exists!");
	}

	/**
	 * {@inheritDoc } 
	 */
	@Override
	public String getIdByClass(Class<? extends Event> aEventClass) throws Exception {
		for (C2SEventDefinition lDef : mDefinitions) {
			if (lDef.getEventClass().equals(aEventClass)) {
				return lDef.getId();
			}
		}

		throw new IndexOutOfBoundsException("The event definition for class '" + aEventClass.getCanonicalName() + "' does not exists!");
	}
}
