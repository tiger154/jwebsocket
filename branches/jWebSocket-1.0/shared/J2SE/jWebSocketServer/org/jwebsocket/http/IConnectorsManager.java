//	---------------------------------------------------------------------------
//	jWebSocket - IConnectorsManager (Community Edition, CE)
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
package org.jwebsocket.http;

import java.util.Iterator;
import java.util.Map;
import org.jwebsocket.api.IConnectorsPacketQueue;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.ISessionManager;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;

/**
 *
 * @author kyberneees
 */
public interface IConnectorsManager extends IInitializable {

	WebSocketConnector add(String aSessionId, String aConnectionId) throws Exception;

	boolean connectorExists(String aConnectorId);

	Long count();

	Map<String, WebSocketConnector> getAll() throws Exception;

	WebSocketConnector getConnectorById(String aConnectorId) throws Exception;

	WebSocketConnector getConnectorById(String aConnectorId, boolean aStartupConnection) throws Exception;

	WebSocketConnector getConnectorBySessionId(String aSessionId) throws Exception;

	WebSocketEngine getEngine();

	IConnectorsPacketQueue getPacketsQueue();

	ISessionManager getSessionManager();

	Map<String, WebSocketConnector> getSharedSession(String aSessionId) throws Exception;

	void remove(String aConnectorId) throws Exception;

	boolean sessionExists(String aSessionId);

	void setEngine(WebSocketEngine aEngine);

	void setPacketsQueue(IConnectorsPacketQueue aPacketsQueue);

	Iterator<WebSocketConnector> getIterator();
}
