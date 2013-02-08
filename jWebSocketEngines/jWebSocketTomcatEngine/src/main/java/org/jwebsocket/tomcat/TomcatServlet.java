//	---------------------------------------------------------------------------
//	jWebSocket - Tomcat WebSocket Servlet, from Tomcat 7.0.35
//	Copyright (c) 2012 jWebSocket.org, Innotrade GmbH
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
import org.apache.log4j.Logger;
import org.jwebsocket.engines.ServletUtils;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.instance.JWebSocketInstance;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.tcp.EngineUtils;

/**
 *
 * @author aschulze
 * @author kyberneees
 */
public class TomcatServlet extends WebSocketServlet {

	private static Logger mLog;
	private ThreadLocal<HttpServletRequest> mRequestContainer = new ThreadLocal<HttpServletRequest>();
	private TomcatEngine mEngine;

	public boolean isRunningEmbedded() {
		return ContextListener.isRunningEmbedded();
	}

	@Override
	public void init() throws ServletException {
		if (JWebSocketInstance.STARTED == JWebSocketInstance.getStatus()) {
			mLog = Logging.getLogger();

			String lEngineId = getInitParameter(ServletUtils.SERVLET_ENGINE_CONFIG_KEY);
			if (null == lEngineId) {
				lEngineId = "tomcat0";
			}
			mEngine = (TomcatEngine) JWebSocketFactory.getEngine(lEngineId);

			super.init();
			if (mLog.isDebugEnabled()) {
				mLog.debug("Servlet successfully initialized.");
			}
		} else {
			throw new ServletException("The jWebSocket server is not started!");
		}
	}

	@Override
	protected void service(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		// save the request, since this is not available anymore in the createWebSocketInbound method
		mRequestContainer.set(aRequest);
		super.service(aRequest, aResponse);
	}

	@Override
	protected String selectSubProtocol(List<String> aSubProtocols) {
		// super.selectSubProtocol(aSubProtocols);
		return TomcatWrapper.selectSubProtocol(aSubProtocols);
	}

	@Override
	protected boolean verifyOrigin(String aOrigin) {
		return EngineUtils.isOriginValid(aOrigin, mEngine.getConfiguration().getDomains());
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
	protected StreamInbound createWebSocketInbound(String aSubProtocol, HttpServletRequest aRequest) {
		// do not use the aRequest paremeter here,
		// it is a facede to be used only in this context
		return new TomcatWrapper(mEngine, mRequestContainer.get(), aSubProtocol);
	}
}