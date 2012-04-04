//	---------------------------------------------------------------------------
//	jWebSocket - Shared Logging Support
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.logging;

import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.LoggingConfig;

/**
 * Provides the common used jWebSocket logging support based on Apache's log4j.
 *
 * @author Alexander Schulze (aschulze, Innotrade GmbH, jWebSocket.org)
 */
public class Logging {

	private static boolean mIsStackTraceEnabled = false;
	private static FastMap<Object, Logger> mLoggers = new FastMap<Object, Logger>();
	/**
	 * Log output is send to the console (stdout).
	 */
	public final static int CONSOLE = 0;
	/**
	 * Log output is send to a rolling file.
	 */
	public final static int ROLLING_FILE = 1;
	/**
	 * Log output is send to a single file.
	 */
	public final static int SINGLE_FILE = 2;
	/**
	 * Name of jWebSocket log file.
	 */
	private static String mFilename = "jWebSocket.log";
	/**
	 * Pattern for jWebSocket log file.
	 */
	private static String mPattern = "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p - %C{1}: %m%n";
	private static int mReloadDelay = 20000;

	private static String getLogsFolderPath(String aFileName) {
		// try to obtain JWEBSOCKET_HOME environment variable
		String lFileSep = System.getProperty("file.separator");
		String lWebSocketLogs = JWebSocketConfig.getJWebSocketHome()
				+ "logs" + lFileSep + aFileName;
		return lWebSocketLogs;
	}

	/**
	 * Initializes the jWebSocket logging system with the given log level. All
	 * subsequently instantiated class specific loggers will use this setting.
	 *
	 * @param aLogLevel
	 */
	public static void initLogs(Integer aReloadDelay) {
		if (aReloadDelay != null) {
			mReloadDelay = aReloadDelay;
		}
	}

	public static void initLogs(LoggingConfig aLoggingConfig) {
		if (aLoggingConfig != null) {
			initLogs(aLoggingConfig.getReloadDelay());
		}
	}

	public static boolean isStackTraceEnabled() {
		return mIsStackTraceEnabled;
	}

	public static void setStackTraceEnabled(boolean aEnabled) {
		mIsStackTraceEnabled = aEnabled;
	}

	public static String getStackTraceAsString(Throwable aThrowable) {
		final StringBuilder result = new StringBuilder("");
		for (StackTraceElement lElement : aThrowable.getStackTrace()) {
			result.append(lElement);
			result.append("\n");
		}
		return result.toString();
	}

	public static String getExceptionMessage(Exception aException) {
		return aException.getMessage()
				+ (mIsStackTraceEnabled ? "\n" + getStackTraceAsString(aException) : "");
	}

	public static String getSimpleExceptionMessage(Exception aException, String aHint) {
		return aException.getClass().getSimpleName()
				+ " on " + aHint + ": "
				+ getExceptionMessage(aException);
	}

	/**
	 * closes the log file. Take care that no further lines are appended to the
	 * logs after it has been closed!
	 */
	public static void exitLogs() {
	}

	/**
	 * Returns a logger for a certain class by using the jWebSocket settings for
	 * logging and ignoring inherited log4j settings.
	 *
	 * @param aClass
	 * @return Logger the new logger for the given class.
	 */
	public static Logger getLogger(Class aClass) {
		// if a logger for a certain class is already created use it
		Logger lLogger = (null != mLoggers ? mLoggers.get(aClass) : null);
		// if there is no cached one, create a new one
		if (null == lLogger) {
			lLogger = Logger.getLogger(aClass);
		}
		// otherwise the logger should be initialized properly already
		// by the configuration file
		return lLogger;
	}

	public static Logger getLogger(String aClassName) {
		// if a logger for a certain class is already created use it
		Logger lLogger = (null != mLoggers ? mLoggers.get(aClassName) : null);
		// if there is no cached one, create a new one
		if (null == lLogger) {
			lLogger = Logger.getLogger(aClassName);
		}

		// otherwise the logger should be initialized properly already
		// by the configuration file
		return lLogger;
	}

	public static Logger getRootLogger() {
		return Logger.getRootLogger();
	}

	public static Logger addLogger(Class aClass) {
		Logger lLogger = getLogger(aClass);
		mLoggers.put(aClass, lLogger);
		return lLogger;
	}
}
