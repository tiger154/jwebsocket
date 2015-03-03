// ---------------------------------------------------------------------------
// jWebSocket - LoggingConfigHandler (Community Edition, CE)
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
 * Handler for the logging configuration
 *
 * @author Alexander Schulze, Puran Singh
 * @version $Id: LoggingConfigHandler.java 616 2010-07-01 08:04:51Z
 * fivefeetfurther $
 */
public class LoggingConfigHandler implements ConfigHandler {

	private static final String ELEMENT_LOG4J = "log4j";
	private static final String RELOAD_DELAY = "autoreload";
	private static final String MAX_LOG_TOKEN_LENGTH = "max_log_token_length";

	/**
	 * {@inheritDoc}
	 *
	 * @param aStreamReader
	 * @throws javax.xml.stream.XMLStreamException
	 */
	@Override
	public Config processConfig(XMLStreamReader aStreamReader)
			throws XMLStreamException {
		Integer lReloadDelay = null, lMaxLogTokenLength = null;
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String elementName = aStreamReader.getLocalName();
				if (elementName.equals(RELOAD_DELAY)) {
					aStreamReader.next();
					lReloadDelay = Integer.parseInt(aStreamReader.getText());
				} else if (elementName.equals(MAX_LOG_TOKEN_LENGTH)) {
					aStreamReader.next();
					lMaxLogTokenLength = Integer.parseInt(aStreamReader.getText());
				} else {
					//ignore
				}
			}
			if (aStreamReader.isEndElement()) {
				String elementName = aStreamReader.getLocalName();
				if (elementName.equals(ELEMENT_LOG4J)) {
					break;
				}
			}
		}
		return new LoggingConfig(lReloadDelay, lMaxLogTokenLength);
	}
}
