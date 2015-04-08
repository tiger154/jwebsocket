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

import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author Alexander Schulze
 */
public abstract class BaseAppender implements ILog4JAppender {

	private Level mLevel = Level.INFO;
	protected List<LoggingEventFieldFilter> mFieldFilterList;

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
	 * Obtains the configured Filter List for a certain appender
	 * @return The filters
	 */
	public List<LoggingEventFieldFilter> getFieldFilterList() {
		return mFieldFilterList;
	}
	
	/**
	 * Every jWebSocket Appender might be configured with a list of filters to 
	 * avoid logging certain events if they match a configured black and white 
	 * list, for more information, please see the class LoggingEventFieldFilter
	 * 
	 * @param aFieldFilterList 
	 */
	public void setFieldFilterList(List<LoggingEventFieldFilter> aFieldFilterList) {
		this.mFieldFilterList = aFieldFilterList;
	}

	/**
	 *
	 * @param aLE
	 */
	@Override
	public void append(LoggingEvent aLE) {
		// do nothing on this level
	}

	@Override
	public void initialize() throws Exception {
		// do nothing on this level
	}

	@Override
	public void shutdown() throws Exception {
		// do nothing on this level
	}

}
