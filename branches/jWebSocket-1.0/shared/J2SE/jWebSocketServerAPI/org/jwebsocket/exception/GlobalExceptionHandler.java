/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
			mLog.error(Logging.getSimpleExceptionMessage(aThrowable, "Uncaught Exception"));
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
