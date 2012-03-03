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
public class GetUser implements APDU {

	@Override
	public byte[] getBytes() {
		byte[] lAPDU = new byte[5];
		lAPDU[0] = (byte) 0x90;
		lAPDU[1] = (byte) 0x05;
		lAPDU[2] = (byte) 0x00;
		lAPDU[3] = (byte) 0x00;
		lAPDU[4] = (byte) 0x00;

		return lAPDU;
	}

	@Override
	public byte[] getData() {
		return null;
	}
}
