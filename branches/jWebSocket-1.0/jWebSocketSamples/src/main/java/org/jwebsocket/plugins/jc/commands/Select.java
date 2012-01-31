//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.jc.commands;

import org.jwebsocket.plugins.jc.api.APDU;

/**
 *
 * @author kyberneees
 */
public class Select implements APDU {

	private byte[] mAppName;

	public Select(byte[] aAppName) {
		this.mAppName = aAppName;
	}

	@Override
	public byte[] getBytes() {

		byte[] lAPDU = new byte[5 + mAppName.length];
		lAPDU[0] = (byte) 0x0;
		lAPDU[1] = (byte) 0xA4;
		lAPDU[2] = (byte) 0x4;
		lAPDU[3] = (byte) 0x0;
		lAPDU[4] = (byte) mAppName.length;

		System.arraycopy(mAppName, 0, lAPDU, 5, mAppName.length);

		return lAPDU;
	}

	@Override
	public byte[] getData() {
		return mAppName;
	}
}
