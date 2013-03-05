//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketHttpSessionMerger (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.appserver;

import javax.servlet.http.HttpSession;
import javolution.util.FastMap;
import org.jwebsocket.kit.WebSocketSession;

/**
 * This class combines both the sessions of the servlet container and the
 * websocket engine.
 *
 * @author aschulze
 */
public class WebSocketHttpSessionMerger {

	private static FastMap<String, HttpSession> mHttpSessions = new FastMap<String, HttpSession>();
	private static FastMap<String, ServletConnector> mServletConnectors = new FastMap<String, ServletConnector>();
	private static FastMap<String, WebSocketSession> mWsSessions = new FastMap<String, WebSocketSession>();
	private static FastMap<String, String> mAssignments = new FastMap<String, String>();

	/**
	 *
	 * @param aHttpSession
	 */
	public static void addHttpSession(HttpSession aHttpSession) {
		mHttpSessions.put(aHttpSession.getId(), aHttpSession);
		// create a new servlet connector for this http session
		mServletConnectors.put(aHttpSession.getId(), new ServletConnector());
	}

	/**
	 *
	 * @param aHttpSession
	 */
	public static void removeHttpSession(HttpSession aHttpSession) {
		mHttpSessions.remove(aHttpSession.getId());
		// discard the servlet connector for the terminated http session
		mServletConnectors.remove(aHttpSession.getId());
	}

	/**
	 *
	 * @param aHttpSession
	 * @return
	 */
	public static ServletConnector getHttpConnector(HttpSession aHttpSession) {
		return mServletConnectors.get(aHttpSession.getId());
	}

	/**
	 *
	 * @param aWebSocketSession
	 */
	public static void addWebSocketSession(WebSocketSession aWebSocketSession) {
		mWsSessions.put(aWebSocketSession.getSessionId(), aWebSocketSession);
	}

	/**
	 *
	 * @param aWebSocketSession
	 */
	public static void removeWebSocketSession(WebSocketSession aWebSocketSession) {
		mWsSessions.remove(aWebSocketSession.getSessionId());
	}

	/**
	 *
	 * @return
	 */
	public static String getHttpSessionsCSV() {
		String lRes = "";
		for (HttpSession lSession : mHttpSessions.values()) {
			lRes += lSession.getId() + ",";
		}
		if (lRes.length() > 0) {
			lRes = lRes.substring(0, lRes.length() - 1);
		}
		return lRes;
	}

	/**
	 *
	 * @return
	 */
	public static String getWebSocketSessionsCSV() {
		String lRes = "";
		for (WebSocketSession lSession : mWsSessions.values()) {
			lRes += lSession.getSessionId() + ",";
		}
		if (lRes.length() > 0) {
			lRes = lRes.substring(0, lRes.length() - 1);
		}
		return lRes;
	}
}
