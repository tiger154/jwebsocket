//	---------------------------------------------------------------------------
//	jWebSocket - ServletBridge (Community Edition, CE)
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

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * demonstrates how to communicate between servlets and web sockets.
 *
 * @author aschulze
 */
public class ServletBridge extends HttpServlet {

	// reference to the token server
	private static TokenServer mServer = null;
	private static Logger mLog = null;

	private void mCheckLogs() {
		if (mLog == null) {
			mLog = Logging.getLogger(ServletBridge.class);
		}
	}

	/**
	 * @return the server
	 */
	public static TokenServer getServer() {
		return mServer;
	}

	/**
	 *
	 * @param aServer
	 */
	public static void setServer(TokenServer aServer) {
		mServer = aServer;
	}

	/**
	 * Processes requests for both HTTP
	 * <code>GET</code> and
	 * <code>POST</code> methods.
	 *
	 * @param aRequest servlet request
	 * @param aResponse servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
		aResponse.setContentType("text/plain;charset=UTF-8");
		PrintWriter out = aResponse.getWriter();
		mCheckLogs();

		try {
			if (mServer != null) {
				// convert request arguments to token
				FastMap<String, String[]> lParms = new FastMap(aRequest.getParameterMap());
				if (mLog.isDebugEnabled()) {
					mLog.debug("Received http request, sid: " + aRequest.getSession().getId() + ", url-args: " + lParms.toText());
				}
				Token lToken = TokenFactory.createToken();
				for (String lParm : lParms.keySet()) {
					String[] lValues = lParms.get(lParm);
					if (lValues != null && lValues.length > 0) {
						lToken.setValidated(lParm, lValues[0]);
					}
				}
				ServletConnector lConn = WebSocketHttpSessionMerger.getHttpConnector(aRequest.getSession());
				if (lConn != null) {
					lConn.setRequest(aRequest);
					mServer.getPlugInChain().processToken(lConn, lToken);
					String lResponse = lConn.getPlainResponse();
					out.println(lResponse);
					if (mLog.isDebugEnabled()) {
						mLog.debug("Sent http response: " + lResponse);
					}
				} else {
					String lMsg = "Connector not found for request!";
					out.println("ERROR:\n" + lMsg);
					mLog.error(lMsg);
				}
			} else {
				String lMsg = "No WebSocket server assigned to servlet!";
				out.println("ERROR:\n" + lMsg);
				mLog.error(lMsg);
			}
		} finally {
			out.close();
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP
	 * <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP
	 * <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>
}
