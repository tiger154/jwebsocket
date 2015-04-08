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
import org.jwebsocket.api.IInitializable;

/**
 *
 * @author Alexander Schulze
 */
public interface ILog4JAppender extends IInitializable {

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

	/**
	 * Filters the event to check if it can be logged or not, if returns true,
	 * then the event will be logged, otherwise it will not
	 *
	 * @param aLE
	 * @return boolean, if the even can be logged or not
	 */
	public boolean filterEvent(LoggingEvent aLE);

}
