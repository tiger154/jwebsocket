//  ---------------------------------------------------------------------------
//  jWebSocket - Copyright (c) 2010 Innotrade GmbH
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.api;

/**
 * WebSocket connection status. These status values are based on 
 * HTML5 WebSocket API specification
 * {@linkplain http://dev.w3.org/html5/websockets/#websocket}
 * 
 * @author puran, aschulze
 */
public enum WebSocketStatus {

	/** The connection has not yet been established. */
	CONNECTING(0),
	/** The WebSocket connection is established and communication is possible */
	OPEN(1),
	/** The connection is going through the closing handshake. */
	CLOSING(2),
	/** The connection has been closed or could not be opened. */
	CLOSED(3),
	/** The connection manager is trying to re-connect, but not yet connected.
	This is jWebSocket specific and not part of the W3C API. */
	RECONNECTING(1000),
	/** The connection is established and authenticated.
	This is jWebSocket specific and not part of the W3C API. */
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
