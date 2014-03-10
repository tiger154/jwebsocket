//	---------------------------------------------------------------------------
//	jWebSocket - C2SEventDefinitionManager (Community Edition, CE)
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
 * @author Rolando Santamaria Maso
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
	 * @throws Exception
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
	 *
	 * @throws Exception
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
	 *
	 * @throws Exception
	 */
	@Override
	public void registerDefinition(C2SEventDefinition aDef) throws Exception {
		mIdToDef.put(aDef.getId(), aDef);
		mClassToId.put(aDef.getEventClass(), aDef.getId());
	}

	/**
	 * {@inheritDoc }
	 *
	 * @throws Exception
	 */
	@Override
	public void removeDefinition(C2SEventDefinition aDef) throws Exception {
		String lId = getIdByClass(aDef.getEventClass());

		//Removing
		mClassToId.remove(aDef.getEventClass());
		mIdToDef.remove(lId);
	}
}
