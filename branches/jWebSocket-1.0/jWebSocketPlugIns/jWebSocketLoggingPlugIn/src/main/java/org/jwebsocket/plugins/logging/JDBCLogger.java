//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket JDBCLogger Plug-In
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
 * Logger Implementation for JDBC Databases
 * @author aschulze
 */
public class JDBCLogger extends BaseLogger implements ILogger {

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void debug(String aMsg) {
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
	public void info(String aMsg) {
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void error(String aMsg) {
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void warn(String aMsg) {
	}
}
