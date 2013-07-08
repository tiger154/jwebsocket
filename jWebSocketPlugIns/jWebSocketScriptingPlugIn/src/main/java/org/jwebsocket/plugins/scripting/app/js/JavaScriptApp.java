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

import java.io.File;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.scripting.ScriptingPlugIn;
import org.jwebsocket.plugins.scripting.app.BaseScriptApp;
import org.springframework.util.Assert;

/**
 * The class represents the "App" global object in the JavaScript application.
 * Contains specific JavaScript implementation for abstract methods of the
 * BaseScriptApp class.
 *
 * @author kyberneees
 */
public class JavaScriptApp extends BaseScriptApp {

	private Logger mLog = Logging.getLogger();
	private final Object mApp;

	/**
	 * Constructor
	 *
	 * @param aServer The ScriptingPlugIn reference that allows to script
	 * applications to get access to the TokenServer instance.
	 * @param aAppName The application name (unique value)
	 * @param aAppPath The application directory path
	 * @param aScriptApp The scripting engine that runs the application
	 */
	public JavaScriptApp(ScriptingPlugIn aServer, String aAppName, String aAppPath, ScriptEngine aScriptApp) {
		super(aServer, aAppName, aAppPath, aScriptApp);

		try {
			File lAppTemplate = new File(JWebSocketConfig.getConfigFolder("ScriptingPlugIn/js/AppTemplate.js"));
			if (!lAppTemplate.exists()) {
				throw new RuntimeException("The JavaScript application template does not exists in expected location: "
						+ lAppTemplate.getPath() + "!");
			}
			// loading app
			aScriptApp.eval(FileUtils.readFileToString(lAppTemplate));
			mApp = aScriptApp.get("App");
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void notifyEvent(String aEventName, Object[] aArgs) {
		mLog.debug("Notifying '" + aEventName + "' event in '" + getName() + "' js app...");

		try {
			((Invocable) getScriptApp()).invokeMethod(mApp, "notifyEvent", new Object[]{aEventName, aArgs});
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "notifying '" + aEventName + "' event"));
		}
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public Object callMethod(String aObjectId, String aMethod, Object[] aArgs) throws Exception {
		Invocable lInvocable = (Invocable) getScriptApp();
		Boolean lExists = (Boolean) lInvocable.invokeMethod(mApp, "isPublished", new Object[]{aObjectId});
		Assert.isTrue(lExists, "The object with id ' " + aObjectId + "' is not published!");

		Object lObject = lInvocable.invokeMethod(mApp, "getPublished", new Object[]{aObjectId});

		return lInvocable.invokeMethod(lObject, aMethod, aArgs);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public String getVersion() throws Exception {
		Invocable lInvocable = (Invocable) getScriptApp();
		String lVersion = (String) lInvocable.invokeMethod(mApp, "getVersion", new Object[0]);

		return lVersion;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public String getDescription() throws Exception {
		Invocable lInvocable = (Invocable) getScriptApp();
		String lVersion = (String) lInvocable.invokeMethod(mApp, "getDescription", new Object[0]);

		return lVersion;
	}
}
