//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
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

import org.jwebsocket.kit.WebSocketSession;

/**
 *
 * @author kyberneees
 */
public interface ISessionReconnectionManager {

	/**
	 *
	 * @return The session expiration time after a client gets disconnected (minutes)
	 */
	Integer getSessionExpirationTime();

	/**
	 *
	 * @return Contains the sessions identifiers to be expired
	 */
	IBasicCacheStorage<String, Object> getReconnectionIndex();

	/**
	 *
	 * @param aSessionId The session identifier
	 * @return TRUE if the session has been expired, FALSE otherwise
	 */
	boolean isExpired(String aSessionId);

	/**
	 * Put a WebSocketSession in "reconnection mode", this means, the client
	 * data will be stored for a possible future reconnection. <br> The
	 * "session expiration time" setting is used
	 *
	 * @param aSessionId
	 */
	void putInReconnectionMode(WebSocketSession aSession);

	/**
	 *
	 * @return The session identifiers trash. Contains obsolete sessions
	 * identifiers
	 */
	IBasicStorage<String, Object> getSessionIdsTrash();

	/**
	 * @return the mStorageProvider
	 */
	IStorageProvider getStorageProvider();
}
