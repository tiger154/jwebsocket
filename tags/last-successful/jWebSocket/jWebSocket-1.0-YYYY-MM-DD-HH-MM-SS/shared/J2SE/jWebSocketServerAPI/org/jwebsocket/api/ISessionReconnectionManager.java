//	---------------------------------------------------------------------------
//	jWebSocket - ISessionReconnectionManager (Community Edition, CE)
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

import org.jwebsocket.kit.WebSocketSession;

/**
 *
 * @author Rolando Santamaria Maso
 */
public interface ISessionReconnectionManager {

	/**
	 *
	 * @return The session expiration time after a client gets disconnected
	 * (minutes)
	 */
	Integer getSessionExpirationTime();

	/**
	 *
	 * @param aSessionId The session identifier
	 * @return TRUE if the session has been expired, FALSE otherwise
	 */
	boolean isExpired(String aSessionId);

	/**
	 * Put a WebSocketSession in "reconnection mode", this means, the client
	 * data will be stored for a possible future reconnection. <br> The "session
	 * expiration time" setting is used
	 *
	 * @param aSession
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
