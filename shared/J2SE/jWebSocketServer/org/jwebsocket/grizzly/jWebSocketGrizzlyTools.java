/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
