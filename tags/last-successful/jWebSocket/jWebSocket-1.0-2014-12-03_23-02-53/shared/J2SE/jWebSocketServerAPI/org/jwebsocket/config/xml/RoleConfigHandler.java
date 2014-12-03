// ---------------------------------------------------------------------------
// jWebSocket - RoleConfigHandler (Community Edition, CE)
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

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javolution.util.FastList;
import org.jwebsocket.config.Config;
import org.jwebsocket.config.ConfigHandler;

/**
 * Handler class to read roles
 *
 * @author puran
 * @version $Id: RoleConfigHandler.java 596 2010-06-22 17:09:54Z fivefeetfurther
 * $
 *
 */
public class RoleConfigHandler implements ConfigHandler {

	private static final String ID = "id";
	private static final String DESCRIPTION = "description";
	private static final String ELEMENT_RIGHT = "right";
	private static final String ELEMENT_RIGHTS = "rights";
	private static final String ELEMENT_ROLE = "role";

	/**
	 * {@inheritDoc}
	 * @throws javax.xml.stream.XMLStreamException
	 */
	@Override
	public Config processConfig(XMLStreamReader streamReader)
			throws XMLStreamException {
		String id = "", description = "";
		List<String> rights = new FastList<String>();
		while (streamReader.hasNext()) {
			streamReader.next();
			if (streamReader.isStartElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ID)) {
					streamReader.next();
					id = streamReader.getText().replace(" ", "");
				} else if (elementName.equals(DESCRIPTION)) {
					streamReader.next();
					description = streamReader.getText();
				} else if (elementName.equals(ELEMENT_RIGHTS)) {
					streamReader.next();
					rights = getRights(streamReader);
				} else {
					// ignore
				}
			}
			if (streamReader.isEndElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_ROLE)) {
					break;
				}
			}
		}
		return new RoleConfig(id, description, rights);
	}

	/**
	 * private method that reads the list of rights from the role configuration
	 *
	 * @param streamReader the stream reader object
	 * @return the list of right ids
	 * @throws XMLStreamException if exception while reading
	 */
	private List<String> getRights(XMLStreamReader streamReader)
			throws XMLStreamException {
		List<String> rights = new FastList<String>();
		while (streamReader.hasNext()) {
			streamReader.next();
			if (streamReader.isStartElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_RIGHT)) {
					streamReader.next();
					String right = streamReader.getText().replace(" ", "");
					rights.add(right);
				}
			}
			if (streamReader.isEndElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_RIGHTS)) {
					break;
				}
			}
		}
		return rights;
	}
}
