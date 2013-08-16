//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer ClusterEndPoint (Community Edition, CE)
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

	private EndPointStatus mStatus = EndPointStatus.OFFLINE;
	private WebSocketConnector mConnector;
	private long mRequests = 0;
	private final String mServiceId;

	public ClusterEndPoint() {
		mServiceId = UUID.randomUUID().toString();
	}

	public ClusterEndPoint(WebSocketConnector aConnector) {
		this();
		mConnector = aConnector;
	}

	/**
	 * @return the status.
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

	@Override
	public void writeToToken(Token aToken) {
		aToken.setString("id", getServiceId());
		aToken.setString("status", getStatus().name());
		aToken.setLong("requests", getRequests());
	}

	@Override
	public void readFromToken(Token aToken) {
	}
}
