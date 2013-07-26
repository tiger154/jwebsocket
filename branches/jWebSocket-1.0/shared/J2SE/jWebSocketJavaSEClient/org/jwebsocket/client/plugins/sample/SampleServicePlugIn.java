//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer Cluster (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//      Alexander Schulze, Germany (NRW)
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
import org.jwebsocket.client.plugins.BaseServiceTokenPlugIn;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;

/**
 *
 * @author rbetancourt
 */
public class SampleServicePlugIn extends BaseServiceTokenPlugIn {

	/**
	 *
	 */
	public static final String NS_SAMPLESERVICE = "org.jwebsocket.plugins.sampleservice1";

	/**
	 *
	 * @param aClient
	 */
	public SampleServicePlugIn(BaseTokenClient aClient) {
		super(aClient, NS_SAMPLESERVICE);
	}

	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		// get the type of the token
		// the type can be associated with a "command"
		String lType = aToken.getType();
		
		if (aToken.getType().equals("response")) {
			System.out.println(aToken.toString());
		}
		// get the namespace of the token
		// each plug-in should have its own unique namespace
		String lNS = aToken.getNS();

		// check if token has a type and a matching namespace
		if (lType != null && lNS != null && lNS.equals(NS_SAMPLESERVICE)) {
			// here you can interpret incoming tokens from the server
			// according to the name space of the plug-in
			if (lType.equals("echo")) {
				echo(aToken);
			}
		}
	}

	/**
	 *
	 * @param aToken
	 */
	private void echo(Token aToken) {
		String lData = aToken.getString("data");
		Token lResponse = createResponse(aToken);
		lResponse.setString("data", lData);
		try {
			getTokenClient().sendToken(lResponse);
		} catch (WebSocketException ex) {
			Logger.getLogger(SampleServicePlugIn.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
