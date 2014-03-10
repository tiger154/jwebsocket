//	---------------------------------------------------------------------------
//	jWebSocket - ILogger (Community Edition, CE)
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
