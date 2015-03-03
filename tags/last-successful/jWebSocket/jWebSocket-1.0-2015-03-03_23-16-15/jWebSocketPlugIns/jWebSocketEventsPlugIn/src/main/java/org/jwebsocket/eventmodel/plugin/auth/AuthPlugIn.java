//	---------------------------------------------------------------------------
//	jWebSocket - AuthPlugIn (Community Edition, CE)
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
package org.jwebsocket.eventmodel.plugin.auth;

import java.util.Map;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IUserUniqueIdentifierContainer;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.event.auth.Logoff;
import org.jwebsocket.eventmodel.event.auth.Logon;
import org.jwebsocket.eventmodel.event.auth.UserLogoff;
import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.eventmodel.util.Util;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class AuthPlugIn extends EventModelPlugIn {

	private AuthenticationManager mAuthenticationManager;
	private AuthenticationProvider mAuthenticationProvider;
	private static Logger mLog = Logging.getLogger(AuthPlugIn.class);

	/**
	 *
	 */
	public AuthPlugIn() {
		//Registering internal events
		addEvents(UserLogoff.class);
	}

	/**
	 * The login process
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(Logon aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		String lUsername = aEvent.getUsername();
		String lPassword = aEvent.getPassword();

		WebSocketConnector lConnector = aEvent.getConnector();

		//Login process
		Authentication lRequest = new UsernamePasswordAuthenticationToken(lUsername, lPassword);
		Authentication lAuthentication;

		if (null != mAuthenticationProvider) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Authenticating with the '" + mAuthenticationProvider.getClass().
						getName() + "' authentication provider...");
			}
			lAuthentication = mAuthenticationProvider.authenticate(lRequest);
		} else {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Authenticating with the '" + mAuthenticationManager.getClass().
						getName() + "' authentication manager...");
			}
			lAuthentication = mAuthenticationManager.authenticate(lRequest);
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Updating the user session...");
		}
		//Getting the user session
		Map<String, Object> lSession = lConnector.getSession().getStorage();

		//Setting the is_authenticated flag
		lSession.put(SystemPlugIn.IS_AUTHENTICATED, lAuthentication.isAuthenticated());

		//Setting the username
		lSession.put(SystemPlugIn.USERNAME, lUsername);

		//Setting the username in the connector instance...
		lConnector.setUsername(lUsername);

		//Setting the uuid
		String lUUID;
		Object lDetails = lAuthentication.getDetails();
		if (null != lDetails && lDetails instanceof IUserUniqueIdentifierContainer) {
			lUUID = ((IUserUniqueIdentifierContainer) lDetails).getUUID();
		} else {
			lUUID = lUsername;
		}
		lSession.put(SystemPlugIn.UUID, lUUID);

		//Setting the uuid in the connectot instance...
		lConnector.setString(SystemPlugIn.UUID, lUUID);

		//Setting the roles
		String lRoles = "";
		for (GrantedAuthority lGrantedAuthority : lAuthentication.getAuthorities()) {
			lRoles = lRoles.concat(lGrantedAuthority.getAuthority() + " ");
		}
		lSession.put(SystemPlugIn.AUTHORITIES, lRoles);

		//Creating the response
		aResponseEvent.getArgs().setString("uuid", lUUID);
		aResponseEvent.getArgs().setString("username", lUsername);
		aResponseEvent.getArgs().setList("roles", Util.parseStringArrayToList(lRoles.split(" ")));

		if (mLog.isDebugEnabled()) {
			mLog.debug("Logon successfully!");
		}
	}

	/**
	 * The logout process
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(Logoff aEvent, C2SResponseEvent aResponseEvent) throws Exception {
		WebSocketConnector lConnector = aEvent.getConnector();
		String lUUID = lConnector.getString(SystemPlugIn.UUID);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Loging off the user '" + lUUID + "...");
		}

		//Cleaning the session
		lConnector.getSession().getStorage().clear();
		lConnector.setUsername(null);
		lConnector.setString(SystemPlugIn.UUID, null);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Logoff successfully!");
		}

		//Notify internal listeners about the UserLogoff event...
		notify(new UserLogoff(lUUID), null, true);
	}

	/**
	 * @return The AuthenticationManager
	 */
	public AuthenticationManager getAuthenticationManager() {
		return mAuthenticationManager;
	}

	/**
	 * @param aAuthenticationManager
	 */
	public void setAuthenticationManager(AuthenticationManager aAuthenticationManager) {
		this.mAuthenticationManager = aAuthenticationManager;
	}

	/**
	 *
	 * @return The AuthenticationProvider
	 */
	public AuthenticationProvider getAuthenticationProvider() {
		return mAuthenticationProvider;
	}

	/**
	 *
	 *
	 * @param aAuthenticationProvider
	 */
	public void setAuthenticationProvider(AuthenticationProvider aAuthenticationProvider) {
		this.mAuthenticationProvider = aAuthenticationProvider;
	}
}
