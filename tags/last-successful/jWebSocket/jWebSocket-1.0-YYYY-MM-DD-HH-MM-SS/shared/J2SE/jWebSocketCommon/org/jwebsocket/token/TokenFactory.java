//	---------------------------------------------------------------------------
//	jWebSocket TokenFactory (Community Edition, CE)
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
package org.jwebsocket.token;

import java.util.Map;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.packetProcessors.CSVProcessor;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.packetProcessors.XMLProcessor;

/**
 *
 * @author Alexander Schulze
 * @author jang
 */
public class TokenFactory {

	/**
	 *
	 * @return
	 */
	public static Token createToken() {
		return new MapToken();
	}

	/**
	 *
	 * @param aMap
	 * @return
	 */
	public static Token createToken(Map aMap) {
		Token lToken = createToken();
		lToken.setMap(aMap);

		return lToken;
	}

	/**
	 *
	 * @param aType
	 * @return
	 */
	public static Token createToken(String aType) {
		return new MapToken(aType);
	}

	/**
	 *
	 * @param aNS
	 * @param aType
	 * @return
	 */
	public static Token createToken(String aNS, String aType) {
		return new MapToken(aNS, aType);
	}

	/**
	 *
	 * @param aFormat
	 * @param aDataPacket
	 * @return
	 */
	public static Token packetToToken(String aFormat, WebSocketPacket aDataPacket) {
		Token lToken = null;
		if (JWebSocketCommonConstants.WS_FORMAT_JSON.equals(aFormat)) {
			lToken = JSONProcessor.packetToToken(aDataPacket);
		} else if (JWebSocketCommonConstants.WS_FORMAT_CSV.equals(aFormat)) {
			lToken = CSVProcessor.packetToToken(aDataPacket);
		} else if (JWebSocketCommonConstants.WS_FORMAT_XML.equals(aFormat)) {
			lToken = XMLProcessor.packetToToken(aDataPacket);
		}
		return lToken;
	}

	/**
	 *
	 * @param aFormat
	 * @param aToken
	 * @return
	 */
	public static WebSocketPacket tokenToPacket(String aFormat, Token aToken) {
		WebSocketPacket lPacket = null;
		if (JWebSocketCommonConstants.WS_FORMAT_JSON.equals(aFormat)) {
			lPacket = JSONProcessor.tokenToPacket(aToken);
		} else if (JWebSocketCommonConstants.WS_FORMAT_CSV.equals(aFormat)) {
			lPacket = CSVProcessor.tokenToPacket(aToken);
		} else if (JWebSocketCommonConstants.WS_FORMAT_XML.equals(aFormat)) {
			lPacket = XMLProcessor.tokenToPacket(aToken);
		}
		return lPacket;
	}
}
