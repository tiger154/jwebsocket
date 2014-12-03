//	---------------------------------------------------------------------------
//	jWebSocket - IEmbeddedAuthentication (Community Edition, CE)
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
package org.jwebsocket.api;

import java.net.InetAddress;

/**
 *
 * @author Rolando Santamaria Maso
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
