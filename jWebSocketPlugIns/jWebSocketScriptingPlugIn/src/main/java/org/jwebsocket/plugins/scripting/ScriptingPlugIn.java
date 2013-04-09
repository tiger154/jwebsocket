//	---------------------------------------------------------------------------
//	jWebSocket Scripting Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.scripting;

import java.util.List;
import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javolution.util.FastMap;
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
import org.springframework.context.ApplicationContext;

/**
 *
 * Refer to
 * http://docs.oracle.com/javase/6/docs/technotes/guides/scripting/programmer_guide/index.html
 * http://download.java.net/jdk8/docs/technotes/guides/scripting/programmer_guide/index.html
 *
 * @author aschulze
 */
public class ScriptingPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	/**
	 * Namespace for scripting plug-in.
	 */
	public static final String NS_Scripting =
			JWebSocketServerConstants.NS_BASE + ".plugins.scripting";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ScriptingPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket Scripting Plug-in - Community Edition";
	private static ScriptEngineManager mScrEngMgr = new ScriptEngineManager();
	/**
	 *
	 */
	public static ScriptEngine mJavaScript = null;
	/**
	 *
	 */
	protected ApplicationContext mBeanFactory;
	/**
	 * Configuration settings for the scripting plug-in. Controlled by Spring configuration.
	 */
	protected Settings mSettings;

	/**
	 *
	 * @param aConfiguration
	 * @throws Exception
	 */
	public ScriptingPlugIn(PluginConfiguration aConfiguration) throws Exception {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Scripting plug-in...");
		}
		// specify default name space for file system plugin
		this.setNamespace(NS_Scripting);

		mScrEngMgr = new ScriptEngineManager();
		mJavaScript = mScrEngMgr.getEngineByExtension("js");

		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for scripting plug-in, some features may not be available.");
			} else {
				mBeanFactory = getConfigBeanFactory();
				mSettings = (Settings) mBeanFactory.getBean("org.jwebsocket.plugins.scripting.settings");

				if (mLog.isInfoEnabled()) {
					mLog.info("Scripting plug-in successfully instantiated.");
				}
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating scripting plug-in"));
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
		return NS_Scripting;
	}

	/*
	 @Override
	 public synchronized void engineStarted(WebSocketEngine aEngine) {
	 }

	 @Override
	 public synchronized void engineStopped(WebSocketEngine aEngine) {
	 }
	 */
	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("invokeJavaScript")) {
				invokeJavaScript(aConnector, aToken);
			} else if (lType.equals("executeJavaScript")) {
				executeJavaScript(aConnector, aToken);
			}
		}
	}

	/* example for a static method which can be accessed from a script */
	/**
	 *
	 * @return
	 */
	public static Map getScriptingPlugInInfo() {
		FastMap<String, String> lInfo = new FastMap<String, String>();
		lInfo.put("version", VERSION);
		lInfo.put("vendor", VENDOR);
		return lInfo;
	}

	/**
	 *
	 * @param aConnectorId
	 * @param aToken
	 */
	public void sendToken(String aConnectorId, Object[] aToken) {
		TokenServer lServer = getServer();
		Token lToken = TokenFactory.createToken();
		FastMap<String, Object> lArgs = new FastMap<String, Object>();
		for (int lIdx = 0; lIdx < aToken.length; lIdx++) {
			lArgs.put("f" + lIdx, aToken[lIdx]);
		}
		lToken.setMap(lArgs);
		WebSocketConnector lConnector = lServer.getConnector(aConnectorId);
		if (null != lConnector) {
			lServer.sendToken(lConnector, lToken);
		}
	}

	/**
	 * internally invoke a java script file
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private Token mInvokeJavaScript(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Invoking JavaScript...");
		}

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		String lJavaScript = aToken.getString("javaScript");
		String lFunction = aToken.getString("function");
		List lArgs = aToken.getList("args");

		Invocable lScript;
		try {
			mJavaScript.eval(lJavaScript);
			mJavaScript.put("server", this);
			lScript = (Invocable) mJavaScript;
			Object lRes = lScript.invokeFunction(lFunction, lArgs.toArray());
			lResponse.getMap().put("result", lRes);
			if (mLog.isInfoEnabled()) {
				mLog.info("Parsing successful.");
			}
			return lResponse;
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "invoking function..."));
			return lServer.createErrorToken(aToken, -1, lEx.getMessage());
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	protected void invokeJavaScript(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		String lMsg;

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'invokeJavaScript'...");
		}

		// check if user is allowed to run 'load' command
		/*
		 if (!hasAuthority(aConnector, NS_Scripting + ".run")) {
		 if (mLog.isDebugEnabled()) {
		 mLog.debug("Returning 'Access denied'...");
		 }
		 lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
		 return;
		 }
		 */

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		String lAlias = aToken.getString("alias");
		String lFunction = aToken.getString("function");
		String lJavaScript =
				"function main() {"
				+ "server.sendToken('" + aConnector.getId() + "', ['arg1','arg2'] );"
				+ "var a = org.jwebsocket.plugins.scripting.ScriptingPlugIn.getScriptingPlugInInfo();"
				+ "return a;"
				+ "}";
		
		if (null == lFunction) {
			lMsg = "No function passed in scripting call.";
			if (mLog.isDebugEnabled()) {
				mLog.debug(lMsg);
			}
			lServer.sendErrorToken(aConnector, lResponse, -1, lMsg);
			return;
		}
		List lArgs = aToken.getList("args");

		Token aCall = TokenFactory.createToken();
		aCall.setString("javaScript", lJavaScript);
		aCall.setString("function", lFunction);
		aCall.setList("args", lArgs);

		lResponse = mInvokeJavaScript(aConnector, aCall);
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 * Executes a JavaScript file on the server.
	 * @param aConnector
	 * @param aToken
	 */
	protected void executeJavaScript(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		String lMsg;

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'executeJavaScript'...");
		}

		// check if user is allowed to run 'load' command
		/*
		 if (!hasAuthority(aConnector, NS_Scripting + ".run")) {
		 if (mLog.isDebugEnabled()) {
		 mLog.debug("Returning 'Access denied'...");
		 }
		 lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
		 return;
		 }
		 */

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		String lFunction = aToken.getString("function");
		if (null == lFunction) {
			lMsg = "No function passed in scripting call.";
			if (mLog.isDebugEnabled()) {
				mLog.debug(lMsg);
			}
			lServer.sendErrorToken(aConnector, lResponse, -1, lMsg);
			return;
		}

		String lJavaScript = aToken.getString("javascript");
		if (null == lJavaScript) {
			lMsg = "No javascript passed in scripting call.";
			if (mLog.isDebugEnabled()) {
				mLog.debug(lMsg);
			}
			lServer.sendErrorToken(aConnector, lResponse, -1, lMsg);
			return;
		}

		List lArgs = aToken.getList("args");

		Token aCall = TokenFactory.createToken();
		aCall.setString("javaScript", lJavaScript);
		aCall.setString("function", lFunction);
		aCall.setList("args", lArgs);

		lResponse = mInvokeJavaScript(aConnector, aCall);
		lServer.sendToken(aConnector, lResponse);
	}
}
