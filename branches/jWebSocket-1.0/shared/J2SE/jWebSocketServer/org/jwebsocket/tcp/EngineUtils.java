//	---------------------------------------------------------------------------
//	jWebSocket - TCP Connector
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.tcp;

import org.apache.log4j.Logger;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.RequestHeader;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import org.jwebsocket.kit.WebSocketProtocolAbstraction;

/**
 * Utility methods for tcp and nio engines.
 *
 * @author jang
 */
public class EngineUtils {

	/**
	 * Validates draft header and constructs RequestHeader object.
	 */
	public static RequestHeader validateC2SRequest(Map lRespMap, Logger logger) throws UnsupportedEncodingException {
		// Check for WebSocket protocol version.
		// If it is present and if it's something unrecognizable, force disconnect (return null).
		String lDraft = (String) lRespMap.get(RequestHeader.WS_DRAFT);
		Integer lVersion = (Integer) lRespMap.get(RequestHeader.WS_VERSION);

		// run validation
		if (!WebSocketProtocolAbstraction.isValidDraft(lDraft)) {
			logger.error("Error in Handshake: Draft #'" + lDraft + "' not supported.");
			return null;
		}
		if (!WebSocketProtocolAbstraction.isValidVersion(lVersion)) {
			logger.error("Error in Handshake: Version #'" + lVersion + "' not supported.");
			return null;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Client uses websocket protocol version #" + lVersion + "/draft #" + lDraft + " for communication.");
		}

		RequestHeader lHeader = new RequestHeader();
		Map<String, String> lArgs = new HashMap<String, String>();
		String lPath = (String) lRespMap.get("path");

		// isolate search string
		String lSearchString = "";
		if (lPath != null) {
			int lPos = lPath.indexOf(JWebSocketCommonConstants.PATHARG_SEPARATOR);
			if (lPos >= 0) {
				lSearchString = lPath.substring(lPos + 1);
				if (lSearchString.length() > 0) {
					String[] lArgsArray =
							lSearchString.split(JWebSocketCommonConstants.ARGARG_SEPARATOR);
					for (int lIdx = 0; lIdx < lArgsArray.length; lIdx++) {
						String[] lKeyValuePair =
								lArgsArray[lIdx].split(JWebSocketCommonConstants.KEYVAL_SEPARATOR, 2);
						if (lKeyValuePair.length == 2) {
							lArgs.put(lKeyValuePair[0], lKeyValuePair[1]);
							if (logger.isDebugEnabled()) {
								logger.debug("arg" + lIdx + ": "
										+ lKeyValuePair[0] + "="
										+ lKeyValuePair[1]);
							}
						}
					}
				}
			}
		}

		// if no sub protocol given in request header , try
		String lSubProt = (String) lRespMap.get(RequestHeader.WS_PROTOCOL);
		if (lSubProt == null) {
			lSubProt = lArgs.get(RequestHeader.WS_PROTOCOL);
		}
		if (lSubProt == null) {
			lSubProt = JWebSocketCommonConstants.WS_SUBPROT_DEFAULT;
		}

		// Sub protocol header might contain multiple entries
		// (e.g. 'jwebsocket.org/json jwebsocket.org/xml chat.example.com/custom').
		// So, someone has to decide, which entry to use and send the client appropriate
		// choice. Right now, we will just choose the first one if more than one are
		// available.
		// TODO: implement subprotocol choice handling by deferring the decision to plugins/listeners
		if (lSubProt.indexOf(' ') != -1) {
			lSubProt = lSubProt.split(" ")[0];
			lRespMap.put(RequestHeader.WS_PROTOCOL, lSubProt);
		}

		lHeader.put(RequestHeader.WS_HOST, lRespMap.get(RequestHeader.WS_HOST));
		lHeader.put(RequestHeader.WS_ORIGIN, lRespMap.get(RequestHeader.WS_ORIGIN));
		lHeader.put(RequestHeader.WS_LOCATION, lRespMap.get(RequestHeader.WS_LOCATION));
		lHeader.put(RequestHeader.WS_PROTOCOL, lSubProt);
		lHeader.put(RequestHeader.WS_PATH, lRespMap.get(RequestHeader.WS_PATH));
		lHeader.put(RequestHeader.WS_SEARCHSTRING, lSearchString);
		lHeader.put(RequestHeader.URL_ARGS, lArgs);
		lHeader.put(RequestHeader.WS_DRAFT,
				lDraft == null
				? JWebSocketCommonConstants.WS_DRAFT_DEFAULT
				: lDraft);
		lHeader.put(RequestHeader.WS_VERSION,
				lVersion == null
				? JWebSocketCommonConstants.WS_VERSION_DEFAULT
				: lVersion);

		return lHeader;
	}
}
