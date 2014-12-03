//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketJSONType (Community Edition, CE)
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
 * These enumeration specifies the supported data types for data exchange
 * between multiple platforms.
 *
 * @author Alexander Schulze
 */
public enum WebSocketJSONType {

	/**
	 * Invalid data type
	 */
	INVALID(-1),
	/**
	 * string
	 */
	STRING(0),
	/**
	 * number (64 bit float)
	 */
	NUMBER(2),
	/**
	 * boolean (true/false)
	 */
	BOOLEAN(3),
	/**
	 * date including time
	 */
	DATE(4),
	/**
	 * array
	 */
	ARRAY(5),
	/**
	 * object
	 */
	OBJECT(6),
	/**
	 * function
	 */
	FUNCTION(7);
	private int mJSONType;

	WebSocketJSONType(int aDataType) {
		mJSONType = aDataType;
	}

	/**
	 * @return the status int value
	 */
	public int getDataType() {
		return mJSONType;
	}
}
