//	---------------------------------------------------------------------------
//	jWebSocket - EngineUtils (Community Edition, CE)
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
package org.jwebsocket.tcp;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.kit.WebSocketProtocolAbstraction;

/**
 * Utility methods for TCP and NIO engines.
 *
 * @author jang
 * @author Rolando Santamaria Maso
 */
public class EngineUtils {

	/**
	 * Indicates if a given origin is included on a domain's list.
	 *
	 * @param aOrigin
	 * @param aDomains
	 * @return
	 */
	public static boolean isOriginValid(String aOrigin, List<String> aDomains) {
		boolean lAccepted = false;
		String lOrigin = aOrigin;

		if (null == aDomains || aDomains.isEmpty()) {
			return true;
		}

		if (null != lOrigin) {
			for (String lDomain : aDomains) {
				lDomain = lDomain.replace("*", ".*");
				if (lOrigin.matches(lDomain)) {
					lAccepted = true;
					break;
				}
			}
		}

		return lAccepted;
	}

	/**
	 * Validates draft header and constructs RequestHeader object.
	 *
	 * @param aDomains
	 * @param aReqMap
	 * @param aLog
	 * @param aClientSocket
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static RequestHeader validateC2SRequest(List<String> aDomains,
			Map<String, Object> aReqMap, Logger aLog,
			Socket aClientSocket) throws UnsupportedEncodingException {

		InetAddress lAddr = aClientSocket.getInetAddress();
		if (aLog.isDebugEnabled()) {
			aLog.debug("Validating connection from "
					+ (null != lAddr
					? lAddr.getHostAddress() + ", " + lAddr.getHostName()
					: "[no IP/hostname available]")
					+ ", headers: "
					+ (null != aReqMap
					? aReqMap.toString()
					: "[no headers passed]")
			);
		}
		Object lOrigin = null;
		Object lUserAgent = null;
		if (null != aReqMap) {
			lOrigin = aReqMap.get("origin");
			lUserAgent = aReqMap.get("User-Agent");
		}
		boolean lAccepted = (null != lOrigin)
				&& isOriginValid(lOrigin.toString(), aDomains);
		if (!lAccepted) {
			aLog.error("Client origin '"
					+ (null != lOrigin
					? lOrigin
					: "[no 'origin' in request headers" + (null != lUserAgent ? ", user-agent: " + lUserAgent : ", no user-agent given") + "]")
					+ "' does not match allowed domains"
					+ ", host: "
					+ (null != lAddr
					? lAddr.getHostAddress() + ", " + lAddr.getHostName()
					: "[no IP/hostname available]")
					+ ", headers: "
					+ (null != aReqMap
					? aReqMap.toString()
					: "[no headers passed]"
					+ ". Check engine <domains> section in jWebSocket.xml!"));
			return null;
		}

		// Check for WebSocket protocol version.
		// If it is present and if it's something unrecognizable, force disconnect (return null).
		String lDraft = (String) aReqMap.get(RequestHeader.WS_DRAFT);
		Integer lVersion = (Integer) aReqMap.get(RequestHeader.WS_VERSION);

		// run validation
		if (!WebSocketProtocolAbstraction.isValidDraft(lDraft)) {
			aLog.error("Error in Handshake: Draft #'" + lDraft + "' not supported.");
			return null;
		}

		if (!WebSocketProtocolAbstraction.isValidVersion(lVersion)) {
			aLog.error("Error in Handshake: Version #'" + lVersion + "' not supported.");
			return null;
		}

		if (aLog.isDebugEnabled()) {
			aLog.debug("Client uses websocket protocol version #" + lVersion + "/draft #" + lDraft + " for communication.");
		}
		RequestHeader lHeader = new RequestHeader();
		Map<String, String> lArgs = new HashMap<String, String>();
		String lPath = (String) aReqMap.get("path");
		// isolate search string
		String lSearchString = "";
		if (lPath
				!= null) {
			int lPos = lPath.indexOf(JWebSocketCommonConstants.PATHARG_SEPARATOR);
			if (lPos >= 0) {
				lSearchString = lPath.substring(lPos + 1);
				if (lSearchString.length() > 0) {
					String[] lArgsArray
							= lSearchString.split(JWebSocketCommonConstants.ARGARG_SEPARATOR);
					for (int lIdx = 0; lIdx < lArgsArray.length; lIdx++) {
						String[] lKeyValuePair
								= lArgsArray[lIdx].split(JWebSocketCommonConstants.KEYVAL_SEPARATOR, 2);
						if (lKeyValuePair.length == 2) {
							lArgs.put(lKeyValuePair[0], lKeyValuePair[1]);
							if (aLog.isDebugEnabled()) {
								aLog.debug("arg" + lIdx + ": "
										+ lKeyValuePair[0] + "="
										+ lKeyValuePair[1]);
							}
						}
					}
				}
			}
		}
		// if no sub protocol given in request header , try
		String lSubProt = (String) aReqMap.get(RequestHeader.WS_PROTOCOL);
		if (lSubProt
				== null) {
			lSubProt = lArgs.get(RequestHeader.WS_PROTOCOL);
		}
		if (lSubProt
				== null) {
			lSubProt = JWebSocketCommonConstants.WS_SUBPROT_DEFAULT;
		}

		// Sub protocol header might contain multiple entries
		// (e.g. 'jwebsocket.org/json jwebsocket.org/xml chat.example.com/custom').
		// So, someone has to decide, which entry to use and send the client appropriate
		// choice. Right now, we will just choose the first one if more than one are
		// available.
		// TODO: implement subprotocol choice handling by deferring the decision to plugins/listeners
		if (lSubProt.indexOf(
				' ') != -1) {
			lSubProt = lSubProt.split(" ")[0];
			aReqMap.put(RequestHeader.WS_PROTOCOL, lSubProt);
		}

		lHeader.put(RequestHeader.WS_HOST, aReqMap.get(RequestHeader.WS_HOST));
		lHeader.put(RequestHeader.WS_ORIGIN, aReqMap.get(RequestHeader.WS_ORIGIN));
		lHeader.put(RequestHeader.WS_LOCATION, aReqMap.get(RequestHeader.WS_LOCATION));
		lHeader.put(RequestHeader.WS_PROTOCOL, lSubProt);

		lHeader.put(RequestHeader.WS_PATH, aReqMap.get(RequestHeader.WS_PATH));
		lHeader.put(RequestHeader.WS_SEARCHSTRING, lSearchString);

		lHeader.put(RequestHeader.URL_ARGS, lArgs);

		lHeader.put(RequestHeader.WS_DRAFT,
				lDraft
				== null
				? JWebSocketCommonConstants.WS_DRAFT_DEFAULT
				: lDraft);
		lHeader.put(RequestHeader.WS_VERSION,
				lVersion
				== null
				? JWebSocketCommonConstants.WS_VERSION_DEFAULT
				: lVersion);

		//Setting cookies in the headers
		lHeader.put(RequestHeader.WS_COOKIES, aReqMap.get(RequestHeader.WS_COOKIES));

		return lHeader;
	}

	/**
	 * Parse cookies into a map
	 *
	 * @param aReqMap
	 */
	public static void parseCookies(Map<String, Object> aReqMap) {
		String lTempEntry[];
		Map<String, String> lCookiesMap = new FastMap<String, String>().shared();

		if (aReqMap.containsKey(RequestHeader.WS_COOKIES)
				&& null != aReqMap.get(RequestHeader.WS_COOKIES)) {
			Object lCookieObj = aReqMap.get(RequestHeader.WS_COOKIES);
			if (null != lCookieObj) {
				String lCookieStr = lCookieObj.toString();
				if (lCookieStr.length() > 0) {
					String[] lCookies = lCookieStr.split("; ");
					for (String lCookie : lCookies) {
						lTempEntry = lCookie.split("=");
						if (lTempEntry.length >= 2) {
							lCookiesMap.put(lTempEntry[0], lTempEntry[1]);
						}
					}
				}
			}
		}

		aReqMap.put(RequestHeader.WS_COOKIES, lCookiesMap);
	}
}
