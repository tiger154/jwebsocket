//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 Innotrade GmbH
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
package org.jwebsocket.client.plugins.sample;

import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.client.plugins.BaseClientTokenPlugIn;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author aschulze
 */
public class SamplePlugIn extends BaseClientTokenPlugIn {

	public static final String NS_SAMPLES = "org.jwebsocket.plugins.samples";

	public SamplePlugIn(BaseTokenClient aClient) {
		super(aClient, NS_SAMPLES);
	}

	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		// get the type of the token
		// the type can be associated with a "command"
		String lType = aToken.getType();

		// get the namespace of the token
		// each plug-in should have its own unique namespace
		String lNS = aToken.getNS();

		// check if token has a type and a matching namespace
		if (lType != null && lNS != null && lNS.equals(NS_SAMPLES)) {
			// here you can interpret incoming tokens from the server
			// according to the name space of the plug-in
		}
	}

	public void getRandom() throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_SAMPLES, "getRandom");
		getTokenClient().sendToken(lToken);
	}
}
