//	---------------------------------------------------------------------------
//	jWebSocket Main Class for Plug-in Development (Community Edition, CE)
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
 * @author Alexander Schulze
 */
public class Main {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] aArgs) {
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
						"tld.domain.myplugin", // id, needs to be unique
						"MyPlugIn", // name, just for info
						"org.jwebsocket.plugins.myplugin", // package
						"jWebSocketDevTemplate.jar", // name of the jar file
						null, // jars
						"org.jwebsocket.plugins.myplugin", // namespace
						lServers, // list of servers to be bound to
						null, // settings, if interpreted by plug-in
						true // enabled?
						);
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
