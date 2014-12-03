//	---------------------------------------------------------------------------
//	jWebSocket - Global Exception Handler (Community Edition, CE)
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
package org.jwebsocket.exception;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author Alexander Schulze
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final Logger mLog = Logging.getLogger();

	@Override
	public void uncaughtException(Thread aThread, Throwable aThrowable) {
		handleException(aThread, aThrowable);
	}

	/**
	 *
	 * @param aThread
	 * @param aThrowable
	 */
	public void handleException(Thread aThread, Throwable aThrowable) {
		try {
			StringBuilder lOut = new StringBuilder();
			int lCount = 0, lDuplicates = 0;

			if (aThrowable != null) {
				StackTraceElement[] lStackTrace = aThrowable.getStackTrace();
				String lCurLine, lPrevLine = "";
				StringBuilder lBuilder = new StringBuilder();
				for (StackTraceElement lElem : lStackTrace) {
					lBuilder.setLength(0);
					lBuilder.append(lElem.getClassName())
							.append(", ").append(lElem.getFileName())
							.append(" (").append(lElem.getLineNumber()).append(")")
							.append(": ").append(lElem.getMethodName()).append("\n");
					lCurLine = lBuilder.toString();
					if (lPrevLine.equals(lCurLine)) {
						lDuplicates++;
					} else {
						lOut.append(lCount).append(": ").append(lCurLine);
						lCount++;
						lPrevLine = lCurLine;
					}
				}
			}

			mLog.error("Uncaught exception in thread "
					+ (aThread != null
					&& aThread.getName() != null
					&& aThread.getName().length() > 0
					? "'" + aThread.getName() + "'"
					: "[no name assigned]")
					+ ": "
					+ (aThrowable != null
					? aThrowable.getMessage()
					: "[no exception info available]")
					+ "\n\n--- Stracktrace (" + lCount
					+ " lines, " + lDuplicates + " duplicates): ---\n"
					+ (lOut.length() > 0 ? lOut.toString() : "[empty]\n"));
		} catch (Throwable lThrowable) {
			// don't let the exception get thrown out, will cause infinite looping!
		}
	}

	/**
	 *
	 */
	public static void registerGlobalExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
		System.setProperty("sun.awt.exception.handler", GlobalExceptionHandler.class.getName());
	}
}
