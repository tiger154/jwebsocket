//	---------------------------------------------------------------------------
//	jWebSocket - Global Exception Handler (Community Edition, CE)
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
package org.jwebsocket.exception;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author aschulze
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final Logger mLog = Logging.getLogger();

	@Override
	public void uncaughtException(Thread aThread, Throwable aThrowable) {
		handleException(aThrowable);
	}

	/**
	 *
	 * @param aThrowable
	 */
	public void handleException(Throwable aThrowable) {
		try {
			mLog.error("Uncaught Exception: " + aThrowable.getMessage());
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
