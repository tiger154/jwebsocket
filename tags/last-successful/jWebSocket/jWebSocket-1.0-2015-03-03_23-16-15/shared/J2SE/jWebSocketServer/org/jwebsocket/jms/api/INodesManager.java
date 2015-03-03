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

import java.util.List;
import java.util.Map;
import org.jwebsocket.api.IInitializable;

/**
 * Component for nodes status management
 *
 * @author Rolando Santamaria Maso
 */
public interface INodesManager extends IInitializable {

	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	List<Map<String, Object>> listNodes() throws Exception;
	
	/**
	 * Get the nodes load average.
	 *
	 * @return The nodes load percent (cluster load)
	 * @throws Exception
	 */
	double getNodesLoadAvg() throws Exception;

	/**
	 * Register a new jWebSocket server node. Stores the registration date/time
	 * (startup time)
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
	 * Set a jWebSocket server node status.
	 *
	 * @param aNodeId
	 * @param aStatus 0 = ready, 1 = paused (does not accept new requests), 2 =
	 * offline
	 * @throws java.lang.Exception
	 */
	void setStatus(String aNodeId, int aStatus) throws Exception;
	
	/**
	 * Get a jWebSocket server node status
	 * 
	 * @param aNodeId The node identifier
	 * @return
	 * @throws Exception 
	 */
	Integer getStatus(String aNodeId) throws Exception;

	/**
	 * Get the optimum jWebSocket server node name.
	 *
	 * @return
	 * @throws java.lang.Exception
	 */
	String getOptimumNode() throws Exception;

	/**
	 * Increase a jWebSocket server node number of processed requests.
	 *
	 * @param aNodeId
	 * @throws java.lang.Exception
	 */
	void increaseRequests(String aNodeId) throws Exception;

	/**
	 * Get the jWebSocket server node name by its consumer identifier.
	 *
	 * @param aConsumerId
	 * @return
	 * @throws Exception
	 */
	String getNodeId(String aConsumerId) throws Exception;

	/**
	 * Return TRUE if exists a jWebSocket server node id, matches the given
	 * node id, FALSE otherwise
	 *
	 * @param aNodeId
	 * @return
	 * @throws Exception
	 */
	boolean exists(String aNodeId) throws Exception;

	/**
	 * Set the jWebSocket server node description.
	 *
	 * @param aNodeDescription
	 */
	void setNodeDescription(String aNodeDescription);

	/**
	 * Get the jWebSocket server node description.
	 *
	 * @return
	 */
	String getNodeDescription();

	/**
	 * Get the available jWebSocket server nodes count.
	 *
	 * @return
	 */
	long count();

	/**
	 * Get the jWebSocket server nodes synchronizer.
	 *
	 * @return
	 */
	IClusterSynchronizer getSynchronizer();

	/**
	 * Get the IConsumerAdviceTempStorage instance.
	 *
	 * @return
	 */
	IConsumerAdviceTempStorage getConsumerAdviceTempStorage();

	/**
	 * Set the IConsumerAdviceTempStorage instance.
	 *
	 * @param aConsumerAdviceTempStorage
	 */
	void setConsumerAdviceTempStorage(IConsumerAdviceTempStorage aConsumerAdviceTempStorage);
}
