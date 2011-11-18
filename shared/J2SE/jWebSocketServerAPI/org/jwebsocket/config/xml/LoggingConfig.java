//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
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
package org.jwebsocket.config.xml;

import org.jwebsocket.config.Config;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * Configuration for logging
 * User: puran, aschulze
 *
 * @version $Id: LoggingConfig.java 616 2010-07-01 08:04:51Z fivefeetfurther $
 */
public class LoggingConfig implements Config {

	private final String mAppender;
	private final String mPattern;
	private final String mLevel;
	private final String mFilename;
	private final Integer mBufferSize;
	
	private final String mLog4JConfigFile;
	private final Integer mReloadDelay;

	/**
	 * Constructor
	 *
	 * @param aAppender the logging appender
	 * @param aPattern  logging pattern
	 * @param aLevel    the level of logging
	 * @param aFilename the log file name
	 * @param aBufferSize 
	 * @param aLog4JConfigFile  
	 * @param aReloadDelay
	 */
	public LoggingConfig(String aAppender, String aPattern, String aLevel,
			String aFilename, Integer aBufferSize, String aLog4JConfigFile, 
			Integer aReloadDelay) {
		this.mAppender = aAppender;
		this.mPattern = aPattern;
		this.mLevel = aLevel;
		this.mFilename = aFilename;
		this.mBufferSize = aBufferSize;
		this.mLog4JConfigFile = aLog4JConfigFile;
		this.mReloadDelay = aReloadDelay;
	}

	/**
	 * 
	 * @return
	 */
	public String getAppender() {
		return mAppender;
	}

	/**
	 * 
	 * @return
	 */
	public String getPattern() {
		return mPattern;
	}

	/**
	 * 
	 * @return
	 */
	public String getLevel() {
		return mLevel;
	}

	/**
	 * 
	 * @return
	 */
	public String getFilename() {
		return mFilename;
	}

	/**
	 * 
	 * @return
	 */
	public Integer getBufferSize() {
		return mBufferSize;
	}

	/**
	 * 
	 * @return
	 */
	public String getConfigFile() {
		return mLog4JConfigFile;
	}

	/**
	 * 
	 * @return
	 */
	public Integer getReloadDelay() {
		return mReloadDelay;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() {
		if ((mAppender != null && mAppender.length() > 0)
				&& (mPattern != null && mPattern.length() > 0)
				&& (mLevel != null && mLevel.length() > 0)
				&& (mFilename != null && mFilename.length() > 0)
				&& (mBufferSize != null && mBufferSize >= 0)) {
			return;
		}
		throw new WebSocketRuntimeException(
				"Missing one of the logging configuration directives, "
				+ "please check your configuration file");
	}
}
