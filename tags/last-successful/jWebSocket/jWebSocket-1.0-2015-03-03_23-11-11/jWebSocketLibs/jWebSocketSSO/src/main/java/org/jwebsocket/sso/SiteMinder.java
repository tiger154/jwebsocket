//	---------------------------------------------------------------------------
//	jWebSocket SiteMinder support for Java (Community Edition, CE)
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
//	this plug-in is based on: 
//	http://tools.ietf.org/html/rfc6749 - The OAuth 2.0 Authorization Framework
//	http://tools.ietf.org/html/rfc6750 - The OAuth 2.0 Authorization Framework: Bearer Token Usage
//	http://oauth.net/2/ - OAuth 2.0
//	---------------------------------------------------------------------------
package org.jwebsocket.sso;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author aschulze
 */
public class SiteMinder {

	private static String mSMHost = null;

	/**
	 *
	 * @param aTimeout
	 * @return
	 */
	public static String getSSOSession(long aTimeout) {
		String lPostBody;
		try {
			lPostBody = null;
			Map lHeaders = new HashMap<String, String>();
			lHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			lHeaders.put("Accept-Encoding", "gzip,deflate");
			lHeaders.put("Cache-Control", "no-cache");
			lHeaders.put("Connection", "keep-alive");
			// lHeaders.put("Host", "<host>");
			lHeaders.put("Host", "aschulze-dt" );
			lHeaders.put("Pragma", "no-cache");
			lHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.3 Safari/537.36");
			String lResult = HTTPSupport.request(mSMHost, "GET",
					lHeaders, lPostBody, aTimeout);
			return lResult;
		} catch (Exception lEx) {
			// mReturnCode = -1;
			// mReturnMsg = lEx.getClass().getSimpleName() + " authenticating directly against OAuth host.";
			return "{\"code\":-1, \"msg\":\""
					+ lEx.getClass().getSimpleName() + "\"}";
		}

	}

	/**
	 * @return the mSMHost
	 */
	public static String getSMHost() {
		return mSMHost;
	}

	/**
	 * @param aSMHost the mSMHost to set
	 */
	public static void setSMHost(String aSMHost) {
		mSMHost = aSMHost;
	}
}
