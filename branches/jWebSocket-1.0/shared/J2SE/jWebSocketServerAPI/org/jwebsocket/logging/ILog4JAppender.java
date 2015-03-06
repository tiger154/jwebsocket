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

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author Alexander Schulze
 */
public interface ILog4JAppender {

	/**
	 *
	 * @param aLevel
	 */
	public void setLevel(Level aLevel);

	/**
	 *
	 * @return
	 */
	public Level getLevel();

	/**
	 *
	 * @param aLE
	 */
	public void append(LoggingEvent aLE);

}
