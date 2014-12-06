//	---------------------------------------------------------------------------
//	jWebSocket - SampleServicePlugIn (Community Edition, CE)
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketTokenClient;
import org.jwebsocket.client.plugins.BaseServiceTokenPlugIn;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;

/**
 *
 * @author Rolando Betancourt Toucet
 */
public class SampleServicePlugIn extends BaseServiceTokenPlugIn {

	/**
	 *
	 * @param aClient
	 * @param aServiceNS
	 */
	public SampleServicePlugIn(WebSocketTokenClient aClient, String aServiceNS) {
		super(aClient, aServiceNS);
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
		if (lType != null && lNS != null) {

                        //&& lNS.equals(getNS())) {
			// here you can interpret incoming tokens from the server
			// according to the name space of the plug-in
			if (lNS.equals(getNS()) && lType.equals("test")) {
				test(aToken);
			}
			if (lNS.equals("org.jwebsocket.plugins.loadbalancer") && lType.equals("shutdown")) {
				shutdown();
			}
		}
	}

	/**
	 *
	 * @param aToken
	 */
	private void test(Token aToken) {
		try {
			Token lResponse = createResponse(aToken);
			getTokenClient().sendToken(lResponse);
		} catch (WebSocketException ex) {
			Logger.getLogger(SampleServicePlugIn.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 *
	 */
	private void shutdown() {
		try {
			getTokenClient().close();
		} catch (WebSocketException ex) {
			Logger.getLogger(BaseServiceTokenPlugIn.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
