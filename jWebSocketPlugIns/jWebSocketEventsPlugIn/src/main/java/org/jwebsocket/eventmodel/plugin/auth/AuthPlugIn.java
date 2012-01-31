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
package org.jwebsocket.eventmodel.plugin.auth;

import java.util.Map;
import org.jwebsocket.eventmodel.api.IUserUniqueIdentifierContainer;
import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.event.auth.Logon;
import org.jwebsocket.eventmodel.event.auth.Logoff;
import org.jwebsocket.eventmodel.util.CommonUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.event.auth.UserLogoff;
import org.jwebsocket.plugins.system.SystemPlugIn;

/**
 * 
 * @author kyberneees
 */
public class AuthPlugIn extends EventModelPlugIn {

	private AuthenticationManager mAm;
	private static Logger mLog = Logging.getLogger(AuthPlugIn.class);

	public AuthPlugIn() {
		//Registering internal events
		addEvents(UserLogoff.class);
	}

	/**
	 * The login process
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Logon aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		String lUsername = aEvent.getUsername();
		String lPassword = aEvent.getPassword();

		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Authenticating with the '" + mAm.getClass().getName() + "' authentication manager ...");
		}
		//Login process
		Authentication lRequest = new UsernamePasswordAuthenticationToken(lUsername, lPassword);
		Authentication lLoggon = getAm().authenticate(lRequest);

		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Updating the user session...");
		}
		//Getting the user session
		Map<String, Object> lSession = aEvent.getConnector().getSession().getStorage();

		//Setting the is_authenticated flag
		lSession.put(SystemPlugIn.IS_AUTHENTICATED, lLoggon.isAuthenticated());

		//Setting the username
		lSession.put(SystemPlugIn.USERNAME, lUsername);

		//Setting the uuid
		String lUUID = null;
		Object lDetails = lLoggon.getDetails();
		if (null != lDetails && lDetails instanceof IUserUniqueIdentifierContainer) {
			lUUID = ((IUserUniqueIdentifierContainer) lDetails).getUUID();
		} else {
			lUUID = lUsername;
		}
		lSession.put(SystemPlugIn.UUID, lUUID);

		//Setting the roles
		String lRoles = "";
		for (GrantedAuthority lGrantedAuthority : lLoggon.getAuthorities()) {
			lRoles = lRoles.concat(lGrantedAuthority.getAuthority() + " ");
		}
		lSession.put(SystemPlugIn.AUTHORITIES, lRoles);

		//Setting the username in the connector instance...
		aEvent.getConnector().setUsername(lUsername);

		//Creating the response
		aResponseEvent.getArgs().setString("uuid", lUUID);
		aResponseEvent.getArgs().setString("username", aEvent.getUsername());
		aResponseEvent.getArgs().setList("roles", CommonUtil.parseStringArrayToList(lRoles.split(" ")));
		aResponseEvent.setMessage(">> Login process has finished successfully!");

		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Logon successfully!");
		}
	}

	/**
	 * The logout process
	 * 
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Logoff aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Loging off the user '" + aEvent.getConnector().getUsername() + "...");
		}

		//Cleaning the session
		getSession(aEvent.getConnector()).clear();

		aResponseEvent.setMessage("<< Logout process has finished successfully!");

		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Logoff successfully!");
		}

		//Notify internal listeners about the UserLogoff event...
		notify(new UserLogoff(aEvent.getConnector().getUsername()), null, true);
	}

	/**
	 * @return the am
	 */
	public AuthenticationManager getAm() {
		return mAm;
	}

	/**
	 * @param aAm the am to set
	 */
	public void setAm(AuthenticationManager aAm) {
		this.mAm = aAm;
	}
}
