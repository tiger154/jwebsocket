//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketClientEvent (Community Edition, CE)
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

/**
 * Base interface jWebSocket events
 *
 * @author puran
 * @author Alexander Schulze
 * @version $Id: WebSocketClientEvent.java 702 2010-07-18 17:54:17Z
 * mailtopuran@gmail.com $
 */
public interface WebSocketClientEvent {

	/**
	 * Returns the name of the event.
	 *
	 * @return
	 */
	String getName();

	/**
	 * Returns the data (usually a message) for the event.
	 *
	 * @return
	 */
	String getData();

	/**
	 * Returns the WebSocket client which fired the event.
	 *
	 * @return
	 */
	WebSocketClient getClient();
}
