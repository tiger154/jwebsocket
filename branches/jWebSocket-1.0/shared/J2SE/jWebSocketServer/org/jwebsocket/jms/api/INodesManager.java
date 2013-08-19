package org.jwebsocket.jms.api;

import org.jwebsocket.api.IInitializable;

/**
 *
 * @author kyberneees
 */
public interface INodesManager extends IInitializable {

	/**
	 * Register a new jWebSocket node.
	 *
	 * @param aSessionID The JMS session id
	 * @param aNodeId The node id
	 * @param aDescription The node description
	 * @param aIpAddress The node ip-address
	 * @param aCpuUsage The node CPU usage
	 * @throws Exception
	 */
	void register(String aSessionID, String aNodeId, String aDescription,
			String aIpAddress, double aCpuUsage) throws Exception;

	/**
	 * Update jWebSocket node CPU usage.
	 *
	 * @param aNodeId
	 * @param aCpuUsage
	 * @throws Exception
	 */
	void updateCPU(String aNodeId, double aCpuUsage) throws Exception;

	/**
	 * Sets the jWebSocket node status.
	 *
	 * @param aNodeId
	 * @param aMode 0 = ready, 1 = paused (does not accept new requests), 2 =
	 * offline
	 */
	void setStatus(String aNodeId, int aMode) throws Exception;

	/**
	 * Gets the optimum node name.
	 *
	 * @return
	 */
	String getOptimumNode() throws Exception;

	/**
	 * Increases the redirected requestes number on target node.
	 *
	 * @param aNodeId
	 */
	void increaseRequests(String aNodeId) throws Exception;

	/**
	 * Gets the node name by its session ID
	 *
	 * @param aSessionID
	 * @return
	 * @throws Exception
	 */
	String getNodeId(String aSessionID) throws Exception;
}
