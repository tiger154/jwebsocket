//	---------------------------------------------------------------------------
//	jWebSocket - IInternalConnectorListener (Community Edition, CE)
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
import org.jwebsocket.token.Token;

/**
 *
 * @author Rolando Santamaria Maso
 */
public interface IInternalConnectorListener {

	/**
	 * Called when a data packet is received from the server
	 *
	 * @param aPacket
	 */
	void processPacket(WebSocketPacket aPacket);

	/**
	 * Called when a token is received from the server
	 *
	 * @param aToken
	 */
	void processToken(Token aToken);

	/**
	 * Called when the welcome token is received from the server
	 *
	 * @param aToken
	 */
	void processWelcome(Token aToken);

	/**
	 * Called when the connection has been closed.
	 *
	 * @param aReason
	 */
	void processClosed(CloseReason aReason);

	/**
	 * Called when the connection has been opened.
	 */
	void processOpened();
}
