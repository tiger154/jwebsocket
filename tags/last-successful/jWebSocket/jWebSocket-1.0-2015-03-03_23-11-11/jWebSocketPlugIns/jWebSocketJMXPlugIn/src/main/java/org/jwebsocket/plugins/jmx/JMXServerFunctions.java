// ---------------------------------------------------------------------------
// jWebSocket - JMXServerFunctions (Community Edition, CE)
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
package org.jwebsocket.plugins.jmx;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import javax.management.openmbean.CompositeData;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jmx.util.JMXHandler;

/**
 * Class that allows to manage certain functions of jWebSocket server .
 *
 * @author Lisdey Perez Hernandez
 */
public class JMXServerFunctions {

	private static final Logger mLog = Logging.getLogger();

	/**
	 * The class constructor.
	 */
	public JMXServerFunctions() {
	}

	/**
	 * Displays information about the existing connectors on a certain
	 * jWebSocket server.
	 *
	 * @param aServer
	 * @return
	 * @throws Exception
	 */
	public CompositeData allConnectors(String aServer) throws Exception {
		CompositeData lCompData = null;
		try {
			if (aServer.equals("") || aServer.equals("String")) {
				throw new IllegalArgumentException("The server Id must not be "
						+ "empty.");
			}
			Map lAllConnectors =
					JWebSocketFactory.getServer(aServer).getAllConnectors();

			Map lServerConnectors = new FastMap();
			if (!lAllConnectors.isEmpty()) {
				for (int i = 0; i < lAllConnectors.size(); i++) {
					Map lTempConnectors = new FastMap();
					lTempConnectors.put("connectionId",
							lAllConnectors.keySet().toArray()[i]);

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
	 * Shows characteristics about the jWebSocket servers that are running.
	 *
	 * @return CompositeData
	 * @throws Exception
	 */
	public CompositeData getServers() throws Exception {
		CompositeData result = null;
		try {
			List<WebSocketServer> lAllServers = JWebSocketFactory.getServers();
			Map lServers = new FastMap();

			for (int i = 0; i < lAllServers.size(); i++) {
				Map lServerData = new FastMap();
				lServerData.put("id", lAllServers.get(i).getId());
				lServerData.put("jarName",
						lAllServers.get(i).getServerConfiguration().getJar());
				lServerData.put("corePoolSize",
						lAllServers.get(i).getServerConfiguration().getThreadPoolConfig().getCorePoolSize());
				lServerData.put("maximunPoolSize",
						lAllServers.get(i).getServerConfiguration().getThreadPoolConfig().getMaximumPoolSize());
				lServerData.put("keepAliveTime",
						lAllServers.get(i).getServerConfiguration().getThreadPoolConfig().getKeepAliveTime());
				lServerData.put("blockingQueueSize",
						lAllServers.get(i).getServerConfiguration().getThreadPoolConfig().getBlockingQueueSize());

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
	 * Displays information about the plugins that are loaded on the jWebSocket
	 * server.
	 *
	 * @return CompositeData
	 * @throws Exception
	 */
	public CompositeData getPlugIns() throws Exception {
		CompositeData result = null;
		try {
			List<WebSocketServer> lAllServers = JWebSocketFactory.getServers();
			Map lServers = new FastMap();

			for (int i = 0; i < lAllServers.size(); i++) {
				if (lAllServers.get(i).getPlugInChain() != null) {
					List<WebSocketPlugIn> lAllPlugins =
							lAllServers.get(i).getPlugInChain().getPlugIns();
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
						lServerPlugins.put("plugin", "This server doesn't have "
								+ "any plugins loaded");
					}

					lServers.put("serverId_" + lAllServers.get(i).getId(), lServerPlugins);
				} else {
					lServers.put("serverId_" + lAllServers.get(i).getId(), "This"
							+ " server doesn't have any plugins chain");
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
	 * Displays information about the filters that are loaded on the jWebSocket
	 * server.
	 *
	 * @return CompositeData
	 * @throws Exception
	 */
	public CompositeData getFilters() throws Exception {
		CompositeData result = null;
		try {
			List<WebSocketServer> lAllServers = JWebSocketFactory.getServers();
			Map lServers = new FastMap();

			for (int i = 0; i < lAllServers.size(); i++) {
				if (lAllServers.get(i).getFilterChain() != null) {
					List<WebSocketFilter> lAllFilters =
							lAllServers.get(i).getFilterChain().getFilters();
					Map lServerFilters = new FastMap();
					if (!lAllFilters.isEmpty()) {
						for (int j = 1; j <= lAllFilters.size(); j++) {
							Map lFilters = new FastMap();
							WebSocketFilter lValue = lAllFilters.get(j - 1);
							lFilters.put("id", lValue.getId());
							lFilters.put("namespace", lValue.getNS());
							lFilters.put("version", lValue.getVersion());
							lFilters.put("isEnable", lValue.getEnabled());

							lServerFilters.put("filter_"
									+ lValue.getId(), lFilters);
						}
					} else {
						lServerFilters.put("filter", "This server doesn't have "
								+ "any filters loaded");
					}
					lServers.put("serverId_" + lAllServers.get(i).getId(),
							lServerFilters);
				} else {
					lServers.put("serverId_" + lAllServers.get(i).getId(), "This"
							+ " server doesn't have any filter chain");
				}
			}
			result = JMXHandler.convertMapToCompositeData(lServers);
		} catch (Exception ex) {
			mLog.error("JMXPlugIn on getFilters: " + ex.getMessage());
			throw new Exception(ex.getMessage());
		}
		return result;
	}

	/**
	 * Shows characteristics of the jWebSocket server engine.
	 *
	 * @return CompositeData
	 * @throws Exception
	 */
	public CompositeData getEngine() throws Exception {
		CompositeData result = null;
		try {
			WebSocketEngine lEngine = JWebSocketFactory.getEngine();

			Map lEngineConf = new FastMap();
			lEngineConf.put("id", lEngine.getId());
			lEngineConf.put("jarName", lEngine.getConfiguration().getJar());
			lEngineConf.put("port", lEngine.getConfiguration().getPort());
			lEngineConf.put("sslPort", lEngine.getConfiguration().getSSLPort());
			lEngineConf.put("timeout", lEngine.getConfiguration().getTimeout());
			lEngineConf.put("maxFrameSize",
					lEngine.getConfiguration().getMaxFramesize());
			lEngineConf.put("maxConnections",
					lEngine.getConfiguration().getMaxConnections());
			lEngineConf.put("onMaxConnectionStrategy",
					lEngine.getConfiguration().getOnMaxConnectionStrategy());
			lEngineConf.put("domains", lEngine.getConfiguration().getDomains());

			result = JMXHandler.convertMapToCompositeData(lEngineConf);
		} catch (Exception ex) {
			mLog.error("JMXServerFunctions on getEngine: " + ex.getMessage());
			throw new Exception(ex.getMessage());
		}
		return result;
	}

	/**
	 * Displays information about the node where the jWebSocket server is
	 * located .
	 *
	 * @return CompositeData
	 * @throws Exception
	 */
	public CompositeData getInstanceInfo() throws Exception {
		CompositeData result = null;
		try {
			String lNodeId = JWebSocketConfig.getConfig().getNodeId();
			String lEngine = JWebSocketFactory.getEngine().getId();

			InetAddress lAddress = InetAddress.getLocalHost();
			byte[] lBytes = lAddress.getAddress();
			String lIpNumber = "";

			for (int i = 0; i < lBytes.length; i++) {
				int lByte = lBytes[i] < 0 ? lBytes[i] + 256 : lBytes[i];
				lIpNumber += lByte + ".";
			}

			FastMap instanceInfo = new FastMap();
			instanceInfo.put("nodeId", lNodeId);
			instanceInfo.put("engine", lEngine);
			instanceInfo.put("ipNumber",
					lIpNumber.substring(0, lIpNumber.length() - 1));

			result = JMXHandler.convertMapToCompositeData(instanceInfo);
		} catch (Exception ex) {
			mLog.error("JMXServerFunctions on getInstanceInfo: " + ex.getMessage());
			throw new Exception(ex.getMessage());
		}
		return result;
	}
}
