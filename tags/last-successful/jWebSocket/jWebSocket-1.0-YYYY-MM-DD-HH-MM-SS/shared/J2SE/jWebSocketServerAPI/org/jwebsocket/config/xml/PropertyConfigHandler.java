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
 * Handler class that reads the properties configuration
 *
 * @author Rolando Santamaria Maso
 */
public class PropertyConfigHandler implements ConfigHandler {

	private static final String NAME = "name";
	private static final String VALUE = "value";

	/**
	 * {@inheritDoc}
	 * @throws javax.xml.stream.XMLStreamException
	 */
	@Override
	public Config processConfig(XMLStreamReader streamReader) throws XMLStreamException {
		String lName = null, lValue = null;
		while (streamReader.hasNext()) {
			if (streamReader.isStartElement()) {
				for (int lIndex = 0, lEnd = streamReader.getAttributeCount(); lIndex < lEnd; ++lIndex) {
					if (streamReader.getAttributeName(lIndex).toString().equals(NAME)) {
						lName = streamReader.getAttributeValue(lIndex);
					} else if (streamReader.getAttributeName(lIndex).toString().equals(VALUE)) {
						lValue = streamReader.getAttributeValue(lIndex);
					}
				}
			}
			if (streamReader.isEndElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(JWebSocketConfigHandler.ELEMENT_PROPERTY)) {
					break;
				}
			}
			streamReader.next();
		}
		return new PropertyConfig(lName, lValue);
	}
}
