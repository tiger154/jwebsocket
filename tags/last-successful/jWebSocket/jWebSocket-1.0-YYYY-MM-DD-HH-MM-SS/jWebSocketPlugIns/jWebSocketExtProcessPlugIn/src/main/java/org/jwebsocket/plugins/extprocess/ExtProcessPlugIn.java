//	---------------------------------------------------------------------------
//	jWebSocket External Process Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.extprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

/**
 *
 * @author aschulze
 */
public class ExtProcessPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	/**
	 *
	 */
	public static final String NS_EXTPROCESS = JWebSocketServerConstants.NS_BASE + ".plugins.extprocess";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ExtProcessPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket ExtProcessPlugIn - Community Edition";
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
	public ExtProcessPlugIn(PluginConfiguration aConfiguration) throws Exception {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating External Process plug-in...");
		}
		// specify default name space for file system plugin
		this.setNamespace(NS_EXTPROCESS);

		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for external process plug-in, some features may not be available.");
			} else {
				mSettings = (Settings) mBeanFactory.getBean("org.jwebsocket.plugins.extprocess.settings");
				// replace all alias values with environment variables
				Map<String, String> lAllowedProgs = mSettings.getAllowedProgs();
				for (Entry<String, String> lEntry : lAllowedProgs.entrySet()) {
					lEntry.setValue(Tools.expandEnvVarsAndProps(lEntry.getValue()));
				}
				if (mLog.isInfoEnabled()) {
					mLog.info("External Process plug-in successfully instantiated.");
				}
			}
		} catch (BeansException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating ExtProcess plug-in"));
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
		return NS_EXTPROCESS;
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
			if (lType.equals("call")) {
				call(aConnector, aToken);
			}
		}
	}

	@Override
	public Token invoke(WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			/*
			 if (lType.equals("getFilelist")) {
			 return getFilelist(aConnector.getUsername(), aToken);
			 } else if (lType.equals("getAliasPath")) {
			 String lTargetAlias = aToken.getString("alias");
			 Token lToken = TokenFactory.createToken();
			 lToken.setString("aliasPath", getAliasPath(aConnector, lTargetAlias));

			 return lToken;
			 }
			 */
		}

		return null;
	}

	private void call(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		Token lResponse = createResponse(aToken);

		// check if user is allowed to run 'exists' command
		/*
		 if (!hasAuthority(aConnector, NS_EXTPROCESS + ".call")) {
		 if (mLog.isDebugEnabled()) {
		 mLog.debug("Returning 'Access denied'...");
		 }
		 lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
		 return;
		 }
		 */

		String lAlias = aToken.getString("alias");
		if (null == lAlias) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "No alias passed.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}
		String lCmdLine = mSettings.getAllowedProgs().get(lAlias);
		if (null == lCmdLine) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "Alias '" + lAlias + "' not found.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}
		List<?> lArgs = aToken.getList("args");

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'call'"
					+ " (alias: "
					+ lAlias
					+ ", args: "
					+ StringUtils.collectionToDelimitedString(lArgs, ", ")
					+ ")...");
		}
		String[] lCmdTokens = StringUtils.tokenizeToStringArray(lCmdLine, " ", true, false);
		List<String> lCmd = new ArrayList<String>();
		for (String lCmdToken : lCmdTokens) {
			for (int lArgIdx = 0; lArgIdx < lArgs.size(); lArgIdx++) {
				lCmdToken = lCmdToken.replace("${" + (lArgIdx + 1) + "}", lArgs.get(lArgIdx).toString());
			}
			lCmd.add(lCmdToken);
		}

		ProcessBuilder lProcBuilder = new ProcessBuilder(lCmd);
		// Map<String, String> lEnv = lProcBuilder.environment();
		lProcBuilder.directory(new File(System.getenv("temp")));

		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Directory: " + System.getenv("temp"));
			}
			final Process process = lProcBuilder.start();
			InputStream lIS = process.getInputStream();
			InputStreamReader lISR = new InputStreamReader(lIS);
			BufferedReader lBR = new BufferedReader(lISR);
			String lLine;
			StringBuilder lStrBuf = new StringBuilder();
			while ((lLine = lBR.readLine()) != null) {
				Token lEventToken = TokenFactory.createToken(getNamespace(), "event");
				lEventToken.setString("line", lLine);
				lStrBuf.append(lLine).append("\n");
				lServer.sendToken(aConnector, lEventToken);
			}
			lResponse.setInteger("exitCode", process.exitValue());
			if (mLog.isDebugEnabled()) {
				mLog.debug("Sent '" + lStrBuf.toString().replace("\n", "\\n") + "'.");
			}
		} catch (IOException lEx) {
			lResponse.setInteger("code", -1);
			String lMsg = Logging.getSimpleExceptionMessage(lEx, "calling external process");
			lResponse.setString("msg", lMsg);
			mLog.error(lMsg);
		}

		lServer.sendToken(aConnector, lResponse);
	}
}
