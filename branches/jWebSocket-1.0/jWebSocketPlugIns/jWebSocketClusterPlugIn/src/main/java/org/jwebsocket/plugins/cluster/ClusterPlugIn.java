//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Cluster Plug-In
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//  THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
//  THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
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
	private static final String NS_TEST = JWebSocketServerConstants.NS_BASE + ".plugins.cluster";

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
		this.setNamespace(NS_TEST);
		if (mLog.isInfoEnabled()) {
			mLog.info("Cluster plug-in successfully loaded.");
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
