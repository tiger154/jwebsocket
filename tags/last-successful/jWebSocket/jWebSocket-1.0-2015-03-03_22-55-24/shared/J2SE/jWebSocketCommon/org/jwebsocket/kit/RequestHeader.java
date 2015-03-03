//	---------------------------------------------------------------------------
//	jWebSocket - RequestHeader (Community Edition, CE)
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
package org.jwebsocket.kit;

import java.util.Map;
import java.util.UUID;
import javolution.util.FastMap;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.util.Tools;

/**
 * Holds the header of the initial WebSocket request from the client to the
 * server. The RequestHeader internally maintains a FastMap to store key/values
 * pairs.
 *
 * @author Alexander Schulze
 * @author jang
 * @version $Id: RequestHeader.java 596 2010-06-22 17:09:54Z fivefeetfurther $
 */
public class RequestHeader {

	private Map<String, Object> mFields = new FastMap<String, Object>();
	/**
	 *
	 */
	public static final String WS_PROTOCOL = "subprot";
	/**
	 *
	 */
	public static final String WS_DRAFT = "draft";
	/**
	 *
	 */
	public static final String WS_VERSION = "version";
	/**
	 *
	 */
	public static final String WS_ORIGIN = "origin";
	/**
	 *
	 */
	public static final String WS_LOCATION = "location";
	/**
	 *
	 */
	public static final String WS_PATH = "path";
	/**
	 *
	 */
	public static final String WS_SEARCHSTRING = "searchString";
	/**
	 *
	 */
	public static final String WS_HOST = "host";
	/**
	 *
	 */
	public static final String WS_SECKEY = "secKey";
	/**
	 *
	 */
	public static final String WS_SECKEY1 = "secKey1";
	/**
	 *
	 */
	public static final String WS_SECKEY2 = "secKey2";
	/**
	 *
	 */
	public static final String WS_COOKIES = "cookie";
	/**
	 *
	 */
	public static final String SESSION_COOKIE_NAME = "sessionCookieName";
	/**
	 *
	 */
	public static final String SESSION_ID = "sessionId";
	/**
	 *
	 */
	public static final String URL_ARGS = "args";
	/**
	 *
	 */
	public static final String TIMEOUT = "timeout";
	/**
	 *
	 */
	public static final String USER_AGENT = "User-Agent";

	/**
	 * Puts a new object value to the request header.
	 *
	 * @param aKey
	 * @param aValue
	 */
	public void put(String aKey, Object aValue) {
		mFields.put(aKey, aValue);
	}

	/**
	 * Returns the object value for the given key or {@code null} if the key
	 * does not exist in the header.
	 *
	 * @param aKey
	 * @return object value for the given key or {@code null}.
	 */
	public Object get(String aKey) {
		return mFields.get(aKey);
	}

	/**
	 * Returns the connection session cookie name
	 *
	 * @return
	 */
	public String getSessionCookieName() {
		// getting the session cookie name (sessionCookieName)
		String lSessionCookieName = (String) getArgs().get(RequestHeader.SESSION_COOKIE_NAME);
		if (null == lSessionCookieName) {
			lSessionCookieName = JWebSocketCommonConstants.SESSIONID_COOKIE_NAME;
		}

		return lSessionCookieName;
	}

	/**
	 * Returns the connection session id value
	 *
	 * @return
	 */
	public String getSessionId() {
		String lSessionId = (String) getArgs().get(RequestHeader.SESSION_ID);
		if (null != lSessionId) {
			getCookies().put(getSessionCookieName(), lSessionId);
		}
		if (!getCookies().containsKey(getSessionCookieName())) {
			getCookies().put(getSessionCookieName(), Tools.getMD5(UUID.randomUUID().toString()));
		}

		return (String) getCookies().get(getSessionCookieName());
	}

	/**
	 * Returns the string value for the given key or {@code null} if the key
	 * does not exist in the header.
	 *
	 * @param aKey
	 * @return String value for the given key or {@code null}.
	 */
	public String getString(String aKey) {
		return (String) mFields.get(aKey);
	}

	/**
	 * Returns a Map of the optional URL arguments passed by the client.
	 *
	 * @return Map of the optional URL arguments.
	 */
	public Map getArgs() {
		return (Map) mFields.get(URL_ARGS);
	}

	/**
	 * Returns the sub protocol passed by the client or a default value if no
	 * sub protocol has been passed either in the header or in the URL
	 * arguments.
	 *
	 * @return Sub protocol passed by the client or default value.
	 */
	public String getSubProtocol() {
		return resolveSubprotocol()[0];
	}

	/**
	 * Returns the subprotocol format in which messages are exchanged between
	 * client and server.
	 *
	 * @return subprotocol format passed by the client or default value
	 */
	public String getFormat() {
		return resolveSubprotocol()[1];
	}

	/**
	 * Tries to resolve correct subprotocol & format regardless of client
	 * version (old, new, hixie, hybi, browser, java). TODO: deprecate this
	 * method once majority of clients switch to new 'subprotocol/format' scheme
	 *
	 * @return array with two members: protocol and format
	 */
	private String[] resolveSubprotocol() {
		String lSubProt = (String) mFields.get(WS_PROTOCOL);
		if (lSubProt == null) {
			lSubProt = JWebSocketCommonConstants.WS_SUBPROT_DEFAULT;
		}
		if (lSubProt.indexOf('/') != -1) {
			// expecting 'subprotocol/format' scheme
			return lSubProt.split("/");
		} else {
			String lFormat = JWebSocketCommonConstants.WS_FORMAT_DEFAULT;
			if (JWebSocketCommonConstants.WS_SUBPROT_JSON.equals(lSubProt)) {
				lFormat = JWebSocketCommonConstants.WS_FORMAT_JSON;
			} else if (JWebSocketCommonConstants.WS_SUBPROT_XML.equals(lSubProt)) {
				lFormat = JWebSocketCommonConstants.WS_FORMAT_XML;
			} else if (JWebSocketCommonConstants.WS_SUBPROT_CSV.equals(lSubProt)) {
				lFormat = JWebSocketCommonConstants.WS_FORMAT_CSV;
			} else if (JWebSocketCommonConstants.WS_SUBPROT_TEXT.equals(lSubProt)) {
				lFormat = JWebSocketCommonConstants.WS_FORMAT_TEXT;
			} else if (JWebSocketCommonConstants.WS_SUBPROT_BINARY.equals(lSubProt)) {
				lFormat = JWebSocketCommonConstants.WS_FORMAT_BINARY;
			}

			return new String[]{lSubProt, lFormat};
		}
	}

	/**
	 * Returns the session timeout passed by the client or a default value if no
	 * session timeout has been passed either in the header or in the URL
	 * arguments.
	 *
	 * @param aDefault
	 * @return Session timeout passed by the client or default value.
	 */
	public Integer getTimeout(Integer aDefault) {
		Map lArgs = getArgs();
		Integer lTimeout = null;
		if (lArgs != null) {
			try {
				lTimeout = Integer.parseInt((String) (lArgs.get(TIMEOUT)));
			} catch (Exception lEx) {
			}
		}
		return (lTimeout != null ? lTimeout : aDefault);
	}

	/**
	 *
	 * @return
	 */
	public int getVersion() {
		return (Integer) mFields.get(WS_VERSION);
	}

	/**
	 *
	 * @return
	 */
	public Map getCookies() {
		return (Map) get(WS_COOKIES);
	}
}
