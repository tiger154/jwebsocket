// ---------------------------------------------------------------------------
// jWebSocket - JMXPlugIn v1.0
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
// THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
// THIS CODE IS NOT YET SECURE AND MAY NOT BE USED FOR PRODUCTION ENVIRONMENTS!
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

package org.jwebsocket.plugins.jmx;

import java.util.Map;
import javax.management.openmbean.CompositeData;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.factory.JWebSocketFactory;

/**
 *
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class JMXServerFunctions {

	private static Logger mLog;

	public JMXServerFunctions() {
	}

	public static void setLog(Logger aLog) {
		JMXServerFunctions.mLog = aLog;
	}
	
	public CompositeData allConnectors(String aServer) throws Exception {
		CompositeData lCompData = null;
		try {
			if (aServer.equals("") || aServer.equals("String")) {
				throw new IllegalArgumentException("The server Id must not be empty.");
			}
			Map lAllConnectors = JWebSocketFactory.getServer(aServer).getAllConnectors();
			Map lServerConnectors = new FastMap();
			if (!lAllConnectors.isEmpty()) {
				for (int i = 0; i < lAllConnectors.size(); i++) {
					Map lTempConnectors = new FastMap();
					lTempConnectors.put("connectionId", lAllConnectors.keySet().toArray()[i]);
					if (null != ((WebSocketConnector) lAllConnectors.values().toArray()[i]).getUsername()) {
						lTempConnectors.put("userName", ((WebSocketConnector) lAllConnectors.values().toArray()[i]).getUsername());
					} else {
						lTempConnectors.put("userName", "empty");
					}
					lTempConnectors.put("remotePort", ((WebSocketConnector) lAllConnectors.values().toArray()[i]).getRemotePort());

					lServerConnectors.put("connector_" + i, lTempConnectors);
				}

				lCompData = JMXHandler.convertMapToCompositeData(lServerConnectors);
			} else {
				throw new Exception("This server doesn't have any connectors");
			}
		} catch (Exception ex) {
			mLog.error("JMXServerFunctions on allConnectors: " + ex.getMessage());
			throw new Exception(ex.getMessage());
		}
		return lCompData;
	}
}
