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
 * server. It also provides information about the plugins that are loaded and the 
 * functionalities that can be invoked.
 * 
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class JMXPlugInFunctions {
	
	private static CompositeData mInformationOfRunningServers;
	private static Logger mLog = Logging.getLogger();

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
			Map lServers = new FastMap();

			for (int i = 0; i < lAllServers.size(); i++) {
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
						List<String> methodsList = lValue.invokeMethodList();
						if(methodsList != null)
							lPlugins.put("methodsName", methodsList);
						else 
							lPlugins.put("methodsName", "No supported yet.");
						
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
		} catch (Exception ex) {
			mLog.error("JMXPlugInFunctions on invokePluginOperation: " 
					+ ex.getMessage());
			throw new Exception(ex.getMessage());
		}

	}
}
