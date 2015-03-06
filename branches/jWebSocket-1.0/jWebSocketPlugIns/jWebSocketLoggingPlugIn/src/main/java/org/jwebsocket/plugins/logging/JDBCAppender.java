//	---------------------------------------------------------------------------
//	jWebSocket - JDBC Appender (Community Edition, CE)
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

import java.util.Map;
import org.apache.log4j.spi.LoggingEvent;
import org.jwebsocket.logging.BaseAppender;

/**
 *
 * @author Alexander Schulze
 */
public class JDBCAppender extends BaseAppender {

	@Override
	public void append(LoggingEvent aLE) {
		Object lMsg = aLE.getMessage();
		Map lInfo = null;
		if (null != lMsg) {
			lInfo = getInfoMapFromMsg((String) lMsg);
		}
		if (null != lInfo) {
			lMsg = lInfo.get("message");
			lInfo.remove("message");
		}
		System.out.println("[JDBC Appender]: "
				+ aLE.getLevel().toString() + ": "
				+ lMsg
				+ (lInfo != null
						? ", info: " + lInfo
						: "")
		);
	}

}
