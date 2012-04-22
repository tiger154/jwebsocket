//  ---------------------------------------------------------------------------
//  jWebSocket - SystemPlugIn
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
package org.jwebsocket.eventmodel.plugin.system;

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
 * @author kyberneees
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
		for (IEventModelPlugIn lPlugIn : getEm().getPlugIns()) {
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
			mLog.debug("Setting the client cache aspect status ...");
		}

		aEvent.getConnector().setVar(CacheFilter.CLIENT_CACHE_ASPECT_STATUS, aEvent.isEnabled());
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
		for (IEventModelPlugIn lPlugIn : getEm().getPlugIns()) {
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
