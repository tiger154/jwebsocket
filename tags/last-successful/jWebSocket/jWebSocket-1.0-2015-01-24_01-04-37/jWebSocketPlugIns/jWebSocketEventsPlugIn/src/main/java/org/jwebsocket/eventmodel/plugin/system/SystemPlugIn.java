//	---------------------------------------------------------------------------
//	jWebSocket - SystemPlugIn (Community Edition, CE)
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
package org.jwebsocket.eventmodel.plugin.system;

import java.util.Iterator;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.api.IEventModelPlugIn;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.event.system.ClientCacheAspectStatus;
import org.jwebsocket.eventmodel.event.system.GetPlugInAPI;
import org.jwebsocket.eventmodel.event.system.GetPlugInList;
import org.jwebsocket.eventmodel.event.system.HasPlugIn;
import org.jwebsocket.eventmodel.filter.cache.CacheFilter;
import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class SystemPlugIn extends EventModelPlugIn {

	private static Logger mLog = Logging.getLogger(SystemPlugIn.class);

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void initialize() throws Exception {
	}

	/**
	 * Return the plug-ins identifiers list
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(GetPlugInList aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Exporting the plugIns identifiers list...");
		}

		FastList<String> lPlugInIds = new FastList<String>();
		for (Iterator<IEventModelPlugIn> lIt = getEm().getPlugIns().iterator(); lIt.hasNext();) {
			IEventModelPlugIn lPlugIn = lIt.next();
			lPlugInIds.add(lPlugIn.getId());
		}

		aResponseEvent.getArgs().setList("identifiers", lPlugInIds);
	}

	/**
	 * Return the plug-ins identifiers list
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(ClientCacheAspectStatus aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Setting the client cache aspect status in the session storage ...");
		}

		aEvent.getConnector().getSession().getStorage().
				put(CacheFilter.CLIENT_CACHE_ASPECT_STATUS, aEvent.isEnabled());
	}

	/**
	 * Indicate if a plug-in exists using it identifier
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(HasPlugIn aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		boolean lHas = false;
		for (Iterator<IEventModelPlugIn> lIt = getEm().getPlugIns().iterator(); lIt.hasNext();) {
			IEventModelPlugIn lPlugIn = lIt.next();
			if (lPlugIn.getId().equals(aEvent.getPluginId())) {
				lHas = true;
				break;
			}
		}

		aResponseEvent.getArgs().setBoolean("has", lHas);
	}

	/**
	 * Return a plug-in API using it identifier
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(GetPlugInAPI aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		String lPlugInId = aEvent.getPlugInId();
		if (mLog.isDebugEnabled()) {
			mLog.debug("Exporting API for '" + lPlugInId + "' plugIn...");
		}

		IEventModelPlugIn lPlugIn = getEm().getPlugIn(lPlugInId);
		lPlugIn.writeToToken(aResponseEvent.getArgs());
	}
}
