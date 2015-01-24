//	---------------------------------------------------------------------------
//	jWebSocket - ISessionManager (Community Edition, CE)
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

import org.jwebsocket.kit.CloseReason;

/**
 * Provides methods to retrieve sessions from the global session storage.
 *
 * The session storage is a persistence engine.
 *
 * @author Rolando Santamaria Maso, Alexander Schulze
 */
public interface ISessionManager extends IInitializable {

	/**
	 *
	 * @param aConnector
	 * @return
	 * @throws Exception
	 */
	IBasicStorage<String, Object> getSession(WebSocketConnector aConnector) throws Exception;

	/**
	 *
	 * @param aSessionId
	 * @return
	 * @throws Exception
	 */
	IBasicStorage<String, Object> getSession(String aSessionId) throws Exception;

	/**
	 *
	 * @return
	 */
	ISessionReconnectionManager getReconnectionManager();

	/**
	 *
	 * @return
	 */
	IStorageProvider getStorageProvider();

	/**
	 *
	 * @param aConnector
	 * @throws Exception
	 */
	void connectorStarted(WebSocketConnector aConnector) throws Exception;

	/**
	 *
	 * @param aConnector
	 * @param aCloseReason
	 * @param aIsSessionShared
	 * @throws Exception
	 */
	void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason, boolean aIsSessionShared) throws Exception;
}
