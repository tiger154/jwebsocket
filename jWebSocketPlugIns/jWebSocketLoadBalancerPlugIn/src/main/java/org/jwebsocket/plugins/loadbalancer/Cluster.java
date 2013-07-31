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

	private List<ClusterEndPoint> mEndpoints = new FastList<ClusterEndPoint>();
	private String mNamespace;
	private int mStaticEntries;
	private int mEndPointPosition = 0;

	/**
	 * @return the mEndpoints
	 */
	public List<ClusterEndPoint> getEndpoints() {
		return mEndpoints;
	}

	/**
	 * @param mEndpoints the mEndpoints to set
	 */
	public void setEndpoints(List<ClusterEndPoint> aEndpoints) {
		mEndpoints = aEndpoints;
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

	public boolean addEndpoints(WebSocketConnector aConnector) {
		if (!isAlreadyExist(aConnector.getId())) {
			for (int lPos = 0; lPos < mEndpoints.size(); lPos++) {
				if (mEndpoints.get(lPos).getStatus().equals(EndPointStatus.OFFLINE)) {
					mEndpoints.get(lPos).setConnector(aConnector);
					mEndpoints.get(lPos).setStatus(EndPointStatus.ONLINE);
					return true;
				}
			}
			ClusterEndPoint lEndpoint = new ClusterEndPoint();
			lEndpoint.setConnector(aConnector);
			lEndpoint.setStatus(EndPointStatus.ONLINE);
			return mEndpoints.add(lEndpoint);
		} else {
			return false;
		}
	}

	public boolean isAlreadyExist(String aConnectorID) {
		if (mEndpoints.isEmpty()) {
			return false;
		} else {
			for (int lPos = 0; lPos < mEndpoints.size(); lPos++) {
				if (mEndpoints.get(lPos).getConnector() != null) {
					if (mEndpoints.get(lPos).getConnector().getId().equals(aConnectorID)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public ClusterEndPoint getOptimumEndpoint() {
		if (!mEndpoints.isEmpty() && containsAvailableEndpoint()) {
			mEndPointPosition = (mEndPointPosition + 1 < mEndpoints.size()
					? mEndPointPosition + 1 : 0);
			return (availableEndpoint(mEndPointPosition)
					? mEndpoints.get(mEndPointPosition) : getOptimumEndpoint());
		} else {
			return null;
		}
	}

	private boolean containsAvailableEndpoint() {
		for (int lPos = 0; lPos < 10; lPos++) {
			if (availableEndpoint(lPos)) {
				return true;
			}
		}
		return false;
	}

	private boolean availableEndpoint(int lPos) {
		if (mEndpoints.get(lPos).getStatus().equals(EndPointStatus.ONLINE)) {
			return true;
		} else {
			return false;
		}
	}

	public List<String> getStickyId() {
		List<String> lIDs = new FastList<String>();
		for (int lPos = 0; lPos < mEndpoints.size(); lPos++) {
			if (mEndpoints.get(lPos).getStatus().equals(EndPointStatus.ONLINE)) {
				lIDs.add(mEndpoints.get(lPos).getServiceID());
			}
		}
		return lIDs;
	}

	public boolean removeEndpoint(int aEndpointPosition) {
		if (aEndpointPosition != -1) {
			if (aEndpointPosition < mStaticEntries) {
				ClusterEndPoint lEndpoint = mEndpoints.get(aEndpointPosition);
				lEndpoint.setConnector(null);
				lEndpoint.setStatus(EndPointStatus.OFFLINE);
				lEndpoint.setRequests(0);
				return true;
			} else {
				mEndpoints.remove(aEndpointPosition);
				return true;
			}
		} else {
			return false;
		}
	}

	public int getPosition(String aEndpointId) {
		for (int lPos = 0; lPos < mEndpoints.size(); lPos++) {
			if (mEndpoints.get(lPos).getServiceID().equals(aEndpointId)) {
				return lPos;
			}
		}
		return -1;
	}

	public List<EndPointStatus> getEndpointsStatus() {
		List<EndPointStatus> lEndPointStatus = new FastList<EndPointStatus>();
		for (int lPos = 0; lPos < mEndpoints.size(); lPos++) {
			lEndPointStatus.add(mEndpoints.get(lPos).getStatus());
		}
		return lEndPointStatus;
	}

	public List<String> getEndpointsId() {
		List<String> lEndPointsId = new FastList<String>();
		for (int lPos = 0; lPos < mEndpoints.size(); lPos++) {
			lEndPointsId.add(mEndpoints.get(lPos).getServiceID());
		}
		return lEndPointsId;
	}

	public List<Integer> getEndpointsRequests() {
		List<Integer> lEndpointsRequests = new FastList<Integer>();
		for (int lPos = 0; lPos < mEndpoints.size(); lPos++) {
			lEndpointsRequests.add(mEndpoints.get(lPos).getRequests());
		}
		return lEndpointsRequests;
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
