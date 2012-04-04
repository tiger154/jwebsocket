//	---------------------------------------------------------------------------
//	jWebSocket - Sample Main Class to create your own jWebSocket Plug-Ins
//	Copyright (c) 2011 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.templates.plugindev;

import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.PluginConfig;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.plugins.myplugin.MyPlugIn;
import org.jwebsocket.server.TokenServer;

/**
 *
 * @author aschulze
 */
public class Main {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] aArgs) {
		// the following line may not be removed due to GNU LGPL 3.0 license!
		JWebSocketFactory.printCopyrightToConsole();
		JWebSocketConfig.initForConsoleApp(aArgs);

		try {
			JWebSocketFactory.start();

			// get the token server
			TokenServer lServer = JWebSocketFactory.getTokenServer();
			if (lServer != null) {
				// create list of server to bind the plug-in to.
				List<String> lServers = new FastList<String>();
				lServers.add(lServer.getId());
				// and add the sample listener to the server's listener chain
				PluginConfiguration lPlugInConfig = new PluginConfig(
						// id, needs to be unique
						"tld.domain.myplugin",
						// name, just for info
						"MyPlugIn",
						// package
						"org.jwebsocket.plugins.myplugin",
						// name of the jar file
						"jWebSocketDevTemplate.jar",
						// namespace
						"org.jwebsocket.plugins.myplugin",
						// list of servers to be bound to
						lServers,
						// settings, if interpreted by plug-in
						null,
						// enabled?
						true);

				lServer.getPlugInChain().addPlugIn(new MyPlugIn(lPlugInConfig));
			}
			// run server until shut down request
			JWebSocketFactory.run();
		} catch (Exception lEx) {
			System.out.println(
					lEx.getClass().getSimpleName()
					+ " on starting jWebsocket server: " + lEx.getMessage());
		} finally {
			JWebSocketFactory.stop();
		}
	}
}
