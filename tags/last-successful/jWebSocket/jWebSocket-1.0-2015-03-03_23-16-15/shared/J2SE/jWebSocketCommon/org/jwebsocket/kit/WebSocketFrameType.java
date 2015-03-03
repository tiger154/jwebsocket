//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketFrameType (Community Edition, CE)
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

/**
 *
 * @author Alexander Schulze
 */
public enum WebSocketFrameType {

	/**
	 *
	 */
	INVALID(-1),
	/**
	 *
	 */
	FRAGMENT(0x00),
	/**
	 *
	 */
	TEXT(0x01),
	/**
	 *
	 */
	BINARY(0x02),
	/**
	 *
	 */
	CLOSE(0x08),
	/**
	 *
	 */
	PING(0x09),
	/**
	 *
	 */
	PONG(0x0A);
	private final int mFrameType;

	WebSocketFrameType(int aFrameType) {
		mFrameType = aFrameType;
	}

	/**
	 * @return the status int value
	 */
	public int getFrameType() {
		return mFrameType;
	}
}
