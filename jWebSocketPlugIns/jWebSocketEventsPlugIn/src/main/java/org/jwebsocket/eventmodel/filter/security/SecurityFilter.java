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
package org.jwebsocket.eventmodel.filter.security;

import java.util.List;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.ISecureComponent;
import org.jwebsocket.eventmodel.util.CommonUtil;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author kyberneees
 */
public class SecurityFilter extends EventModelFilter {

	private static Logger mLog = Logging.getLogger(SecurityFilter.class);
	public final static String USERNAME = "__USERNAME__";
	public final static String ROLES = "__ROLES__";
	public final static String IS_AUTHENTICATED = "__IS_AUTH__";
	public final static String UUID = "__UUID__";

	/**
	 *{@inheritDoc }
	 */
	@Override
	public void beforeCall(WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
		//Processing plug-in restrictions
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Processing security restrictions in the 'EventsPlugIn' level...");
		}

		//Getting the user session
		IBasicStorage<String, Object> session = getSession(aConnector);

		//Getting the security validation data
		boolean isAuth = (session.containsKey(IS_AUTHENTICATED))
				? (Boolean) session.get(IS_AUTHENTICATED)
				: false;
		String username = (session.containsKey(USERNAME))
				? (String) session.get(USERNAME)
				: null;
		List<String> roles = (session.containsKey(ROLES))
				? CommonUtil.parseStringArrayToList(session.get(ROLES).toString().split(" "))
				: null;

		CommonUtil.checkSecurityRestrictions((ISecureComponent) getEm().getParent(),
				aConnector, isAuth, username, roles);

		//Processing the C2SEvent restrictions
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Processing security restrictions in the 'WebSocketEventDefinition' level...");
		}
		CommonUtil.checkSecurityRestrictions((ISecureComponent) getEm().getEventFactory().
				getEventDefinitions().getDefinition(aEvent.getId()),
				aConnector, isAuth, username, roles);
	}
}
