//	---------------------------------------------------------------------------
//	jWebSocket - Log4JLogger (Community Edition, CE)
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
package org.jwebsocket.plugins.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.jwebsocket.logging.Logging;

/**
 * Logger Implementation for Log4J
 *
 * @author Alexander Schulze
 */
public class Log4JLogger extends BaseLogger implements ILogger {

	private static final Logger mLog = Logging.getLogger(Log4JLogger.class);

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void log(LogLevel aLogLevel, String aInfo, String aMsg) {
		Priority lPrio = Priority.DEBUG;
		switch (aLogLevel) {
			case INFO: {
				lPrio = Priority.INFO;
				break;
			}
			case WARN: {
				lPrio = Priority.WARN;
				break;
			}
			case ERROR: {
				lPrio = Priority.ERROR;
				break;
			}
			case FATAL: {
				lPrio = Priority.FATAL;
				break;
			}
		}
		mLog.log(lPrio, (aInfo != null ? "[" + aInfo + "] " : "") + aMsg);
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void debug(String aMsg) {
		mLog.debug(aMsg);
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void info(String aMsg) {
		mLog.info(aMsg);
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void warn(String aMsg) {
		mLog.warn(aMsg);
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void error(String aMsg) {
		mLog.error(aMsg);
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void fatal(String aMsg) {
		mLog.fatal(aMsg);
	}
}
