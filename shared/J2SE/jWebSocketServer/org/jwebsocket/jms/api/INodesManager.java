//	---------------------------------------------------------------------------
//	jWebSocket - INodesManager interface (Community Edition, CE)
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
package org.jwebsocket.jms.api;

import org.jwebsocket.api.IInitializable;

/**
 * Component for nodes status management
 *
 * @author Rolando Santamaria Maso
 */
public interface INodesManager extends IInitializable {

	/**
	 * Register a new jWebSocket server node.
	 *
	 * @param aConsumerId The consumer id
	 * @param aNodeId The node id
	 * @param aDescription The node description
	 * @param aIpAddress The node ip-address
	 * @param aCpuUsage The node CPU usage
	 * @throws Exception
	 */
	void register(String aConsumerId, String aNodeId, String aDescription,
			String aIpAddress, double aCpuUsage) throws Exception;

	/**
	 * Update a jWebSocket server node CPU usage.
	 *
	 * @param aNodeId
	 * @param aCpuUsage
	 * @throws Exception
	 */
	void updateCPU(String aNodeId, double aCpuUsage) throws Exception;

	/**
	 * Sets the a jWebSocket server node status.
	 *
	 * @param aNodeId
	 * @param aStatus 0 = ready, 1 = paused (does not accept new requests), 2 =
	 * offline
	 * @throws java.lang.Exception
	 */
	void setStatus(String aNodeId, int aStatus) throws Exception;

	/**
	 * Gets the optimum jWebSocket server node name.
	 *
	 * @return
	 * @throws java.lang.Exception
	 */
	String getOptimumNode() throws Exception;

	/**
	 * Increases a jWebSocket server node number of processed requests.
	 *
	 * @param aNodeId
	 * @throws java.lang.Exception
	 */
	void increaseRequests(String aNodeId) throws Exception;

	/**
	 * Gets the jWebSocket server node name by its consumer identifier.
	 *
	 * @param aConsumerId
	 * @return
	 * @throws Exception
	 */
	String getNodeId(String aConsumerId) throws Exception;

	/**
	 * Returns TRUE if exists a jWebSocket server node id, matches the given
	 * node id, FALSE otherwise
	 *
	 * @param aNodeId
	 * @return
	 * @throws Exception
	 */
	boolean exists(String aNodeId) throws Exception;

	/**
	 * Sets the jWebSocket server node description.
	 *
	 * @param aNodeDescription
	 */
	void setNodeDescription(String aNodeDescription);

	/**
	 * Gets tje jWebSocket server node description.
	 *
	 * @return
	 */
	String getNodeDescription();

	/**
	 * Gets the available jWebSocket server nodes count.
	 *
	 * @return
	 */
	long count();

	/**
	 * Gets the jWebSocket server nodes synchronizer.
	 *
	 * @return
	 */
	IClusterSynchronizer getSynchronizer();

	/**
	 * Gets the IConsumerAdviceTempStorage instance.
	 *
	 * @return
	 */
	IConsumerAdviceTempStorage getConsumerAdviceTempStorage();

	/**
	 * Sets the IConsumerAdviceTempStorage instance.
	 *
	 * @param aConsumerAdviceTempStorage
	 */
	void setConsumerAdviceTempStorage(IConsumerAdviceTempStorage aConsumerAdviceTempStorage);
}
