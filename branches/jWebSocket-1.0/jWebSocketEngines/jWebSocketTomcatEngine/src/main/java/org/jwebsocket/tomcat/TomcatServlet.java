//	---------------------------------------------------------------------------
//	jWebSocket - Tomcat WebSocket Servlet, from Tomcat 7.0.27
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
package org.jwebsocket.tomcat;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.console.JWebSocketServer;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.instance.JWebSocketInstance;

/**
 *
 * @author aschulze
 * @author kyberneees
 */
public class TomcatServlet extends WebSocketServlet {

	private HttpServletRequest mRequest;

	@Override
	public void init() throws ServletException {
		if (JWebSocketInstance.STOPPED == JWebSocketInstance.getStatus()){
			// running in embedded mode
			// starting the jWebSocket application server
			JWebSocketServer.main(new String[0]);
		}
		super.init();
	}
	
	
	@Override
	protected void service(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		// save the request, since this is not available anymore in the createWebSocketInbound method
		mRequest = aRequest;
		super.service(aRequest, aResponse);
	}

	@Override
	protected String selectSubProtocol(List<String> aSubProtocols) {
		// super.selectSubProtocol(aSubProtocols);
		return TomcatWrapper.selectSubProtocol(aSubProtocols);
	}

	@Override
	protected boolean verifyOrigin(String aOrigin) {
		// TODO: we need to fix this hardcoded solution
		WebSocketEngine mEngine = JWebSocketFactory.getEngine("tomcat0");
		
		// super.verifyOrigin(aOrigin);
		return TomcatWrapper.verifyOrigin(aOrigin, mEngine.getConfiguration().getDomains());
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "jWebSocket Tomcat WebSocket Servlet";
	}

	@Override
	protected StreamInbound createWebSocketInbound(String aSubProtocol) {
		return new TomcatWrapper(mRequest, aSubProtocol);
	}
}
