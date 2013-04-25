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
package org.jwebsocket.plugins.scripting.app.js;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.scripting.ScriptingPlugIn;
import org.jwebsocket.plugins.scripting.app.BaseScriptApp;

/**
 * The object acts as the "app" global object in the JavaScript application
 *
 * @author kyberneees
 */
public class JavaScriptApp extends BaseScriptApp {

	private Logger mLog = Logging.getLogger();

	public JavaScriptApp(ScriptingPlugIn aServer, String aAppName, String aAppPath, ScriptEngine aScriptApp) {
		super(aServer, aAppName, aAppPath, aScriptApp);

		try {
			// setting function caller hook. DO NOT REMOVE!
			getScriptApp().eval(""
					+ "function __fnCallerHook__(aFn, aArgs){"
					+ "var lArgs = new Array();"
					+ "for (var i = 0; i < aArgs.length; i++){"
					+ "lArgs.push(aArgs[i]);"
					+ "}"
					+ "aFn.apply(App, lArgs);"
					+ "};");
		} catch (Exception lEx) {
			// never happens
		}
	}

	@Override
	public void notifyEvent(String aEventName, Object[] aArgs) {
		mLog.debug("Notifying '" + aEventName + "' event in '" + getName() + "' js app...");

		if (getCallbacks().containsKey(aEventName)) {
			for (Object lListener : getCallbacks().get(aEventName)) {
				try {
					// using javascript function caller
					((Invocable) getScriptApp()).invokeFunction("__fnCallerHook__", new Object[]{lListener, aArgs});
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "calling event listener"));
				}
			}
		}
	}
}
