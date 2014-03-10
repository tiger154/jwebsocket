//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketConnectorStatus (Community Edition, CE)
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
 * These enumeration specifies the supported data types for data exchange
 * between multiple platforms.
 *
 * @author Alexander Schulze
 */
public enum WebSocketConnectorStatus {

	/**
	 * connector is down, data cannot be send or received
	 */
	DOWN(0),
	/**
	 * connector is up, data can be send and received
	 */
	UP(1);
	private int mStatus;

	WebSocketConnectorStatus(int aStatus) {
		mStatus = aStatus;
	}

	/**
	 * @return the status int value
	 */
	public int getStatus() {
		return mStatus;
	}
}
