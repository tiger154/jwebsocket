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
	 * @param aStatus 0 = ready, 1 = paused (does not accept new requests), 2 =
	 * offline
	 */
	void setStatus(String aNodeId, int aStatus) throws Exception;

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
	 * Gets the node name by its session identifier
	 *
	 * @param aSessionId
	 * @return
	 * @throws Exception
	 */
	String getNodeId(String aSessionId) throws Exception;

	/**
	 * Returns TRUE if exists a node that matches the given node id, FALSE
	 * otherwise
	 *
	 * @param aNodeId
	 * @return
	 * @throws Exception
	 */
	boolean exists(String aNodeId) throws Exception;

	/**
	 * Sets the active node description
	 *
	 * @param aNodeDescription
	 */
	void setNodeDescription(String aNodeDescription);

	/**
	 * Gets the node description
	 *
	 * @return
	 */
	String getNodeDescription();

	/**
	 * Gets the available nodes number
	 *
	 * @return
	 */
	long count();

	/**
	 * Gets the node id by sent message's id
	 *
	 * @param lMsgId
	 * @return
	 */
	public String getNodeIdByAckMessageId(String lMsgId) throws Exception;

	/**
	 * Registers an acknowledge message id.
	 *
	 * @param aNodeId
	 * @param aMsgId
	 * @throws Exception
	 */
	void registerAckMessageId(String aNodeId, String aMsgId) throws Exception;

	/**
	 * Clear node existing acknowledge message ids
	 *
	 * @param aNodeId
	 * @throws Exception
	 */
	void clearAcks(String aNodeId) throws Exception;
}
