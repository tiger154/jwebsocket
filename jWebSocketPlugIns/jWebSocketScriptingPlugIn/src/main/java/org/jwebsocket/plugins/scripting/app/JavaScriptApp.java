//	---------------------------------------------------------------------------
//	jWebSocket - JavaScriptApp for Scripting Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.scripting.app;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.script.ScriptEngine;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.scripting.ScriptingPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;
import sun.org.mozilla.javascript.Context;
import sun.org.mozilla.javascript.Function;
import sun.org.mozilla.javascript.ScriptableObject;

/**
 * The object acts as the "app" global object in the JavaScript application
 *
 * @author kyberneees
 */
public class JavaScriptApp {

	private ScriptingPlugIn mServer;
	private String mAppName;
	private String mAppPath;
	private ScriptEngine mScriptApp;
	private Map<String, List<Function>> mCallbacks = new FastMap<String, List<Function>>().shared();
	private JavaScriptLogger mLogger;
	private Logger mLog = Logging.getLogger();
	private Map<String, Object> mApi = new FastMap<String, Object>().shared();
	public final static String EVENT_CONNECTOR_STARTED = "connectorStarted";
	public final static String EVENT_CONNECTOR_STOPPED = "connectorStopped";
	public final static String EVENT_ENGINE_STARTED = "engineStarted";
	public final static String EVENT_ENGINE_STOPPED = "engineStopped";
	public final static String EVENT_SESSION_STARTED = "sessionStarted";
	public final static String EVENT_SESSION_STOPPED = "sessionStopped";
	public final static String EVENT_LOGON = "logon";
	public final static String EVENT_LOGOFF = "logoff";
	public final static String EVENT_TOKEN = "token";
	public final static String EVENT_FILTER_IN = "filterIn";
	public final static String EVENT_FILTER_OUT = "filterOut";

	public JavaScriptApp(ScriptingPlugIn aServer, String aAppName, String aAppPath, ScriptEngine aScriptApp) {
		mServer = aServer;
		mAppName = aAppName;
		mAppPath = aAppPath;
		mScriptApp = aScriptApp;
		mLogger = new JavaScriptLogger(mLog, aAppName);
	}

	public void notifyEvent(String aEventName, Object[] aArgs) {
		mLog.debug("Notifying '" + aEventName + "' event in '" + mAppName + "' js app...");

		Context lContext = Context.enter();
		ScriptableObject lScope = lContext.initStandardObjects();

		if (mCallbacks.containsKey(aEventName)) {
			for (Function lFn : mCallbacks.get(aEventName)) {
				lFn.call(lContext, lScope, lScope, aArgs);
			}
		}
	}

	public String getName() {
		return mAppName;
	}

	public void publish(String aObjectId, Object aObject) {
		mApi.put(aObjectId, aObject);
	}

	public void unpublish(String aObjectId) {
		mApi.remove(aObjectId);
	}

	public boolean isPublished(String aObjectId) {
		return mApi.containsKey(aObjectId);
	}

	public Object getPublished(String aObjectId) {
		return mApi.get(aObjectId);
	}

	public String getPath() {
		return mAppPath;
	}

	public JavaScriptLogger getLogger() {
		return mLogger;
	}

	public void assertTrue(Boolean aBoolean, String aMessage) {
		Assert.isTrue(aBoolean, aMessage);
	}

	public void assertNotNull(Object aObject, String aMessage) {
		Assert.notNull(aObject, aMessage);
	}

	public void importScript(String aFile) throws Exception {
		aFile = aFile.replace("${APP_HOME}", mAppPath);
		String lFile = FileUtils.readFileToString(new File(Tools.expandEnvVarsAndProps(aFile)));
		mScriptApp.eval(lFile);
	}

	public void sendToken(WebSocketConnector aConnector, Map aMap) {
		sendTokenFragmented(aConnector, aMap, null);
	}

	public void sendTokenFragmented(WebSocketConnector aConnector, Map aMap, Integer aFragmentSize) {
		notifyEvent(JavaScriptApp.EVENT_FILTER_OUT, new Object[]{aConnector, aMap});
		if (!aMap.containsKey("reqType")) {
			aMap.put("ns", ScriptingPlugIn.NS);
			aMap.put("app", mAppName);
		}

		Token lToken = TokenFactory.createToken();
		lToken.setMap(aMap);

		if (null == aFragmentSize) {
			mServer.sendTokenFragmented(aConnector, lToken, aFragmentSize);
		} else {
			mServer.sendToken(aConnector, lToken);
		}
	}

	public Collection<WebSocketConnector> getAllConnectors() {
		return mServer.getServer().getAllConnectors().values();
	}

	public boolean hasAuthority(WebSocketConnector aConnector, String aAuthority) {
		return mServer.hasAuthority(aConnector, aAuthority);
	}

	public void requireAuthority(WebSocketConnector aConnector, String aAuthority) throws Exception {
		if (!mServer.hasAuthority(aConnector, aAuthority)) {
			throw new Exception("Not authorized. Missing required '" + aAuthority + "' authority!");
		}
	}

	public void on(Collection<String> aEvents, Function aFn) {
		for (String lEventName : aEvents) {
			on(lEventName, aFn);
		}
	}

	public void on(String aEventName, Function aFn) {
		if (!mCallbacks.containsKey(aEventName)) {
			mCallbacks.put(aEventName, new FastList<Function>());
		}
		mCallbacks.get(aEventName).add(aFn);
	}

	public void un(String aEventName, Function aFn) {
		if (mCallbacks.containsKey(aEventName)) {
			mCallbacks.get(aEventName).remove(aFn);
		}
	}

	Token mapToToken(Map aMap) {
		Token lToken = TokenFactory.createToken();
		lToken.setMap(aMap);

		return lToken;
	}

	public Map createResponse(Map aInToken) {
		return mServer.createResponse(mapToToken(aInToken)).getMap();
	}

	public void broadcast(Collection<WebSocketConnector> aConnectors, Map aToken) {
		for (WebSocketConnector aConnector : aConnectors) {
			sendToken(aConnector, aToken);
		}
	}

	public Map newThreadSafeMap() {
		return new FastMap().shared();
	}

	public Collection newThreadSafeCollection() {
		return new FastList().shared();
	}
}
