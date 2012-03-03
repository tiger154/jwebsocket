//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.eventmodel.plugin.jc;

import javax.smartcardio.ResponseAPDU;
import org.apache.commons.codec.binary.Base64;
import org.jwebsocket.eventmodel.s2c.OnResponse;
import org.jwebsocket.util.Tools;

/**
 *
 * @author kyberneees
 */
public abstract class JcResponseCallback extends OnResponse {

	public JcResponseCallback(Object aContext) {
		super(aContext);
	}

	/**
	 * Execute custom validations in client responses 
	 * 
	 * @param aResponse The response to validate
	 * @param aFrom The target client connector
	 * @return
	 */
	@Override
	public boolean isValid(Object aResponse, String aFrom) {
		return isValid(new ResponseAPDU(Tools.hexStringToByteArray(aResponse.toString())), aFrom);
	}

	/**
	 * Execute custom validations for client card calls responses 
	 * 
	 * @param aResponse The response to validate
	 * @param aFrom The target client connector
	 * @return
	 */
	public boolean isValid(ResponseAPDU aResponse, String aFrom) {
		if (aResponse.getSW() == 0x9000) {
			return true;
		}
		
		return false;
	}

	/**
	 * Callback used to handle the success response from the client
	 * 
	 * @param aResponse The response returned by the client-side 
	 * @param aFrom The target client connector
	 */
	@Override
	public void success(Object aResponse, String aFrom) {
		success(new ResponseAPDU(Tools.hexStringToByteArray(aResponse.toString())), aFrom);
	}

	/**
	 * Callback used to handle the success response from the client card
	 * 
	 * @param aResponse The response returned by the client-side 
	 * @param aFrom The target client connector
	 */
	public void success(ResponseAPDU aResponse, String aFrom) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
