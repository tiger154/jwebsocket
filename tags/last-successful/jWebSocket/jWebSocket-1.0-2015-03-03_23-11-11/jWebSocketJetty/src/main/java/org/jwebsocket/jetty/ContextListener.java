//	---------------------------------------------------------------------------
//	jWebSocket - Context Listener for Jetty Web Applications (Community Edition, CE)
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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.console.JWebSocketTokenListenerSample;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.server.TokenServer;

/**
 * Web application life cycle listener.
 *
 * @author alexanderschulze
 */
public class ContextListener implements ServletContextListener {

	/**
	 * initializes the web application on startup.
	 *
	 * @param aSCE
	 */
	@Override
	public void contextInitialized(ServletContextEvent aSCE) {
		// the following line must not be removed due to GNU LGPL 3.0 license!
		JWebSocketFactory.printCopyrightToConsole();

		JWebSocketConfig.initForWebApp(aSCE.getServletContext());

		// start the jWebSocket server sub system with default config and bootstrap
		JWebSocketFactory.start();

		// get the token server
		// and and a listener to it (for demo purposes)
		TokenServer lTS0 = JWebSocketFactory.getTokenServer();
		if (lTS0 != null) {
			// and add the sample listener to the server's listener chain
			lTS0.addListener(new JWebSocketTokenListenerSample());
		}
	}

	/**
	 * cleans up the web application on termination.
	 *
	 * @param aSCE
	 */
	@Override
	public void contextDestroyed(ServletContextEvent aSCE) {
		// stop the jWebSocket server sub system
		JWebSocketFactory.stop();
	}
}
