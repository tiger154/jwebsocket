//	---------------------------------------------------------------------------
//	jWebSocket - EngineConfiguration (Community Edition, CE)
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

/**
 * Base interface that provides the read-only access to all the engine
 * configuration values configured via <tt>jWebSocket.xml</tt> file for a given
 * engine
 *
 * @author puran
 * @version $Id: EngineConfiguration.java 615 2010-07-01 07:49:54Z
 * mailtopuran@gmail.com $
 */
public interface EngineConfiguration extends Configuration {

	/**
	 * Returns the fully qualified name of the external jar file from which the
	 * engine is loaded. In case, no external library or jar file is used then
	 * this value will return null or empty string.
	 *
	 * @return the jar file name or null value
	 */
	String getJar();

	/**
	 * Returns the context for servlet based engines like Jetty
	 *
	 * @return the context for servlet based engines, null for native servers
	 */
	String getContext();

	/**
	 * Returns the servlet for servlet based engines like Jetty
	 *
	 * @return the servlet for servlet based engines, null for native servers
	 */
	String getServlet();

	/**
	 * Returns the name of the key store file
	 *
	 * @return the name of the key store file (null or empty for non-ssl
	 * engines)
	 */
	String getKeyStore();

	/**
	 * Returns the password of the key store file
	 *
	 * @return the password of the key store file (null or empty for non-ssl
	 * engines)
	 */
	String getKeyStorePassword();

	/**
	 * Returns the port at which the engine is running
	 *
	 * @return the port number by default it's 8787 for jWebSocket
	 */
	Integer getPort();

	/**
	 * Returns the port at which the SSL encrypted engine is running
	 *
	 * @return the port number by default it's 9797 for jWebSocket with SSL
	 */
	Integer getSSLPort();

	/**
	 * Engine timeout value in milliseconds
	 *
	 * @return timeout value
	 */
	int getTimeout();

	/**
	 * The maximum frame size in KB, any data frame with size greater than this
	 * value will cause connection to be terminated
	 *
	 * @return the maximum frame size
	 */
	int getMaxFramesize();

	/**
	 * These are the list of allowed domains for accepting connections for the
	 * origin based security model. Any connection request with different origin
	 * than the origins in this list is not accepted and the connection is
	 * terminated immediately.
	 *
	 * @return the list of allowed domains
	 */
	List<String> getDomains();

	/**
	 *
	 * @return The maximum number of connections allowed by this engine
	 */
	Integer getMaxConnections();

	/**
	 * The on max connections strategy indicates the procedure to execute when
	 * the maximum number of concurrent connections has been reached.
	 *
	 * @return The "on max connections" strategy
	 */
	String getOnMaxConnectionStrategy();

	/**
	 * @return the settings
	 */
	Map<String, Object> getSettings();

	/**
	 * Gets the engine socket server hostname
	 *
	 * @return
	 */
	String getHostname();

	/**
	 * Indicates if the engine notifies connected clients during the jWebSocket
	 * server stopping process.
	 *
	 * @return
	 */
	boolean isNotifySystemStopping();
	
	/**
	 * Indicates whether to check or not if the connector is still alive, this 
	 * allows to detect when the network cable from the user is disconnected or 
	 * simply if the wifi network failed then we know when we can disconnect the 
	 * connector properly
	 * @return
	 */
	boolean getKeepAliveConnectors();
	
	/**
	 * Used to define a certain interval to ping the connector on the client 
	 * side to check if it is still alive
	 * @return
	 */
	Integer getKeepAliveConnectorsInterval();
	
	/**
	 * Sets the default timeout that must wait the server before closing the 
	 * connection if the client didn't respond to a ping in the specified time
	 * @return 
	 */
	Integer getKeepAliveConnectorsTimeout();
	
}
