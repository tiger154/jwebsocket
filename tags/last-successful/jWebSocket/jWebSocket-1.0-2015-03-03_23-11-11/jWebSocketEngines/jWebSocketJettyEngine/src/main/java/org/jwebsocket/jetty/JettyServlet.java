//	---------------------------------------------------------------------------
//	jWebSocket - Jetty WebSocket Servlet (Community Edition, CE)
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
package org.jwebsocket.jetty;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.jwebsocket.engines.ServletUtils;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.tcp.EngineUtils;

/**
 *
 * @author Alexander Schulze
 */
public class JettyServlet extends WebSocketServlet {

	private static final Logger mLog = Logging.getLogger();
	private JettyEngine mEngine;

	/**
	 *
	 * @param aEngine
	 */
	public JettyServlet(JettyEngine aEngine) {
		mEngine = aEngine;
	}

	/**
	 *
	 */
	public JettyServlet() {
	}

	/**
	 *
	 * @throws ServletException
	 */
	@Override
	public void init() throws ServletException {
		if (null == mEngine) {
			String lEngineId = getInitParameter(ServletUtils.SERVLET_ENGINE_CONFIG_KEY);
			if (null == lEngineId) {
				lEngineId = "jetty0";
			}
			mEngine = (JettyEngine) JWebSocketFactory.getEngine(lEngineId);
		}
	}

	/**
	 *
	 * @param aRequest
	 * @param aResponse
	 * @throws ServletException
	 * @throws IOException
	 */
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
	 *
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

	/**
	 *
	 * @param aRequest
	 * @param aProtocol
	 * @return
	 */
	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest aRequest, String aProtocol) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing incoming WebSocket connection...");
		}
		return new JettyWrapper(mEngine, aRequest, aProtocol);
	}

	/**
	 *
	 * @param aRequest
	 * @param aOrigin
	 * @return
	 */
	@Override
	public boolean checkOrigin(HttpServletRequest aRequest, String aOrigin) {
		return EngineUtils.isOriginValid(aOrigin, mEngine.getConfiguration().getDomains());
	}

	/**
	 * Returns a short description of the servlet.
	 *
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
