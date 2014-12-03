//	---------------------------------------------------------------------------
//	jWebSocket - Grizzly Tools (Community Edition, CE)
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
package org.jwebsocket.grizzly;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import javolution.util.FastMap;

/**
 *
 * @author Victor Antonio Barzana Crespo
 */
public class jWebSocketGrizzlyTools {

	/**
	 *
	 * @param aUri
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static Map<String, String> getUrlParameters(String aUri)
			throws UnsupportedEncodingException {
		Map<String, String> lParams = new FastMap<String, String>();

		for (String lParam : aUri.split("&")) {
			String lPair[] = lParam.split("=");
			String lKey = URLDecoder.decode(lPair[0], "UTF-8");
			String lValue = "";
			if (lPair.length > 1) {
				lValue = URLDecoder.decode(lPair[1], "UTF-8");
			}
			lParams.put(lKey, lValue);
		}

		return lParams;
	}
}
