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
package org.jwebsocket.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jwebsocket.api.WebSocketInitializer;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.config.xml.JWebSocketConfigHandler;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.security.SecurityFactory;

/**
 * An object that does the process of loading configuration, intialization of
 * the jWebSocket server system.
 * 
 * @author puran
 * @version $Id: JWebSocketLoader.java 345 2010-04-10 20:03:48Z fivefeetfurther$
 */
public final class JWebSocketLoader {

	// We cannot use the logging subsystem here because its config needs to be
	// loaded first!
	private JWebSocketConfigHandler mConfigHandler = new JWebSocketConfigHandler();

	/**
	 * Initialize the jWebSocket Server system
	 * @param aOverrideConfigPath the overridden path to xml config file
	 * @return the initializer object
	 * @throws WebSocketException if there's an exception while initialization
	 */
	public final WebSocketInitializer initialize(String aOverrideConfigPath)
			throws WebSocketException {
		String lConfigPath;
		if (aOverrideConfigPath != null && !"".equals(aOverrideConfigPath)) {
			lConfigPath = aOverrideConfigPath;
			System.out.println("Using config file " + aOverrideConfigPath + "...");
		} else {
			lConfigPath = JWebSocketConfig.getConfigurationPath();
		}
		if (lConfigPath == null) {
			throw new WebSocketException("Either "
					+ JWebSocketServerConstants.JWEBSOCKET_HOME
					+ " variable is not set"
					+ " or jWebSocket.xml file does neither exist at ${"
					+ JWebSocketServerConstants.JWEBSOCKET_HOME
					+ "}/conf nor at ${CLASSPATH}/conf.");
		}
		// load the entire settings from the configuration xml file
		JWebSocketConfig lConfig = loadConfiguration(lConfigPath);

		// initialize security by using config settings
		SecurityFactory.initFromConfig(lConfig);
		WebSocketInitializer lInitializer = JWebSocketXmlConfigInitializer.getInitializer(lConfig);
		return lInitializer;
	}

	/**
	 * Load all the configurations based on jWebSocket.xml file at the given
	 * <tt>configFilePath</tt> location.
	 *
	 * @param aConfigFilePath the path to jWebSocket.xml file
	 * @return the web socket config object with all the configuration
	 * @throws WebSocketException if there's any while loading configuration
	 */
	public JWebSocketConfig loadConfiguration(final String aConfigFilePath) throws WebSocketException {
		JWebSocketConfig lConfig = null;
		File lFile = new File(aConfigFilePath);
		String lMsg;
		try {
			FileInputStream lFIS = new FileInputStream(lFile);
			XMLInputFactory lFactory = XMLInputFactory.newInstance();
			XMLStreamReader lStreamReader = null;
			lStreamReader = lFactory.createXMLStreamReader(lFIS);
			lConfig = mConfigHandler.processConfig(lStreamReader);
		} catch (XMLStreamException ex) {
			lMsg = ex.getClass().getSimpleName() + " occurred while creating XML stream (" + aConfigFilePath + ").";
			throw new WebSocketException(lMsg);
		} catch (FileNotFoundException ex) {
			lMsg = "jWebSocket config file not found while creating XML stream (" + aConfigFilePath + ").";
			throw new WebSocketException(lMsg);
		}
		return lConfig;
	}
}
