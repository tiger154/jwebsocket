//	---------------------------------------------------------------------------
//	jWebSocket - SecurityFilter (Community Edition, CE)
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
package org.jwebsocket.eventmodel.filter.security;

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IEmbeddedAuthentication;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IServerSecureComponent;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.eventmodel.util.Util;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.events.EventsPlugIn;
import org.jwebsocket.plugins.system.SystemPlugIn;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class SecurityFilter extends EventModelFilter {

	private static Logger mLog = Logging.getLogger(SecurityFilter.class);

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void beforeCall(WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
		//Processing plug-in restrictions
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing security restrictions ...");
		}

		//Getting the user session
		Map<String, Object> lSession = aConnector.getSession().getStorage();

		EventsPlugIn lParent = getEm().getParent();

		if (TokenPlugIn.AUTHENTICATION_METHOD_EMBEDDED.equals(lParent.getAuthenticationMethod())) {
			if (aConnector instanceof IEmbeddedAuthentication) {
				// checking for permissions on application level
				Util.checkSecurityRestrictions(lParent, (IEmbeddedAuthentication) aConnector);
				// checking for permissions on event notification level
				Util.checkSecurityRestrictions((IServerSecureComponent) getEm().getEventFactory().
						getEventDefinitions().getDefinition(aEvent.getId()), (IEmbeddedAuthentication) aConnector);
			} else {
				throw new Exception("Authentication method is set to 'embedded', but "
						+ "connector does not implements 'IEmbeddedAuthentication' interface!");
			}
		} else if (TokenPlugIn.AUTHENTICATION_METHOD_SPRING.equals(lParent.getAuthenticationMethod())) {
			//Getting the user authentication data
			boolean lIsAuth = (lSession.containsKey(SystemPlugIn.IS_AUTHENTICATED))
					? (Boolean) lSession.get(SystemPlugIn.IS_AUTHENTICATED)
					: false;
			String lUsername = (lSession.containsKey(SystemPlugIn.USERNAME))
					? (String) lSession.get(SystemPlugIn.USERNAME)
					: null;
			List<String> lAuthorities = (lSession.containsKey(SystemPlugIn.AUTHORITIES))
					? Util.parseStringArrayToList(lSession.get(SystemPlugIn.AUTHORITIES).toString().split(" "))
					: null;

			// checking for permissions on application level
			Util.checkSecurityRestrictions((IServerSecureComponent) lParent,
					aConnector, lIsAuth, lUsername, lAuthorities);

			// checking for permissions on event notification level
			Util.checkSecurityRestrictions((IServerSecureComponent) getEm().getEventFactory().
					getEventDefinitions().getDefinition(aEvent.getId()),
					aConnector, lIsAuth, lUsername, lAuthorities);
		} else {
			throw new Exception("Invalid authentication method. '"
					+ lParent.getAuthenticationMethod() + "' not supported!");
		}
	}
}
