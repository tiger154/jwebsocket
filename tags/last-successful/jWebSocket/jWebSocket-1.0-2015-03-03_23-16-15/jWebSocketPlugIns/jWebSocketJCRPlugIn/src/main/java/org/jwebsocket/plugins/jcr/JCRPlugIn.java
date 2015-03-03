//	---------------------------------------------------------------------------
//	jWebSocket JCR Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.jcr;

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Alexander Schulze
 */
public class JCRPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	/**
	 *
	 */
	public static final String NS_JCR = JWebSocketServerConstants.NS_BASE + ".plugins.jcr";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket JCR PlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket Java Content Repository PlugIn - Community Edition";
	/**
	 *
	 */
	protected ApplicationContext mBeanFactory;
	/**
	 *
	 */
	protected Settings mSettings;

	/**
	 *
	 * @param aConfiguration
	 * @throws Exception
	 */
	public JCRPlugIn(PluginConfiguration aConfiguration) throws Exception {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Java Content Repository (JCR) plug-in...");
		}
		// specify default name space for file system plugin
		this.setNamespace(NS_JCR);

		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for JCR plug-in, some features may not be available.");
			} else {
				mSettings = (Settings) mBeanFactory.getBean("org.jwebsocket.plugins.jcr.settings");

				if (mLog.isInfoEnabled()) {
					mLog.info("Java Content Repository (JCR) plug-in successfully instantiated.");
				}
			}
		} catch (BeansException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating JCR plug-in"));
			throw lEx;
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
		return NS_JCR;
	}

	@Override
	public synchronized void engineStarted(WebSocketEngine aEngine) {
	}

	@Override
	public synchronized void engineStopped(WebSocketEngine aEngine) {
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("todo")) {
				todo(aConnector, aToken);
			}
		}
	}

	@Override
	public Token invoke(WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("todo")) {
				return null; // todo(aConnector, aToken);
			}
		}

		return null;
	}

	private void todo(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		Token lResponse = createResponse(aToken);

		// check if user is allowed to run 'todo' command
		if (!hasAuthority(aConnector, NS_JCR + ".todo")) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Returning 'Access denied'...");
			}
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		lServer.sendToken(aConnector, lResponse);
	}
}
