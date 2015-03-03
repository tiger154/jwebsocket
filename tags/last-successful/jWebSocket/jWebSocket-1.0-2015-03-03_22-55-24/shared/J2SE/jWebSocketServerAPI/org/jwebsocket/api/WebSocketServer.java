//	---------------------------------------------------------------------------
//	jWebSocket - Server API (Community Edition, CE)
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
import java.util.List;
import java.util.Map;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketSession;

/**
 * Specifies the API of the jWebSocket server core and its capabilities. Each
 * server can be bound to one or multiple engines. Each engine can drive or more
 * servers above. The servers usually are not supposed to directly implement any
 * business logic - except for very small or special non token based
 * applications. For applications it is recommended to implement them in
 * plug-ins based on the token server.
 *
 * @author Alexander Schulze
 * @version $Id: WebSocketServer.java 625 2010-07-06 17:33:33Z fivefeetfurther $
 * @author Rolando Santamaria Maso
 */
public interface WebSocketServer extends ISystemLifecycle {

	/**
	 * Called when a WebSocketSession is created. Future enterprise applications
	 * will use this event instead of "connectorStarted", because the second
	 * does not guarantee a session storage creation.
	 *
	 * @param aConnector
	 * @param aSession
	 */
	void sessionStarted(WebSocketConnector aConnector, WebSocketSession aSession);

	/**
	 * Called when a WebSocketSession expired. This event represents the real
	 * client disconnection. The "connectorStopped" event should happen multiple
	 * times, but the session is kept. When a session is stopped (expired) it
	 * means: A client is finally disconnected.
	 *
	 * @param aSession
	 */
	void sessionStopped(WebSocketSession aSession);

	/**
	 * Starts the server and all underlying engines.
	 *
	 * @throws WebSocketException
	 */
	void startServer() throws WebSocketException;

	/**
	 * States if at least one of the engines is still running.
	 *
	 * @return Boolean state if at least one of the underlying engines is still
	 * running.
	 */
	boolean isAlive();

	/**
	 * Stops the server and all underlying engines.
	 *
	 * @throws WebSocketException
	 */
	void stopServer() throws WebSocketException;

	/**
	 * Adds a new engine to the server.
	 *
	 * @param aEngine to be added to the server.
	 */
	void addEngine(WebSocketEngine aEngine);

	/**
	 * Removes a already bound engine from the server.
	 *
	 * @param aEngine to be removed from the server.
	 */
	void removeEngine(WebSocketEngine aEngine);

	/**
	 * Is called from the underlying engine when the engine is started.
	 *
	 * @param aEngine
	 */
	void engineStarted(WebSocketEngine aEngine);

	/**
	 * Is called from the underlying engine when the engine is stopped.
	 *
	 * @param aEngine
	 */
	void engineStopped(WebSocketEngine aEngine);

	/**
	 * Notifies the application that a client connector has been started.
	 *
	 * @param aConnector the new connector that has been instantiated.
	 */
	void connectorStarted(WebSocketConnector aConnector);

	/**
	 * Notifies the application that a client connector has been stopped.
	 *
	 * @param aConnector
	 * @param aCloseReason
	 */
	void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason);

	/**
	 * Is called when the underlying engine received a packet from a connector.
	 *
	 * @param aEngine
	 * @param aConnector
	 * @param aDataPacket
	 */
	void processPacket(WebSocketEngine aEngine, WebSocketConnector aConnector, WebSocketPacket aDataPacket);

	/**
	 * Sends a packet to a certain connector.
	 *
	 * @param aConnector
	 * @param aDataPacket
	 */
	void sendPacket(WebSocketConnector aConnector, WebSocketPacket aDataPacket);

	/**
	 * Sends a packet to a certain connector in transaction.
	 *
	 * @param aConnector The target connector
	 * @param aDataPacket The packet to be sent
	 * @param aFragmentSize The fragment size to use in the packet fragmentation
	 * @param aListener The packet delivery listener
	 */
	void sendPacketInTransaction(WebSocketConnector aConnector, WebSocketPacket aDataPacket,
			Integer aFragmentSize, IPacketDeliveryListener aListener);

	/**
	 * Sends a packet to a certain connector in transaction.
	 *
	 * @param aConnector The target connector
	 * @param aDataPacket The packet to be sent
	 * @param aListener The packet delivery listener
	 */
	void sendPacketInTransaction(WebSocketConnector aConnector, WebSocketPacket aDataPacket,
			IPacketDeliveryListener aListener);

	/**
	 * Sends the data packet asynchronously to the output channel through the
	 * given target connector. This is a asynchronous output process which
	 * returns the future object to check the status and control the output
	 * operation.
	 *
	 * @param aConnector the target connector to use for the packet output
	 * @param aDataPacket the data packet
	 * @return the future object for this output operation
	 */
	IOFuture sendPacketAsync(WebSocketConnector aConnector, WebSocketPacket aDataPacket);

	/**
	 * Broadcasts a datapacket to all connectors.
	 *
	 * @param aSource
	 * @param aDataPacket
	 * @param aBroadcastOptions
	 */
	void broadcastPacket(WebSocketConnector aSource, WebSocketPacket aDataPacket,
			BroadcastOptions aBroadcastOptions);

	/**
	 * Broadcasts a datapacket to all connectors.
	 *
	 * @param aSource
	 * @param aDataPacket
	 */
	void broadcastPacket(WebSocketConnector aSource, WebSocketPacket aDataPacket);

	/**
	 * Returns the unique ID of the server. Because the jWebSocket model
	 * supports multiple servers based on one or more engines (drivers) each
	 * server has its own ID so that it can be addressed properly.
	 *
	 * @return String Unique ID of the Server.
	 */
	String getId();

	/**
	 * Returns the plugin chain for the server .
	 *
	 * @return the plugInChain
	 */
	WebSocketPlugInChain getPlugInChain();

	/**
	 * Returns plugin identified by id for the server.
	 *
	 * @param aId
	 * @return the plugInChain
	 */
	WebSocketPlugIn getPlugInById(String aId);

	/**
	 * Returns the filter chain for the server.
	 *
	 * @return the filterChain
	 */
	WebSocketFilterChain getFilterChain();

	/**
	 * Returns filter identified by Id for the server.
	 *
	 * @param aId
	 * @return the filterChain
	 */
	WebSocketFilter getFilterById(String aId);

	/**
	 *
	 * @param aListener
	 */
	void addListener(WebSocketServerListener aListener);

	/**
	 *
	 * @param aListener
	 */
	void removeListener(WebSocketServerListener aListener);

	/**
	 * Returns the list of listeners for the server.
	 *
	 * @return the filterChain
	 */
	List<WebSocketServerListener> getListeners();

	/**
	 *
	 * @param aConnector
	 * @return
	 */
	String getUsername(WebSocketConnector aConnector);

	/**
	 *
	 * @param aConnector
	 * @param aUsername
	 */
	void setUsername(WebSocketConnector aConnector, String aUsername);

	/**
	 *
	 * @param aConnector
	 */
	void removeUsername(WebSocketConnector aConnector);

	/**
	 *
	 * @param aConnector
	 * @return
	 */
	String getNodeId(WebSocketConnector aConnector);

	/**
	 *
	 * @param aConnector
	 * @param aNodeId
	 */
	void setNodeId(WebSocketConnector aConnector, String aNodeId);

	/**
	 *
	 * @param aConnector
	 */
	void removeNodeId(WebSocketConnector aConnector);

	/**
	 *
	 * @param aId
	 * @return
	 */
	WebSocketConnector getConnector(String aId);

	/**
	 *
	 * @param aFilterId
	 * @param aFilterValue
	 * @return
	 */
	WebSocketConnector getConnector(String aFilterId, Object aFilterValue);

	/**
	 *
	 * @param aNodeId
	 * @return
	 */
	WebSocketConnector getNode(String aNodeId);

	/**
	 *
	 * @param aUsername
	 * @return
	 */
	WebSocketConnector getConnectorByUsername(String aUsername);

	/**
	 *
	 * @param aEngine
	 * @return
	 */
	Map<String, WebSocketConnector> getConnectors(WebSocketEngine aEngine);

	/**
	 *
	 * @param aFilter
	 * @return
	 */
	Map<String, WebSocketConnector> selectConnectors(Map<String, Object> aFilter);

	/**
	 *
	 * @return
	 */
	Map<String, WebSocketConnector> selectTokenConnectors();

	/**
	 *
	 * @return
	 */
	Map<String, WebSocketConnector> getAllConnectors();

	/**
	 *
	 * @param configuration
	 */
	void setServerConfiguration(ServerConfiguration configuration);

	/**
	 *
	 * @return
	 */
	ServerConfiguration getServerConfiguration();

	/**
	 * Get the connectors that share the same session id
	 *
	 * @param aSessionId The shared session id
	 * @return
	 */
	Map<String, WebSocketConnector> getSharedSessionConnectors(String aSessionId);

	/**
	 * Get the connectors count
	 *
	 * @return
	 */
	Long getConnectorsCount();

	/**
	 * Get all the connectors through a java.util.Iterator instance
	 *
	 * @return
	 */
	Iterator<WebSocketConnector> getAllConnectorsIterator();
}
