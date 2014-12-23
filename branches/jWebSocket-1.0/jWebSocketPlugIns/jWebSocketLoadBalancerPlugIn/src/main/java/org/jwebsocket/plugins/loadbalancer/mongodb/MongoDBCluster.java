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
import static org.jwebsocket.plugins.loadbalancer.api.Attributes.*;
import org.jwebsocket.plugins.loadbalancer.api.ICluster;
import org.jwebsocket.plugins.loadbalancer.api.IClusterEndPoint;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class MongoDBCluster implements ICluster {

	private DBCollection mClusters, mEndPoints;
	private DBObject mDocument;

	public MongoDBCluster(DBObject aCluster, DBCollection aClusters, DBCollection aEndPoints) {
		mClusters = aClusters;
		mEndPoints = aEndPoints;
		mDocument = aCluster;
	}

	@Override
	public boolean isEndPointAvailable() {
		return mEndPoints.count(new BasicDBObject()
				.append(CLUSTER_ALIAS, getAlias())
				.append(STATUS, EndPointStatus.ONLINE.name())) > 0;
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
				.append(ENDPOINT_ID, aEndPointId)
				.append(STATUS, EndPointStatus.ONLINE.name())));
	}

	@Override
	public Iterator<IClusterEndPoint> getEndPoints() {
		final DBCursor lCursor = mEndPoints.find(new BasicDBObject()
				.append(CLUSTER_ALIAS, getAlias()))
				.sort(new BasicDBObject().append("_id", 1));
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
		lInfoCluster.put(CLUSTER_ALIAS, getAlias());
		lInfoCluster.put(CLUSTER_NS, getNamespace());
		lInfoCluster.put(ENDPOINTS_COUNT, new Long(mEndPoints
				.count(new BasicDBObject().append(CLUSTER_ALIAS, getAlias()))).intValue());
		lInfoCluster.put(ENDPOINTS, getEndPointsList());
		lInfoCluster.put(REQUESTS, getTotalEndPointsRequests());

		return lInfoCluster;
	}

	@Override
	public String getNamespace() {
		return (String) mDocument.get(CLUSTER_NS);
	}

	@Override
	public String getPassword() {
		return (String) mDocument.get(CLUSTER_PASSWORD);
	}

	@Override
	public IClusterEndPoint getOptimumEndPoint() {
		DBCursor lCursor = mEndPoints.find(new BasicDBObject()
				.append(CLUSTER_ALIAS, getAlias())
				.append(STATUS, EndPointStatus.ONLINE.name())
				.append(RUNTIME_PLATFORM, new BasicDBObject()
						.append("$ne", JAVASCRIPT_RUNTIME_PLATFORM)));

		double lMinCPU = Double.MAX_VALUE;
		DBObject lTemp;
		DBObject lCandidate = null;

		while (lCursor.hasNext()) {
			lTemp = lCursor.next();
			double lEndPointCPU = (Double) lTemp.get(CPU);
			if (lEndPointCPU < lMinCPU) {
				lMinCPU = lEndPointCPU;
				lCandidate = lTemp;
			}
		}

		return toEndPoint(lCandidate);
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
		DBCursor lCursor = mEndPoints.find(new BasicDBObject()
				.append(CLUSTER_ALIAS, getAlias())
				.append(STATUS, EndPointStatus.ONLINE.name()));

		Long lMinUses = Long.MAX_VALUE;
		DBObject lTemp;
		DBObject lCandidate = null;

		while (lCursor.hasNext()) {
			lTemp = lCursor.next();
			Long lEndPointUses = (Long) lTemp.get(ENDPOINT_USES);
			if (lEndPointUses < lMinUses) {
				lMinUses = lEndPointUses;
				lCandidate = lTemp;
			}
		}

		if (null != lCandidate) {
			MongoDBClusterEndPoint lEndPoint = toEndPoint(lCandidate);
			lEndPoint.incrementUses();

			return lEndPoint;
		}
		return null;
	}

	@Override
	public void getStickyRoutes(List<Map<String, String>> aBuffer) {
		Map<String, String> lInfoCluster;
		DBCursor lCursor = mEndPoints.find(new BasicDBObject()
				.append(STATUS, EndPointStatus.ONLINE.name())
				.append(CLUSTER_ALIAS, getAlias()));

		while (lCursor.hasNext()) {
			lInfoCluster = new HashMap<String, String>();
			lInfoCluster.put(CLUSTER_ALIAS, getAlias());
			lInfoCluster.put(ENDPOINT_ID, (String) lCursor.next().get(ENDPOINT_ID));

			aBuffer.add(lInfoCluster);
		}
	}

	@Override
	public void updateCpuUsage(String aConnectorId, double aCpuUsage) {
		mEndPoints.updateMulti(new BasicDBObject().append(CONNECTOR_ID, aConnectorId),
				new BasicDBObject().append(CPU, aCpuUsage));
	}

	@Override
	public IClusterEndPoint registerEndPoint(WebSocketConnector aConnector) {
		String lServiceId = UUID.randomUUID().toString();
		mEndPoints.save(new BasicDBObject()
				.append(ENDPOINT_ID, lServiceId)
				.append(CLUSTER_ALIAS, getAlias())
				.append(REQUESTS, new Long(0))
				.append(STATUS, EndPointStatus.ONLINE.name())
				.append(CPU, new Double(-1.0))
				.append(RUNTIME_PLATFORM, aConnector.getVar("jwsType"))
				.append(ENDPOINT_USES, new Long(0))
				.append(CONNECTOR_ID, aConnector.getId()));

		return getEndPoint(lServiceId);
	}

	@Override
	public void removeEndPoint(IClusterEndPoint aClusterEndPoint) {
		mEndPoints.remove(new BasicDBObject().append(ENDPOINT_ID, aClusterEndPoint.getEndPointId()));
	}

	@Override
	public int removeConnectorEndPoints(String aConnectorId) {
		return mEndPoints.remove(new BasicDBObject().append(CONNECTOR_ID, aConnectorId)).getN();
	}

	@Override
	public void setNamespace(String aNamespace) {
		mDocument.put(CLUSTER_NS, aNamespace);
		mClusters.save(mDocument);
	}

	@Override
	public void setPassword(String aPassword) {
		mDocument.put(CLUSTER_PASSWORD, aPassword);
		mClusters.save(mDocument);
	}

	@Override
	public void setAlias(String aAlias) {
		mDocument.put(CLUSTER_ALIAS, aAlias);
		mClusters.save(mDocument);
	}

	@Override
	public String getAlias() {
		return (String) mDocument.get(CLUSTER_ALIAS);
	}

	private Object getTotalEndPointsRequests() {
		long lTotal = 0;
		Iterator<IClusterEndPoint> lIt = getEndPoints();
		while (lIt.hasNext()) {
			lTotal += lIt.next().getRequests();
		}

		return lTotal;
	}

	@Override
	public void setGrantedEndPoints(String aGrantedEndPoints) {
		mDocument.put(GRANTED_ENDPOINTS, aGrantedEndPoints);
		mClusters.save(mDocument);
	}

	@Override
	public String getGrantedEndPoints() {
		return (String) mDocument.get(GRANTED_ENDPOINTS);
	}
}
