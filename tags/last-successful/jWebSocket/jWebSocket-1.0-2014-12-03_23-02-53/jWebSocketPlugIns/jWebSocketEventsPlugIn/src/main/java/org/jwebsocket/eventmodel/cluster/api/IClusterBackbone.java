//	---------------------------------------------------------------------------
//	jWebSocket - IClusterBackbone (Community Edition, CE)
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
