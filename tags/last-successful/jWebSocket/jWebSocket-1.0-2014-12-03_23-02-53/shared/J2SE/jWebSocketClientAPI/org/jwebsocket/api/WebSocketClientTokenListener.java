//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketClientTokenListener (Community Edition, CE)
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

import org.jwebsocket.token.Token;

/**
 * Interface for the Token WebSocket listeners based on the low level listeners.
 *
 * @author Alexander Schulze
 */
public interface WebSocketClientTokenListener extends WebSocketClientListener {

	/**
	 * This method is invoked when a token (JSON, CSV or XML) from a client is
	 * received. The event provides getter for the Client and the connector to
	 * send responses to back the client.
	 *
	 * @param aEvent
	 * @param aToken
	 */
	public void processToken(WebSocketClientEvent aEvent, Token aToken);
}
