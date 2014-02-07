//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer ClusterEndPoint (Community Edition, CE)
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
package org.jwebsocket.plugins.loadbalancer;

import java.util.UUID;
import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.token.Token;

/**
 * Specifies one single endpoint of a certain cluster.
 *
 * @author aschulze
 * @author rbetancourt
 * @author kyberneees
 */
public class ClusterEndPoint implements ITokenizable {

	/**
	 * Cluster endpoint status, initial value 'OFFLINE'.
	 */
	private EndPointStatus mStatus = EndPointStatus.OFFLINE;
	/**
	 * Cluster endpoint connector.
	 */
	private WebSocketConnector mConnector;
	/**
	 * Cluster endpoint requests, initial value '0'.
	 */
	private long mRequests = 0;
	/**
	 * Cluster endpoint id.
	 */
	private final String mServiceId;
	/**
	 * Cluster endpoint CPU usage, initial value '-1.0'.
	 */
	private double mCpuUsage = -1.0;

	public ClusterEndPoint() {
		// creates an unique service id. 
		mServiceId = UUID.randomUUID().toString();
	}

	public ClusterEndPoint(WebSocketConnector aConnector) {
		this();
		mConnector = aConnector;
	}

	/**
	 * @return the cluster endpoint status.
	 */
	public EndPointStatus getStatus() {
		return mStatus;
	}

	/**
	 * @param aStatus the status to set.
	 */
	public void setStatus(EndPointStatus aStatus) {
		mStatus = aStatus;
	}

	/**
	 * @return the cluster endpoint connector.
	 */
	public WebSocketConnector getConnector() {
		return mConnector;
	}

	/**
	 * @param aConnector the connector to set.
	 */
	public void setConnector(WebSocketConnector aConnector) {
		this.mConnector = aConnector;
	}

	/**
	 *
	 * @return the cluster endpoint id.
	 */
	public String getServiceId() {
		return mServiceId;
	}

	/**
	 * Increase requests for this cluster endpoint.
	 */
	public void increaseRequests() {
		mRequests++;
	}

	/**
	 *
	 * @return cluster endpoint requests.
	 */
	public long getRequests() {
		return mRequests;
	}

	/**
	 *
	 * @param aRequests the requests to set.
	 */
	public void setRequests(int aRequests) {
		this.mRequests = aRequests;
	}

	/**
	 * @return the CPU usage.
	 */
	public double getCpuUsage() {
		return mCpuUsage;
	}

	/**
	 * @param aCpuUsage the CPU usage to set.
	 */
	public void setCpuUsage(double aCpuUsage) {
		this.mCpuUsage = aCpuUsage;
	}

	@Override
	public void writeToToken(Token aToken) {
		aToken.setString("id", getServiceId());
		aToken.setString("status", getStatus().name());
		aToken.setLong("requests", getRequests());
		if (getCpuUsage() != -1) {
			aToken.setDouble("cpu", getCpuUsage());
		}
	}

	@Override
	public void readFromToken(Token aToken) {
	}
}
