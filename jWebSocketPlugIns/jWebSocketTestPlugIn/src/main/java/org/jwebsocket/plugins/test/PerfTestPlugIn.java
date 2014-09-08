//	---------------------------------------------------------------------------
//	jWebSocket - Performance Test Plug-In(Community Edition, CE)
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
package org.jwebsocket.plugins.test;

import java.util.Date;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
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
 * @author Alexander Schulze
 */
public class PerfTestPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	// if namespace changed update client plug-in accordingly!
	private static final String NS_TEST
			= JWebSocketServerConstants.NS_BASE + ".plugins.test";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket PerfTestPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION
			= "jWebSocket Performance Tests PlugIn - Community Edition";

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
		if (mLog.isInfoEnabled()) {
			mLog.info("Performance test plug-in successfully instantiated.");
		}
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getVendor() {
		return VENDOR;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getLicense() {
		return LICENSE;
	}

	@Override
	public String getNamespace() {
		return NS_TEST;
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
