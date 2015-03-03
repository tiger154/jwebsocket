// ---------------------------------------------------------------------------
// jWebSocket - ServerConfigHandler (Community Edition, CE)
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

import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javolution.util.FastMap;
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
public class ServerConfigHandler implements ConfigHandler {

	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String JAR = "jar";
	private static final String ELEMENT_THREAD_POOL = "threadPool";
	private static final String ELEMENT_SERVER = "server";

	/**
	 * {@inheritDoc}
	 *
	 * @param aStreamReader
	 * @throws javax.xml.stream.XMLStreamException
	 */
	@Override
	public Config processConfig(XMLStreamReader aStreamReader) throws XMLStreamException {
		String lId = "", lName = "", lJar = "";
		Map<String, Object> lSettings = new FastMap();

		ThreadPoolConfig lThreadPoolConfig = null;
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String elementName = aStreamReader.getLocalName();
				if (elementName.equals(ID)) {
					aStreamReader.next();
					lId = aStreamReader.getText();
				} else if (elementName.equals(NAME)) {
					aStreamReader.next();
					lName = aStreamReader.getText();
				} else if (elementName.equals(JAR)) {
					aStreamReader.next();
					lJar = aStreamReader.getText();
				} else if (elementName.equals(JWebSocketConfigHandler.SETTINGS)) {
					lSettings = JWebSocketConfigHandler.getSettings(aStreamReader);
				} else if (elementName.equals(ELEMENT_THREAD_POOL)) {
					aStreamReader.next();
					lThreadPoolConfig = (ThreadPoolConfig) new ThreadPoolConfigHandler().processConfig(aStreamReader);
				} else {
					//ignore
				}
			}
			if (aStreamReader.isEndElement()) {
				String elementName = aStreamReader.getLocalName();
				if (elementName.equals(ELEMENT_SERVER)) {
					break;
				}
			}
		}
		return new ServerConfig(lId, lName, lJar, lThreadPoolConfig, lSettings);
	}
}
