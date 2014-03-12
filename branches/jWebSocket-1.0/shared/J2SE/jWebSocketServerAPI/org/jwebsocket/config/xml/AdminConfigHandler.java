// ---------------------------------------------------------------------------
// jWebSocket - AdminConfigHandler (Community Edition, CE)
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
import org.jwebsocket.config.AdminConfig;
import org.jwebsocket.config.ConfigHandler;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 *
 * @author Marcos Antonio Gonzalez Huerta
 */
public class AdminConfigHandler extends JWebSocketConfigHandler implements ConfigHandler {

	@Override
	public AdminConfig processConfig(XMLStreamReader aStreamReader) {
		AdminConfig lConfig = new AdminConfig();

		try {
			while (aStreamReader.hasNext()) {
				aStreamReader.next();
				if (aStreamReader.isStartElement()) {
					String lElementName = aStreamReader.getLocalName();
					if (lElementName.equals(ELEMENT_PLUGINS)) {
						List<PluginConfig> lPlugins = handlePlugins(aStreamReader);
						lConfig.setPlugins(lPlugins);
					} else if (lElementName.equals(ELEMENT_FILTERS)) {
						List<FilterConfig> lFilters = handleFilters(aStreamReader);
						lConfig.setFilters(lFilters);
					} else {
						// ignore
					}
				}
				if (aStreamReader.isEndElement()) {
					String lElementName = aStreamReader.getLocalName();
					if (lElementName.equals(JWEBSOCKET)) {
						break;
					}
				}
			}
		} catch (XMLStreamException lEx) {
			throw new WebSocketRuntimeException("Error parsing jwsMgmtDesk.xml configuration file", lEx);
		}

		return lConfig;
	}
}
