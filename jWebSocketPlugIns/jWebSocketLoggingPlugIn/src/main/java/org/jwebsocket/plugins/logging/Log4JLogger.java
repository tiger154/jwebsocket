//	---------------------------------------------------------------------------
//	jWebSocket - Log4JLogger (Community Edition, CE)
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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * Logger Implementation for Log4J
 *
 * @author Alexander Schulze
 */
public class Log4JLogger extends BaseLogger implements ILogger {

	private static final Logger mLog = Logger.getLogger(Log4JLogger.class);

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void log(LogLevel aLogLevel, String aMsg, Map aInfo) {

		String lInfo = null;
		if (null != aInfo) {
			ObjectMapper lObjMap = new ObjectMapper();
			try {
				ByteArrayOutputStream lBAOS = new ByteArrayOutputStream();
				lObjMap.writeValue(lBAOS, aInfo);
				lInfo = new String(lBAOS.toByteArray(), "UTF-8");
			} catch (IOException ex) {
				aInfo = null;
			}
		}

		// don't change "info: " here, this is required to extract the info object again from the string!
		String lMessage = aMsg + (aInfo != null ? ", info: " + lInfo : "");
		switch (aLogLevel) {
			case INFO: {
				mLog.info(lMessage);
				break;
			}
			case WARN: {
				mLog.warn(lMessage);
				break;
			}
			case ERROR: {
				mLog.error(lMessage);
				break;
			}
			case FATAL: {
				mLog.fatal(lMessage);
				break;
			}
			default: {
				mLog.debug(lMessage);
			}
		}
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void debug(String aMsg) {
		mLog.debug(aMsg);
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void info(String aMsg) {
		mLog.info(aMsg);
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void warn(String aMsg) {
		mLog.warn(aMsg);
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void error(String aMsg) {
		mLog.error(aMsg);
	}

	/**
	 *
	 * @param aMsg
	 */
	@Override
	public void fatal(String aMsg) {
		mLog.fatal(aMsg);
	}
}
