//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer MongoDBCluster (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//      Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.plugins.loadbalancer.mongodb;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.plugins.loadbalancer.EndPointStatus;
import org.jwebsocket.plugins.loadbalancer.api.ICluster;
import org.jwebsocket.plugins.loadbalancer.api.IClusterEndPoint;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class MongoDBCluster implements ICluster {

	private DBCollection mClusters, mEndPoints;
	private DBObject mThis;

	public MongoDBCluster(DBObject aCluster, DBCollection aClusters, DBCollection aEndPoints) {
		mClusters = aClusters;
		mEndPoints = aEndPoints;
		mThis = aCluster;
	}

	@Override
	public boolean isEndPointAvailable() {
		return mEndPoints.count(new BasicDBObject()
				.append("clusterAlias", getAlias())
				.append("status", EndPointStatus.ONLINE.name())) > 0;
	}

	MongoDBClusterEndPoint toEndPoint(DBObject aRecord) {
		if (null == aRecord) {
			return null;
		}

		return new MongoDBClusterEndPoint(aRecord, mEndPoints);
	}

	@Override
	public IClusterEndPoint getEndPoint(String aEndPointId) {
		return toEndPoint(mEndPoints.findOne(new BasicDBObject()
				.append("serviceId", aEndPointId)
				.append("status", EndPointStatus.ONLINE.name())));
	}

	@Override
	public Iterator<IClusterEndPoint> getEndPoints() {
		final DBCursor lCursor = mEndPoints.find().sort(new BasicDBObject().append("_id", 1));
		return new Iterator<IClusterEndPoint>() {

			@Override
			public boolean hasNext() {
				return lCursor.hasNext();
			}

			@Override
			public IClusterEndPoint next() {
				try {
					return toEndPoint(lCursor.next());
				} catch (Exception lEx) {
					throw new RuntimeException(lEx);
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove");
			}
		};
	}

	List<IClusterEndPoint> getEndPointsList() {
		List<IClusterEndPoint> lList = new ArrayList<IClusterEndPoint>();
		Iterator<IClusterEndPoint> lIt = getEndPoints();

		while (lIt.hasNext()) {
			lList.add(lIt.next());
		}

		return lList;
	}

	@Override
	public Map<String, Object> getInfo() {
		Map<String, Object> lInfoCluster = new HashMap<String, Object>();
		lInfoCluster.put("clusterAlias", getAlias());
		lInfoCluster.put("clusterNS", getNamespace());
		lInfoCluster.put("endPointsCount", new Long(mEndPoints.count()).intValue());
		lInfoCluster.put("endPoints", getEndPointsList());
		lInfoCluster.put("requests", getTotalEndPointsRequests());

		return lInfoCluster;
	}

	@Override
	public String getNamespace() {
		return (String) mThis.get("ns");
	}

	@Override
	public String getPassword() {
		return (String) mThis.get("password");
	}

	@Override
	public IClusterEndPoint getOptimumEndPoint() {
		AggregationOutput lResult = mEndPoints.aggregate(new BasicDBObject()
				.append("$match", new BasicDBObject()
						.append("status", EndPointStatus.ONLINE.name())
						.append("$ne", new BasicDBObject().append("runtimePlatform", "javascript")))
				.append("$group", new BasicDBObject()
						.append("serviceId", "$serviceId")
						.append("cpu", new BasicDBObject().append("$min", "$cpu"))), new BasicDBObject());

		if (lResult.results().iterator().hasNext()) {
			MongoDBClusterEndPoint lEndPoint = (MongoDBClusterEndPoint) getEndPoint((String) lResult
					.results().iterator().next().get("serviceId"));
			lEndPoint.incrementUses();
			return lEndPoint;
		}

		return null;
	}

	@Override
	public IClusterEndPoint getOptimumRREndPoint() {
		IClusterEndPoint lEndPoint = getOptimumEndPoint();
		if (null == lEndPoint) {
			lEndPoint = getRoundRobinEndPoint();
		}

		return lEndPoint;
	}

	@Override
	public IClusterEndPoint getRoundRobinEndPoint() {
		AggregationOutput lResult = mEndPoints.aggregate(new BasicDBObject()
				.append("$match", new BasicDBObject()
						.append("status", EndPointStatus.ONLINE.name()))
				.append("$group", new BasicDBObject()
						.append("serviceId", "$serviceId")
						.append("uses", new BasicDBObject().append("$min", "$uses"))), new BasicDBObject());

		Iterator<DBObject> lIt = lResult.results().iterator();
		if (lIt.hasNext()) {
			MongoDBClusterEndPoint lEndPoint = (MongoDBClusterEndPoint) getEndPoint((String) lIt.next().get("serviceId"));
			lEndPoint.incrementUses();
			return lEndPoint;
		}

		return null;
	}

	@Override
	public void getStickyRoutes(List<Map<String, String>> aStickyRoutes) {
		Map<String, String> lInfoCluster;
		DBCursor lCursor = mEndPoints.find(new BasicDBObject()
				.append("status", EndPointStatus.ONLINE.name())
				.append("clusterAlias", getAlias()));

		while (lCursor.hasNext()) {
			lInfoCluster = new HashMap<String, String>();
			lInfoCluster.put("clusterAlias", getAlias());
			lInfoCluster.put("endPointId", (String) lCursor.next().get("serviceId"));

			aStickyRoutes.add(lInfoCluster);
		}
	}

	@Override
	public void updateCpuUsage(String aConnectorId, double aCpuUsage) {
		mEndPoints.update(new BasicDBObject().append("cpu", aCpuUsage),
				new BasicDBObject().append("connectorId", aConnectorId));
	}

	@Override
	public IClusterEndPoint registerEndPoint(WebSocketConnector aConnector) {
		String lServiceId = UUID.randomUUID().toString();
		mEndPoints.save(new BasicDBObject()
				.append("serviceId", lServiceId)
				.append("clusterAlias", getAlias())
				.append("requests", new Integer(0))
				.append("status", EndPointStatus.ONLINE.name())
				.append("cpu", new Double(-1.0))
				.append("runtimePlatform", aConnector.getVar("jwsType"))
				.append("uses", new Long(0))
				.append("connectorId", aConnector.getId()));

		return getEndPoint(lServiceId);
	}

	@Override
	public void removeEndPoint(IClusterEndPoint aClusterEndPoint) {
		mEndPoints.remove(new BasicDBObject().append("serviceId", aClusterEndPoint.getServiceId()));
	}

	@Override
	public int removeConnectorEndPoints(String aConnectorId) {
		return mEndPoints.remove(new BasicDBObject().append("connectorId", aConnectorId)).getN();
	}

	@Override
	public void setNamespace(String aNamespace) {
		mThis.put("ns", aNamespace);
		mClusters.save(mThis);
	}

	@Override
	public void setPassword(String aPassword) {
		mThis.put("password", aPassword);
		mClusters.save(mThis);
	}

	@Override
	public void setAlias(String aAlias) {
		mThis.put("alias", aAlias);
		mClusters.save(mThis);
	}

	@Override
	public String getAlias() {
		return (String) mThis.get("alias");
	}

	private Object getTotalEndPointsRequests() {
		AggregationOutput lResult = mEndPoints.aggregate(new BasicDBObject()
				.append("$group", new BasicDBObject()
						.append("requests", new BasicDBObject().append("$sum", "$requests"))), new BasicDBObject());

		return lResult.results().iterator().next().get("requests");
	}

}
