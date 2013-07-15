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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.activation.MimetypesFileTypeMap;
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
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketSession;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.ActionPlugIn;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.scripting.app.BaseScriptApp;
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
 * @author aschulze
 * @author kyberneees
 */
public class ScriptingPlugIn extends ActionPlugIn {

	private static Logger mLog = Logging.getLogger();
	/**
	 *
	 */
	public static final String NS = JWebSocketServerConstants.NS_BASE + ".plugins.scripting";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ScriptingPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket Scripting Plug-in - Community Edition";
	private static ScriptEngineManager mEngineManager = new ScriptEngineManager();
	/**
	 *
	 */
	private Map<String, BaseScriptApp> mApps = new FastMap<String, BaseScriptApp>().shared();
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

		mEngineManager = new ScriptEngineManager();
		try {
			mBeanFactory = getConfigBeanFactory(NS);
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for scripting plug-in, some features may not be available.");
			} else {
				mSettings = (Settings) mBeanFactory.getBean("org.jwebsocket.plugins.scripting.settings");

				// initializing JMS connection at this level if present
				if (mBeanFactory.containsBean("jmsConnection")) {
					mBeanFactory.getBean("jmsConnection");
				}

				// initializing apps
				Map<String, String> lApps = mSettings.getApps();
				for (String lApp : lApps.keySet()) {
					loadApp(lApp, lApps.get(lApp), false);
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
	 * @param aApp The application name
	 * @param aAppDirPath The application home path
	 * @param aHotLoad
	 * @return
	 * @throws Exception
	 */
	private boolean loadApp(final String aApp, String aAppDirPath, boolean aHotLoad) throws Exception {
		// notifying before app reload event here
		BaseScriptApp lScript = mApps.get(aApp);
		if (null != lScript) {
			lScript.notifyEvent(BaseScriptApp.EVENT_BEFORE_APP_RELOAD, new Object[]{aHotLoad});
		}

		// parsing app name to get the extension if present. default: js
		String[] lAppNameExt = aApp.split(".");
		String lExt = (lAppNameExt.length == 2) ? lAppNameExt[1] : "js";

		// validating bootstrap file
		final File lBootstrap = new File(aAppDirPath + "/App." + lExt);
		if (!lBootstrap.exists() || !lBootstrap.canRead()) {
			mLog.error("Unable to load '" + aApp + "' application. The bootstrap file '" + lBootstrap + "' does not exists!");
			return false;
		}

		// support hot app load
		if (aHotLoad && mApps.containsKey(aApp)) {
			try {
				// loading app
				mApps.get(aApp).getEngine().eval(FileUtils.readFileToString(lBootstrap));
			} catch (ScriptException lEx) {
				mLog.error("Script applicaton '" + aApp + "' failed to start: " + lEx.getMessage());
				mApps.remove(aApp);
				return false;
			}
		} else {
			final ScriptEngine lScriptApp;
			if ("js".equals(lExt)) {
				// making "nashorn" the default engine for JavaScript
				if (null != mEngineManager.getEngineByName("nashorn")) {
					lScriptApp = mEngineManager.getEngineByName("nashorn");
				} else {
					lScriptApp = mEngineManager.getEngineByExtension(lExt);
				}
			} else {
				lScriptApp = mEngineManager.getEngineByExtension(lExt);
			}

			// crating the high level script app instance
			if ("js".equals(lExt)) {
				mApps.put(aApp, new JavaScriptApp(this, aApp, aAppDirPath, lScriptApp));
			} else {
				mLog.error("The extension '" + lExt + "' is not currently supported!");
				return false;
			}

			// loading application into security sandbox
			Boolean lResult = (Boolean) Tools.doPrivileged(mSettings.getAppPermissions(aApp),
					new PrivilegedAction<Boolean>() {
						@Override
						public Boolean run() {
							try {
								// evaluating app content
								lScriptApp.eval(FileUtils.readFileToString(lBootstrap));
								return true;
							} catch (Exception lEx) {
								mLog.error("Script applicaton '" + aApp + "' failed to start: " + lEx.getMessage());
								mApps.remove(aApp);
								return false;
							}
						}
					});

			if (!lResult) {
				return false;
			}
		}

		// notifying app loaded event
		mApps.get(aApp).notifyEvent(BaseScriptApp.EVENT_APP_LOADED, new Object[0]);

		if (mLog.isDebugEnabled()) {
			mLog.debug(aApp + "(" + lExt + ") application loaded successfully!");
		}

		return true;
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
	public void systemStarted() throws Exception {
		notifyToApps(BaseScriptApp.EVENT_SYSTEM_STARTED, new Object[0]);
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

		while (lAppNames.hasNext()) {
			String lName = lAppNames.next();
			if (hasAuthority(aConnector, NS + ".deploy.*")
					|| hasAuthority(aConnector, NS + ".deploy." + lName)) {
				lResult.put(lName, new HashMap());
				// locally caching object
				BaseScriptApp lScript = mApps.get(lName);
				// getting app details
				File lAppDirectory = new File(lScript.getPath());
				lResult.get(lName).put("lastModified", lAppDirectory.lastModified());
				lResult.get(lName).put("size", FileUtils.sizeOf(lAppDirectory));
				try {
					lResult.get(lName).put("description", lScript.getDescription());
					lResult.get(lName).put("version", lScript.getVersion());
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
	 * Reloads a deployed script application.
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	public void reloadAppAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lApp = aToken.getString("app");
		boolean lHotReload = aToken.getBoolean("hotReload", true);

		Assert.notNull(lApp, "The 'app' argument cannot be null!");
		Assert.isTrue(mSettings.getApps().containsKey(lApp), "The target application '" + lApp + "' does not exists!");

		if (!hasAuthority(aConnector, NS + ".reloadApp.*")
				&& !hasAuthority(aConnector, NS + ".reloadApp." + lApp)) {
			sendToken(aConnector, createAccessDenied(aToken));
			return;
		}

		// loading the app (will destroy if exists)
		loadApp(lApp, mSettings.getApps().get(lApp), lHotReload);

		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Calls a custom method on a publish application object.
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	public void callMethodAction(WebSocketConnector aConnector, Token aToken) throws Exception {
		String lApp = aToken.getString("app");
		String lObjectId = aToken.getString("objectId");
		String lMethod = aToken.getString("method");

		List<Object> lArgs = aToken.getList("args", new ArrayList());
		lArgs.add(aConnector);

		Assert.notNull(lApp, "The 'app' argument cannot be null!");
		Assert.isTrue(mApps.containsKey(lApp), "The target application '" + lApp + "' does not exists!");
		BaseScriptApp lScript = mApps.get(lApp);

		// notify filter in
		lScript.notifyEvent(BaseScriptApp.EVENT_FILTER_IN, new Object[]{aToken, aConnector});

		long lStartTime = System.currentTimeMillis();
		Object lResult = callMethod(lApp, lObjectId, lMethod, lArgs.toArray());
		long lEndTime = System.currentTimeMillis();

		Token lResponse = createResponse(aToken);
		lResponse.getMap().put("result", lResult);
		lResponse.getMap().put("processingTime", lEndTime - lStartTime);

		// notify filter out
		lScript.notifyEvent(BaseScriptApp.EVENT_FILTER_OUT, new Object[]{lResponse, aConnector});

		sendToken(aConnector, lResponse);
	}

	/**
	 * Gets a target application version.
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
	 * Calls an application object(published) method
	 *
	 * @param aApp
	 * @param aObjectId
	 * @param aMethod
	 * @param aArgs
	 * @return
	 * @throws Exception
	 */
	public Object callMethod(String aApp, String aObjectId, String aMethod, Object[] aArgs) throws Exception {
		Assert.notNull(aApp, "The 'app' argument cannot be null!");
		Assert.notNull(aObjectId, "The 'objectId' argument cannot be null!");
		Assert.notNull(aMethod, "The 'method' argument cannot be null!");

		BaseScriptApp lApp = mApps.get(aApp);
		Assert.notNull(lApp, "The target app does not exists!");

		Object lRes = lApp.callMethod(aObjectId, aMethod, aArgs);
		return lRes;
	}

	/**
	 * Deploys an application
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

		// umcompressing in TEMP folder
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

		// copying application content to apps directory
		File lAppDir = new File(mSettings.getAppsDirectory() + File.separator
				+ lTempAppDirContent[0].getName());
		FileUtils.copyDirectory(lTempAppDirContent[0], lAppDir);
		FileUtils.deleteDirectory(lTempDir);

		// getting the application name
		String lApp = lAppDir.getName();

		// checking security
		if (!hasAuthority(aConnector, NS + ".deploy.*")
				&& !hasAuthority(aConnector, NS + ".deploy." + lApp)) {
			sendToken(aConnector, createAccessDenied(aToken));
			return;
		}

		// loading the script app
		loadApp(lApp, lAppDir.getAbsolutePath(), lHotDeploy);

		// finally send acknowledge response
		sendToken(aConnector, createResponse(aToken));
	}

	/**
	 * Undeploys an application
	 *
	 * @param aConnector
	 * @param aToken
	 * @throws Exception
	 */
	public void undeployAction(WebSocketConnector aConnector, Token aToken) throws Exception {
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

		// notifying event before undeploy
		lScriptApp.notifyEvent(BaseScriptApp.EVENT_UNDEPLOYING, new Object[0]);

		// deleting app
		mApps.remove(lApp);
		FileUtils.deleteDirectory(new File(lScriptApp.getPath()));

		// acknowledge response for the client
		sendToken(aConnector, createResponse(aToken));
	}
}
