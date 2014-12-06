// ---------------------------------------------------------------------------
//	jWebSocket - ContextListener (Community Edition, CE)
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
package org.jwebsocket.http;

import java.net.URLClassLoader;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.engines.ServletUtils;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.factory.JWebSocketJarClassLoader;
import org.jwebsocket.instance.JWebSocketInstance;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class ContextListener implements ServletContextListener {

	private static boolean mRunningEmbedded = true;

	/**
	 *
	 * @return
	 */
	public static boolean isRunningEmbedded() {
		return mRunningEmbedded;
	}

	/**
	 *
	 * @param aContextEvent
	 */
	@Override
	public void contextInitialized(ServletContextEvent aContextEvent) {
		final ServletContext lContext = aContextEvent.getServletContext();

		if (JWebSocketInstance.STOPPED == JWebSocketInstance.getStatus()) {
			lContext.log("Starting jWebSocket application server...");

			JWebSocketFactory.printCopyrightToConsole();

			// check if home, config or bootstrap path are passed by context params
			JWebSocketConfig.initForWebApp(ServletUtils.extractStartupArguments(lContext),
					lContext.getRealPath("") + "/");

			// getting the tomcat class loader
			JWebSocketJarClassLoader lLoader = new JWebSocketJarClassLoader();
			lLoader.setClassLoader((URLClassLoader) Thread.currentThread().getContextClassLoader());
			JWebSocketFactory.setClassLoader(lLoader);

			// start the jWebSocket Server
			JWebSocketFactory.start();

			// waiting for the jWebSocket server "start process"
			while (JWebSocketInstance.STOPPED == JWebSocketInstance.getStatus()
					|| JWebSocketInstance.STARTING == JWebSocketInstance.getStatus()) {
			}

			if (JWebSocketInstance.STARTED == JWebSocketInstance.getStatus()) {
				lContext.log("jWebSocket application server started!");
				mRunningEmbedded = false;
			} else {
				// error happen during the jWebSocket server load
				lContext.log("ERROR: jWebSocket server could not start!");
			}
		}
	}

	/**
	 *
	 * @param aContextEvent
	 */
	@Override
	public void contextDestroyed(ServletContextEvent aContextEvent) {
		final ServletContext lContext = aContextEvent.getServletContext();
		lContext.log("Stopping jWebSocket application server...");

		if (JWebSocketInstance.STARTED == JWebSocketInstance.getStatus()) {
			JWebSocketFactory.stop();
		}

		lContext.log("jWebSocket application server stopped!");
		mRunningEmbedded = true;
	}
}
