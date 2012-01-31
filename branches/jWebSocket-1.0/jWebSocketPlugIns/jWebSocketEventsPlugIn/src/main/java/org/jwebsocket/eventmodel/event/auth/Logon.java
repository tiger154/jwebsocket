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
package org.jwebsocket.eventmodel.event.auth;

import org.jwebsocket.eventmodel.annotation.ImportFromToken;
import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 * The client order to log on 
 * 
 * @author kyberneees
 */
public class Logon extends C2SEvent {

	/**
	 * @return the username
	 */
	public String getUsername() {
		return getArgs().getString("username");
	}

	/**
	 * @param aUsername the username to set
	 */
	@ImportFromToken
	public void setUsername(String aUsername) {
		getArgs().setString("username", aUsername);
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return getArgs().getString("password");
	}

	/**
	 * @param aPassword the password to set
	 */
	@ImportFromToken
	public void setPassword(String aPassword) {
		getArgs().setString("password", aPassword);
	}
}
