//	---------------------------------------------------------------------------
//	jWebSocket - JavaScriptApp for Scripting Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.scripting.app;

import org.apache.log4j.Logger;

/**
 * The JavaScript application logger abstraction class.
 *
 * @author Rolando Santamaria Maso
 */
public class ScriptAppLogger {

	private Logger mLog;
	private String mAppName;

	/**
	 * Constructor
	 *
	 * @param aLog The logger instance
	 * @param aAppName The unique application name
	 */
	public ScriptAppLogger(Logger aLog, String aAppName) {
		mLog = aLog;
		mAppName = aAppName;
	}

	/**
	 * Append a debug message to the log target
	 *
	 * @param aMessage
	 */
	public void debug(String aMessage) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("(" + mAppName + ") " + aMessage);
		}
	}

	/**
	 * Append a message to the log target
	 *
	 * @param aMessage
	 */
	public void trace(String aMessage) {
		if (mLog.isTraceEnabled()) {
			mLog.trace("(" + mAppName + ") " + aMessage);
		}
	}

	/**
	 * Append an error message to the log target
	 *
	 * @param aMessage
	 */
	public void error(String aMessage) {
		mLog.error("(" + mAppName + ") " + aMessage);
	}

	/**
	 * Append a warning message to the log target
	 *
	 * @param aMessage
	 */
	public void warn(String aMessage) {
		if (mLog.isDebugEnabled()) {
			mLog.warn("(" + mAppName + ") " + aMessage);
		}
	}
}
