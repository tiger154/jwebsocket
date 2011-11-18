//	---------------------------------------------------------------------------
//	jWebSocket - Jetty WebSocket Servlet
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
package org.jwebsocket.jetty;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author aschulze
 */
public class JettyServlet extends WebSocketServlet {

	private static Logger mLog = Logging.getLogger(JettyServlet.class);

	@Override
	protected void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Forwarding incoming GET request...");
		}
		getServletContext().getNamedDispatcher("default").forward(aRequest, aResponse);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * @param aRequest servlet request
	 * @param aResponse servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Ignoring incoming POST request...");
		}
	}

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest aRequest, String aProtocol) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing incoming WebSocket connection...");
		}
		return new JettyWrapper(aRequest, aProtocol);
	}

	/**
	 * Returns a short description of the servlet.
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "jWebSocket Jetty WebSocket Servlet";
	}

}

/*
timing issues: http://jira.codehaus.org/browse/JETTY-933
http://dev.eclipse.org/mhonarc/lists/jetty-users/msg01064.html
 *
 *
 */
