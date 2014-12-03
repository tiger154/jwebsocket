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
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

/**
 *
 * @author Alexander Schulze
 */
public class jWebSocket extends WebSocketServlet {

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
		getServletContext().getNamedDispatcher("default").forward(aRequest, aResponse);
	}

	/**
	 * Handles the HTTP
	 * <code>POST</code> method.
	 *
	 * @param aRequest servlet request
	 * @param aResponse servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
		// not required for now.
	}

	/**
	 *
	 * @param aRequest
	 * @param aProtocol
	 * @return
	 */
	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest aRequest, String aProtocol) {
		// System.out.println("@doWebSocketConnect");
		return new JettyWrapper(aRequest, aProtocol);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "jWebSocket Jetty Servlet";
	}
}
