//	---------------------------------------------------------------------------
//	jWebSocket - JMS Gateway Base Service Endpoint Listener (Community Edition, CE)
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
package org.jwebsocket.jms.endpoint.service;

import java.lang.reflect.Method;
import org.jwebsocket.jms.endpoint.JWSMessageListener;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class JWSServiceEndPointListener extends JWSMessageListener {

	JWSBaseServiceEndPoint mService;
	String mGatewayId;

	public JWSServiceEndPointListener(JWSBaseServiceEndPoint aService) {
		super(aService.getEndPoint());
		mService = aService;
		mGatewayId = mService.getEndPoint().getGatewayId();
	}

	@Override
	public void processToken(String aSourceId, Token aToken) {
		try {
			// processing annotations
			Method lMethod = getClass().getMethod("processToken", String.class, String.class, Token.class, Token.class);

			// creating request response
			Token lResponse = Tools.createLoadBalancerResponse(aToken);

			// getting the remote client username
			String lUser = mService.getAuthManager().authenticate(aToken);
			if (lMethod.isAnnotationPresent(Authenticated.class)) {
				if (null == lUser) {
					lResponse.setCode(-1);
					lResponse.setString("msg", "access denied");

					mService.getEndPoint().sendToken(mGatewayId, lResponse);
					return;
				}
			}
			// invoking service 'processToken' method wrapper
			processToken(aSourceId, lUser, aToken, lResponse);

			// sending service populated response
			sendToken(mGatewayId, lResponse);
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}
	}

	public void processToken(String aSourceId, String aUser, Token aRequest, Token aResponse) throws Exception {

	}

}
