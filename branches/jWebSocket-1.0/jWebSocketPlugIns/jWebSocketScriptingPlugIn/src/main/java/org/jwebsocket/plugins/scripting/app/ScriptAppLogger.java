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

import org.apache.log4j.Logger;

/**
 * The JavaScript application logger
 *
 * @author kyberneees
 */
public class ScriptAppLogger {

	private Logger mLog;
	private String mAppName;

	public ScriptAppLogger(Logger aLog, String aAppName) {
		mLog = aLog;
		mAppName = aAppName;
	}

	public void debug(String aMessage) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("(" + mAppName + ") " + aMessage);
		}
	}

	public void trace(String aMessage) {
		if (mLog.isTraceEnabled()) {
			mLog.trace("(" + mAppName + ") " + aMessage);
		}
	}

	public void error(String aMessage) {
		mLog.error("(" + mAppName + ") " + aMessage);
	}

	public void warn(String aMessage) {
		if (mLog.isDebugEnabled()) {
			mLog.warn("(" + mAppName + ") " + aMessage);
		}
	}
}
