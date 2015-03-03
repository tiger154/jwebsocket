//	---------------------------------------------------------------------------
//	jWebSocket - IWebSocketClusterNode (Community Edition, CE)
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
package org.jwebsocket.eventmodel.cluster.api;

import java.util.List;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.token.Token;

/**
 *
 * @author Rolando Santamaria Maso
 */
public interface IWebSocketClusterNode {

	/**
	 *
	 * @return The cluster node unique identifier
	 */
	String getId();

	/**
	 * Send a token to a connector on a cluster network
	 *
	 * @param aTarget to
	 * @param aToken The token to be sent
	 */
	void sendToken(String aTarget, Token aToken);

	/**
	 * Send a token to a connector on a cluster network
	 *
	 * @param aTarget to
	 * @param aSource from
	 * @param aToken The token to be sent
	 */
	void sendToken(String aTarget, String aSource, Token aToken);

	/**
	 * Process a token from the backbone
	 *
	 * @param aBackbone The backbone connector
	 * @param aToken
	 */
	void processToken(WebSocketConnector aBackbone, Token aToken);

	/**
	 *
	 * @return The list of the connectors identifiers on the network
	 */
	List<String> getAllConnectors();

	/**
	 * Get statistics from a custom node
	 *
	 * @param nodeId The node identifier
	 * @return The node statistics
	 * @throws Exception
	 */
	INodeStatistics getNodeStatistics(String nodeId) throws Exception;

	/**
	 *
	 * @return The node statistics
	 * @throws Exception
	 */
	INodeStatistics getNodeStatistics() throws Exception;

	/**
	 *
	 * @return The cluster statistics
	 */
	IClusterStatistics getClusterStatistics();

	/**
	 * Process an event from the backbone
	 *
	 * @param aEvent The cluster event
	 */
	void processClusterEvent(IClusterEvent aEvent);

	/**
	 * Indicates if the server allow new connections, default FALSE
	 *
	 * @param flag
	 */
	void setAllowConnections(boolean flag);

	/**
	 *
	 * @return TRUE if the server allow new connections, FALSE otherwise
	 */
	boolean isAllowConnections();

	/**
	 * Shutdown the server slowly
	 *
	 * @param minClients Minimal connections number before disconnect
	 * @param disconnectionsNumber Number of disconnections on interval
	 * @param timeInterval Time interval to disconnect the clients slowly
	 */
	void slowShutdown(Integer minClients, Integer disconnectionsNumber, Integer timeInterval);

	/**
	 * Register a new client in the shared clientsIndexStorage
	 *
	 * @param connectorId The connector identifier
	 */
	void registerNewClient(String connectorId);

	/**
	 * Removes a client from the shared clientsIndexStorage
	 *
	 * @param connectorId The connector identifier
	 */
	void removeClient(String connectorId);
}
