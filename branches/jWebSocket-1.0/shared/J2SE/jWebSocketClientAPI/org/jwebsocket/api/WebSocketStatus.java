//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketStatus (Community Edition, CE)
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
 * WebSocket connection status. These status values are based on HTML5 WebSocket
 * API specification {@linkplain http://dev.w3.org/html5/websockets/#websocket}
 *
 * @author Alexander Schulze, Puran Singh
 */
public enum WebSocketStatus {

	/**
	 * The connection has not yet been established.
	 */
	CONNECTING(0),
	/**
	 * The WebSocket connection is established and communication is possible
	 */
	OPEN(1),
	/**
	 * The connection is going through the closing handshake.
	 */
	CLOSING(2),
	/**
	 * The connection has been closed or could not be opened.
	 */
	CLOSED(3),
	/**
	 * The connection manager is trying to re-connect, but not yet connected.
	 * This is jWebSocket specific and not part of the W3C API.
	 */
	RECONNECTING(1000),
	/**
	 * The connection is established and authenticated. This is jWebSocket
	 * specific and not part of the W3C API.
	 */
	AUTHENTICATED(1001);
	private int mStatus;

	WebSocketStatus(int aStatus) {
		this.mStatus = aStatus;
	}

	/**
	 * @return the status int value
	 */
	public int getStatus() {
		return mStatus;
	}

	/**
	 *
	 * @return
	 */
	public boolean isWritable() {
		return (this.equals(OPEN)
				|| this.equals(AUTHENTICATED));
	}

	/**
	 *
	 * @return
	 */
	public boolean isConnected() {
		return (this.equals(OPEN)
				|| this.equals(AUTHENTICATED));
	}

	/**
	 *
	 * @return
	 */
	public boolean isAuthenticated() {
		return (this.equals(AUTHENTICATED));
	}

	/**
	 *
	 * @return
	 */
	public boolean isClosable() {
		return (!this.equals(CLOSED));
	}

	/**
	 *
	 * @return
	 */
	public boolean isClosed() {
		return (this.equals(CLOSED));
	}
}
