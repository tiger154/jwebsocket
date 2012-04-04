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
 * Configuration for logging User: aschulze
 */
public class LoggingConfig implements Config {

	private final String mLog4JConfigFile;
	private final Integer mReloadDelay;

	/**
	 * Constructor
	 *
	 * @param aLog4JConfigFile
	 * @param aReloadDelay
	 */
	public LoggingConfig(String aLog4JConfigFile, Integer aReloadDelay) {
		mLog4JConfigFile = aLog4JConfigFile;
		mReloadDelay = aReloadDelay;
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
		if (mLog4JConfigFile != null) {
			return;
		}
		throw new WebSocketRuntimeException(
				"Missing one of the logging configuration directives, "
				+ "please check your configuration file");
	}
}
