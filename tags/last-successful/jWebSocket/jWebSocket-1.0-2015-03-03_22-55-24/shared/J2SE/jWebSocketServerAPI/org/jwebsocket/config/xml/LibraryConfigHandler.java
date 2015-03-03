// ---------------------------------------------------------------------------
// jWebSocket - LibraryConfigHandler (Community Edition, CE)
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
 * Handler class that reads the <tt>library</tt> configuration
 *
 * @author Alexander Schulze
 */
public class LibraryConfigHandler implements ConfigHandler {

	private static final String ID = "id";
	private static final String URL = "url";
	private static final String DESCRIPTION = "description";
	private static final String ELEMENT_LIBRARY = "library";

	/**
	 * {@inheritDoc}
	 *
	 * @param aStreamReader
	 * @throws javax.xml.stream.XMLStreamException
	 */
	@Override
	public Config processConfig(XMLStreamReader aStreamReader) throws XMLStreamException {
		String lId = "", lURL = "", lDescription = "";
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String elementName = aStreamReader.getLocalName();
				if (elementName.equals(ID)) {
					aStreamReader.next();
					lId = aStreamReader.getText();
				} else if (elementName.equals(URL)) {
					aStreamReader.next();
					lURL = aStreamReader.getText();
				} else if (elementName.equals(DESCRIPTION)) {
					aStreamReader.next();
					lDescription = aStreamReader.getText();
				} else {
					//ignore
				}
			}
			if (aStreamReader.isEndElement()) {
				String elementName = aStreamReader.getLocalName();
				if (elementName.equals(ELEMENT_LIBRARY)) {
					break;
				}
			}
		}
		return new LibraryConfig(lId, lURL, lDescription);
	}
}
