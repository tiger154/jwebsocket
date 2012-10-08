// ---------------------------------------------------------------------------
// jWebSocket - ContextListener
// Copyright (c) 2012 jWebSocket.org, Innotrade GmbH
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
package org.jwebsocket.tomcat;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.engines.ServletUtils;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.instance.JWebSocketInstance;

/**
 *
 * @author kyberneees
 */
public class ContextListener implements ServletContextListener {

	private static boolean mRunningEmbedded = false;

	public static boolean isRunningEmbedded() {
		return mRunningEmbedded;
	}

	@Override
	public void contextInitialized(ServletContextEvent aContextEvent) {
		final ServletContext lContext = aContextEvent.getServletContext();

		if (JWebSocketInstance.STOPPED == JWebSocketInstance.getStatus()) {
			lContext.log("Starting jWebSocket application server...");

			JWebSocketFactory.printCopyrightToConsole();

			// check if home, config or bootstrap path are passed by context params
			JWebSocketConfig.initForConsoleApp(ServletUtils.extractStartupArguments(lContext));

			// start the jWebSocket Server
			JWebSocketFactory.start();

			// waiting for the jWebSocket server "start process"
			while (JWebSocketInstance.STOPPED == JWebSocketInstance.getStatus()
					|| JWebSocketInstance.STARTING == JWebSocketInstance.getStatus()) {
			};

			if (JWebSocketInstance.STARTED == JWebSocketInstance.getStatus()) {
				lContext.log("jWebSocket application server started!");
				mRunningEmbedded = true;
			} else {
				// error happen during the jWebSocket server load
				lContext.log("ERROR: jWebSocket server could not start!");
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent aContextEvent) {
		final ServletContext lContext = aContextEvent.getServletContext();
		lContext.log("Stopping jWebSocket application server...");

		if (JWebSocketInstance.STARTED == JWebSocketInstance.getStatus()) {
			JWebSocketFactory.stop();
		}

		lContext.log("jWebSocket application server stopped!");
	}
}