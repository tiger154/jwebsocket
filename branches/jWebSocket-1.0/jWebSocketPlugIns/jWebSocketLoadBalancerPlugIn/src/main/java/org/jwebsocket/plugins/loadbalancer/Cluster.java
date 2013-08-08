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

	public String getPassword() {
		return mPassword;
	}

	public void setPassword(String aPassword) {
		mPassword = aPassword;
	}

	/**
	 * @return the mEndpoints
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
	 * @return the mStaticEntries
	 */
	public int getStaticEntries() {
		return mStaticEntries;
	}

	/**
	 * @param mStaticEntries the mStaticEntries to set
	 */
	public void setStaticEntries(int aStaticEntries) {
		this.mStaticEntries = aStaticEntries;
	}

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

	private boolean availableEndPoint() {
		for (int lPos = 0; lPos < 10; lPos++) {
			if (availableEndPoint(lPos)) {
				return true;
			}
		}
		return false;
	}

	private boolean availableEndPoint(int lPos) {
		if (mEndPoints.get(lPos).getStatus().equals(EndPointStatus.ONLINE)) {
			return true;
		} else {
			return false;
		}
	}

	public List<String> getStickyId() {
		List<String> lIDs = new FastList<String>();
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			if (mEndPoints.get(lPos).getStatus().equals(EndPointStatus.ONLINE)) {
				lIDs.add(mEndPoints.get(lPos).getServiceId());
			}
		}
		return lIDs;
	}

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

	public int getEndPointPosition(String aEndPointId) {
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			if (mEndPoints.get(lPos).getServiceId().equals(aEndPointId)) {
				return lPos;
			}
		}
		return -1;
	}

	public List<String> getEndPointsId() {
		List<String> lEndPointsId = new FastList<String>();
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			lEndPointsId.add(mEndPoints.get(lPos).getServiceId());
		}
		return lEndPointsId;
	}

	public ClusterEndPoint getEndPointByPosition(int aPosition) {
		return mEndPoints.get(aPosition);
	}

	/**
	 * @return the mNamespace
	 */
	public String getNamespace() {
		return mNamespace;
	}

	/**
	 * @param mNamespace the mNamespace to set
	 */
	public void setNamespace(String aNamespace) {
		this.mNamespace = aNamespace;
	}

	public long getTotalEndPointsRequests() {
		long lRequests = 0;
		Iterator<ClusterEndPoint> lIt = mEndPoints.iterator();
		while (lIt.hasNext()) {
			lRequests += lIt.next().getRequests();
		}

		return lRequests;
	}
}
