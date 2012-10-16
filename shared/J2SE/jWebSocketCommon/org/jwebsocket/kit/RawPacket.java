//	---------------------------------------------------------------------------
//	jWebSocket - Raw Data Packet Implementation
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
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
 * @author aschulze
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

	@Override
	public Integer size() {
		return mData.length;
	}
}
