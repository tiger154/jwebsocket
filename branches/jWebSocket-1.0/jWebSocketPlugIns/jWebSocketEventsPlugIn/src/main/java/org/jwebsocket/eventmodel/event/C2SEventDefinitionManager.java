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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.jwebsocket.eventmodel.api.IC2SEventDefinitionManager;
import org.jwebsocket.eventmodel.observable.Event;

/**
 *
 * @author kyberneees
 */
public class C2SEventDefinitionManager implements IC2SEventDefinitionManager {

	private Map<String, C2SEventDefinition> mIdToDef = new FastMap<String, C2SEventDefinition>().shared();
	private Map<Class, String> mClassToId = new FastMap<Class, String>().shared();

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
	 * @param aSet The C2SEventDefinition collection to set
	 */
	public void setDefinitions(Set<C2SEventDefinition> aSet) throws Exception {
		this.registerDefinitions(aSet);
	}

	/**
	 * 
	 * @return A set of the event definitions
	 */
	public Set<C2SEventDefinition> getDefinitions() {
		Set<C2SEventDefinition> lResult = new FastSet<C2SEventDefinition>();
		for (Iterator<C2SEventDefinition> lIt = mIdToDef.values().iterator(); lIt.hasNext();) {
			C2SEventDefinition lDef = lIt.next();
			lResult.add(lDef);
		}

		return lResult;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public boolean hasDefinition(String aEventId) {
		return mIdToDef.containsKey(aEventId);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public C2SEventDefinition getDefinition(String aEventId) throws Exception {
		if (mIdToDef.containsKey(aEventId)) {
			return mIdToDef.get(aEventId);
		} else {
			throw new IndexOutOfBoundsException("The event definition with id '" + aEventId + "' does not exists!");
		}
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public String getIdByClass(Class<? extends Event> aEventClass) throws Exception {
		if (mClassToId.containsKey(aEventClass)) {
			return mClassToId.get(aEventClass);
		} else {
			throw new IndexOutOfBoundsException("The event definition for class '" + aEventClass.getCanonicalName() + "' does not exists!");
		}
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void registerDefinitions(Set<C2SEventDefinition> aDefs) throws Exception {
		for (Iterator<C2SEventDefinition> lIt = aDefs.iterator(); lIt.hasNext();) {
			C2SEventDefinition lDef = lIt.next();
			registerDefinition(lDef);
		}
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void registerDefinition(C2SEventDefinition aDef) throws Exception {
		mIdToDef.put(aDef.getId(), aDef);
		mClassToId.put(aDef.getEventClass(), aDef.getId());
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void removeDefinition(C2SEventDefinition aDef) throws Exception {
		String lId = getIdByClass(aDef.getEventClass());

		//Removing
		mClassToId.remove(aDef.getEventClass());
		mIdToDef.remove(lId);
	}
}
