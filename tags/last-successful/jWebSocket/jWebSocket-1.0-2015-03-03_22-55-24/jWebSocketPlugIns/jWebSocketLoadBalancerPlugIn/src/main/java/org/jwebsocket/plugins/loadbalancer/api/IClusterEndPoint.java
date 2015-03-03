//	---------------------------------------------------------------------------
//	jWebSocket - ClusterEndPoint interface (Community Edition, CE)
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
package org.jwebsocket.plugins.loadbalancer.api;

import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.plugins.loadbalancer.EndPointStatus;

/**
 *
 * @author Rolando Santamaria Maso
 */
public interface IClusterEndPoint extends ITokenizable {

	/**
	 * @return the cluster endpoint connector id.
	 */
	String getConnectorId();

	/**
	 * @return the CPU usage.
	 */
	double getCpuUsage();

	/**
	 *
	 * @return cluster endpoint requests.
	 */
	long getRequests();

	/**
	 *
	 * @return the cluster endpoint id.
	 */
	String getEndPointId();

	/**
	 * @return the cluster endpoint status.
	 */
	EndPointStatus getStatus();

	/**
	 * Increase requests for this cluster endpoint.
	 */
	void increaseRequests();

	/**
	 * @param aCpuUsage the CPU usage to set.
	 */
	void setCpuUsage(double aCpuUsage);

	/**
	 * @param aStatus the status to set.
	 */
	void setStatus(EndPointStatus aStatus);

	/**
	 * Get the client runtime platform. Example: javascript
	 *
	 * @return
	 */
	String getClientRuntimePlatform();

	/**
	 * Set the client runtime platform.
	 *
	 * @param aPlatform
	 */
	void setClientRuntimePlatform(String aPlatform);
}
