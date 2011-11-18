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
public interface IClusterBackbone {

	/**
	 * Send a token to a connector on the network
	 * 
	 * @param aTarget to
	 * @param aToken The token to be sent
	 */
	void sendToken(String aTarget, Token aToken);

	/**
	 * Send a token to a connector on the network
	 * 
	 * @param aTarget to
	 * @param aSource from 
	 * @param aToken The token to be sent
	 */
	void sendToken(String aTarget, String aSource, Token aToken);

	/**
	 * Process a token from a cluster node
	 * 
	 * @param aNode The cluster node connection
	 * @param aToken The token to process
	 */
	void processToken(WebSocketConnector aNode, Token aToken);

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
	INodeStatistics getNodeStatistics(String nodeId);

	/**
	 * 
	 * @return The cluster statistics 
	 */
	IClusterStatistics getClusterStatistics();

	/**
	 * Notify an event on the network nodes
	 * 
	 * @param aEvent The event to be notified
	 */
	void notifyClusterEvent(IClusterEvent aEvent);
}
