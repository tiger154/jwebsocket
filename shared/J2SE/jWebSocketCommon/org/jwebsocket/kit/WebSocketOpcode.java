//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketOpcode (Community Edition, CE)
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
public class WebSocketOpcode {

	/**
	 *
	 */
	public int OPCODE_INVALID = -1;
	// default for hybi draft 10
	/**
	 *
	 */
	public int OPCODE_FRAGMENT = 0x00;
	/**
	 *
	 */
	public int OPCODE_TEXT = 0x01;
	/**
	 *
	 */
	public int OPCODE_BINARY = 0x02;
	/**
	 *
	 */
	public int OPCODE_CLOSE = 0x08;
	/**
	 *
	 */
	public int OPCODE_PING = 0x09;
	/**
	 *
	 */
	public int OPCODE_PONG = 0x0A;

	/**
	 *
	 * @param aVersion
	 */
	public WebSocketOpcode(int aVersion) {
		if (aVersion >= 7) {
			// tested for hybi draft 10
			OPCODE_FRAGMENT = 0x00;
			OPCODE_TEXT = 0x01;
			OPCODE_BINARY = 0x02;
			OPCODE_CLOSE = 0x08;
			OPCODE_PING = 0x09;
			OPCODE_PONG = 0x0A;
		} else {
			// tested for hybi drafts < 7 
			OPCODE_FRAGMENT = 0x00;
			OPCODE_CLOSE = 0x01;
			OPCODE_PING = 0x02;
			OPCODE_PONG = 0x03;
			OPCODE_TEXT = 0x04;
			OPCODE_BINARY = 0x05;
		}
	}
}
