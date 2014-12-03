// ---------------------------------------------------------------------------
// jWebSocket - JMXPlugInFunctions (Community Edition, CE)
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

import java.util.List;
import java.util.Map;
import javax.management.openmbean.CompositeData;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jmx.util.JMXHandler;
import org.jwebsocket.token.JSONToken;
import org.jwebsocket.token.Token;

/**
 * Class to invoke certain features of the plugins that are running on a given
 * server. It also provides information about the plugins that are loaded and
 * the functionalities that can be invoked.
 *
 * @author Lisdey Perez Hernandez
 */
public class JMXPlugInFunctions {

	private static CompositeData mInformationOfRunningServers;
	private static final Logger mLog = Logging.getLogger();

	/**
	 * Default class constructor.
	 */
	public JMXPlugInFunctions() {
	}

	/**
	 * Displays information about the plugins that are loaded on the jWebSocket
	 * server.
	 *
	 * @return CompositeData
	 * @throws Exception
	 */
	public CompositeData getInformationOfRunningServers() throws Exception {
		CompositeData result = null;
		try {
			List<WebSocketServer> lAllServers = JWebSocketFactory.getServers();
			Map<String, Object> lServers = new FastMap<String, Object>();

			for (int i = 0; i < lAllServers.size(); i++) {
				List<WebSocketPlugIn> lAllPlugins
						= lAllServers.get(i).getPlugInChain().getPlugIns();
				Map<String, Object> lServerPlugins = new FastMap<String, Object>();
				if (!lAllPlugins.isEmpty()) {
					for (int j = 1; j <= lAllPlugins.size(); j++) {
						Map<String, Object> lPlugins = new FastMap<String, Object>();
						TokenPlugIn lValue = (TokenPlugIn) lAllPlugins.get(j - 1);
						lPlugins.put("id", lValue.getId());
						lPlugins.put("name", lValue.getName());
						lPlugins.put("namespace", lValue.getNamespace());
						List<String> methodsList = lValue.invokeMethodList();
						if (methodsList != null) {
							lPlugins.put("methodsName", methodsList);
						} else {
							lPlugins.put("methodsName", "No supported yet.");
						}

						lServerPlugins.put("plugin_" + lValue.getId(), lPlugins);
					}
				} else {
					lServerPlugins.put("plugin", "This server doesn't have any "
							+ "plugins loaded");
				}

				lServers.put("serverId_" + lAllServers.get(i).getId(), lServerPlugins);
			}

			result = JMXHandler.convertMapToCompositeData(lServers);
		} catch (Exception ex) {
			mLog.error("JMXPlugInFunctions on getInformationOfRunningServers: "
					+ ex.getMessage());
			throw new Exception(ex.getMessage());
		}
		return result;
	}

	/**
	 * Allows remotely invoke the functionalities of the plugins that are loaded
	 * on the jWebSocket server.
	 *
	 * @param aServer
	 * @param aPluginId
	 * @param aMethodName
	 * @param aMethodParameters
	 * @return CompositeData
	 * @throws Exception
	 */
	public CompositeData invokePluginOperation(String aServer, String aPluginId,
			String aMethodName, String aMethodParameters) throws Exception {
		try {
			if (aServer.equals("") || aPluginId.equals("") || aMethodName.equals("")
					|| aMethodParameters.equals("")) {
				throw new IllegalArgumentException("The parameters must not be "
						+ "empty.");
			} else {
				//get the object of the server
				WebSocketServer lServer = JWebSocketFactory.getServer(aServer);
				if (lServer != null) {
					//get the plugin
					TokenPlugIn lPlugin = (TokenPlugIn) lServer.getPlugInById(aPluginId);
					if (lPlugin != null) {
						//create the token with json methodParameters
						JSONObject lParameters = new JSONObject(aMethodParameters);
						JSONToken lObjToken = new JSONToken(lParameters);
						lObjToken.setNS(lPlugin.getNamespace());
						lObjToken.setType(aMethodName);
						//invoke the plugin method
						Token lResponse = lPlugin.invoke(null, lObjToken);
						if (lResponse != null) {
							/*
							 * creating a CompositeData to expose the
							 * TokenResponse of the plugins
							 */
							CompositeData lResult = JMXHandler.convertMapToCompositeData(lResponse.getMap());
							if (lResult != null) {
								return lResult;
							} else {
								throw new NullPointerException("Failed to convert"
										+ " the resulting Token to a CompositeData.");
							}
						} else {
							throw new NullPointerException("The method specified "
									+ "is not available to invoke.");
						}
					} else {
						throw new NullPointerException("The plugin Id do not "
								+ "belong to any loaded jWebSocket PlugIn.");
					}
				} else {
					throw new NullPointerException("The server Id do not belong "
							+ "to any running jWebSocket Server.");
				}
			}
		} catch (Exception lEx) {
			mLog.error("JMXPlugInFunctions on invokePluginOperation: "
					+ lEx.getMessage());
			throw new Exception(lEx.getMessage());
		}

	}
}
