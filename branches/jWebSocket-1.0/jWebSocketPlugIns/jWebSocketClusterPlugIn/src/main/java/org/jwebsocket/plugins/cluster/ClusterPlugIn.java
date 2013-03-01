//	---------------------------------------------------------------------------
//	jWebSocket - Cluster Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class ClusterPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	// if namespace changed update client plug-in accordingly!
	private static final String NS_CLUSTER = JWebSocketServerConstants.NS_BASE + ".plugins.cluster";

	/**
	 *
	 * @param aConfiguration
	 */
	public ClusterPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating cluster plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_CLUSTER);
		if (mLog.isInfoEnabled()) {
			mLog.info("Cluster plug-in successfully instantiated.");
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if ("".equals(lType)) {
				mthd(aConnector, aToken);
			}
		}
	}

	private void mthd(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// String lMessage = aToken.getString("message", "Default Text Message");
		// Integer lCount = aToken.getInteger("count", 50);

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);


		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
}
