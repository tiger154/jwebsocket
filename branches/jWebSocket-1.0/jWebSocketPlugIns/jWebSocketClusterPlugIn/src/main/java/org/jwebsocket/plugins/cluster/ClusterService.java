//	---------------------------------------------------------------------------
//	jWebSocket - ClusterService (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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
package org.jwebsocket.plugins.cluster;

import java.util.List;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.xml.EngineConfig;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author alexanderschulze
 */
public class ClusterService {

	private static final Logger mLog = Logging.getLogger();

	/**
	 *
	 * @param aEngineId
	 * @param aEngineName
	 */
	public static void addEngine(String aEngineId, String aEngineName) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Adding engine: " + aEngineId);
		}
		List<String> lDomains = new FastList<String>();
		lDomains.add("*");
		EngineConfig lEngineCfg = new EngineConfig(
				aEngineId, // id
				aEngineName, // name 
				"-", // jar
				0, // port
				0, // ssl port
				// TODO: replace by intended valid id or correct hostname!
				"[hostname]", // hostname
				"-", // keystore
				"-", // keystore pw
				"-", // context
				"-", // servlet
				0, // timeout
				65536, // max frame size
				lDomains, // domains
				1000, // max connections
				"-", // max connection stretegy
				null // settings
				);
		ClusterEngine lEngine = new ClusterEngine(lEngineCfg);
		JWebSocketFactory.getEngines().put(aEngineId, lEngine);
		List<WebSocketServer> lServers = JWebSocketFactory.getServers();
		for (WebSocketServer lServer : lServers) {
			lServer.addEngine(lEngine);
		}
		if (mLog.isInfoEnabled()) {
			mLog.info("Engine added: " + aEngineId);
		}
	}

	/**
	 *
	 * @param aEngineId
	 */
	public static void removeEngine(String aEngineId) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Removing engine '" + aEngineId + "'...");
		}
		WebSocketEngine lEngine = JWebSocketFactory.getEngines().get(aEngineId);
		if (null == lEngine) {
			List<WebSocketServer> lServers = JWebSocketFactory.getServers();
			for (WebSocketServer lServer : lServers) {
				lServer.removeEngine(lEngine);
			}
			JWebSocketFactory.getEngines().remove(aEngineId);
			if (mLog.isInfoEnabled()) {
				mLog.info("Engine removed: " + aEngineId);
			}
		} else {
			mLog.error("Engine '" + aEngineId + "' not found.");
		}
	}
}
