//	---------------------------------------------------------------------------
//	jWebSocket - Grizzly Tools
//	Copyright (c) 2012 Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.grizzly;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import javolution.util.FastMap;

/**
 *
 * @author vbarzana
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
