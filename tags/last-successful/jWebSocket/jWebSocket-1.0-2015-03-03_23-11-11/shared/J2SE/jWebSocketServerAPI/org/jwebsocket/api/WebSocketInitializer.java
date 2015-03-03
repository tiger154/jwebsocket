//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketInitializer (Community Edition, CE)
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
package org.jwebsocket.api;

import java.util.List;
import java.util.Map;

import org.jwebsocket.config.JWebSocketConfig;

/**
 * Base interface that defines the methods to initialize jWebSocket engine,
 * servers and plugins. The implementation of this class can initialize in
 * different way.
 *
 * {@code JWebSocketXmlConfigInitializer} performs the initialization using
 * 'jWebSocket.xml' configuration file.
 *
 * {@code JWebSocketInitializer} performs the initialization directly using the
 * source classes and packages. This class enables user to write initialization
 * code at compile time thus helping debugging their engine, servers and plugin
 * logic.
 *
 * @author puran
 * @version $Id: WebSocketInitializer.java 596 2010-06-22 17:09:54Z
 * fivefeetfurther $
 *
 */
public interface WebSocketInitializer {

	/**
	 * Initializes the loggins sub system
	 */
	void initializeLogging();

	/**
	 * Initialize the libraries
	 *
	 * @return
	 */
	ClassLoader initializeLibraries();

	/**
	 * Initialize the engine
	 *
	 * @return the initialized engine, which is ready to start
	 */
	Map<String, WebSocketEngine> initializeEngines();

	/**
	 * Initialize the servers, these initialized servers will not have plugins
	 * initialized in plugin chain.
	 *
	 * @return the list of initialized servers ready to start
	 */
	List<WebSocketServer> initializeServers();

	/**
	 * Initialize the plugins specific to server ids.
	 *
	 * @return the FastMap of server id to the list of plugins associated with
	 * it.
	 */
	Map<String, List<WebSocketPlugIn>> initializePlugins();

	/**
	 * Initialize the filters specific to the server ids
	 *
	 * @return the FastMap of server id to the list of filters associated with
	 * it.
	 */
	Map<String, List<WebSocketFilter>> initializeFilters();

	/**
	 * Returns the config object
	 *
	 * @return the jwebsocket config object
	 */
	JWebSocketConfig getConfig();
}
