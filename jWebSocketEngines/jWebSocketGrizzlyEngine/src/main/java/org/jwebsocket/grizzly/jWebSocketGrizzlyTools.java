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
	 * @param uri
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static Map<String, String> getUrlParameters(String uri)
			throws UnsupportedEncodingException {
		Map<String, String> params = new FastMap<String, String>();

		for (String param : uri.split("&")) {
			String pair[] = param.split("=");
			String key = URLDecoder.decode(pair[0], "UTF-8");
			String value = "";
			if (pair.length > 1) {
				value = URLDecoder.decode(pair[1], "UTF-8");
			}
			params.put(key, value);
		}
		return params;
	}
}
