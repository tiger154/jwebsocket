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

import java.util.List;
import java.util.Map;
import javax.management.openmbean.CompositeData;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jmx.util.JMXHandler;

/**
 *
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class JMXServerFunctions {

	private static Logger mLog;

	/**
	 *
	 */
	public JMXServerFunctions() {
	}

	/**
	 *
	 * @param aLog
	 */
	public static void setLog(Logger aLog) {
		JMXServerFunctions.mLog = aLog;
	}

	/**
	 *
	 * @param aServer
	 * @return
	 * @throws Exception
	 */
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

	/**
	 *
	 * @return @throws Exception
	 */
	public CompositeData getServers() throws Exception {
		CompositeData result = null;
		try {
			List<WebSocketServer> lAllServers = JWebSocketFactory.getServers();
			Map lServers = new FastMap();

			for (int i = 0; i < lAllServers.size(); i++) {
				Map lServerData = new FastMap();
				lServerData.put("id", lAllServers.get(i).getId());
				lServerData.put("jarName", lAllServers.get(i).getServerConfiguration().getJar());
				lServerData.put("corePoolSize", lAllServers.get(i).getServerConfiguration().getThreadPoolConfig().getCorePoolSize());
				lServerData.put("maximunPoolSize", lAllServers.get(i).getServerConfiguration().getThreadPoolConfig().getMaximumPoolSize());
				lServerData.put("keepAliveTime", lAllServers.get(i).getServerConfiguration().getThreadPoolConfig().getKeepAliveTime());
				lServerData.put("blockingQueueSize", lAllServers.get(i).getServerConfiguration().getThreadPoolConfig().getBlockingQueueSize());

				lServers.put("server_" + lAllServers.get(i).getId(), lServerData);
			}

			result = JMXHandler.convertMapToCompositeData(lServers);
		} catch (Exception ex) {
			mLog.error("JMXPlugIn on getServers: " + ex.getMessage());
			throw new Exception(ex.getMessage());
		}
		return result;
	}

	/**
	 *
	 * @return @throws Exception
	 */
	public CompositeData getPlugIns() throws Exception {
		CompositeData result = null;
		try {
			List<WebSocketServer> lAllServers = JWebSocketFactory.getServers();
			Map lServers = new FastMap();

			for (int i = 0; i < lAllServers.size(); i++) {
				if (lAllServers.get(i).getPlugInChain() != null) {
					List<WebSocketPlugIn> lAllPlugins = lAllServers.get(i).getPlugInChain().getPlugIns();
					Map lServerPlugins = new FastMap();
					if (!lAllPlugins.isEmpty()) {
						for (int j = 1; j <= lAllPlugins.size(); j++) {
							Map lPlugins = new FastMap();
							TokenPlugIn lValue = (TokenPlugIn) lAllPlugins.get(j - 1);
							lPlugins.put("id", lValue.getId());
							lPlugins.put("name", lValue.getName());
							lPlugins.put("namespace", lValue.getNamespace());
							lPlugins.put("version", lValue.getVersion());
							lPlugins.put("isEnable", lValue.getEnabled());

							lServerPlugins.put("plugin_" + lValue.getId(), lPlugins);
						}
					} else {
						lServerPlugins.put("plugin", "This server doesn't have any plugins loaded");
					}

					lServers.put("serverId_" + lAllServers.get(i).getId(), lServerPlugins);
				} else {
					lServers.put("serverId_" + lAllServers.get(i).getId(), "This server doesn't have any plugins chain");
				}

			}

			result = JMXHandler.convertMapToCompositeData(lServers);
		} catch (Exception ex) {
			mLog.error("JMXPlugIn on getPlugIns: " + ex.getMessage());
			throw new Exception(ex.getMessage());
		}
		return result;
	}

	/**
	 *
	 * @return @throws Exception
	 */
	public CompositeData getFilters() throws Exception {
		CompositeData result = null;
		try {
			List<WebSocketServer> lAllServers = JWebSocketFactory.getServers();
			Map lServers = new FastMap();

			for (int i = 0; i < lAllServers.size(); i++) {
				if (lAllServers.get(i).getFilterChain() != null) {
					List<WebSocketFilter> lAllFilters = lAllServers.get(i).getFilterChain().getFilters();
					Map lServerFilters = new FastMap();
					if (!lAllFilters.isEmpty()) {
						for (int j = 1; j <= lAllFilters.size(); j++) {
							Map lFilters = new FastMap();
							WebSocketFilter lValue = lAllFilters.get(j - 1);
							lFilters.put("id", lValue.getId());
							lFilters.put("namespace", lValue.getNS());
							lFilters.put("version", lValue.getVersion());
							lFilters.put("isEnable", lValue.getEnabled());

							lServerFilters.put("filter_" + lValue.getId(), lFilters);
						}
					} else {
						lServerFilters.put("filter", "This server doesn't have any filters loaded");
					}
					lServers.put("serverId_" + lAllServers.get(i).getId(), lServerFilters);
				} else {
					lServers.put("serverId_" + lAllServers.get(i).getId(), "This server doesn't have any filter chain");
				}
			}
			result = JMXHandler.convertMapToCompositeData(lServers);
		} catch (Exception ex) {
			mLog.error("JMXPlugIn on getFilters: " + ex.getMessage());
			throw new Exception(ex.getMessage());
		}
		return result;
	}
}
