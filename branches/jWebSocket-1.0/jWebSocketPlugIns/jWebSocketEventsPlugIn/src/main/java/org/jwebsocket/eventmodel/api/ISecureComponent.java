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
package org.jwebsocket.eventmodel.api;

import java.util.Set;

/**
 *
 * @author kyberneees
 */
public interface ISecureComponent {

	/**
	 * Indicates if the security checks are enabled for this component
	 * 
	 * @return the securityEnabled
	 */
	public boolean isSecurityEnabled();

	/**
	 * @param securityEnabled the securityEnabled to set
	 */
	public void setSecurityEnabled(boolean securityEnabled);

	/**
	 * The roles restrictions
	 * 
	 * @return the roles
	 */
	public Set<String> getRoles();

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(Set<String> roles);

	/**
	 * The users restrictions
	 * 
	 * @return the users
	 */
	public Set<String> getUsers();

	/**
	 * @param users the users to set
	 */
	public void setUsers(Set<String> users);

	/**
	 * The IP addresses restrictions
	 * 
	 * @return the ipAddresses
	 */
	public Set<String> getIpAddresses();

	/**
	 * @param ipAddresses the ipAddresses to set
	 */
	public void setIpAddresses(Set<String> ipAddresses);
}
