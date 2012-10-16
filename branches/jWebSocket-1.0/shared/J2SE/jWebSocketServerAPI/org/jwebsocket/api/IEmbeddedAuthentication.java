//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2012 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.api;

import java.net.InetAddress;

/**
 *
 * @author kyberneees
 */
public interface IEmbeddedAuthentication {

	/**
	 *
	 * @return The embedded authentication type
	 */
	String getAuthenticationType();

	/**
	 * @param aAuthority
	 * @return TRUE if the user has the given authority(commonly role), FALSE
	 * otherwise
	 */
	boolean hasAuthority(String aAuthority);

	/**
	 *
	 * @return The username
	 */
	String getUsername();

	/**
	 *
	 * @return TRUE if the user is authenticated, FALSE otherwise
	 */
	boolean isAuthenticated();

	/**
	 *
	 * @return The remote host InetAddress
	 */
	InetAddress getRemoteHost();
}
