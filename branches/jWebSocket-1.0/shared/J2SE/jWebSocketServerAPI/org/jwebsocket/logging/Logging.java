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
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.xml.DOMConfigurator;
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
	/**
	 * buffer size if write cache for logs is activated (recommended) buffer
	 * size = 0 means no write cache.
	 */
	private static int mBuffersize = 8096; // 8K is log4j default
	private static int mLogTarget = CONSOLE; // ROLLING_FILE;
	private static String mConfigFile = null;
	private static int mReloadDelay = 20000;
	private final static int MIN_RELOAD_DELAY = 5000;
	private static boolean mSettingsLoaded = false;

	private static String getLogsFolderPath(String aFileName) {

		// try to obtain JWEBSOCKET_HOME environment variable
		String lFileSep = System.getProperty("file.separator");
		/*
		 * String lWebSocketHome =
		 * System.getenv(JWebSocketServerConstants.JWEBSOCKET_HOME); String
		 * lFileSep = System.getProperty("file.separator"); String
		 * lWebSocketLogs = null; if (lWebSocketHome != null) { // append
		 * trailing slash if needed if (!lWebSocketHome.endsWith(lFileSep)) {
		 * lWebSocketHome += lFileSep; } // logs are located in
		 * %JWEBSOCKET_HOME%/logs lWebSocketLogs = lWebSocketHome + "logs" +
		 * lFileSep + aFileName; }
		 */

		String lWebSocketLogs = JWebSocketConfig.getJWebSocketHome()
				+ "logs" + lFileSep + aFileName;

		/*
		 * if (lWebSocketLogs == null) { // try to obtain CATALINA_HOME
		 * environment variable String lWebSocketHome =
		 * System.getenv("CATALINA_HOME"); if (lWebSocketHome != null) { //
		 * append trailing slash if needed if
		 * (!lWebSocketHome.endsWith(lFileSep)) { lWebSocketHome += lFileSep; }
		 * // logs are located in %CATALINA_HOME%/logs lWebSocketLogs =
		 * lWebSocketHome + "logs" + lFileSep + aFileName; } }
		 */
		return lWebSocketLogs;
	}

	/**
	 * Initializes the Apache log4j system to produce the desired logging
	 * output.
	 *
	 * @param aLogLevel one of the values TRACE, DEBUG, INFO, WARN, ERROR or
	 * FATAL.
	 *
	 */
	private static void checkLogAppender() {

		if (!mSettingsLoaded
				&& !JWebSocketConfig.isLoadConfigFromResource()) {
			/*
			 * mConfigFile =
			 * "C:/svn/jWebSocketDev/branches/jWebSocket-1.0/jWebSocketAppServer/target/jWebSocketAppServer-1.0/WEB-INF/classes/conf/log4j.xml";
			 */

			String lLog4JConfigFile = JWebSocketConfig.expandEnvAndJWebSocketVars(mConfigFile);

			/*
			 * String lConfigXml = null; try { lConfigXml =
			 * FileUtils.readFileToString(new File(lLog4JConfigFile)); } catch
			 * (IOException ex) { System.out.println("Logs: " + lLog4JConfigFile
			 * + "\nEXCEPTION!"); }
			 * System.out.println("=============================");
			 * System.out.println("Logs: " + lLog4JConfigFile + "\n" +
			 * lLog4JConfigFile);
			 * System.out.println("=============================");
			 */
			if (mReloadDelay >= MIN_RELOAD_DELAY) {
				DOMConfigurator.configureAndWatch(lLog4JConfigFile, mReloadDelay);
			} else {
				DOMConfigurator.configure(lLog4JConfigFile);
			}
			// }	
			mSettingsLoaded = true;
		}
	}

	/**
	 * Initializes the jWebSocket logging system with the given log level. All
	 * subsequently instantiated class specific loggers will use this setting.
	 *
	 * @param aLogLevel
	 */
	public static void initLogs(String aConfigFile, Integer aReloadDelay) {
		if (aConfigFile != null) {
			mConfigFile = aConfigFile;
		}
		if (aReloadDelay != null) {
			mReloadDelay = aReloadDelay;
		}
		checkLogAppender();
	}

	public static void initLogs(LoggingConfig aLoggingConfig) {
		if (aLoggingConfig != null) {
			initLogs(
					aLoggingConfig.getConfigFile(),
					aLoggingConfig.getReloadDelay());
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
		checkLogAppender();

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

	public static Logger addLogger(Class aClass) {
		Logger lLogger = getLogger(aClass);
		mLoggers.put(aClass, lLogger);
		return lLogger;
	}
}
