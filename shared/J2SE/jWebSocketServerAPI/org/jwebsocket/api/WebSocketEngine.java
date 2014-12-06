//	---------------------------------------------------------------------------
//	jWebSocket - Engine API (Community Edition, CE)
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

import java.util.Iterator;
import java.util.Map;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;

/**
 * Specifies the API for jWebSocket engines. An engine maintains multiple
 * connectors. The engine does neither parse nor process the data packets but
 * only passes them from either an underlying connector to the above server(s)
 * or from one of the higher level servers to one or more connectors of a
 * particular engine.
 *
 * @author Alexander Schulze
 * @author Puran Singh
 * @version $Id: WebSocketEngine.java 2010-03-03
 * @author Rolando Santamaria Maso
 */
public interface WebSocketEngine extends ISystemLifecycle {

	/**
	 * Returns the unique id of the engine. Because the jWebSocket model
	 * supports multiple engines as a kind of drivers for the servers on top of
	 * it each engine has its own Id so that it can be addressed properly.
	 *
	 * @return String
	 */
	String getId();

	/**
	 * Starts the engine. Usually an engine is implemented as a thread which
	 * waits for new clients to be connected via a WebSocketConnector. So here
	 * usually the listener threads for incoming connections are instantiated.
	 *
	 * @throws WebSocketException
	 */
	void startEngine() throws WebSocketException;

	/**
	 * Stops the engine. Here usually first all connected clients are stopped
	 * and afterwards the listener threads for incoming new clients is stopped
	 * as well.
	 *
	 * @param aCloseReason
	 * @throws WebSocketException
	 */
	void stopEngine(CloseReason aCloseReason) throws WebSocketException;

	/**
	 * Is called after the web socket engine has been started sucessfully. Here
	 * usually the engine notifies the server(s) above that the engine is
	 * started.
	 */
	void engineStarted();

	/**
	 * Is called after the web socket engine has (been) stopped sucessfully.
	 * Here usually the engine notifies the server(s) above that the engine has
	 * stopped.
	 */
	public void engineStopped();

	/**
	 * Is called after a new client has connected. Here usually the engine
	 * notifies the server(s) above that a new connection has been established.
	 *
	 * @param aConnector
	 */
	void connectorStarted(WebSocketConnector aConnector);

	/**
	 * Is called after a new client has disconnected. Here usually the engine
	 * notifies the server(s) above that a connection has been closed.
	 *
	 * @param aConnector
	 * @param aCloseReason
	 */
	void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason);

	/**
	 * Returns the Map of clients connected to this engine. Please consider that
	 * a server can support multiple engines. This method only returns the
	 * clients of this engine.
	 *
	 * @return the connector clients
	 */
	Map<String, WebSocketConnector> getConnectors();
	
	/**
	 * Returns an java.util.Iterator instance with all clients connected to this engine. Please consider that
	 * a server can support multiple engines. This method only returns the
	 * clients of this engine.
	 *
	 * @return the connectors iterator 
	 */
	Iterator<WebSocketConnector> getConnectorsIterator();

	/**
	 * Returns the TCP connector identified by its remote port number or
	 * {@literal null} if there's no client connector to the port passed.
	 *
	 * @param aRemotePort the remote TCP port searched for.
	 * @return WebSocketConnector that matches the given remote port or
	 * {@literal null} if no connector matches the remote port.
	 */
	WebSocketConnector getConnectorByRemotePort(int aRemotePort);

	/**
	 * Returns the TCP connector identified by its id
	 *
	 * @param aConnectorId the target connector id
	 * @return WebSocketConnector that matches the given connector id.
	 */
	WebSocketConnector getConnectorById(String aConnectorId);

	/**
	 * Returns {@literal true} if the engine is running or {@literal false} otherwise.
	 * The alive status usually represents the state of the main engine listener
	 * thread.
	 *
	 * @return true or false based on the server status
	 */
	boolean isAlive();

	/**
	 * Processes an incoming data packet from a certain connector. The
	 * implementation of the engine usually simply passes the packets to the
	 * server(s) of the overlying communication tier.
	 *
	 * @param aConnector
	 * @param aDataPacket
	 */
	void processPacket(WebSocketConnector aConnector, WebSocketPacket aDataPacket);

	/**
	 * Sends a data packet to a certain connector.
	 *
	 * @param aConnector
	 * @param aDataPacket
	 */
	void sendPacket(WebSocketConnector aConnector, WebSocketPacket aDataPacket);

	/**
	 * Broadcasts a data packet to all connectors. Usually the implementation
	 * simply iterates through the list of connectors and calls their sendPacket
	 * method.
	 *
	 * @param aSource
	 * @param aDataPacket
	 */
	void broadcastPacket(WebSocketConnector aSource, WebSocketPacket aDataPacket);

	/**
	 * Adds a certain connector to the engine. This usually has not to be done
	 * by the application but by the engine implementations only.
	 *
	 * @param aConnector
	 */
	void addConnector(WebSocketConnector aConnector);

	/**
	 * Removes a certain connector from the engine. This usually has not to be
	 * done by the application but by the engine implementations only.
	 *
	 * @param aConnector
	 */
	void removeConnector(WebSocketConnector aConnector);

	/**
	 * Returns a list of all servers that are currenly bound to this engine.
	 * This list of servers is maintained by the engine and should not be
	 * manipulated by the application.
	 *
	 * @return List of servers bound to the engine.
	 */
	Map<String, WebSocketServer> getServers();

	/**
	 * Registers a server at the engine so that the engine is able to notify the
	 * server in case of new connections and incoming data packets from a
	 * connector. This method is not supposed to be called directly from the
	 * application.
	 *
	 * @param aServer
	 */
	void addServer(WebSocketServer aServer);

	/**
	 * Unregisters a server from the engine so that the engine won't notify the
	 * server in case of new connections or incoming data packets from a
	 * connector. This method is not supposed to be called directly from the
	 * application.
	 *
	 * @param aServer
	 */
	void removeServer(WebSocketServer aServer);

	/**
	 * This method might be removed in future, instead use
	 * <tt>getConfiguration()</tt> to get the engine configuration.
	 *
	 * Returns the default session timeout for this engine. The session timeout
	 * is applied if no specific session timeout per connector is passed.
	 * Basically each connector can optionally use his own session timeout.
	 *
	 * @return int The default session timeout in milliseconds.
	 *
	 * @deprecated
	 */
	@Deprecated
	int getSessionTimeout();

	/**
	 * This method might be removed in future, instead use
	 * <tt>getConfiguration()</tt> to get the engine configuration.
	 *
	 * Sets the default session timeout for this engine. The session timeout is
	 * applied if no specific session timeout per connector is passed. Basically
	 * each connector can optionally use his own session timeout.
	 *
	 * @param aSessionTimeout The default session timeout in milliseconds.
	 *
	 * @deprecated
	 */
	@Deprecated
	void setSessionTimeout(int aSessionTimeout);

	/**
	 * This method might be removed in future, instead use
	 * <tt>getConfiguration()</tt> to get the engine configuration.
	 *
	 * Returns the maximum frame size in bytes, If the client sends a frame size
	 * larger than this maximum value, the socket connection will be closed.
	 *
	 * @return the max frame size value
	 *
	 * @deprecated
	 */
	@Deprecated
	int getMaxFrameSize();

	/**
	 * @param configuration the engine configuration to set
	 */
	void setEngineConfiguration(EngineConfiguration configuration);

	/**
	 * Returns the configuration for the engine.
	 *
	 * @return the engine configuration object
	 */
	EngineConfiguration getConfiguration();

	/**
	 *
	 * @return The maximun number of connections allowed by this engine
	 */
	Integer getMaxConnections();

	/**
	 * Set the engine system stopping notification strategy. The strategy is
	 * called only if the 'notifySystemStopping' configuration is set to TRUE.
	 *
	 * @param aStrategy
	 */
	void setSystemStoppingNotificationStrategy(Runnable aStrategy);

	/**
	 * Get shared session connectors
	 *
	 * @param aSessionId
	 * @return
	 */
	Map<String, WebSocketConnector> getSharedSessionConnectors(String aSessionId);

	/**
	 * Get the number of active managed connections
	 *
	 * @return
	 */
	Long getConnectorsCount();
}
