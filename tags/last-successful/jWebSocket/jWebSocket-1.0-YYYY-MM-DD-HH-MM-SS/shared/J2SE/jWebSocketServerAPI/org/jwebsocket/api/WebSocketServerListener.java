//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketServerListener (Community Edition, CE)
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

import org.jwebsocket.kit.WebSocketServerEvent;

/**
 * Interface for the low level WebSocket listeners.
 *
 * @author Alexander Schulze
 */
public interface WebSocketServerListener {

	/**
	 * This method is invoked when a new client connects to the server.
	 *
	 * @param aEvent
	 */
	public void processOpened(WebSocketServerEvent aEvent);

	/**
	 * This method is invoked when a data packet from a client is received. The
	 * event provides getter for the server and the connector to send responses
	 * to back the client.
	 *
	 * @param aEvent
	 * @param aPacket
	 */
	public void processPacket(WebSocketServerEvent aEvent, WebSocketPacket aPacket);

	/**
	 * This method is invoked when a client was disconnected from the server.
	 *
	 * @param aEvent
	 */
	public void processClosed(WebSocketServerEvent aEvent);
}
