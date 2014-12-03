//	---------------------------------------------------------------------------
//	jWebSocket - Statistics Plug-In (Community Edition, CE)
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
package org.jwebsocket.plugins.statistics;

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 */
public class StatisticsPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	/*
	 private static String SMTP_HOST = null;
	 private static final String SMTP_HOST_KEY = "smtp_host";
	 */
	// if namespace changed update client plug-in accordingly!
	private static final String NS_STATISTICS
			= JWebSocketServerConstants.NS_BASE + ".plugins.statistics";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket StatisticsPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION
			= "jWebSocket Statistics PlugIn - Community Edition";
	// private WebSocketPlugIn mStreamingPlugin = null;
	// private BaseStream mStream = null;

	/**
	 *
	 * @param aConfiguration
	 */
	public StatisticsPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating statistics plug-in...");
		}
		/*
		 FilterConfiguration lConfig = new FilterConfig(
		 "");
		 TokenFilter lFilter = new StatisticsFilter(aConfiguration);
		 getServer().getFilterChain().addFilter(lFilter);
		 */
		// specify default name space for admin plugin
		this.setNamespace(NS_STATISTICS);
		//	mGetSettings();

		if (mLog.isInfoEnabled()) {
			mLog.info("Statistics plug-in successfully instantiated.");
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
		return NS_STATISTICS;
	}

	private void mGetSettings() {
		// SMTP_HOST = getString(SMTP_HOST_KEY, null);
		// TODO: remove this hardcoded stuff!
		// TODO: switch to dynamic dependencies here!
		/*
		 WebSocketPlugInChain lPluginChain = getPlugInChain();
		 mStreamingPlugin = lPluginChain.getPlugIn("jws.streaming");
		 if (mStreamingPlugin != null) {
		 mStream = (BaseStream) ((StreamingPlugIn) mStreamingPlugin).getStream("statisticStream");
		 }
		 */
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		mGetSettings();
		/*
		 if (null != mStream) {
		 Token lToken = TokenFactory.createToken(NS_STATISTICS, BaseToken.TT_EVENT);
		 lToken.setString("msg", "client connected");
		 lToken.setString("connId", aConnector.getId());
		 mStream.put(lToken);
		 } else {
		 mLog.warn("'statisticStream' not yet initialized or running!");
		 }
		 */
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseRease) {
		/*
		 if (null != mStream) {
		 Token lToken = TokenFactory.createToken(NS_STATISTICS, BaseToken.TT_EVENT);
		 lToken.setString("msg", "client disconnected");
		 lToken.setString("connId", aConnector.getId());
		 mStream.put(lToken);
		 } else {
		 mLog.warn("'statisticStream' not yet initialized or running!");
		 }
		 */
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			// select from database
			if (lType.equals("sendStatistics")) {
				sendStatistics(aConnector, aToken);
			}
		}
	}

	private void sendStatistics(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		String lFrom = aToken.getString("from", "[unknown]");

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		try {
			lResponse.setString("id", "");
		} catch (Exception lEx) {
			String lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
}
