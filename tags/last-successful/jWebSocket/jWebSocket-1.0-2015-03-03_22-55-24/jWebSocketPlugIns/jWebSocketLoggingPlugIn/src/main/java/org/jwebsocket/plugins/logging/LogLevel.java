//	---------------------------------------------------------------------------
//	jWebSocket - LogLevel (Community Edition, CE)
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
package org.jwebsocket.plugins.logging;

/**
 *
 * @author Alexander Schulze
 */
public enum LogLevel {

	/**
	 *
	 */
	ALL,
	/**
	 *
	 */
	DEBUG,
	/**
	 *
	 */
	INFO,
	/**
	 *
	 */
	WARN,
	/**
	 *
	 */
	ERROR,
	/**
	 *
	 */
	FATAL;

	/**
	 *
	 * @param aLevel
	 * @return
	 */
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
