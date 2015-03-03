//	---------------------------------------------------------------------------
//	jWebSocket - SamplePlugIn (Community Edition, CE)
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
package org.jwebsocket.client.plugins.sample;

import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketTokenClient;
import org.jwebsocket.client.plugins.BaseClientTokenPlugIn;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Alexander Schulze
 */
public class SamplePlugIn extends BaseClientTokenPlugIn {

	/**
	 *
	 */
	public static final String NS_SAMPLES = "org.jwebsocket.plugins.samples";

	/**
	 *
	 * @param aClient
	 */
	public SamplePlugIn(WebSocketTokenClient aClient) {
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

	/**
	 *
	 * @throws WebSocketException
	 */
	public void getRandom() throws WebSocketException {
		Token lToken = TokenFactory.createToken(NS_SAMPLES, "getRandom");
		getTokenClient().sendToken(lToken);
	}
}
