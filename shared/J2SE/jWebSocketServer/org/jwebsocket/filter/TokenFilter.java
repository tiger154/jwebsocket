//	---------------------------------------------------------------------------
//	jWebSocket - TokenFilter (Community Edition, CE)
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
package org.jwebsocket.filter;

import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.ChangeType;
import org.jwebsocket.kit.FilterResponse;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 * @author Marcos Antonio Gonzalez Huerta
 */
public class TokenFilter extends BaseFilter {

	/**
	 *
	 * @param configuration
	 */
	public TokenFilter(FilterConfiguration configuration) {
		super(configuration);
	}

	@Override
	public void processPacketIn(FilterResponse aResponse, WebSocketConnector aConnector, WebSocketPacket aPacket) {
	}

	@Override
	public void processPacketOut(FilterResponse aResponse, WebSocketConnector aSource, WebSocketConnector aTarget, WebSocketPacket aPacket) {
	}

	/**
	 *
	 * @param aResponse
	 * @param aConnector
	 * @param aToken
	 */
	public void processTokenIn(FilterResponse aResponse, WebSocketConnector aConnector, Token aToken) {
	}

	/**
	 *
	 * @param aResponse
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	public void processTokenOut(FilterResponse aResponse, WebSocketConnector aSource, WebSocketConnector aTarget, Token aToken) {
	}

	/**
	 *
	 * @param aResponse
	 * @param aType
	 * @param aVersion
	 * @param aReason
	 */
	public void createReasonOfChange(Token aResponse, ChangeType aType, String aVersion, String aReason) {
		aResponse.setNS(getFilterConfiguration().getNamespace());
		aResponse.setType("processChangeOfPlugIn");
		aResponse.setString("changeType", aType.toString());
		aResponse.setString("version", aVersion);
		aResponse.setString("reason", aReason);
		aResponse.setString("id", getId());
	}

	@Override
	public TokenServer getServer() {
		return (TokenServer) super.getServer();
	}
}
