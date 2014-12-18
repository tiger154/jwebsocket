//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer MemoryClusterEndPoint (Community Edition, CE)
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

import java.util.UUID;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.plugins.loadbalancer.EndPointStatus;
import org.jwebsocket.plugins.loadbalancer.api.BaseClusterEndPoint;

/**
 * Specifies one single endpoint of a certain cluster.
 *
 * @author Alexander Schulze
 * @author Rolando Betancourt Toucet
 * @author Rolando Santamaria Maso
 */
public class MemoryClusterEndPoint extends BaseClusterEndPoint {

	/**
	 * Cluster endpoint status, default value 'ONLINE'.
	 */
	private EndPointStatus mStatus = EndPointStatus.ONLINE;
	/**
	 * Cluster endpoint connector.
	 */
	private String mConnectorId;
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

	private String mClientRuntimePlatform;

	public MemoryClusterEndPoint(WebSocketConnector aConnector) {
		// creates an unique service id. 
		mServiceId = UUID.randomUUID().toString();
		mConnectorId = aConnector.getId();
	}

	@Override
	public String getClientRuntimePlatform() {
		return mClientRuntimePlatform;
	}

	@Override
	public void setClientRuntimePlatform(String aPlatform) {
		mClientRuntimePlatform = aPlatform;
	}

	@Override
	public EndPointStatus getStatus() {
		return mStatus;
	}

	@Override
	public void setStatus(EndPointStatus aStatus) {
		mStatus = aStatus;
	}

	@Override
	public String getConnectorId() {
		return mConnectorId;
	}

	@Override
	public String getEndPointId() {
		return mServiceId;
	}

	@Override
	public void increaseRequests() {
		mRequests++;
	}

	@Override
	public long getRequests() {
		return mRequests;
	}

	@Override
	public double getCpuUsage() {
		return mCpuUsage;
	}

	@Override
	public void setCpuUsage(double aCpuUsage) {
		this.mCpuUsage = aCpuUsage;
	}
}
