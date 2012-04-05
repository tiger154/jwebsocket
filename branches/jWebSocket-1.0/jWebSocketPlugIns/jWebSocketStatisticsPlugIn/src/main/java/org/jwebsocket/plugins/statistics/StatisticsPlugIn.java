//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Statistics Plug-In
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
package org.jwebsocket.plugins.statistics;

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class StatisticsPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	/*
	private static String SMTP_HOST = null;
	private static final String SMTP_HOST_KEY = "smtp_host";
	 */
	// if namespace changed update client plug-in accordingly!
	private static final String NS_STATISTICS = JWebSocketServerConstants.NS_BASE + ".plugins.statistics";
	// private WebSocketPlugIn mStreamingPlugin = null;
	// private BaseStream mStream = null;

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
