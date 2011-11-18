/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.logging;

/**
 *
 * @author aschulze
 */
public class BaseLogger implements ILogger {

	private LogLevel mLogLevel = null;

	/**
	 *
	 * @param aLogLevel
	 */
	@Override
	public void setLevel(LogLevel aLogLevel) {
		mLogLevel = aLogLevel;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public LogLevel getLevel() {
		return mLogLevel;
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void log(LogLevel aLogLevel, String aInfo, String aMsg) {
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void debug(String aMsg) {
		log(LogLevel.DEBUG, null, aMsg);
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void info(String aMsg) {
		log(LogLevel.INFO, null, aMsg);
	}
	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void warn(String aMsg) {
		log(LogLevel.WARN, null, aMsg);
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void error(String aMsg) {
		log(LogLevel.ERROR, null, aMsg);
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void fatal(String aMsg) {
		log(LogLevel.FATAL, null, aMsg);
	}
}
