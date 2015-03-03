//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketJavaType (Community Edition, CE)
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
public enum WebSocketJavaType {

	/**
	 * Invalid data type
	 */
	INVALID(-1),
	/**
	 *
	 */
	BTYE(0),
	/**
	 *
	 */
	SHORT(1),
	/**
	 *
	 */
	INTEGER(2),
	/**
	 *
	 */
	FLOAT(3),
	/**
	 *
	 */
	DOUBLE(4),
	/**
	 *
	 */
	BIGDECIMAL(5),
	/**
	 *
	 */
	BOOLEAN(6),
	/**
	 *
	 */
	STRING(7),
	/**
	 *
	 */
	DATE(8),
	/**
	 *
	 */
	TIME(9),
	/**
	 *
	 */
	TIMESTAMP(10),
	/**
	 *
	 */
	BLOB(11),
	/**
	 *
	 */
	CLOB(12),
	/**
	 *
	 */
	ARRAY(13),
	/**
	 *
	 */
	LIST(14),
	/**
	 *
	 */
	MAP(15);
	private int mJavaType;

	WebSocketJavaType(int aDataType) {
		mJavaType = aDataType;
	}

	/**
	 * @return the status int value
	 */
	public int getDataType() {
		return mJavaType;
	}
}
