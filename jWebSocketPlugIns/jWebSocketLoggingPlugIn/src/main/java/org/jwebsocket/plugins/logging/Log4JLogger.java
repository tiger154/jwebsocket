//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Log4JLogger implementation for Logging Plug-In
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.jwebsocket.logging.Logging;

/**
 * Logger Implementation for Log4J
 * @author aschulze
 */
public class Log4JLogger extends BaseLogger implements ILogger {

	private static Logger mLog = Logging.getLogger(Log4JLogger.class);

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
