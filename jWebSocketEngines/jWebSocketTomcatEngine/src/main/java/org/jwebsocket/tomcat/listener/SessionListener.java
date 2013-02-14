// ---------------------------------------------------------------------------
// jWebSocket - SessionListener
// Copyright (c) 2013 jWebSocket.org, Innotrade GmbH
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.tomcat.listener;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.jwebsocket.kit.WebSocketSession;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.storage.httpsession.HttpSessionStorage;

/**
 * Listener for the context HTTP session events
 *
 * @author kyberneees
 */
public class SessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent aEvent) {
		// DO NOTHING HERE
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent aEvent) {
		WebSocketSession lSession = new WebSocketSession(aEvent.getSession().getId());
		lSession.setStorage(new HttpSessionStorage(aEvent.getSession()));

		SystemPlugIn.stopSession(lSession);
	}
}
