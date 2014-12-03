//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketDataType (Community Edition, CE)
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
public enum WebSocketDataType {

	/**
	 * Invalid data type
	 */
	INVALID(-1),
	/**
	 * UTF formatted text
	 */
	TEXT(0),
	/**
	 * binary data (byte array)
	 */
	BINARY(1),
	/**
	 * 32 bit integer
	 */
	INTEGER(2),
	/**
	 * 8 bit byte
	 */
	BYTE(3),
	/**
	 * 64 long integer
	 */
	LONG(4),
	/**
	 * normal precision float
	 */
	FLOAT(5),
	/**
	 * double precision float
	 */
	DOUBLE(6),
	/**
	 * date without time
	 */
	DATE(7),
	/**
	 * time with outdate
	 */
	TIME(8),
	/**
	 * date and date
	 */
	TIMESTAMP(9),
	/**
	 * boolean (true/false)
	 */
	BOOLEAN(10),
	/**
	 * list of objects (only WebSocketDataTypes allowed)
	 */
	LIST(11),
	/**
	 * map of objects (only WebSocketDataTypes allowed)
	 */
	MAP(12);
	private int mDataType;

	WebSocketDataType(int aDataType) {
		mDataType = aDataType;
	}

	/**
	 * @return the status int value
	 */
	public int getDataType() {
		return mDataType;
	}
}
