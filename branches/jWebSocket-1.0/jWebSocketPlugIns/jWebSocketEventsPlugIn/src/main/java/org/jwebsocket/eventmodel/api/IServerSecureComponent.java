//	---------------------------------------------------------------------------
//	jWebSocket - IServerSecureComponent (Community Edition, CE)
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
package org.jwebsocket.eventmodel.api;

import java.util.Set;

/**
 *
 * @author Rolando Santamaria Maso
 */
public interface IServerSecureComponent {

	/**
	 * Indicates if the security checks are enabled for this component
	 *
	 * @return the securityEnabled
	 */
	public boolean isSecurityEnabled();

	/**
	 * @param aSecurityEnabled the securityEnabled to set
	 */
	public void setSecurityEnabled(boolean aSecurityEnabled);

	/**
	 * The roles restrictions
	 *
	 * @return the roles
	 */
	public Set<String> getRoles();

	/**
	 * @param aRoles the roles to set
	 */
	public void setRoles(Set<String> aRoles);

	/**
	 * The users restrictions
	 *
	 * @return the users
	 */
	public Set<String> getUsers();

	/**
	 * @param aUsers the users to set
	 */
	public void setUsers(Set<String> aUsers);

	/**
	 * The IP addresses restrictions
	 *
	 * @return the ipAddresses
	 */
	public Set<String> getIpAddresses();

	/**
	 * @param aIpAddresses the ipAddresses to set
	 */
	public void setIpAddresses(Set<String> aIpAddresses);
}
