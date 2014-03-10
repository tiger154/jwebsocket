//	---------------------------------------------------------------------------
//	jWebSocket - Shared Logging Support (Community Edition, CE)
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
package org.jwebsocket.logging;

import java.util.List;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.LoggingConfig;
import org.jwebsocket.token.Token;

/**
 * Provides the common used jWebSocket logging support based on Apache's log4j.
 *
 * @author Alexander Schulze
 */
public class Logging {

	private static boolean mIsStackTraceEnabled = false;
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
	private static int mReloadDelay = 20000;
	private static final List<String> mHiddenTokenFields = new FastList<String>();

	static {
		mHiddenTokenFields.add("password");
	}
	/**
	 *
	 */
	public static int mMaxLogTokenLength = 512;

	/**
	 * Initializes the jWebSocket logging system with the given log level. All
	 * subsequently instantiated class specific loggers will use this setting.
	 *
	 * @param aReloadDelay
	 * @param aMaxLogTokenLength
	 */
	public static void initLogs(Integer aReloadDelay, Integer aMaxLogTokenLength) {
		if (aReloadDelay != null) {
			mReloadDelay = aReloadDelay;
		}
		if (aMaxLogTokenLength != null) {
			mMaxLogTokenLength = aMaxLogTokenLength;
		}
	}

	/**
	 *
	 * @param aLoggingConfig
	 */
	public static void initLogs(LoggingConfig aLoggingConfig) {
		if (aLoggingConfig != null) {
			initLogs(aLoggingConfig.getReloadDelay(), aLoggingConfig.getMaxLogTokenLength());
		}
	}

	/**
	 *
	 * @return
	 */
	public static boolean isStackTraceEnabled() {
		return mIsStackTraceEnabled;
	}

	/**
	 *
	 * @param aEnabled
	 */
	public static void setStackTraceEnabled(boolean aEnabled) {
		mIsStackTraceEnabled = aEnabled;
	}

	/**
	 *
	 * @param aThrowable
	 * @return
	 */
	public static String getStackTraceAsString(Throwable aThrowable) {
		final StringBuilder result = new StringBuilder("");
		for (StackTraceElement lElement : aThrowable.getStackTrace()) {
			result.append(lElement);
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 *
	 * @param aThrowable
	 * @return
	 */
	public static String getExceptionMessage(Throwable aThrowable) {
		return aThrowable.getMessage()
				+ (mIsStackTraceEnabled ? "\n" + getStackTraceAsString(aThrowable) : "");
	}

	/**
	 *
	 * @param aException
	 * @return
	 */
	public static String getExceptionMessage(Exception aException) {
		return aException.getMessage()
				+ (mIsStackTraceEnabled ? "\n" + getStackTraceAsString(aException) : "");
	}

	/**
	 *
	 * @param aException
	 * @param aHint
	 * @return
	 */
	public static String getSimpleExceptionMessage(Exception aException, String aHint) {
		return aException.getClass().getSimpleName()
				+ " on " + aHint + ": "
				+ getExceptionMessage(aException);
	}

	/**
	 *
	 * @param aThrowable
	 * @param aHint
	 * @return
	 */
	public static String getSimpleExceptionMessage(Throwable aThrowable, String aHint) {
		return aThrowable.getClass().getSimpleName()
				+ " on " + aHint + ": "
				+ getExceptionMessage(aThrowable);
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
		return JWebSocketConfig.getLogger();
	}

	/**
	 *
	 * @return
	 */
	public static Logger getLogger() {
		return JWebSocketConfig.getLogger();
	}

	/**
	 *
	 * @param aString
	 * @return
	 */
	public static String getTokenStr(String aString) {
		if (null != aString) {
			if (mMaxLogTokenLength > 0 && aString.length() > mMaxLogTokenLength) {
				aString = aString.substring(0, mMaxLogTokenLength) + "...";
			}
		} else {
			aString = "null";
		}
		return aString;
	}

	/**
	 *
	 * @param aToken
	 * @return
	 */
	public static String getTokenStr(Token aToken) {
		String lToken;
		if (null != aToken) {
			lToken = getTokenStr(aToken.getLogString());
		} else {
			lToken = "null";
		}
		return lToken;
	}
}
