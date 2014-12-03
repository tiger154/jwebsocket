// ---------------------------------------------------------------------------
// jWebSocket - UserConfigHandler (Community Edition, CE)
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
 * The config handler for user config
 *
 * @author Alexander Schulze, Puran Singh
 * @version $Id: UserConfigHandler.java 596 2010-06-22 17:09:54Z fivefeetfurther
 * $
 */
public class UserConfigHandler implements ConfigHandler {

	private final static String UUID = "uuid";
	private final static String LOGIN_NAME = "loginname";
	private final static String FIRST_NAME = "firstname";
	private final static String LAST_NAME = "lastname";
	private final static String PASSWORD = "password";
	private final static String DESCRIPTION = "description";
	private final static String STATUS = "status";
	private final static String ELEMENT_USER = "user";
	private final static String ELEMENT_ROLES = "roles";
	private final static String ELEMENT_ROLE = "role";

	/**
	 * {@inheritDoc}
	 *
	 * @param aStreamReader
	 * @throws javax.xml.stream.XMLStreamException
	 */
	@Override
	public Config processConfig(XMLStreamReader aStreamReader) throws XMLStreamException {
		String lUUID = "", lLoginname = "", lFirstname = "", lLastname = "",
				lPassword = "", lDescription = "";
		int status = 0;
		List<String> roles = null;
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String elementName = aStreamReader.getLocalName();
				if (elementName.equals(LOGIN_NAME)) {
					aStreamReader.next();
					lLoginname = aStreamReader.getText().replace(" ", "");
				} else if (elementName.equals(UUID)) {
					aStreamReader.next();
					lUUID = aStreamReader.getText();
				} else if (elementName.equals(FIRST_NAME)) {
					aStreamReader.next();
					lFirstname = aStreamReader.getText();
				} else if (elementName.equals(LAST_NAME)) {
					aStreamReader.next();
					lLastname = aStreamReader.getText();
				} else if (elementName.equals(PASSWORD)) {
					aStreamReader.next();
					//TODO: temporary fix to handle error because of blank password value
					//better figure out something cleaner.
					if (aStreamReader.getEventType() != 2) {
						lPassword = aStreamReader.getText();
					}
				} else if (elementName.equals(DESCRIPTION)) {
					aStreamReader.next();
					lDescription = aStreamReader.getText();
				} else if (elementName.equals(STATUS)) {
					aStreamReader.next();
					status = Integer.parseInt(aStreamReader.getText());
				} else if (elementName.equals(ELEMENT_ROLES)) {
					aStreamReader.next();
					roles = getRoles(aStreamReader);
				} else {
					//ignore
				}
			}
			if (aStreamReader.isEndElement()) {
				String elementName = aStreamReader.getLocalName();
				if (elementName.equals(ELEMENT_USER)) {
					break;
				}
			}
		}

		return new UserConfig(lUUID, lLoginname, lFirstname, lLastname,
				lPassword, lDescription, status, roles);
	}

	/**
	 * private method that reads the roles
	 *
	 * @param aStreamReader the stream reader object
	 * @return the list of user roles
	 */
	private List<String> getRoles(XMLStreamReader aStreamReader) throws XMLStreamException {
		List<String> lRoles = new FastList<String>();
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_ROLE)) {
					aStreamReader.next();
					String role = aStreamReader.getText().replace(" ", "");
					lRoles.add(role);
				}
			}
			if (aStreamReader.isEndElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_ROLES)) {
					break;
				}
			}
		}
		return lRoles;
	}
}
