//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Test Plug-In
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
package org.jwebsocket.plugins.test;

import java.util.Date;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 *
 * @author aschulze
 */
public class PerfTestPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(PerfTestPlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private static final String NS_TEST = JWebSocketServerConstants.NS_BASE + ".plugins.test";

	/**
	 *
	 * @param aConfiguration
	 */
	public PerfTestPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating performane test plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_TEST);
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if ("testS2CPerformance".equals(lType)) {
				testS2CPerformance(aConnector, aToken);
			}
		}
	}

	private Date mSendTestStarted(WebSocketConnector aConnector, String aTest) {
		Token lToken = TokenFactory.createToken(NS_TEST, BaseToken.TT_EVENT);
		lToken.setString("name", "testStarted");
		lToken.setString("test", aTest);
		Date lDate = new Date();
		lToken.setString("timestamp", Tools.DateToISO8601WithMillis(lDate));
		getServer().sendToken(aConnector, lToken);
		return lDate;
	}

	private Date mSendTestStopped(WebSocketConnector aConnector, String aTest) {
		Token lToken = TokenFactory.createToken(NS_TEST, BaseToken.TT_EVENT);
		lToken.setString("name", "testStopped");
		lToken.setString("test", aTest);
		Date lDate = new Date();
		lToken.setString("timestamp", Tools.DateToISO8601WithMillis(lDate));
		getServer().sendToken(aConnector, lToken);
		return lDate;
	}

	private void testS2CPerformance(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		String lMessage = aToken.getString("message", "Default Text Message");
		Integer lCount = aToken.getInteger("count", 50);

		Token lTestToken = TokenFactory.createToken();
		lTestToken.setString("data", lMessage);

		// send test stopped event
		long lStartMillis = mSendTestStarted(aConnector, "testS2CPerformance").getTime();

		// run the test
		for (int lLoop = 0; lLoop < lCount; lLoop++) {
			// lServer.sendToken(aConnector, lTestToken);
			lServer.broadcastToken(lTestToken);
		}
		// send test stopped event
		long lStopMillis = mSendTestStopped(aConnector, "testS2CPerformance").getTime();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		lResponse.setInteger("duration", (int) (lStopMillis - lStartMillis));
		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
}
