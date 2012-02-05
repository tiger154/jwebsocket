//  ---------------------------------------------------------------------------
//  jWebSocket - SecurityFilter
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
package org.jwebsocket.eventmodel.filter.security;

import java.util.List;
import java.util.Map;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IServerSecureComponent;
import org.jwebsocket.eventmodel.util.Util;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.system.SystemPlugIn;

/**
 *
 * @author kyberneees
 */
public class SecurityFilter extends EventModelFilter {

	private static Logger mLog = Logging.getLogger(SecurityFilter.class);

	/**
	 *{@inheritDoc }
	 */
	@Override
	public void beforeCall(WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
		//Processing plug-in restrictions
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing security restrictions in the 'EventsPlugIn' level...");
		}

		//Getting the user session
		Map<String, Object> lSession = aConnector.getSession().getStorage();

		//Getting the security validation data
		boolean lIsAuth = (lSession.containsKey(SystemPlugIn.IS_AUTHENTICATED))
				? (Boolean) lSession.get(SystemPlugIn.IS_AUTHENTICATED)
				: false;
		String lUsername = (lSession.containsKey(SystemPlugIn.USERNAME))
				? (String) lSession.get(SystemPlugIn.USERNAME)
				: null;
		List<String> lAuthorities = (lSession.containsKey(SystemPlugIn.AUTHORITIES))
				? Util.parseStringArrayToList(lSession.get(SystemPlugIn.AUTHORITIES).toString().split(" "))
				: null;

		Util.checkSecurityRestrictions((IServerSecureComponent) getEm().getParent(),
				aConnector, lIsAuth, lUsername, lAuthorities);

		//Processing the C2SEvent restrictions
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing security restrictions in the 'WebSocketEventDefinition' level...");
		}
		Util.checkSecurityRestrictions((IServerSecureComponent) getEm().getEventFactory().
				getEventDefinitions().getDefinition(aEvent.getId()),
				aConnector, lIsAuth, lUsername, lAuthorities);
	}
}
