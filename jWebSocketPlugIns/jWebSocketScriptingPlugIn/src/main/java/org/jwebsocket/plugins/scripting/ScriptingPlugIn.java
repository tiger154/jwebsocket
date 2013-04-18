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

import org.jwebsocket.plugins.scripting.app.JavaScriptApp;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javolution.util.FastMap;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketSession;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.plugins.annotations.Role;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * Refer to
 * http://docs.oracle.com/javase/6/docs/technotes/guides/scripting/programmer_guide/index.html
 * http://download.java.net/jdk8/docs/technotes/guides/scripting/programmer_guide/index.html
 *
 * @author aschulze
 * @author kyberneees
 */
public class ScriptingPlugIn extends ActionPlugIn {

	private static Logger mLog = Logging.getLogger();
	/**
	 * Namespace for scripting plug-in.
	 */
	public static final String NS =
			JWebSocketServerConstants.NS_BASE + ".plugins.scripting";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ScriptingPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket Scripting Plug-in - Community Edition";
	private static ScriptEngineManager mEngine = new ScriptEngineManager();
	/**
	 *
	 */
	private Map<String, ScriptEngine> mApps = new FastMap<String, ScriptEngine>().shared();
	private Map<String, JavaScriptApp> mAppsContext = new FastMap<String, JavaScriptApp>().shared();
	/**
	 *
	 */
	protected ApplicationContext mBeanFactory;
	/**
	 * Configuration settings for the scripting plug-in. Controlled by Spring
	 * configuration.
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
		this.setNamespace(NS);

		mEngine = new ScriptEngineManager();

		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for scripting plug-in, some features may not be available.");
			} else {
				mSettings = (Settings) mBeanFactory.getBean("org.jwebsocket.plugins.scripting.settings");

				// initializing apps
				for (String lApp : mSettings.getApps().keySet()) {
					if (mLog.isInfoEnabled()) {
						mLog.info("Scripting plug-in successfully instantiated.");
					}
					loadApp(lApp, mSettings.getApps().get(lApp));
				}

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
		return NS;
	}

	/**
	 * Loads a JavaScript application
	 *
	 * @param aApp The app name
	 * @param aFilePath The app home path
	 * @throws Exception
	 */
	public void loadApp(String aApp, String aFilePath) throws Exception {
		String lFile = FileUtils.readFileToString(new File(Tools.expandEnvVarsAndProps(aFilePath) + "/App.js"));
		ScriptEngine lScriptApp = mEngine.getEngineByName("javascript");
		mApps.put(aApp, lScriptApp);
		mAppsContext.put(aApp, new JavaScriptApp(this, aApp, aFilePath, lScriptApp));

		// creating application services
		lScriptApp.put("App", mAppsContext.get(aApp));
		lScriptApp.eval(lFile);
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		super.engineStarted(aEngine);

		List<Object> aArgs = new ArrayList();
		aArgs.add(aEngine);

		for (JavaScriptApp lApp : mAppsContext.values()) {
			lApp.notifyEvent(JavaScriptApp.EVENT_ENGINE_STARTED, aArgs.toArray());
		}
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		List<Object> aArgs = new ArrayList();
		aArgs.add(aEngine);

		for (JavaScriptApp lApp : mAppsContext.values()) {
			lApp.notifyEvent(JavaScriptApp.EVENT_ENGINE_STOPPED, aArgs.toArray());
		}
	}

	@Override
	public void processLogon(WebSocketConnector aConnector) {
		List<Object> aArgs = new ArrayList();
		aArgs.add(aConnector);

		for (JavaScriptApp lApp : mAppsContext.values()) {
			lApp.notifyEvent(JavaScriptApp.EVENT_LOGON, aArgs.toArray());
		}
	}

	@Override
	public void processLogoff(WebSocketConnector aConnector) {
		List<Object> aArgs = new ArrayList();
		aArgs.add(aConnector);

		for (JavaScriptApp lApp : mAppsContext.values()) {
			lApp.notifyEvent(JavaScriptApp.EVENT_LOGOFF, aArgs.toArray());
		}
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		List<Object> aArgs = new ArrayList();
		aArgs.add(aConnector);

		for (JavaScriptApp lApp : mAppsContext.values()) {
			lApp.notifyEvent(JavaScriptApp.EVENT_CONNECTOR_STARTED, aArgs.toArray());
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		List<Object> aArgs = new ArrayList();
		aArgs.add(aConnector);
		aArgs.add(aCloseReason);

		for (JavaScriptApp lApp : mAppsContext.values()) {
			lApp.notifyEvent(JavaScriptApp.EVENT_CONNECTOR_STOPPED, aArgs.toArray());
		}
	}

	@Override
	public void sessionStarted(WebSocketConnector aConnector, WebSocketSession aSession) {
		List<Object> aArgs = new ArrayList();
		aArgs.add(aConnector);


		for (JavaScriptApp lApp : mAppsContext.values()) {
			lApp.notifyEvent(JavaScriptApp.EVENT_SESSION_STARTED, aArgs.toArray());
		}
	}

	@Override
	public void sessionStopped(WebSocketSession aSession) {
		List<Object> aArgs = new ArrayList();
		aArgs.add(aSession);

		for (JavaScriptApp lApp : mAppsContext.values()) {
			lApp.notifyEvent(JavaScriptApp.EVENT_SESSION_STOPPED, aArgs.toArray());
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	public void tokenAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lApp = aToken.getString("app");
		Assert.notNull(lApp, "The 'app' argument cannot be null!");
		Assert.isTrue(mSettings.getApps().containsKey(lApp), "The target application '" + lApp + "' does not exists!");

		List<Object> aArgs = new ArrayList();
		aArgs.add(aConnector);
		aArgs.add(aToken.getMap());

		mAppsContext.get(lApp).notifyEvent(JavaScriptApp.EVENT_FILTER_IN, aArgs.toArray());
		mAppsContext.get(lApp).notifyEvent(JavaScriptApp.EVENT_TOKEN, aArgs.toArray());
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	@Role(name = NS + ".reloadApp")
	public void reloadAppAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lApp = aToken.getString("app");
		Assert.notNull(lApp, "The 'app' argument cannot be null!");
		Assert.isTrue(mSettings.getApps().containsKey(lApp), "The target application '" + lApp + "' does not exists!");

		// loading the app (will destroy if exists)
		loadApp(lApp, mSettings.getApps().get(lApp));

		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	public void callJsMethodAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String aApp = aToken.getString("app");
		String aObjectId = aToken.getString("objectId");
		String aMethod = aToken.getString("method");
		List<Object> aArgs = aToken.getList("args", new ArrayList());
		aArgs.add(aConnector);
		Object lResult = callJsMethod(aApp, aObjectId, aMethod, aArgs.toArray());

		Token lResponse = createResponse(aToken);
		lResponse.getMap().put("result", lResult);

		sendToken(aConnector, lResponse);
	}

	/**
	 * Calls an application object(exported) method
	 *
	 * @param aApp
	 * @param aObjectId
	 * @param aMethod
	 * @param aArgs
	 * @return
	 * @throws Exception
	 */
	public Object callJsMethod(String aApp, String aObjectId, String aMethod, Object[] aArgs) throws Exception {
		Assert.notNull(aApp, "The 'app' argument cannot be null!");
		Assert.notNull(aObjectId, "The 'objectId' argument cannot be null!");
		Assert.notNull(aMethod, "The 'method' argument cannot be null!");

		ScriptEngine lApp = mApps.get(aApp);
		Assert.notNull(lApp, "The target app does not exists!");

		Assert.isTrue(mAppsContext.get(aApp).isPublished(aObjectId), "The target object '" + aObjectId + "' is not yet published!");
		Object lObject = mAppsContext.get(aApp).getPublished(aObjectId);

		Invocable lScript = (Invocable) lApp;
		Object lRes = lScript.invokeMethod(lObject, aMethod, aArgs);

		return lRes;
	}
}
