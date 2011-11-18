//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.config.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.jwebsocket.config.Config;
import org.jwebsocket.config.ConfigHandler;

/**
 * Handler for the logging configuration
 *
 * @author puran, aschulze
 * @version $Id: LoggingConfigHandler.java 616 2010-07-01 08:04:51Z fivefeetfurther $
 */
public class LoggingConfigHandler implements ConfigHandler {

	private static final String APPENDER = "appender";
	private static final String PATTERN = "pattern";
	private static final String LEVEL = "level";
	private static final String FILENAME = "filename";
	private static final String BUFFERSIZE = "buffersize";
	private static final String ELEMENT_LOG4J = "log4j";
	private static final String CONFIG_FILE = "config";
	private static final String RELOAD_DELAY = "reload";

	/**
	 * {@inheritDoc}
	 * 
	 * @param aStreamReader 
	 */
	@Override
	public Config processConfig(XMLStreamReader aStreamReader)
			throws XMLStreamException {
		String lAppender = "", lPattern = "", lLevel = "", lFilename = "", lConfigFile = null;
		Integer lBufferSize = 2048, lReloadDelay = 20000;
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String elementName = aStreamReader.getLocalName();
				if (elementName.equals(APPENDER)) {
					aStreamReader.next();
					lAppender = aStreamReader.getText();
				} else if (elementName.equals(PATTERN)) {
					aStreamReader.next();
					lPattern = aStreamReader.getText();
				} else if (elementName.equals(LEVEL)) {
					aStreamReader.next();
					lLevel = aStreamReader.getText();
				} else if (elementName.equals(FILENAME)) {
					aStreamReader.next();
					lFilename = aStreamReader.getText();
				} else if (elementName.equals(CONFIG_FILE)) {
					aStreamReader.next();
					lConfigFile = aStreamReader.getText();
				} else if (elementName.equals(BUFFERSIZE)) {
					aStreamReader.next();
					lBufferSize = Integer.parseInt(aStreamReader.getText());
				} else if (elementName.equals(RELOAD_DELAY)) {
					aStreamReader.next();
					lReloadDelay = Integer.parseInt(aStreamReader.getText());
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
		return new LoggingConfig(lAppender, lPattern, lLevel, lFilename,
				lBufferSize, lConfigFile, lReloadDelay);
	}
}
