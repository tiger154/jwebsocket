//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2012, Innotrade GmbH jwebsocket.org
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
package org.jwebsocket.tomcat.demo;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.console.JWebSocketTokenListenerSample;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.server.TokenServer;

/**
 * Web application lifecycle listener.
 *
 * @author aschulze
 */
@WebListener()
public class ContextListener implements ServletContextListener {

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

	@Override
	public void contextDestroyed(ServletContextEvent aSCE) {
		// stop the jWebSocket server sub system
		JWebSocketFactory.stop();
	}
}
