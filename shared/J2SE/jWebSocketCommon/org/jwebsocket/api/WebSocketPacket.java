//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketPacket (Community Edition, CE)
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

import java.io.UnsupportedEncodingException;
import java.util.Date;
import org.jwebsocket.kit.WebSocketFrameType;

/**
 * Specifies the API for low level data packets which are interchanged between
 * client and server. Data packets do not have a special format at this
 * communication level.
 *
 * @author Alexander Schulze
 */
public interface WebSocketPacket {

	/**
	 * Sets the value of the data packet to the given array of bytes.
	 *
	 * @param aByteArray
	 */
	void setByteArray(byte[] aByteArray);

	/**
	 *
	 * @return
	 */
	boolean isFragment();

	/**
	 *
	 * @param aDate
	 */
	void setCreationDate(Date aDate);

	/**
	 *
	 * @return
	 */
	Date getCreationDate();

	/**
	 * Sets the value of the data packet to the given string by using default
	 * encoding.
	 *
	 * @param aString
	 */
	void setString(String aString);

	/**
	 * Sets the value of the data packet to the given string by using the passed
	 * encoding.
	 *
	 * @param aString
	 * @param aEncoding
	 * @throws UnsupportedEncodingException
	 */
	void setString(String aString, String aEncoding) throws UnsupportedEncodingException;

	/**
	 * Sets the value of the data packet to the given string by using UTF-8
	 * encoding.
	 *
	 * @param aString
	 */
	void setUTF8(String aString);

	/**
	 * Sets the value of the data packet to the given string by using 7 bit
	 * US-ASCII encoding.
	 *
	 * @param aString
	 */
	void setASCII(String aString);

	/**
	 * Returns the content of the data packet as an array of bytes.
	 *
	 * @return Data packet as array of bytes.
	 */
	byte[] getByteArray();

	/**
	 * Returns the content of the data packet as a string using default
	 * encoding.
	 *
	 * @return Raw Data packet as string with default encoding.
	 */
	String getString();

	/**
	 * Returns the content of the data packet as a string using the passed
	 * encoding.
	 *
	 * @param aEncoding
	 * @return String using the passed encoding.
	 * @throws UnsupportedEncodingException
	 */
	String getString(String aEncoding) throws UnsupportedEncodingException;

	/**
	 * Interprets the data packet as a UTF8 string and returns the string in
	 * UTF-8 encoding. If an exception occurs "null" is returned.
	 *
	 * @return Data packet as UTF-8 string or {@code null} if not convertible.
	 */
	String getUTF8();

	/**
	 * Interprets the data packet as a US-ASCII string and returns the string in
	 * US-ASCII encoding. If an exception occurs "null" is returned.
	 *
	 * @return Data packet as US-ASCII string or {@code null} if not
	 * convertible.
	 */
	String getASCII();

	/**
	 *
	 * @return
	 */
	WebSocketFrameType getFrameType();

	/**
	 *
	 * @param aFrameType
	 */
	void setFrameType(WebSocketFrameType aFrameType);

	/**
	 *
	 * @return
	 */
	Integer size();
}
