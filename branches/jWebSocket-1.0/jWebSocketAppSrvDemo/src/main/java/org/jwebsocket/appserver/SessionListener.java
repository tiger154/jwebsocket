//	---------------------------------------------------------------------------
//	jWebSocket - SessionListener (Community Edition, CE)
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

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 * Web application lifecycle listener. Here the http session is added or removed
 * respectively from the global WebSocketHttpSessionMerger.
 *
 * @author aschulze
 */
public class SessionListener implements HttpSessionListener {

	private static Logger mLog = null;

	private void mCheckLogs() {
		if (mLog == null) {
			mLog = Logging.getLogger(SessionListener.class);
		}
	}

	/**
	 *
	 * @param aHSE
	 */
	@Override
	public void sessionCreated(HttpSessionEvent aHSE) {
		// when a new session is created by the servlet engine
		// add this session to the global WebSockethttpSessionMerger.
		WebSocketHttpSessionMerger.addHttpSession(aHSE.getSession());
		mCheckLogs();
		mLog.info("Created Http session: '" + aHSE.getSession().getId() + "'");
	}

	/**
	 *
	 * @param aHSE
	 */
	@Override
	public void sessionDestroyed(HttpSessionEvent aHSE) {
		// when an existing session is destroyed by the servlet engine
		// remove this session from the global WebSockethttpSessionMerger.
		WebSocketHttpSessionMerger.removeHttpSession(aHSE.getSession());
		mCheckLogs();
		mLog.info("Destroyed Http session: '" + aHSE.getSession().getId() + "'");
	}
}
