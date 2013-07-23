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
package org.jwebsocket.client.plugins;

import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author rbetancourt
 */
public class BaseServiceTokenPlugIn extends BaseClientTokenPlugIn {

	/**
	 *
	 * @param aClient
	 */
	public BaseServiceTokenPlugIn(BaseTokenClient aClient, String aNS) {
		super(aClient, aNS);
	}

	public Token createResponse(Token aInToken) {
		Token lResToken = TokenFactory.createToken();
		setResponseFields(aInToken, lResToken);
		return lResToken;
	}

	public void setResponseFields(Token aInToken, Token aOutToken) {
		Integer lTokenId = null;
		String lType = null;
		String lNS = null;
		String lSourceID = null;
		if (aInToken != null) {
			lTokenId = aInToken.getInteger("utid", -1);
			lType = aInToken.getString("type");
			lNS = JWebSocketServerConstants.NS_BASE + ".plugins.loadbalancer";
			lSourceID = aInToken.getString("sourceId");
		}
		aOutToken.setType("response");

		// if code and msg are already part of outgoing token do not overwrite!
		aOutToken.setInteger("code", aOutToken.getInteger("code", 0));
		aOutToken.setString("msg", aOutToken.getString("msg", "ok"));

		if (lTokenId != null) {
			aOutToken.setInteger("utid", lTokenId);
		}
		if (lNS != null) {
			aOutToken.setString("ns", lNS);
		}
		if (lType != null) {
			aOutToken.setString("reqType", lType);
		}
		if (lSourceID != null) {
			aOutToken.setString("sourceId", lSourceID);
		}
	}
}
