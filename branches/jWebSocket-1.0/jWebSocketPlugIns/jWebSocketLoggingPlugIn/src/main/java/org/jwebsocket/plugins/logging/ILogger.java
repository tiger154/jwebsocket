//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket ILogger Interface
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

/**
 *
 * @author aschulze
 */
public interface ILogger {

	/**
	 *
	 * @param aLogLevel
	 */
	void setLevel(LogLevel aLogLevel);

	/**
	 *
	 * @return
	 */
	LogLevel getLevel();

	/**
	 *
	 * @param aLogLevel 
	 * @param aMsg
	 * @param aInfo
	 */
	void log(LogLevel aLogLevel, String aInfo, String aMsg);

	/**
	 *
	 * @param aMsg
	 */
	void debug(String aMsg);

	/**
	 *
	 * @param aMsg
	 */
	void info(String aMsg);

	/**
	 *
	 * @param aMsg
	 */
	void warn(String aMsg);

	/**
	 *
	 * @param aMsg
	 */
	void error(String aMsg);

	/**
	 *
	 * @param aMsg
	 */
	void fatal(String aMsg);
}
