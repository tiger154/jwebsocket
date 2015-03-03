// ---------------------------------------------------------------------------
// jWebSocket - SessionListener (Community Edition, CE)
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
package org.jwebsocket.tomcat.listener;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.kit.WebSocketSession;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.storage.httpsession.HttpSessionStorage;

/**
 * Listener for the context HTTP session events
 *
 * @author Rolando Santamaria Maso
 */
public class SessionListener implements HttpSessionListener {

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void sessionCreated(HttpSessionEvent aEvent) {
		// DO NOTHING HERE
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void sessionDestroyed(HttpSessionEvent aEvent) {
		if (JWebSocketConfig.isWebApp()) {
			WebSocketSession lSession = new WebSocketSession(aEvent.getSession().getId());
			lSession.setStorage(new HttpSessionStorage(aEvent.getSession()));

			SystemPlugIn.stopSession(lSession);
		}
	}
}
