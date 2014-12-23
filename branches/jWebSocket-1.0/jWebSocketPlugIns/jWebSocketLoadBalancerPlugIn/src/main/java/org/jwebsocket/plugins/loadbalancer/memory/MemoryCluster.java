//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer MemoryCluster (Community Edition, CE)
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
package org.jwebsocket.plugins.loadbalancer.memory;

import org.jwebsocket.plugins.loadbalancer.api.ICluster;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.plugins.loadbalancer.EndPointStatus;
import org.jwebsocket.plugins.loadbalancer.api.Attributes;
import org.jwebsocket.plugins.loadbalancer.api.IClusterEndPoint;

/**
 * Manages the list of end points per cluster.
 *
 * @author Alexander Schulze
 * @author Rolando Betancourt Toucet
 * @author Rolando Santamaria Maso
 */
public class MemoryCluster implements ICluster {

	/**
	 * List of endpoints.
	 */
	private List<IClusterEndPoint> mEndPoints = new FastList<IClusterEndPoint>();
	/**
	 * MemoryCluster name space.
	 */
	private String mNamespace;
	/**
	 * Password required for add a new cluster endpoint.
	 */
	private String mPassword;
	/**
	 * MemoryCluster endpoint position used in load balancer algorithms.
	 */
	private int mEndPointPosition = -1;

	/**
	 * Cluster alias.
	 */
	private String mAlias;

	private String mGrantedEndPoints;

	@Override
	public String getGrantedEndPoints() {
		return mGrantedEndPoints;
	}

	@Override
	public void setGrantedEndPoints(String aGrantedEndPoints) {
		mGrantedEndPoints = aGrantedEndPoints;
	}

	@Override
	public String getAlias() {
		return mAlias;
	}

	@Override
	public void setAlias(String aAlias) {
		mAlias = aAlias;
	}

	@Override
	public Iterator<IClusterEndPoint> getEndPoints() {
		return mEndPoints.iterator();
	}

	@Override
	public String getNamespace() {
		return mNamespace;
	}

	@Override
	public void setNamespace(String aNamespace) {
		this.mNamespace = aNamespace;
	}

	@Override
	public String getPassword() {
		return mPassword;
	}

	@Override
	public void setPassword(String aPassword) {
		mPassword = aPassword;
	}

	@Override
	public IClusterEndPoint registerEndPoint(WebSocketConnector aConnector) {
		// creates a new cluster endpoint with the target connector. 
		MemoryClusterEndPoint lEndPoint = new MemoryClusterEndPoint(aConnector);
		lEndPoint.setClientRuntimePlatform((String) aConnector.getVar("jwsType"));

		return (mEndPoints.add(lEndPoint) ? lEndPoint : null);
	}

	@Override
	public void removeEndPoint(IClusterEndPoint aClusterEndPoint) {
		int lEndPointPosition = mEndPoints.indexOf(aClusterEndPoint);

		// remove the cluster endpoint from the list.
		mEndPoints.remove(lEndPointPosition);
	}

	@Override
	public int removeConnectorEndPoints(String aConnectorId) {
		int lCount = 0;
		Iterator<IClusterEndPoint> lIt = mEndPoints.iterator();
		while (lIt.hasNext()) {
			IClusterEndPoint lEndPoint = lIt.next();

			// if the argument is equal to cluster endpoint connector, remove it.
			if (aConnectorId.equals(lEndPoint.getConnectorId())) {
				removeEndPoint(lEndPoint);
				lCount++;
			}
		}
		return lCount;
	}

	@Override
	public Map<String, Object> getInfo() {
		Map<String, Object> lInfoCluster = new HashMap<String, Object>();
		lInfoCluster.put(Attributes.CLUSTER_ALIAS, getAlias());
		lInfoCluster.put(Attributes.CLUSTER_NS, getNamespace());
		lInfoCluster.put(Attributes.ENDPOINTS_COUNT, mEndPoints.size());
		lInfoCluster.put(Attributes.ENDPOINTS, mEndPoints);
		lInfoCluster.put(Attributes.REQUESTS, getTotalEndPointsRequests());

		return lInfoCluster;
	}

	@Override
	public void getStickyRoutes(List<Map<String, String>> aStickyRoutes) {
		Map<String, String> lInfoCluster;
		List<String> lIDs = getStickyIds();
		for (int lPos = 0; lPos < lIDs.size(); lPos++) {
			lInfoCluster = new FastMap<String, String>();
			lInfoCluster.put(Attributes.CLUSTER_ALIAS, getAlias());
			lInfoCluster.put(Attributes.ENDPOINT_ID, lIDs.get(lPos));

			aStickyRoutes.add(lInfoCluster);
		}
	}

	@Override
	public IClusterEndPoint getRoundRobinEndPoint() {
		// determine the cluster endpoint position to be returned. 
		mEndPointPosition = (mEndPointPosition + 1 < mEndPoints.size()
				? mEndPointPosition + 1 : 0);

		// if the cluster endpoint position is valid then return it,
		// but repeat this method.
		return (availableEndPoint(mEndPointPosition)
				? mEndPoints.get(mEndPointPosition) : getRoundRobinEndPoint());
	}

	@Override
	public IClusterEndPoint getOptimumEndPoint() {
		double lLeastCpuUsage = Double.MAX_VALUE;
		int lEndPointPos = -1;
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			IClusterEndPoint lClusterEndPoint = mEndPoints.get(lPos);
			if (lClusterEndPoint.getStatus().equals(EndPointStatus.ONLINE)) {
				double lTempCpuUsage = lClusterEndPoint.getCpuUsage();

				// discard all java script clients because they can't update the CPU usage.
				if (!lClusterEndPoint.getClientRuntimePlatform().equals(Attributes.JAVASCRIPT_RUNTIME_PLATFORM)
						&& lTempCpuUsage < lLeastCpuUsage) {
					lLeastCpuUsage = lTempCpuUsage;
					lEndPointPos = lPos;
				}
			}
		}
		return (lEndPointPos == -1 ? null : mEndPoints.get(lEndPointPos));
	}

	@Override
	public IClusterEndPoint getOptimumRREndPoint() {
		IClusterEndPoint lTempClusterEndPoint = getRoundRobinEndPoint();
		String lRuntimePlatform = lTempClusterEndPoint.getClientRuntimePlatform();

		if (null == lRuntimePlatform) {
			return lTempClusterEndPoint;
		} else {
			// if 'MemoryClusterEndPoint' is javascript client executes round robin algorithm,
			// but executes least CPU usage algorithm (with CPU usage).
			if (lRuntimePlatform.equals(Attributes.JAVASCRIPT_RUNTIME_PLATFORM)) {
				return lTempClusterEndPoint;
			} else {
				return getOptimumEndPoint();
			}
		}
	}

	@Override
	public void updateCpuUsage(String aConnectorId, double aCpuUsage) {
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			IClusterEndPoint lTempEndPoint = mEndPoints.get(lPos);
			if (lTempEndPoint.getStatus().equals(EndPointStatus.ONLINE)) {
				if (lTempEndPoint.getConnectorId().equals(aConnectorId)) {
					lTempEndPoint.setCpuUsage(aCpuUsage);
				}
			}
		}
	}

	@Override
	public IClusterEndPoint getEndPoint(String aEndPointId) {
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			IClusterEndPoint lClusterEndPoint = mEndPoints.get(lPos);
			if (lClusterEndPoint.getEndPointId().equals(aEndPointId)) {
				return lClusterEndPoint;
			}
		}
		return null;
	}

	@Override
	public boolean isEndPointAvailable() {
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			if (availableEndPoint(lPos)) {
				return true;
			}
		}
		return false;
	}

	private boolean availableEndPoint(int lPos) {
		return mEndPoints.get(lPos).getStatus().equals(EndPointStatus.ONLINE);
	}

	private List<String> getStickyIds() {
		List<String> lIDs = new FastList<String>();
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			if (mEndPoints.get(lPos).getStatus().equals(EndPointStatus.ONLINE)) {
				lIDs.add(mEndPoints.get(lPos).getEndPointId());
			}
		}

		return lIDs;
	}

	private long getTotalEndPointsRequests() {
		long lRequests = 0;
		Iterator<IClusterEndPoint> lIt = mEndPoints.iterator();
		while (lIt.hasNext()) {
			lRequests += lIt.next().getRequests();
		}

		return lRequests;
	}
}
