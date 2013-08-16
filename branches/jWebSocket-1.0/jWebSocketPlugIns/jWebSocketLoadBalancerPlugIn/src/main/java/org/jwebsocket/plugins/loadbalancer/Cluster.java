//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer Cluster (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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
package org.jwebsocket.plugins.loadbalancer;

import java.util.Iterator;
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.api.WebSocketConnector;

/**
 * Manages the list of end points per cluster.
 *
 * @author aschulze
 * @author rbetancourt
 * @author kyberneees
 */
public class Cluster {

	private List<ClusterEndPoint> mEndPoints = new FastList<ClusterEndPoint>();
	private String mNamespace;
	private int mStaticEntries;
	private int mEndPointPosition = 0;
	private String mPassword;

	/**
	 *
	 * @return cluster password.
	 */
	public String getPassword() {
		return mPassword;
	}

	/**
	 *
	 * @param aPassword the password to set.
	 */
	public void setPassword(String aPassword) {
		mPassword = aPassword;
	}

	/**
	 *
	 * @return a list of endpoints.
	 */
	public List<ClusterEndPoint> getEndPoints() {
		return mEndPoints;
	}

	/**
	 *
	 * @param aEndPoints
	 */
	public void setEndPoints(List<ClusterEndPoint> aEndPoints) {
		mEndPoints = aEndPoints;
	}

	/**
	 * @return the static entries
	 */
	public int getStaticEntries() {
		return mStaticEntries;
	}

	/**
	 * @param mStaticEntries the static entries to set
	 */
	public void setStaticEntries(int aStaticEntries) {
		this.mStaticEntries = aStaticEntries;
	}

	/**
	 * Register a new cluster endpoint.
	 *
	 * @param aConnector
	 * @return the cluster endpoint added.
	 */
	public ClusterEndPoint registerEndPoint(WebSocketConnector aConnector) {
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			if (mEndPoints.get(lPos).getStatus().equals(EndPointStatus.OFFLINE)) {
				mEndPoints.get(lPos).setConnector(aConnector);
				mEndPoints.get(lPos).setStatus(EndPointStatus.ONLINE);

				return mEndPoints.get(lPos);
			}
		}

		ClusterEndPoint lEndPoint = new ClusterEndPoint(aConnector);
		lEndPoint.setStatus(EndPointStatus.ONLINE);
		mEndPoints.add(lEndPoint);

		return lEndPoint;
	}

	/**
	 *
	 * @param aServiceId Service Id.
	 * @return <code>true</code> if endpoint list contains aServiceId;
	 * <code>false</code> otherwise.
	 */
	public boolean endPointExists(String aServiceId) {
		if (mEndPoints.isEmpty()) {
			return false;
		} else {
			for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
				if (mEndPoints.get(lPos).getConnector() != null) {
					if (mEndPoints.get(lPos).getServiceId().equals(aServiceId)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 *
	 * @return optimum cluster endpoint.
	 */
	public ClusterEndPoint getOptimumEndPoint() {
		if (!mEndPoints.isEmpty() && availableEndPoint()) {
			mEndPointPosition = (mEndPointPosition + 1 < mEndPoints.size()
				? mEndPointPosition + 1 : 0);
			return (availableEndPoint(mEndPointPosition)
				? mEndPoints.get(mEndPointPosition) : getOptimumEndPoint());
		} else {
			return null;
		}
	}

	/**
	 *
	 * @return <code>true</code> if any cluster endpoint have status online;
	 * <code>false</code> otherwise.
	 */
	private boolean availableEndPoint() {
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			if (availableEndPoint(lPos)) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * @param lPos position in cluster endpoint list.
	 * @return <code>true</code> if any cluster endpoint have status online;
	 * <code>false</code> otherwise.
	 */
	private boolean availableEndPoint(int lPos) {
		if (mEndPoints.get(lPos).getStatus().equals(EndPointStatus.ONLINE)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 *
	 * @return a list with all sticky id.
	 */
	public List<String> getStickyId() {
		List<String> lIDs = new FastList<String>();
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			if (mEndPoints.get(lPos).getStatus().equals(EndPointStatus.ONLINE)) {
				lIDs.add(mEndPoints.get(lPos).getServiceId());
			}
		}
		return lIDs;
	}

	/**
	 *
	 * @param aEndPointPosition
	 * @return <code>true</code> if the cluster endpoint was removed successfully;
	 * <code>false</code> otherwise.
	 */
	public boolean removeEndPoint(int aEndPointPosition) {
		if (aEndPointPosition != -1) {
			if (aEndPointPosition < mStaticEntries) {
				ClusterEndPoint lEndPoint = mEndPoints.get(aEndPointPosition);
				lEndPoint.setConnector(null);
				lEndPoint.setStatus(EndPointStatus.OFFLINE);
				lEndPoint.setRequests(0);
				return true;
			} else {
				mEndPoints.remove(aEndPointPosition);
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 *
	 * @param aConnector
	 * @return the count of cluster endpoints removed.
	 */
	public int removeEndPointsByConnector(WebSocketConnector aConnector) {
		int lCount = 0;
		Iterator<ClusterEndPoint> lIt = mEndPoints.iterator();
		while (lIt.hasNext()) {
			ClusterEndPoint lEndPoint = lIt.next();
			if (aConnector.equals(lEndPoint.getConnector())) {
				removeEndPoint(getEndPointPosition(lEndPoint.getServiceId()));
				lCount++;
			}
		}

		return lCount;
	}

	/**
	 *
	 * @param aEndPointId
	 * @return the position where the endpoint id coincides.
	 */
	public int getEndPointPosition(String aEndPointId) {
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			if (mEndPoints.get(lPos).getServiceId().equals(aEndPointId)) {
				return lPos;
			}
		}
		return -1;
	}

	/**
	 *
	 * @return a list with all endpoints id.
	 */
	public List<String> getEndPointsId() {
		List<String> lEndPointsId = new FastList<String>();
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			lEndPointsId.add(mEndPoints.get(lPos).getServiceId());
		}
		return lEndPointsId;
	}

	/**
	 *
	 * @param aPosition
	 * @return an specific cluster endpoint by the position given.
	 */
	public ClusterEndPoint getEndPointByPosition(int aPosition) {
		return mEndPoints.get(aPosition);
	}

	/**
	 * @return the cluster name space
	 */
	public String getNamespace() {
		return mNamespace;
	}

	/**
	 * @param mNamespace the name space to set
	 */
	public void setNamespace(String aNamespace) {
		this.mNamespace = aNamespace;
	}

	/**
	 *
	 * @return total of endpoints requests. 
	 */
	public long getTotalEndPointsRequests() {
		long lRequests = 0;
		Iterator<ClusterEndPoint> lIt = mEndPoints.iterator();
		while (lIt.hasNext()) {
			lRequests += lIt.next().getRequests();
		}

		return lRequests;
	}
}
