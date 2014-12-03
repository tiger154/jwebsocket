//	---------------------------------------------------------------------------
//	jWebSocket - Benchmark Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.benchmark;

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class BenchmarkPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	private final String S2C_PERFORMANCE = "s2c_performance";
	private static final String NS_BENCHMARK
			= JWebSocketServerConstants.NS_BASE + ".plugins.benchmark";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket BenchmarkPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket BenchmarkPlugIn - Community Edition";

	/**
	 *
	 * @param aConfiguration
	 * @throws Exception
	 */
	public BenchmarkPlugIn(PluginConfiguration aConfiguration) throws Exception {
		super(aConfiguration);

		setNamespace(NS_BENCHMARK);
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
		return NS_BENCHMARK;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		if (getNamespace().equals(aToken.getNS())) {
			if (S2C_PERFORMANCE.equals(aToken.getType())) {
				testS2CPerformance(aConnector, aToken);
			}
		}
	}

	/**
	 * Test the performance of a broadcast operation
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void testS2CPerformance(WebSocketConnector aConnector,
			Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting broadcasting benchmark...");
		}
		TokenServer lServer = getServer();

		String lMessage = aToken.getString("message");
		Integer lCount = aToken.getInteger("count", 0);

		Token lTestToken = TokenFactory.createToken(getNamespace(), S2C_PERFORMANCE);
		lTestToken.setString("data", lMessage);

		for (int lLoop = 0; lLoop < lCount; lLoop++) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Broadcasting test token (loop " + (lLoop + 1) + "/" + lCount + ")...");
			}
			lServer.broadcastToken(lTestToken);
		}

		// Instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		// Send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
}
