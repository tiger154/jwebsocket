package org.jwebsocket.jms.mongodb;

import org.jwebsocket.jms.api.INodesManager;

/**
 *
 * @author kyberneees
 */
public class MongoDBNodesManager implements INodesManager {

	@Override
	public void register(String aSessionID, String aNodeId, String aDescription,
			String aIpAddress, double aCpuUsage) throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateCPU(String aNodeId, double aCpuUsage) throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setStatus(String aNodeId, int aMode) throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getOptimumNode() throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void increaseRequests(String aNodeId) throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getNodeId(String aSessionID) throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void initialize() throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void shutdown() throws Exception {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
