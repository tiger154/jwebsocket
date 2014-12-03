// ---------------------------------------------------------------------------
// jWebSocket - ThreadPoolConfigHandler (Community Edition, CE)
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
package org.jwebsocket.config.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.jwebsocket.config.Config;
import org.jwebsocket.config.ConfigHandler;

/**
 * Handler class that reads the server configuration
 *
 * @author puran
 * @version $Id: ServerConfigHandler.java 596 2010-06-22 17:09:54Z
 * fivefeetfurther $
 *
 */
public class ThreadPoolConfigHandler implements ConfigHandler {

	private static final String CORE_POOL_SIZE = "corePoolSize";
	private static final String MAXIMUM_POOL_SIZE = "maximumPoolSize";
	private static final String KEEP_ALIVE_TIME = "keepAliveTime";
	private static final String BLOCKING_QUEUE_SIZE = "blockingQueueSize";
	private static final String ELEMENT_THREAD_POOL = "threadPool";

	/**
	 * {@inheritDoc}
	 * @throws javax.xml.stream.XMLStreamException
	 */
	@Override
	public Config processConfig(XMLStreamReader streamReader) throws XMLStreamException {
		int corePoolSize = Runtime.getRuntime().availableProcessors(), 
				maximumPoolSize = corePoolSize * 2, 
				keepAliveTime = 60, 
				blockingQueueSize = 1000;
		while (streamReader.hasNext()) {
			streamReader.next();
			if (streamReader.isStartElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(CORE_POOL_SIZE)) {
					streamReader.next();
					try {
						corePoolSize = Integer.valueOf(streamReader.getText());
					} catch (NumberFormatException e) {
					}
				} else if (elementName.equals(MAXIMUM_POOL_SIZE)) {
					streamReader.next();
					try {
						maximumPoolSize = Integer.valueOf(streamReader.getText());
					} catch (NumberFormatException e) {
					}
				} else if (elementName.equals(KEEP_ALIVE_TIME)) {
					streamReader.next();
					try {
						keepAliveTime = Integer.valueOf(streamReader.getText());
					} catch (NumberFormatException e) {
					}
				} else if (elementName.equals(BLOCKING_QUEUE_SIZE)) {
					streamReader.next();
					try {
						blockingQueueSize = Integer.valueOf(streamReader.getText());
					} catch (NumberFormatException e) {
					}
				} else {
					//ignore
				}
			}
			if (streamReader.isEndElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_THREAD_POOL)) {
					break;
				}
			}
		}
		return new ThreadPoolConfig(corePoolSize, maximumPoolSize, keepAliveTime, blockingQueueSize);
	}
}
