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
package org.jwebsocket.eventmodel.cluster.api;

import java.util.List;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.token.Token;

/**
 *
 * @author kyberneees
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
	 */
	INodeStatistics getNodeStatistics(String nodeId) throws Exception;

	/**
	 * 
	 * @return The node statistics 
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
