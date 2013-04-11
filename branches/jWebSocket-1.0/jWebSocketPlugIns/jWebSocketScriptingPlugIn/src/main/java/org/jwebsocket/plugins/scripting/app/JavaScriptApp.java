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
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 * The object acts as the "app" global object in the JavaScript application
 *
 * @author kyberneees
 */
public class JavaScriptApp {

	private TokenServer mServer;
	private String mAppName;
	private String mAppPath;
	private ScriptEngine mEngine;
	private Map<String, List<Object>> mCallbacks = new FastMap<String, List<Object>>().shared();
	private JavaScriptLogger mLogger;
	private Logger mLog = Logging.getLogger();
	private Map<String, Object> mApi = new FastMap<String, Object>().shared();

	public JavaScriptApp(TokenServer aServer, String aAppName, String aAppPath, ScriptEngine aEngine) {
		mServer = aServer;
		mAppName = aAppName;
		mAppPath = aAppPath;
		mEngine = aEngine;
		mLogger = new JavaScriptLogger(mLog, aAppName);
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

	public void importScript(String aFile) throws Exception {
		String lFile = FileUtils.readFileToString(new File(Tools.expandEnvVarsAndProps(mAppPath) + "/" + aFile));
		mEngine.eval(lFile);
	}

	public void send(WebSocketConnector aConnector, Map aMap) {
		Token lToken = TokenFactory.createToken();
		lToken.setMap(aMap);

		mServer.sendToken(aConnector, lToken);
	}

	public Collection<WebSocketConnector> getConnectors() {
		return mServer.getAllConnectors().values();
	}

	public void on(String aEventName, Object aFn) {
		if (!mCallbacks.containsKey(aEventName)) {
			mCallbacks.put(aEventName, new FastList<Object>());
		}

		mCallbacks.get(aEventName).add(aFn);
	}

	public void un(String aEventName, Object aFn) {
		if (mCallbacks.containsKey(aEventName)) {
			mCallbacks.get(aEventName).remove(aFn);
		}
	}
}
