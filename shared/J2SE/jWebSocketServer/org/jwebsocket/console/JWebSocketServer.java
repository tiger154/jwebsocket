//	---------------------------------------------------------------------------
//	jWebSocket - Server Main Class
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.console;

import org.jwebsocket.factory.JWebSocketFactory;

/**
 * Main entry point for stand alone jWebSocket server system
 * 
 * @author puran
 * @version $Id: JWebSocketServer.java 443 2010-05-06 12:03:08Z fivefeetfurther$
 */
public class JWebSocketServer {

	/**
	 * @param aArgs the command line arguments
	 */
	public static void main(String[] aArgs) {
		// the following line must not be removed due to GNU LGPL 3.0 license!
		JWebSocketFactory.printCopyrightToConsole();

		String lOverrideConfigPath = JWebSocketFactory.getConfigOverridePath(aArgs);
		try {
			JWebSocketFactory.start(lOverrideConfigPath,
                    JWebSocketFactory.getBootstrapOverridePath(aArgs));

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
