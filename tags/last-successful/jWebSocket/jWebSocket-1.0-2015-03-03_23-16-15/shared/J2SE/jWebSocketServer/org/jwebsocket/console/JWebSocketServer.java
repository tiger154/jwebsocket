//	---------------------------------------------------------------------------
//	jWebSocket - Server Main Class (Community Edition, CE)
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

import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.factory.JWebSocketFactory;

/**
 * Main entry point for stand alone jWebSocket server system
 *
 * @author Alexander Schulze, puran
 */
public class JWebSocketServer {

	/**
	 * @param aArgs the command line arguments
	 */
	public static void main(String[] aArgs) {
		// the following line must not be removed due to Apache 2.0 license!
		JWebSocketFactory.printCopyrightToConsole();

		// check if home, config or bootstrap path are passed by command line
		JWebSocketConfig.initForConsoleApp(aArgs);

		try {
			// start the jWebSocket Server
			JWebSocketFactory.start();

			// run server until shut down request
			JWebSocketFactory.run();

		} catch (Exception lEx) {
			System.out.println(
					lEx.getClass().getSimpleName()
					+ " on starting jWebSocket server: "
					+ lEx.getMessage());
		} finally {
			JWebSocketFactory.stop();
		}
	}
}
