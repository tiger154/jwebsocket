//	---------------------------------------------------------------------------
//	jWebSocket Scripting Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.scripting;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.activation.MimetypesFileTypeMap;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javolution.util.FastMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.async.AsyncResult;
import org.jwebsocket.async.AsyncResultHandler;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.factory.LocalLoader;
import org.jwebsocket.jms.Attributes;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketSession;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.scripting.app.BaseScriptApp;
import org.jwebsocket.plugins.scripting.app.ClusterMessageTypes;
import org.jwebsocket.plugins.scripting.app.Manifest;
import org.jwebsocket.plugins.scripting.app.js.JavaScriptApp;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * Refer to
 * http://docs.oracle.com/javase/6/docs/technotes/guides/scripting/programmer_guide/index.html
 * http://download.java.net/jdk8/docs/technotes/guides/scripting/programmer_guide/index.html
 *
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 */
public class ScriptingPlugIn extends ActionPlugIn {

	private static final Logger mLog = Logging.getLogger();
	/**
	 * The ScriptingPlugIn namespace.
	 */
	public static final String NS = JWebSocketServerConstants.NS_BASE + ".plugins.scripting";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ScriptingPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket Scripting Plug-in - Community Edition";
	/**
	 * The running applications container.
	 */
	private Map<String, BaseScriptApp> mApps = new FastMap<String, BaseScriptApp>().shared();
	/**
	 * ScriptingPlugIn local bean factory instance.
	 */
	protected ApplicationContext mBeanFactory;
	/**
	 * Configuration settings for the scripting plug-in. Controlled by Spring configuration.
	 */
	protected Settings mSettings;

	/**
	 * Constructor.
	 *
	 * @param aConfiguration
	 */
	public ScriptingPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Scripting plug-in...");
		}
		// specify default name space for file system plugin
		this.setNamespace(NS);

		try {
			mBeanFactory = getConfigBeanFactory(NS);
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for scripting plug-in, some features may not be available.");
			} else {
				mSettings = (Settings) mBeanFactory.getBean("org.jwebsocket.plugins.scripting.settings");

				// initializing JMS connection at this level if present
				try {
					if (mBeanFactory.containsBean("jmsConnection")) {
						mBeanFactory.getBean("jmsConnection0");
					}
				} catch (Exception lEx) {
					mLog.error("Unable to load default JMS connection. Resource will not be able on Script Apps!");
				}

				if (mLog.isInfoEnabled()) {
					mLog.info("Scripting plug-in successfully instantiated.");
				}
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating scripting plug-in"));
		}
	}

	@Override
	public void systemStarted() throws Exception {
		// initializing apps
		Map<String, String> lApps = mSettings.getApps();
		for (String lAppName : lApps.keySet()) {
			try {
				execAppBeforeLoadChecks(lAppName, lApps.get(lAppName));
				loadApp(lAppName, lApps.get(lAppName), false);
			} catch (Exception lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "loading '" + lAppName + "' application"));
			}
		}

		notifyToApps(BaseScriptApp.EVENT_SYSTEM_STARTED, new Object[0]);
		try {
			// registering on message hub if running on a cluster
			getServer().getJMSManager().subscribe(new MessageListener() {

				@Override
				public void onMessage(Message aMessage) {
					try {
						// discard processing if the message comes from the current server node
						if (JWebSocketConfig.getConfig().getNodeId().equals(aMessage.getStringProperty(Attributes.NODE_ID))) {
							return;
						}

						ClusterMessageTypes lType = ClusterMessageTypes.valueOf(aMessage.getStringProperty(Attributes.MESSAGE_TYPE));
						switch (lType) {
							case LOAD_APP: {
								String lAppName = aMessage.getStringProperty("appName");
								Boolean lHotLoad = aMessage.getBooleanProperty("hotLoad");
								String lPath = mSettings.getApps().get(lAppName);

								// loading app
								loadApp(lAppName, lPath, lHotLoad);
								break;
							}
							case UNDEPLOY_APP: {
								String lAppName = aMessage.getStringProperty("appName");
								// validating
								BaseScriptApp lScriptApp = mApps.get(lAppName);

								// notifying event before undeploy
								lScriptApp.notifyEvent(BaseScriptApp.EVENT_UNDEPLOYING, new Object[0]);

								// deleting app
								mApps.remove(lAppName);
								FileUtils.deleteDirectory(new File(lScriptApp.getPath()));
								break;
							}
						}
					} catch (Exception lEx) {
						mLog.error(Logging.getSimpleExceptionMessage(lEx, "processing cluster message: " + aMessage.toString()));
					}
				}
			}, "ns = '" + NS + "'");
		} catch (Exception aException) {
			mLog.error("Exception catched while getting the JMS Manager instance with the following message: " + aException.getMessage());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Scripting plug-in finished startup process!");
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
	 *
	 * @param aAppName
	 * @param aAppPath
	 * @return
	 */
	public Permissions getAppPermissions(String aAppName, String aAppPath) {
		return mSettings.getAppPermissions(aAppName, aAppPath);
	}

	/**
	 *
	 * @return
	 */
	public String getExtensionsDirectoryPath() {
		return mSettings.getExtensionsDirectory();
	}

	private void execAppBeforeLoadChecks(final String aAppName, String aAppPath) throws Exception {
		// parsing app manifest
		File lManifestFile = new File(aAppPath + "/manifest.json");
		if (!lManifestFile.exists() || !lManifestFile.canRead()) {
			String lMsg = "Unable to load '" + aAppName + "' application. Manifest file no found!";
			mLog.error(lMsg);
			throw new FileNotFoundException(lMsg);
		}
		// parsing app manifest file
		ObjectMapper lMapper = new ObjectMapper();
		Map<String, Object> lTree = lMapper.readValue(lManifestFile, Map.class);
		Token lManifestJSON = TokenFactory.createToken();
		lManifestJSON.setMap(lTree);

		// getting script language extension
		String lExt = lManifestJSON.getString(Manifest.LANGUAGE_EXT, "js");

		// checking jWebSocket version 
		Manifest.checkJwsVersion(lManifestJSON.getString(Manifest.JWEBSOCKET_VERSION, "1.0.0"));

		// checking jWebSocket plug-ins dependencies
		Manifest.checkJwsDependencies(lManifestJSON.getList(
				Manifest.JWEBSOCKET_PLUGINS_DEPENDENCIES, new ArrayList<String>()));

		// checking sandbox permissions dependency
		Manifest.checkPermissions(lManifestJSON.getList(Manifest.PERMISSIONS,
				new ArrayList()), mSettings.getAppPermissions(aAppName, aAppPath),
				aAppPath);

		// validating bootstrap file
		final File lBootstrap = new File(aAppPath + "/App." + lExt);
		if (!lBootstrap.exists() || !lBootstrap.canRead()) {
			String lMsg = "Unable to load '" + aAppName + "' application. Bootstrap file not found!";
			mLog.error(lMsg);
			throw new FileNotFoundException(lMsg);
		}

		LocalLoader lClassLoader = new LocalLoader((URLClassLoader) ClassLoader.getSystemClassLoader());
		ScriptEngineManager lManager = new ScriptEngineManager(lClassLoader);

		final ScriptEngine lScriptApp;
		final BaseScriptApp lApp;
		if ("js".equals(lExt)) {
			// making "nashorn" the default engine for JavaScript
			if (null != lManager.getEngineByName("nashorn")) {
				lScriptApp = lManager.getEngineByName("nashorn");
			} else {
				lScriptApp = lManager.getEngineByExtension(lExt);
			}
		} else {
			lScriptApp = lManager.getEngineByExtension(lExt);
		}

		// creating the high level script app instance
		if ("js".equals(lExt)) {
			lApp = new JavaScriptApp(this, aAppName, aAppPath, lScriptApp, lClassLoader);
		} else {
			String lMsg = "The extension '" + lExt + "' is not currently supported!";
			mLog.error(lMsg);
			throw new Exception(lMsg);
		}

		// loading application into security sandbox
		Tools.doPrivileged(mSettings.getAppPermissions(aAppName, aAppPath),
				new PrivilegedAction<Object>() {
					@Override
					public Object run() {
						try {
							// evaluating app content
							lScriptApp.eval(FileUtils.readFileToString(lBootstrap));
							return null;
						} catch (Exception lEx) {
							String lAction = (mApps.containsKey(aAppName)) ? "reloaded" : "loaded";
							String lMsg = "Script applicaton '" + aAppName + "' not " + lAction
							+ " because it failed the 'before-load' checks: " + lEx.getMessage();
							mLog.info(lMsg);
							throw new RuntimeException(lMsg);
						}
					}
				});

		if (mLog.isDebugEnabled()) {
			mLog.debug(aAppName + "(" + lExt + ") application passed the 'before-load' checks successfully!");
		}
	}

	/**
	 * Loads an script application.
	 *
	 * @param aAppName The application name
	 * @param aAppPath The application home path
	 * @param aHotLoad
	 * @return
	 * @throws Exception
	 */
	private void loadApp(final String aAppName, String aAppPath, boolean aHotLoad) throws Exception {
		// notifying before app reload event here
		BaseScriptApp lScript = mApps.get(aAppName);
		if (null != lScript) {
			lScript.notifyEvent(BaseScriptApp.EVENT_BEFORE_APP_RELOAD, new Object[]{aHotLoad});
			if (!aHotLoad) {
				destroyAppBeans(lScript);
			}
		}

		// parsing app manifest
		File lManifestFile = new File(aAppPath + "/manifest.json");

		// parsing app manifest file
		ObjectMapper lMapper = new ObjectMapper();
		Map<String, Object> lTree = lMapper.readValue(lManifestFile, Map.class);
		Token lManifestJSON = TokenFactory.createToken();
		lManifestJSON.setMap(lTree);

		// getting script language extension
		String lExt = lManifestJSON.getString(Manifest.LANGUAGE_EXT, "js");

		// validating bootstrap file
		final File lBootstrap = new File(aAppPath + "/App." + lExt);

		// support hot app load
		if (aHotLoad && mApps.containsKey(aAppName)) {
			try {
				// loading app
				mApps.get(aAppName).eval(lBootstrap.getPath());
			} catch (ScriptException lEx) {
				mLog.error("Script applicaton '" + aAppName + "' failed to start: " + lEx.getMessage());
				mApps.remove(aAppName);
				throw new ScriptException(lEx.getMessage());
			}
		} else {
			LocalLoader lClassLoader = new LocalLoader((URLClassLoader) ClassLoader.getSystemClassLoader());
			ScriptEngineManager lManager = new ScriptEngineManager(lClassLoader);

			final ScriptEngine lScriptApp;
			if ("js".equals(lExt)) {
				// making "nashorn" the default engine for JavaScript
				if (null != lManager.getEngineByName("nashorn")) {
					lScriptApp = lManager.getEngineByName("nashorn");
				} else {
					lScriptApp = lManager.getEngineByExtension(lExt);
				}
			} else {
				lScriptApp = lManager.getEngineByExtension(lExt);
			}

			// crating the high level script app instance
			if ("js".equals(lExt)) {
				mApps.put(aAppName, new JavaScriptApp(this, aAppName, aAppPath, lScriptApp, lClassLoader));
			} else {
				String lMsg = "The extension '" + lExt + "' is not currently supported!";
				mLog.error(lMsg);
				throw new Exception(lMsg);
			}

			final BaseScriptApp lApp = mApps.get(aAppName);
			// loading application into security sandbox
			Tools.doPrivileged(mSettings.getAppPermissions(aAppName, aAppPath),
					new PrivilegedAction<Object>() {
						@Override
						public Object run() {
							try {
								// loading app
								lApp.eval(lBootstrap.getPath());
								return null;
							} catch (Exception lEx) {
								mLog.error("Script applicaton '" + aAppName + "' failed to start: " + lEx.getMessage());
								mApps.remove(aAppName);
								throw new RuntimeException(lEx);
							}
						}
					});
		}

		// notifying app loaded event
		mApps.get(aAppName).notifyEvent(BaseScriptApp.EVENT_APP_LOADED, new Object[]{aHotLoad});

		if (mLog.isDebugEnabled()) {
			mLog.debug(aAppName + "(" + lExt + ") application loaded successfully!");
		}
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		super.engineStarted(aEngine);

		List<Object> lArgs = new ArrayList();
		lArgs.add(aEngine);

		notifyToApps(BaseScriptApp.EVENT_ENGINE_STARTED, lArgs.toArray());
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		super.engineStopped(aEngine);

		List<Object> lArgs = new ArrayList();
		lArgs.add(aEngine);

		notifyToApps(BaseScriptApp.EVENT_ENGINE_STOPPED, lArgs.toArray());
	}

	@Override
	public void processLogon(WebSocketConnector aConnector) {
		List<Object> lArgs = new ArrayList();
		lArgs.add(aConnector);

		notifyToApps(BaseScriptApp.EVENT_LOGON, lArgs.toArray());
	}

	void notifyToApps(String aEventName, Object[] aArgs) {
		for (BaseScriptApp lApp : mApps.values()) {
			lApp.notifyEvent(aEventName, aArgs);
		}
	}

	@Override
	public void processLogoff(WebSocketConnector aConnector) {
		List<Object> lArgs = new ArrayList();
		lArgs.add(aConnector);

		notifyToApps(BaseScriptApp.EVENT_LOGOFF, lArgs.toArray());
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		List<Object> lArgs = new ArrayList();
		lArgs.add(aConnector);

		notifyToApps(BaseScriptApp.EVENT_CONNECTOR_STARTED, lArgs.toArray());
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		List<Object> lArgs = new ArrayList();
		lArgs.add(aConnector);
		lArgs.add(aCloseReason);

		notifyToApps(BaseScriptApp.EVENT_CONNECTOR_STOPPED, lArgs.toArray());
	}

	@Override
	public void sessionStarted(WebSocketConnector aConnector, WebSocketSession aSession) {
		List<Object> lArgs = new ArrayList();
		lArgs.add(aConnector);

		notifyToApps(BaseScriptApp.EVENT_SESSION_STARTED, lArgs.toArray());
	}

	@Override
	public void sessionStopped(WebSocketSession aSession) {
		List<Object> lArgs = new ArrayList();
		lArgs.add(aSession);

		notifyToApps(BaseScriptApp.EVENT_SESSION_STOPPED, lArgs.toArray());
	}

	@Override
	public void systemStarting() throws Exception {
		notifyToApps(BaseScriptApp.EVENT_SYSTEM_STARTING, new Object[0]);
	}

	@Override
	public void systemStopped() throws Exception {
		notifyToApps(BaseScriptApp.EVENT_SYSTEM_STOPPED, new Object[0]);
	}

	@Override
	public void systemStopping() throws Exception {
		notifyToApps(BaseScriptApp.EVENT_SYSTEM_STOPPING, new Object[0]);
	}

	/**
	 * Capture and redirect tokens to target apps.
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	public void tokenAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lApp = aToken.getString("app");
		Assert.notNull(lApp, "The 'app' argument cannot be null!");
		Assert.isTrue(mApps.containsKey(lApp), "The target application '" + lApp + "' does not exists!");

		// creating a new token
		Map lToken = aToken.getMap("token", new HashMap());
		lToken.put("utid", aToken.getInteger("utid"));

		// creating arguments
		List<Object> lArgs = new ArrayList();
		lArgs.add(aConnector);
		lArgs.add(lToken);

		mApps.get(lApp).notifyEvent(BaseScriptApp.EVENT_FILTER_IN, new Object[]{lToken, aConnector});
		mApps.get(lApp).notifyEvent(BaseScriptApp.EVENT_TOKEN, lArgs.toArray());
	}

	/**
	 * List the client authorized script apps.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void listAppsAction(WebSocketConnector aConnector, Token aToken) {
		Iterator<String> lAppNames = mApps.keySet().iterator();
		Map<String, Map> lResult = new HashMap<String, Map>();
		boolean lUserOnly = aToken.getBoolean("userOnly", false);
		boolean lNamesOnly = aToken.getBoolean("namesOnly", false);

		while (lAppNames.hasNext()) {
			String lAppName = lAppNames.next();
			if (!lUserOnly || (lUserOnly && hasAuthority(aConnector, NS + ".deploy.*")
					|| hasAuthority(aConnector, NS + ".deploy." + lAppName))) {
				lResult.put(lAppName, new HashMap());

				if (lNamesOnly) {
					continue;
				}

				// locally caching object
				BaseScriptApp lApp = mApps.get(lAppName);
				// getting app details
				File lAppDirectory = new File(lApp.getPath());
				lResult.get(lAppName).put("lastModified", lAppDirectory.lastModified());
				lResult.get(lAppName).put("size", FileUtils.sizeOf(lAppDirectory));
				lResult.get(lAppName).put("whiteListedBeans", mSettings.getAppWhiteListedBeans(lAppName));

				// getting app security permissions
				List<String> lPermissions = new ArrayList<String>();
				lPermissions.addAll(mSettings.getGlobalSecurityPermissions());
				if (mSettings.getAppsSecurityPermissions().containsKey(lAppName)) {
					lPermissions.addAll(mSettings.getAppsSecurityPermissions().get(lAppName));
				}
				lResult.get(lAppName).put("permissions", lPermissions);

				// getting description and version
				try {
					lResult.get(lAppName).put("description", lApp.getDescription());
					lResult.get(lAppName).put("version", lApp.getVersion());
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "retrieving application info"));
				}
			}
		}
		Token lResponse = createResponse(aToken);
		lResponse.setMap("data", lResult);

		sendToken(aConnector, lResponse);
	}

	/**
	 * Reload a deployed script application.
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	public void reloadAppAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lAppName = aToken.getString("app");
		boolean lHotReload = aToken.getBoolean("hotReload", true);

		Assert.notNull(lAppName, "The 'app' argument cannot be null!");
		Assert.isTrue(mSettings.getApps().containsKey(lAppName), "The target application '" + lAppName + "' does not exists!");

		if (!hasAuthority(aConnector, NS + ".reloadApp.*")
				&& !hasAuthority(aConnector, NS + ".reloadApp." + lAppName)) {
			sendToken(aConnector, createAccessDenied(aToken));
			return;
		}

		// loading the app
		String lAppPath = mSettings.getApps().get(lAppName);
		execAppBeforeLoadChecks(lAppName, lAppPath);
		loadApp(lAppName, lAppPath, lHotReload);

		// broadcasting event to other ScriptingPlugIn nodes
		MapMessage lMessage = getServer().getJMSManager().buildMessage(NS, ClusterMessageTypes.LOAD_APP.name());
		lMessage.setStringProperty("appName", lAppName);
		lMessage.setBooleanProperty("hotLoad", lHotReload);
		lMessage.setStringProperty(Attributes.NODE_ID, JWebSocketConfig.getConfig().getNodeId());

		// sending the message
		getServer().getJMSManager().send(lMessage);

		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Call a custom method on a public application object.
	 *
	 * @param aConnector
	 * @param aToken
	 * @return
	 * @throws Exception
	 */
	private void callMethod(final WebSocketConnector aConnector, final Token aToken) throws Exception {
		String lApp = aToken.getString("app");
		String lObjectId = aToken.getString("objectId");
		String lMethod = aToken.getString("method");

		List<Object> lArgs = aToken.getList("args", new ArrayList());
		lArgs.add(aConnector);

		Assert.notNull(lApp, "The 'app' argument cannot be null!");
		Assert.isTrue(mApps.containsKey(lApp), "The target application '" + lApp + "' does not exists!");
		final BaseScriptApp lScript = mApps.get(lApp);

		// notify filter in
		lScript.notifyEvent(BaseScriptApp.EVENT_FILTER_IN, new Object[]{aToken, aConnector});

		final long lStartTime = System.currentTimeMillis();
		AsyncResultHandler<Object> lResultHandler = new AsyncResultHandler<Object>() {

			@Override
			public void handle(final AsyncResult<Object> aResult) {
				final Token lResponse = createResponse(aToken);
				if (aResult.isSuccees()) {
					long lEndTime = System.currentTimeMillis();
					lResponse.getMap().put("result", aResult.getResult());
					lResponse.getMap().put("processingTime", lEndTime - lStartTime);
				} else {
					lResponse.setCode(-1);
					lResponse.setString("msg", aResult.getFailure().getLocalizedMessage());
				}

				// sending response back
				lScript.sendToken(aConnector, lResponse.getMap());
			}
		};
		// passing the handler as method last argument to support async responses
		lArgs.add(lResultHandler);
		// calling the method
		Object lResult = callMethod(lApp, lObjectId, lMethod, lArgs);
		// supporting synchronous responses
		if (null != lResult) {
			new AsyncResult<Object>(lResultHandler).setResult(lResult);
		}
	}

	/**
	 * Call a custom method on a public application object.
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	public void callMethodAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		callMethod(aConnector, aToken);
	}

	/**
	 * Get a target application version.
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	public void getVersionAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lApp = aToken.getString("app");
		BaseScriptApp lScriptApp = mApps.get(lApp);
		Assert.notNull(lScriptApp, "The target app does not exists!");

		Token lResponse = createResponse(aToken);
		lResponse.setString("version", lScriptApp.getVersion());

		sendToken(aConnector, lResponse);
	}

	/**
	 * Get a target application client API
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	public void getClientAPIAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lApp = aToken.getString("app");
		BaseScriptApp lScriptApp = mApps.get(lApp);
		Assert.notNull(lScriptApp, "The target app does not exists!");

		Token lResponse = createResponse(aToken);
		lResponse.setMap("API", lScriptApp.getClientAPI());

		sendToken(aConnector, lResponse);
	}

	/**
	 * Call a public application object method
	 *
	 * @param aApp
	 * @param aObjectId
	 * @param aMethod
	 * @param aArgs
	 * @return
	 * @throws Exception
	 */
	public Object callMethod(String aApp, String aObjectId, String aMethod, Collection aArgs) throws Exception {
		Assert.notNull(aApp, "The 'app' argument cannot be null!");
		Assert.notNull(aObjectId, "The 'objectId' argument cannot be null!");
		Assert.notNull(aMethod, "The 'method' argument cannot be null!");

		BaseScriptApp lApp = mApps.get(aApp);
		Assert.notNull(lApp, "The target app does not exists!");

		Object lRes = lApp.callMethod(aObjectId, aMethod, aArgs);
		return lRes;
	}

	/**
	 * Deploy an application
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	public void deployAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		// getting calling arguments
		String lAppFile = aToken.getString("appFile");
		boolean lDeleteAfterDeploy = aToken.getBoolean("deleteAfterDeploy", false);
		boolean lHotDeploy = aToken.getBoolean("hotDeploy", false);

		// getting the FSP instance
		TokenPlugIn lFSP = (TokenPlugIn) getPlugInChain().getPlugIn("jws.filesystem");
		Assert.notNull(lFSP, "FileSystem plug-in is not running!");

		// creating invoke request for FSP
		Token lCommand = TokenFactory.createToken(JWebSocketServerConstants.NS_BASE + ".plugins.filesystem", "getAliasPath");
		lCommand.setString("alias", "privateDir");
		Token lResult = lFSP.invoke(aConnector, lCommand);
		Assert.notNull(lResult, "Unable to communicate with the FileSystem plug-in "
				+ "to retrieve the client private directory!");

		// locating the app zip file
		File lAppZipFile = new File(lResult.getString("aliasPath") + File.separator + lAppFile);
		Assert.isTrue(lAppZipFile.exists(), "The target application file '" + lAppFile + "' does not exists"
				+ " on the user file-system scope!");

		// validating MIME type
		String lFileType = new MimetypesFileTypeMap().getContentType(lAppZipFile);
		Assert.isTrue("application/zip, application/octet-stream".contains(lFileType),
				"The file format is not valid! Expecting a ZIP compressed directory.");

		// umcompressing in TEMP unique folder
		File lTempDir = new File(FileUtils.getTempDirectory().getCanonicalPath()
				+ File.separator
				+ UUID.randomUUID().toString()
				+ File.separator);

		try {
			Tools.unzip(lAppZipFile, lTempDir);
			if (lDeleteAfterDeploy) {
				lAppZipFile.delete();
			}
		} catch (IOException lEx) {
			throw new Exception("Unable to uncompress zip file: " + lEx.getMessage());
		}

		// validating structure
		File[] lTempAppDirContent = lTempDir.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
		Assert.isTrue(1 == lTempAppDirContent.length && lTempAppDirContent[0].isDirectory(),
				"Compressed application has invalid directory structure! "
				+ "Expecting a single root folder.");

		// executing before-load checks
		execAppBeforeLoadChecks(lTempAppDirContent[0].getName(), lTempAppDirContent[0].getPath());

		// copying application content to apps directory
		File lAppDir = new File(mSettings.getAppsDirectory() + File.separator
				+ lTempAppDirContent[0].getName());

		FileUtils.copyDirectory(lTempAppDirContent[0], lAppDir);
		FileUtils.deleteDirectory(lTempDir);

		// getting the application name
		String lAppName = lAppDir.getName();

		// checking security
		if (!hasAuthority(aConnector, NS + ".deploy.*")
				&& !hasAuthority(aConnector, NS + ".deploy." + lAppName)) {
			sendToken(aConnector, createAccessDenied(aToken));
			return;
		}

		// loading the script app
		loadApp(lAppName, lAppDir.getAbsolutePath(), lHotDeploy);

		// broadcasting event to other ScriptingPlugIn nodes
		MapMessage lMessage = getServer().getJMSManager().buildMessage(NS, ClusterMessageTypes.LOAD_APP.name());
		lMessage.setStringProperty("appName", lAppName);
		lMessage.setBooleanProperty("hotLoad", lHotDeploy);
		lMessage.setStringProperty(Attributes.NODE_ID, JWebSocketConfig.getConfig().getNodeId());

		// sending the message
		getServer().getJMSManager().send(lMessage);

		// finally send acknowledge response
		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Get a target application manifest content.
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	public void getManifestAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		// getting calling events
		String lApp = aToken.getString("app");
		Assert.notNull(lApp, "The 'app' argument cannot be null!");

		// validating
		BaseScriptApp lScriptApp = mApps.get(lApp);
		Assert.notNull(lScriptApp, "The target app does not exists!");

		// checking security
		if (!hasAuthority(aConnector, NS + ".deploy.*")
				&& !hasAuthority(aConnector, NS + ".deploy." + lApp)) {
			sendToken(aConnector, createAccessDenied(aToken));
			return;
		}

		// parsing app manifest
		File lManifestFile = new File(lScriptApp.getPath() + "/manifest.json");
		ObjectMapper lMapper = new ObjectMapper();
		Map<String, Object> lContent = lMapper.readValue(lManifestFile, Map.class);

		Token lResponse = createResponse(aToken);
		lResponse.setMap("data", lContent);

		sendToken(aConnector, lResponse);
	}

	/**
	 * Undeploy an application
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	public void undeployAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		// getting calling arguments
		String lApp = aToken.getString("app");
		Assert.notNull(lApp, "The 'app' argument cannot be null!");

		// validating
		BaseScriptApp lScriptApp = mApps.get(lApp);
		Assert.notNull(lScriptApp, "The target app does not exists!");

		// checking security
		if (!hasAuthority(aConnector, NS + ".deploy.*")
				&& !hasAuthority(aConnector, NS + ".deploy." + lApp)) {
			sendToken(aConnector, createAccessDenied(aToken));
			return;
		}

		// notifying event before undeploy
		lScriptApp.notifyEvent(BaseScriptApp.EVENT_UNDEPLOYING, new Object[0]);

		// propertly destroying script app active beans
		destroyAppBeans(lScriptApp);

		// deleting app
		mApps.remove(lApp);
		FileUtils.deleteDirectory(new File(lScriptApp.getPath()));

		// broadcasting event to other ScriptingPlugIn nodes
		MapMessage lMessage = getServer().getJMSManager().buildMessage(NS, ClusterMessageTypes.UNDEPLOY_APP.name());
		lMessage.setStringProperty("appName", lApp);
		lMessage.setStringProperty(Attributes.NODE_ID, JWebSocketConfig.getConfig().getNodeId());

		// sending the message
		getServer().getJMSManager().send(lMessage);

		// acknowledge response for the client
		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Check if an app has access to a target bean.
	 *
	 * @param aAppName The app name
	 * @param aBeanPath The bean path
	 */
	public void checkWhiteListedBean(String aAppName, String aBeanPath) {
		Iterator<String> lIt = mSettings.getAppWhiteListedBeans(aAppName).iterator();
		while (lIt.hasNext()) {
			String lWLB = lIt.next();
			// basic checks
			if (lWLB.equals(aBeanPath) || lWLB.equals("*:*")) {
				return;
			}

			// complex checks
			String[] lParts = aBeanPath.split(":");
			String lNS = lParts[0];

			if ("".equals(lNS) && lWLB.equals("*:*")) {
				return;
			}
			if (lWLB.equals(lNS + ":*")) {
				return;
			}
		}

		throw new AccessControlException("The '" + aBeanPath + "' bean access "
				+ "is not allowed in '" + aAppName + "' app!");
	}

	private void destroyAppBeans(BaseScriptApp aScriptApp) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Invoking script app active beans destruction...");
		}
		aScriptApp.getAppBeanFactory().refresh();
		aScriptApp.getAppBeanFactory().destroy();
	}
}
