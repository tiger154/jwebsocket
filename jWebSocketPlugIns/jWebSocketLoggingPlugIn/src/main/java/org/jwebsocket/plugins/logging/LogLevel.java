//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket LogLevel
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
public enum LogLevel {

	ALL, DEBUG, INFO, WARN, ERROR, FATAL;

	public static LogLevel stringToLevel(String aLevel) {
		if (aLevel != null) {
			aLevel = aLevel.toLowerCase();
			if ("debug".equals(aLevel)) {
				return DEBUG;
			} else if ("info".equals(aLevel)) {
				return INFO;
			} else if ("warn".equals(aLevel)) {
				return WARN;
			} else if ("error".equals(aLevel)) {
				return ERROR;
			} else if ("fatal".equals(aLevel)) {
				return FATAL;
			}
		}
		return ALL;
	}
}
