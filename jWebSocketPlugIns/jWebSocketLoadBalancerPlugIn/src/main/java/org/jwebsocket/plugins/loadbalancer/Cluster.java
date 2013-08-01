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

import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.api.WebSocketConnector;

/**
 * Manages the list of end points per cluster.
 *
 * @author aschulze
 * @author rbetancourt
 */
public class Cluster {

	private List<ClusterEndPoint> mEndPoints = new FastList<ClusterEndPoint>();
	private String mNamespace;
	private int mStaticEntries;
	private int mEndPointPosition = 0;

	/**
	 * @return the mEndpoints
	 */
	public List<ClusterEndPoint> getEndpoints() {
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

	public boolean addEndPoints(WebSocketConnector aConnector) {
		if (!endPointExists(aConnector.getId())) {
			for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
				if (mEndPoints.get(lPos).getStatus().equals(EndPointStatus.OFFLINE)) {
					mEndPoints.get(lPos).setConnector(aConnector);
					mEndPoints.get(lPos).setStatus(EndPointStatus.ONLINE);
					return true;
				}
			}
			ClusterEndPoint lEndPoint = new ClusterEndPoint();
			lEndPoint.setConnector(aConnector);
			lEndPoint.setStatus(EndPointStatus.ONLINE);
			return mEndPoints.add(lEndPoint);
		} else {
			return false;
		}
	}

	public boolean endPointExists(String aConnectorID) {
		if (mEndPoints.isEmpty()) {
			return false;
		} else {
			for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
				if (mEndPoints.get(lPos).getConnector() != null) {
					if (mEndPoints.get(lPos).getConnector().getId().equals(aConnectorID)) {
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
				lIDs.add(mEndPoints.get(lPos).getServiceID());
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

	public int getPosition(String aEndPointId) {
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			if (mEndPoints.get(lPos).getServiceID().equals(aEndPointId)) {
				return lPos;
			}
		}
		return -1;
	}

	public List<EndPointStatus> getEndPointsStatus() {
		List<EndPointStatus> lEndPointStatus = new FastList<EndPointStatus>();
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			lEndPointStatus.add(mEndPoints.get(lPos).getStatus());
		}
		return lEndPointStatus;
	}

	public List<String> getEndPointsId() {
		List<String> lEndPointsId = new FastList<String>();
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			lEndPointsId.add(mEndPoints.get(lPos).getServiceID());
		}
		return lEndPointsId;
	}

	public List<Integer> getEndPointsRequests() {
		List<Integer> lEndPointsRequests = new FastList<Integer>();
		for (int lPos = 0; lPos < mEndPoints.size(); lPos++) {
			lEndPointsRequests.add(mEndPoints.get(lPos).getRequests());
		}
		return lEndPointsRequests;
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
}
