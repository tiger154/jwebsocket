//	---------------------------------------------------------------------------
//	jWebSocket - XML Token Processor (Community Edition, CE)
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
package org.jwebsocket.packetProcessors;

import java.util.Collection;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * converts XML formatted data packets into tokens and vice versa.
 *
 * @author Alexander Schulze
 */
public class XMLProcessor {

	// TODO: Logging cannot be used in common module because not supported on all clients
	// private static Logger log = Logging.getLogger(XMLProcessor.class);
	/**
	 * converts a XML formatted data packet into a token.
	 *
	 * @param aDataPacket
	 * @return
	 */
	public static Token packetToToken(WebSocketPacket aDataPacket) {
		// todo: implement!
		Token lArgs = TokenFactory.createToken();
		return lArgs;
	}

	private static String stringToXML(String aString) {
		// todo: implement!
		String lRes = null;
		return lRes;
	}

	private static String listToXML(Collection<Object> aCollection) {
		// todo: implement!
		String lRes = null;
		return lRes;
	}

	private static String objectToXML(Object aObj) {
		// todo: implement!
		String lRes = null;
		return lRes;
	}

	/**
	 * converts a token into a XML formatted data packet.
	 *
	 * @param aToken
	 * @return
	 */
	public static WebSocketPacket tokenToPacket(Token aToken) {
		// todo: implement!
		return null;
	}
}
