//	---------------------------------------------------------------------------
//	jWebSocket - Log4J Appender Support (Community Edition, CE)
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
package org.jwebsocket.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author Alexander Schulze
 */
public class BaseAppender implements ILog4JAppender {

	private Level mLevel = Level.INFO;

	/**
	 *
	 * @param aLevel
	 */
	@Override
	public void setLevel(Level aLevel) {
		mLevel = aLevel;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Level getLevel() {
		return mLevel;
	}

	/**
	 *
	 * @param aLE
	 */
	@Override
	public void append(LoggingEvent aLE) {
		// do nothing on this level
	}

	/**
	 *
	 * @param aMsg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map getInfoMapFromMsg(String aMsg) {
		try {
			if (null == aMsg) {
				return null;
			}
			int lIdx = aMsg.lastIndexOf(", info:");
			if (lIdx < 0) {
				return null;
			}
			String lJSON = aMsg.substring(lIdx + 8);
			Map lMap = new ObjectMapper().readValue(lJSON, HashMap.class);
			lMap.put("message", aMsg.substring(0, lIdx));
			return lMap;
		} catch (Exception ex) {
			return null;
		}
	}

}
