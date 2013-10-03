package org.jwebsocket.jms.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.jwebsocket.jms.Attributes;
import org.jwebsocket.jms.NodeStatus;
import org.jwebsocket.jms.api.IClusterSynchronizer;
import org.jwebsocket.jms.api.IConsumerAdviceTempStorage;
import org.jwebsocket.jms.api.INodesManager;
import org.springframework.util.Assert;

/**
 *
 * @author kyberneees
 */
public class MongoDBNodesManager implements INodesManager {

	private DBCollection mNodesCollection;
	private String mNodeDescription;
	private IClusterSynchronizer mSynchronizer;
	private IConsumerAdviceTempStorage mConsumerAdviceTempStorage;

	@Override
	public IConsumerAdviceTempStorage getConsumerAdviceTempStorage() {
		return mConsumerAdviceTempStorage;
	}

	@Override
	public void setConsumerAdviceTempStorage(IConsumerAdviceTempStorage aConsumerAdviceTempStorage) {
		mConsumerAdviceTempStorage = aConsumerAdviceTempStorage;
	}

	@Override
	public IClusterSynchronizer getSynchronizer() {
		return mSynchronizer;
	}

	public void setSynchronizer(IClusterSynchronizer aSynchronizer) {
		mSynchronizer = aSynchronizer;
	}

	public DBCollection getCollection() {
		return mNodesCollection;
	}

	public void setCollection(DBCollection aCollection) {
		mNodesCollection = aCollection;
	}

	@Override
	public void register(String aConsumerId, String aNodeId, String aDescription,
			String aIpAddress, double aCpuUsage) throws Exception {
		if (exists(aNodeId)) {
			mNodesCollection.update(new BasicDBObject().append(Attributes.NODE_ID, aNodeId),
					new BasicDBObject()
					.append("$set", new BasicDBObject()
					.append(Attributes.CONSUMER_ID, aConsumerId)
					.append(Attributes.DESCRIPTION, aDescription)
					.append(Attributes.IP_ADDRESS, aIpAddress)
					.append(Attributes.STATUS, NodeStatus.UP)
					.append(Attributes.CPU, aCpuUsage)));
		} else {
			mNodesCollection.save(new BasicDBObject()
					.append(Attributes.NODE_ID, aNodeId)
					.append(Attributes.CONSUMER_ID, aConsumerId)
					.append(Attributes.DESCRIPTION, aDescription)
					.append(Attributes.IP_ADDRESS, aIpAddress)
					.append(Attributes.STATUS, NodeStatus.UP)
					.append(Attributes.CPU, aCpuUsage));
		}
	}

	@Override
	public boolean exists(String aNodeId) throws Exception {
		return null != mNodesCollection.findOne(new BasicDBObject().append(Attributes.NODE_ID, aNodeId));
	}

	@Override
	public void updateCPU(String aNodeId, double aCpuUsage) throws Exception {
		Assert.isTrue(exists(aNodeId), "The target node does not exists!");

		mNodesCollection.update(new BasicDBObject().append(Attributes.NODE_ID, aNodeId), new BasicDBObject()
				.append("$set", new BasicDBObject()
				.append(Attributes.CPU, aCpuUsage)));
	}

	@Override
	public void setStatus(String aNodeId, int aStatus) throws Exception {
		Assert.isTrue(exists(aNodeId), "The target node does not exists!");

		mNodesCollection.update(new BasicDBObject().append(Attributes.NODE_ID, aNodeId), new BasicDBObject()
				.append("$set", new BasicDBObject()
				.append(Attributes.STATUS, aStatus)));
	}

	@Override
	public String getOptimumNode() throws Exception {
		DBCursor lCursor = mNodesCollection.find(new BasicDBObject().append(Attributes.STATUS, NodeStatus.UP))
				.sort(new BasicDBObject().append(Attributes.CPU, 1)
				.append(Attributes.REQUESTS, 1)).limit(1);

		String lNodeId = null;
		if (lCursor.hasNext()) {
			lNodeId = (String) lCursor.next().get(Attributes.NODE_ID);
		}

		return lNodeId;
	}

	@Override
	public void increaseRequests(String aNodeId) throws Exception {
		Assert.isTrue(exists(aNodeId), "The target node does not exists!");

		mNodesCollection.update(new BasicDBObject().append(Attributes.NODE_ID, aNodeId), new BasicDBObject()
				.append("$inc", new BasicDBObject()
				.append(Attributes.REQUESTS, 1)));
	}

	@Override
	public String getNodeId(String aConsumerId) throws Exception {
		DBObject lRecord = mNodesCollection.findOne(new BasicDBObject().append(Attributes.CONSUMER_ID, aConsumerId));
		if (null != lRecord) {
			return (String) lRecord.get(Attributes.NODE_ID);
		}

		return null;
	}

	@Override
	public void initialize() throws Exception {
		// creating index for CPU and REQUESTS fields for sorting
		mNodesCollection.ensureIndex(new BasicDBObject().append(Attributes.CPU, 1).append(Attributes.REQUESTS, 1));

		// setting 'CONSUMER_ID' as primary key
		mNodesCollection.ensureIndex(new BasicDBObject().append(Attributes.CONSUMER_ID, 1),
				new BasicDBObject().append("unique", true));

		// setting NODE id as primary key
		mNodesCollection.ensureIndex(new BasicDBObject().append(Attributes.NODE_ID, 1),
				new BasicDBObject().append("unique", true));
	}

	@Override
	public void setNodeDescription(String aNodeDescription) {
		mNodeDescription = aNodeDescription;
	}

	@Override
	public String getNodeDescription() {
		return mNodeDescription;
	}

	@Override
	public void shutdown() throws Exception {
	}

	@Override
	public long count() {
		return mNodesCollection.count(new BasicDBObject().append(Attributes.STATUS, NodeStatus.UP));
	}
}
