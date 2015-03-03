//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketServerEvent (Community Edition, CE)
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
package org.jwebsocket.kit;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketServer;

/**
 *
 * @author Alexander Schulze
 */
public class WebSocketServerEvent {

	private WebSocketServer mServer = null;
	private WebSocketConnector mConnector = null;

	/**
	 *
	 * @param aConnector
	 * @param aServer
	 */
	public WebSocketServerEvent(WebSocketConnector aConnector, WebSocketServer aServer) {
		mConnector = aConnector;
		mServer = aServer;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return mConnector.getSession().getSessionId();
	}

	/**
	 * @return the session
	 */
	public WebSocketSession getSession() {
		return mConnector.getSession();
	}

	/**
	 * @return the server
	 */
	public WebSocketServer getServer() {
		return mServer;
	}

	/**
	 * @return the connector
	 */
	public WebSocketConnector getConnector() {
		return mConnector;
	}

	/**
	 *
	 * @param aPacket
	 */
	public void sendPacket(WebSocketPacket aPacket) {
		mServer.sendPacket(mConnector, aPacket);
	}
}
