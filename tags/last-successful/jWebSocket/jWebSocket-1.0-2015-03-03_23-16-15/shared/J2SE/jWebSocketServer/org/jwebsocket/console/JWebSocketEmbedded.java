//	---------------------------------------------------------------------------
//	jWebSocket - JWebSocketEmbedded (Community Edition, CE)
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
package org.jwebsocket.console;

import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.server.CustomServer;
import org.jwebsocket.server.TokenServer;

/**
 *
 * @author Alexander Schulze
 */
public class JWebSocketEmbedded {

	/**
	 *
	 * @param aArgs
	 */
	public static void main(String[] aArgs) {
		// instantiate an embedded jWebSocket Server
		JWebSocketSubSystemSample jWebSocketSubsystem = new JWebSocketSubSystemSample(aArgs);
		// instantiate an embedded listener class and add it to the subsystem
		jWebSocketSubsystem.addListener(new JWebSocketTokenListenerSample());

		// start the subsystem
		jWebSocketSubsystem.start();

		// get the token server
		TokenServer lTS0 = JWebSocketFactory.getTokenServer();
		if (lTS0 != null) {
			// and add the sample listener to the server's listener chain
			lTS0.addListener(new JWebSocketTokenListenerSample());
		}

		// get the custom server
		CustomServer lCS0 = (CustomServer) JWebSocketFactory.getServer("cs0");
		if (lCS0 != null) {
			// and add the sample listener to the server's listener chain
			lCS0.addListener(new JWebSocketCustomListenerSample());
		}


		// wait until shutdown requested
		jWebSocketSubsystem.run();
		// stop the subsystem
		jWebSocketSubsystem.stop();
	}
}
