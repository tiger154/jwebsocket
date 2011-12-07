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
	 * @param response The response to validate
	 * @param from The target client connector
	 * @return
	 */
	@Override
	public boolean isValid(Object response, String from) {
		return isValid(new ResponseAPDU(Base64.decodeBase64(response.toString())), from);
	}

	/**
	 * Execute custom validations for client card calls responses 
	 * 
	 * @param response The response to validate
	 * @param from The target client connector
	 * @return
	 */
	public boolean isValid(ResponseAPDU response, String from) {
		return true;
	}

	/**
	 * Callback used to handle the success response from the client
	 * 
	 * @param response The response returned by the client-side 
	 * @param from The target client connector
	 */
	@Override
	public void success(Object response, String from) {
		success(new ResponseAPDU(Base64.decodeBase64(response.toString())), from);
	}

	/**
	 * Callback used to handle the success response from the client card
	 * 
	 * @param response The response returned by the client-side 
	 * @param from The target client connector
	 */
	public void success(ResponseAPDU response, String from) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
