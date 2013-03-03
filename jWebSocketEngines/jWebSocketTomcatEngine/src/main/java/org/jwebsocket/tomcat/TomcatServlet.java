//	---------------------------------------------------------------------------
//	jWebSocket - Tomcat WebSocket Servlet, from Tomcat 7.0.35 (Community Edition, CE)
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

	/**
	 *
	 * @return
	 */
	public boolean isRunningEmbedded() {
		return ContextListener.isRunningEmbedded();
	}

	/**
	 *
	 * @throws ServletException
	 */
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

	/**
	 *
	 * @param aRequest
	 * @param aResponse
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected void service(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		mRequestContainer.set(aRequest);

		// setting the session timeout
		if (ContextListener.isRunningEmbedded()) {
			aRequest.getSession().setMaxInactiveInterval(mEngine.getConfiguration().getTimeout());
		}

		super.service(aRequest, aResponse);
	}

	/**
	 *
	 * @param aSubProtocols
	 * @return
	 */
	@Override
	protected String selectSubProtocol(List<String> aSubProtocols) {
		// super.selectSubProtocol(aSubProtocols);
		return TomcatWrapper.selectSubProtocol(aSubProtocols);
	}

	/**
	 *
	 * @param aOrigin
	 * @return
	 */
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

	/**
	 *
	 * @param aSubProtocol
	 * @param aRequest
	 * @return
	 */
	@Override
	protected StreamInbound createWebSocketInbound(String aSubProtocol, HttpServletRequest aRequest) {
		// do not use the aRequest paremeter here,
		// it is a facede to be used only in this context
		return new TomcatWrapper(mEngine, mRequestContainer.get(), aSubProtocol);
	}
}