//	---------------------------------------------------------------------------
//	jWebSocket - SampleHttpServlet (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.appserver;

import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.token.Token;

/**
 * Sample jWebSocket plug-in
 *
 * @author kyberneees
 */
public class SampleWebSocketPlugIn extends ActionPlugIn {

	public SampleWebSocketPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(aConfiguration.getNamespace());
	}

	public void sayHelloAction(WebSocketConnector aConnector, Token aRequest) throws Exception {
		String lName = aRequest.getString("name");

		Token lResponse = createResponse(aRequest);
		lResponse.setString("data", "Hello '" + lName + "', from a jWebSocket plug-in ;)");

		sendToken(aConnector, lResponse);
	}
}
