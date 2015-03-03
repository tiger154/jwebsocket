//	---------------------------------------------------------------------------
//	jWebSocket - Cluster interface (Community Edition, CE)
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jwebsocket.api.WebSocketConnector;

/**
 *
 * @author Rolando Santamaria Maso
 */
public interface ICluster {

	/**
	 * @return <code>true</code> if any cluster endpoint have status online; <code>false</code>
	 * otherwise.
	 */
	boolean isEndPointAvailable();

	/**
	 * Verify if endpoints list contains a cluster endpoint with a specific id.
	 *
	 * @param aEndPointId endpoint id.
	 * @return if the endpoints list contains the specified cluster endpoint returns it, but returns
	 * <code>null</code>
	 */
	IClusterEndPoint getEndPoint(String aEndPointId);

	/**
	 * @return a list of endpoints.
	 */
	Iterator<IClusterEndPoint> getEndPoints();

	/**
	 * Gets information about this cluster.
	 *
	 * @return a map with information about this cluster.
	 */
	Map<String, Object> getInfo();

	/**
	 * @return the cluster name space.
	 */
	String getNamespace();

	/**
	 * Gets a balanced cluster endpoint using the least CPU usage algorithm.
	 *
	 * @return optimum cluster endpoint.
	 */
	IClusterEndPoint getOptimumEndPoint();

	/**
	 * Gets a balanced cluster endpoint using both algorithms (round robin & least CPU usage).
	 *
	 * @return optimum cluster endpoint.
	 */
	IClusterEndPoint getOptimumRREndPoint();

	/**
	 * @return cluster password.
	 */
	String getPassword();

	/**
	 * Gets a balanced cluster endpoint using the round robin algorithm.
	 *
	 * @return optimum cluster endpoint or <code>null</code> if endpoints list is empty.
	 */
	IClusterEndPoint getRoundRobinEndPoint();

	/**
	 * Gets all sticky routes in this cluster. A sticky routes is a cluster endpoint with status
	 * online.
	 *
	 * @param aStickyRoutes sticky routes list.
	 */
	void getStickyRoutes(List<Map<String, String>> aStickyRoutes);

	/**
	 * Update the CPU usage to a specific cluster endpoint by the connector id.
	 *
	 * @param aConnectorId cluster endpoint connector.
	 * @param aCpuUsage CPU usage.
	 */
	void updateCpuUsage(String aConnectorId, double aCpuUsage);

	/**
	 * Registers a new cluster endpoint.
	 *
	 * @param aConnector cluster endpoint connector.
	 * @return the cluster endpoint registered.
	 */
	IClusterEndPoint registerEndPoint(WebSocketConnector aConnector);

	/**
	 * Removes the specific cluster endpoint from endpoints list.
	 *
	 * @param aIClusterEndPoint endpoint to be removed.
	 */
	void removeEndPoint(IClusterEndPoint aIClusterEndPoint);

	/**
	 * Removes all cluster end-point by a specific connector id.
	 *
	 * @param aConnectorId the connector id value.
	 * @return the removed endpoints count.
	 */
	int removeConnectorEndPoints(String aConnectorId);

	/**
	 * @param aNamespace
	 */
	void setNamespace(String aNamespace);

	/**
	 * @param aPassword the password to set.
	 */
	void setPassword(String aPassword);

	/**
	 * Set the cluster alias.
	 *
	 * @param aAlias
	 */
	void setAlias(String aAlias);

	/**
	 * Get the cluster alias.
	 *
	 * @return
	 */
	String getAlias();

	/**
	 * Get the comma separated list of granted endpoints identifier. Example:
	 * 'chatAppEndPoint1,chatAppEndPoint2,mailAppEndPoint1'
	 *
	 * @return
	 */
	String getGrantedEndPoints();

	/**
	 * Set the comma separated list of granted endpoints identifier. Example:
	 * 'chatAppEndPoint1,chatAppEndPoint2,mailAppEndPoint1'
	 *
	 * @param aGrantedEndPoints
	 */
	void setGrantedEndPoints(String aGrantedEndPoints);
}
