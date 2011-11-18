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

import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.eventmodel.api.IUserUniqueIdentifierContainer;
import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.event.auth.Logon;
import org.jwebsocket.eventmodel.event.auth.Logoff;
import org.jwebsocket.eventmodel.filter.security.SecurityFilter;
import org.jwebsocket.eventmodel.util.CommonUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;

/**
 * 
 * @author kyberneees
 */
public class AuthPlugIn extends EventModelPlugIn {

	private AuthenticationManager am;
	private static Logger mLog = Logging.getLogger(AuthPlugIn.class);
	
	/**
	 * The login process
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Logon aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		String username = aEvent.getUsername();
		String password = aEvent.getPassword();

		if (mLog.isDebugEnabled()){
			mLog.debug(">> Authenticating with the '" + am.getClass().getName() + "' authentication manager ...");
		}
		//Login process
		Authentication request = new UsernamePasswordAuthenticationToken(username, password);
		Authentication loggon = getAm().authenticate(request);

		if (mLog.isDebugEnabled()){
			mLog.debug(">> Updating the user session...");
		}
		//Getting the user session
		IBasicStorage<String, Object> session = getSession(aEvent.getConnector());

		//Setting the is_authenticated flag
		session.put(SecurityFilter.IS_AUTHENTICATED, loggon.isAuthenticated());
		
		//Setting the username
		session.put(SecurityFilter.USERNAME, username);

		//Setting the uuid
		String uuid = null;
		Object details = loggon.getDetails();
		if (null != details && details instanceof IUserUniqueIdentifierContainer){
			uuid = ((IUserUniqueIdentifierContainer)details).getUUID();
		} else {
			uuid = username;
		}
		session.put(SecurityFilter.UUID, uuid);

		//Setting the roles
		String roles = "";
		for (GrantedAuthority ga : loggon.getAuthorities()) {
			roles = roles.concat(ga.getAuthority() + " ");
		}
		session.put(SecurityFilter.ROLES, roles);

		//Creating the response
		aResponseEvent.getArgs().setString("uuid", uuid);
		aResponseEvent.getArgs().setString("username", aEvent.getUsername());
		aResponseEvent.getArgs().setList("roles", CommonUtil.parseStringArrayToList(roles.split(" ")));
		aResponseEvent.setMessage(">> Login process has finished successfully!");
		
		if (mLog.isDebugEnabled()){
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
		if (mLog.isDebugEnabled()){
			mLog.debug(">> Destroying the user session...");
		}
		//Cleaning the session
		getSession(aEvent.getConnector()).clear();

		aResponseEvent.setMessage("<< Logout process has finished successfully!");
		
		if (mLog.isDebugEnabled()){
			mLog.debug(">> Logoff successfully!");
		}
	}

	/**
	 * @return the am
	 */
	public AuthenticationManager getAm() {
		return am;
	}

	/**
	 * @param am the am to set
	 */
	public void setAm(AuthenticationManager am) {
		this.am = am;
	}
}
