//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketConnector (Community Edition, CE)
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

import java.net.InetAddress;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.kit.WebSocketSession;

/**
 * Specifies the API for jWebSocket connectors. Connectors are the low level link to the client.
 * Connectors are maintained by the engine only but can be accessed up to the application. Each
 * connector provides a Map for shared custom variables (public) which can be used in all overlying
 * tiers.
 *
 * @author Alexander Schulze
 */
public interface WebSocketConnector {

	/**
	 * Starts and initializes the connector. Usually a connector is implemented as as thread which
	 * waits on incoming data. The listener thread should implement a timeout to close a connection
	 * after a configurable time of inactivity on the connection. Further the
	 * {@code connectorStarted} method of the overlying engine is called if the connector
	 * successfully started.
	 */
	void startConnector();

	/**
	 * Stops and cleans up the connector. Usually here the listener thread for this connection is
	 * stopped. Further the {@code connectorStopped} method of the overlying engine is called if the
	 * connector successfully started.
	 *
	 * @param aCloseReason
	 */
	void stopConnector(CloseReason aCloseReason);

	/**
	 * Returns the current status for the connector. Please refer to the WebSocketConnectorStatus
	 * enumeration.
	 *
	 * @return
	 */
	WebSocketConnectorStatus getStatus();

	/**
	 * Sets the current status for the connector. Please refer to the WebSocketConnectorStatus
	 * enumeration.
	 *
	 * @param aStatus
	 */
	void setStatus(WebSocketConnectorStatus aStatus);

	/**
	 * Returns the engine the connector is bound to.
	 *
	 * @return WebSocketEngine Engine the connector is bound to
	 */
	WebSocketEngine getEngine();

	/**
	 * Processes an incoming ping from a WebSocket client. Usually the ping needs to be answered by
	 * a pong.
	 *
	 * @param aDataPacket raw web socket data packet
	 */
	void processPing(WebSocketPacket aDataPacket);

	/**
	 * Processes an incoming pong from a WebSocket client. Usually the pong packet is an answer to a
	 * previously send ping.
	 *
	 * @param aDataPacket raw web socket data packet
	 */
	void processPong(WebSocketPacket aDataPacket);

	/**
	 * Processes an incoming datapacket from a WebSocket client. Usually the data packet is not
	 * processed in any way but only passed up to the {@code processPacket} method of the overlying
	 * engine.
	 *
	 * @param aDataPacket raw web socket data packet
	 */
	void processPacket(WebSocketPacket aDataPacket);

	/**
	 * Return the synchronization object for write transactions.
	 *
	 * @return
	 */
	public Object getWriteLock();

	/**
	 * Return the synchronization object for read transactions.
	 *
	 * @return
	 */
	public Object getReadLock();

	/**
	 * Send a data packet to a WebSocket client and control the delivery by using a listener.
	 *
	 * @param aDataPacket
	 * @param aListener
	 */
	void sendPacketInTransaction(WebSocketPacket aDataPacket, IPacketDeliveryListener aListener);

	/**
	 * Send a fragmented data packet to a WebSocket client and control the delivery by using a
	 * listener.
	 *
	 * @param aDataPacket
	 * @param aFragmentSize
	 * @param aListener
	 */
	void sendPacketInTransaction(WebSocketPacket aDataPacket,
			Integer aFragmentSize, IPacketDeliveryListener aListener);

	/**
	 * Sends a data packet to a WebSocket client. Here the packet is finally passed to client via
	 * the web socket connection. This method is synchronized to ensure that not multiple threads
	 * send at the same time.
	 *
	 * @param aDataPacket raw web socket data packet
	 */
	void sendPacket(WebSocketPacket aDataPacket);

	/**
	 * Sends a data packet to a WebSocket client asynchronously. This method immediately returns the
	 * future object to the caller so that it can proceed with the processing and not wait for the
	 * response.
	 *
	 * @param aDataPacket raw web socket data packet
	 * @return the {@link IOFuture} which will be notified when the write request succeeds or fails
	 * null if there's any problem with the send operation.
	 */
	IOFuture sendPacketAsync(WebSocketPacket aDataPacket);

	/**
	 * Returns the request header from the client during the connection establishment. In the
	 * request header all fields of the client request and its URL parameters are stored.
	 *
	 * @return RequestHeader object
	 */
	RequestHeader getHeader();

	/**
	 * Sets the request header. This methode is called after the hand shake of the web socket
	 * protocol has been accomplished and all data of the request header is known.
	 *
	 * @param aHeader RequestHeader object
	 */
	void setHeader(RequestHeader aHeader);

	/**
	 * Returns the given custom variable as an Object. Custom variables in a connector are public
	 * and can be shared over all modules of an application.
	 *
	 * @param aKey Name of the shared custom variable
	 * @return Object
	 */
	Object getVar(String aKey);

	/**
	 * Set the given custom variable to the passed value. Custom variables in a connector are public
	 * and can be shared over all modules of an application.
	 *
	 * @param aKey Name of the shared custom variable
	 * @param aValue Object
	 */
	void setVar(String aKey, Object aValue);

	/**
	 * Returns the boolean object of the passed variable or null if the variable does not exist.
	 *
	 * @param aKey Name of the shared custom variable
	 * @return Boolean object
	 */
	Boolean getBoolean(String aKey);

	/**
	 * Returns the boolean value of the passed variable. If the variable does not exist always
	 * {@code false} is returned.
	 *
	 * @param aKey Name of the shared custom variable
	 * @return boolean value (simple type, not an Object)
	 */
	boolean getBool(String aKey);

	/**
	 * Sets the boolean value of the given shared custom variable.
	 *
	 * @param aKey Name of the shared custom variable
	 * @param aValue Boolean value
	 */
	void setBoolean(String aKey, Boolean aValue);

	/**
	 * Returns the string object of the passed variable or null if the variable does not exist. The
	 * default character encoding is applied.
	 *
	 * @param aKey Name of the shared custom variable
	 * @return String
	 */
	String getString(String aKey);

	/**
	 * Sets the string value of the given shared custom variable.
	 *
	 * @param aKey Name of the shared custom variable
	 * @param aValue String
	 */
	void setString(String aKey, String aValue);

	/**
	 * Returns the integer object of the passed variable or null if the variable does not exist.
	 *
	 * @param aKey Name of the shared custom variable
	 * @return Integer object
	 */
	Integer getInteger(String aKey);

	/**
	 * Sets the integer value of the given shared custom variable.
	 *
	 * @param aKey Name of the shared custom variable
	 * @param aValue Integer value
	 */
	void setInteger(String aKey, Integer aValue);

	/**
	 * Removes the given shared custom variable from the connector. After this operation the
	 * variable is not accessible anymore.
	 *
	 * @param aKey Name of the shared custom variable
	 */
	void removeVar(String aKey);

	/**
	 * Generates a unique ID for this connector to be used to calculate a session ID in overlying
	 * tiers.
	 *
	 * @return a unique ID for this connector
	 */
	String generateUID();

	/**
	 * Returns the remote port of the connected client.
	 *
	 * @return int Number of the remote port.
	 */
	int getRemotePort();

	/**
	 * Returns the IP number of the connected remote host.
	 *
	 * @return InetAddress object of the given remote host
	 */
	InetAddress getRemoteHost();

	/**
	 * Returns the unique id of the connector. This ID is not security related, but to address a
	 * certain client in the WebSocket network work only. Because a multiple logins for a user are
	 * basically supported, the user-id cannot be used to address a client. The descendant classes
	 * use the shared custom variables of the connectors to store user specific data.
	 *
	 * @return String Unique id of the connector.
	 */
	String getId();

	/*
	 * Returns the session for the websocket connection.
	 */
	/**
	 *
	 * @return
	 */
	WebSocketSession getSession();

	/**
	 *
	 * @return
	 */
	String getUsername();

	/**
	 *
	 * @param aUsername
	 */
	void setUsername(String aUsername);

	/**
	 *
	 */
	void removeUsername();

	/**
	 *
	 * @return
	 */
	String getSubprot();

	/**
	 *
	 * @param aSubprot
	 */
	void setSubprot(String aSubprot);

	/**
	 *
	 * @return
	 */
	int getVersion();

	/**
	 *
	 * @param aVersion
	 */
	void setVersion(int aVersion);

	/**
	 *
	 */
	void removeSubprot();

	/**
	 * returns if the connector is connected to a local TCP port or if it is a connection on a
	 * remote (cluster) node.
	 *
	 * @return
	 */
	boolean isLocal();

	/**
	 *
	 * @return
	 */
	String getNodeId();

	/**
	 *
	 * @param aNodeId
	 */
	void setNodeId(String aNodeId);

	/**
	 *
	 */
	void removeNodeId();

	/**
	 *
	 *
	 * @return
	 */
	boolean isSSL();

	/**
	 *
	 *
	 * @param aIsSSL
	 */
	void setSSL(boolean aIsSSL);

	/**
	 *
	 * @return
	 */
	boolean isHixie();

	/**
	 *
	 * @return
	 */
	boolean isHybi();

	/**
	 * The "max frame size" variable indicates the "maximum packet size" supported by a client. The
	 * clients are able to choose the variable according to their scenarios. The client value is
	 * transmitted in the connection handshake. If the value exceeds the engine max frame size
	 * value, then the client value is ignored. The server transmit the selected value in the
	 * welcome message.
	 *
	 * @return The connector specific max frame size
	 */
	Integer getMaxFrameSize();

	/**
	 *
	 * @return TRUE if the client support tokens receiving, FALSE otherwise.
	 */
	boolean supportTokens();

	/**
	 *
	 * @return TRUE if the client represents a server internal connector instance.
	 */
	boolean isInternal();

	/**
	 *
	 * @return TRUE if the client supports transactions, FALSE otherwise.
	 */
	public boolean supportsTransactions();
}
