//	---------------------------------------------------------------------------
//	jWebSocket - Raw Data Packet (Community Edition, CE)
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

import java.io.UnsupportedEncodingException;
import java.util.Date;
import org.jwebsocket.api.WebSocketPacket;

/**
 * Implements the low level data packets which are interchanged between client
 * and server. Data packets do not have a special format at this communication
 * level.
 *
 * @author Alexander Schulze
 */
public class RawPacket implements WebSocketPacket {

	private byte[] mData = null;
	// private String mUTF8 = null;
	private Date mCreationDate = null;
	private WebSocketFrameType mFrameType = WebSocketFrameType.TEXT;

	/**
	 * Instantiates a new data packet and initializes its value to the passed
	 * array of bytes.
	 *
	 * @param aByteArray byte array to be used as value for the data packet.
	 */
	public RawPacket(byte[] aByteArray) {
		setByteArray(aByteArray);
	}

	/**
	 * Instantiates a new data packet and initializes its value to the passed
	 * array of bytes.
	 *
	 * @param aFrameType
	 * @param aByteArray byte array to be used as value for the data packet.
	 */
	public RawPacket(WebSocketFrameType aFrameType, byte[] aByteArray) {
		setFrameType(aFrameType);
		setByteArray(aByteArray);
	}

	/**
	 * Instantiates a new data packet and initializes its value to the passed
	 * string using the default encoding.
	 *
	 * @param aString string to be used as value for the data packet.
	 */
	public RawPacket(String aString) {
		setString(aString);
	}

	/**
	 * Instantiates a new data packet and initializes its value to the passed
	 * string using the default encoding.
	 *
	 * @param aFrameType
	 * @param aString string to be used as value for the data packet.
	 */
	public RawPacket(WebSocketFrameType aFrameType, String aString) {
		setFrameType(aFrameType);
		setString(aString);
	}

	/**
	 * Instantiates a new data packet and initializes its value to the passed
	 * string using the passed encoding (should always be "UTF-8").
	 *
	 * @param aString string to be used as value for the data packet.
	 * @param aEncoding should always be "UTF-8"
	 * @throws UnsupportedEncodingException
	 */
	public RawPacket(String aString, String aEncoding)
			throws UnsupportedEncodingException {
		setString(aString, aEncoding);
	}

	@Override
	public final void setByteArray(byte[] aByteArray) {
		mData = aByteArray;
	}

	@Override
	public final void setString(String aString) {
		mData = aString.getBytes();
	}

	@Override
	public final void setString(String aString, String aEncoding)
			throws UnsupportedEncodingException {
		mData = aString.getBytes(aEncoding);
	}

	@Override
	public final void setUTF8(String aString) {
		try {
			mData = aString.getBytes("UTF-8");
		} catch (UnsupportedEncodingException lEx) {
			// ignore exception here
		}
	}

	@Override
	public final void setASCII(String aString) {
		try {
			mData = aString.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException lEx) {
			// ignore exception here
		}
	}

	@Override
	public final byte[] getByteArray() {
		return mData;
	}

	@Override
	public String toString() {
		return getString();
	}

	@Override
	public final String getString() {
		return new String(mData);
	}

	@Override
	public final String getString(String aEncoding)
			throws UnsupportedEncodingException {
		return new String(mData, aEncoding);
	}

	@Override
	public final String getUTF8() {
		try {
			return new String(mData, "UTF-8");
		} catch (UnsupportedEncodingException lEx) {
			return null;
		}
	}

	@Override
	public final String getASCII() {
		try {
			return new String(mData, "US-ASCII");
		} catch (UnsupportedEncodingException lEx) {
			return null;
		}
	}

	/**
	 * @return the frameType
	 */
	@Override
	public final WebSocketFrameType getFrameType() {
		return mFrameType;
	}

	/**
	 * @param aFrameType the frameType to set
	 */
	@Override
	public final void setFrameType(WebSocketFrameType aFrameType) {
		this.mFrameType = aFrameType;
	}

	@Override
	public final boolean isFragment() {
		return mFrameType.equals(WebSocketFrameType.FRAGMENT);
	}

	@Override
	public final void setCreationDate(Date aDate) {
		mCreationDate = aDate;
	}

	@Override
	public final Date getCreationDate() {
		return mCreationDate;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Integer size() {
		return mData.length;
	}
}
