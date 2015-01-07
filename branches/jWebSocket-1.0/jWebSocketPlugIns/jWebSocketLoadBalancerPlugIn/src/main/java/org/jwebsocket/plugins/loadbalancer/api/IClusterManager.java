//	---------------------------------------------------------------------------
//	jWebSocket - Cluster Manager (Community Edition, CE)
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

/**
 *
 * @author Rolando Santamaria Maso
 */
public interface IClusterManager {

	/**
	 * Get all clusters iterator.
	 *
	 * @return
	 */
	Iterator<ICluster> getClusters();

	/**
	 * Get all clusters basic information.
	 *
	 * @return
	 */
	List<Map<String, Object>> getClustersInfo();

	/**
	 * Get all sticky routes managed by the load balancer, consisting of
	 * cluster-alias, client endpoint-id, service endpoint-id.
	 *
	 * @return
	 */
	List<Map<String, String>> getStickyRoutes();

	/**
	 * Get cluster by alias.
	 *
	 * @param aAlias
	 * @return
	 */
	ICluster getClusterByAlias(String aAlias);

	/**
	 * Set the load balancer algorithm.
	 * <br>
	 * <code>
	 *  As we know, in the load balancer, the client applications will functions as services.
	 *	Until now these services only can to be Web client (JavaScript) or Java client (java).
	 *	As consequence of this, it have implemented 3 types of algorithms differently for load
	 *	balancing considering the type of service (Web or Java) .The algorithm "1" to behave as
	 *	a simple round robin. This algorithm will work with both types of services. For example:
	 *
	 *							Algorithm Round Robin ( value = 1 )
	 *
	 *	Requests   R1   R2   R3   R1   R2   R3
	 *			  _____________________________
	 *	Services | Web|Java| Web|Java|Web |Java|
	 *			 |____|____|____|____|____|____|
	 *
	 *
	 *	The algorithm "2" was implemented only for java services; if all services are java clients
	 *	this is the perfect choice, because this algorithm uses the “sigar” library to get the cpu usage
	 *	in each online service. In this way we can achieve load balance more accurately. For example:
	 *
	 *						   Algorithm Least CPU Usage ( value = 2 )
	 *
	 *	Requests    R2   R6   R3        R4        R5   R1   R7
	 *			   ______________________________________________________
	 *	Services  |Java|Java|Java|Web |Java|Web |Java|Java|Java|Web |Web |
	 *	CPU Usage |24%_|90%_|32%_|____|45%_|____|60%_|15%_|100%|____|____|
	 *
	 *
	 *	The algorithm "3" is a mixture of the previous algorithms, this algorithm is appropriate when
	 *	services were created using both types of clients (Web and Java). When the service selected
	 *	is (Web) it executes the round robin algorithm and when the service selected is (Java)
	 *	it execute the least cpu usage algorithm. For example:
	 *
	 *						  Algorithm Optimum Balance ( value = 3 )
	 *
	 *	Requests    R1   R8   R3   R4   R5   R6   R7   R2   R1   R2   R3
	 *			   ______________________________________________________
	 *	Services  |Web |Java|Java|Web |Java|Web |Java|Java|Web |Web |Web |
	 *	CPU Usage |____|90%_|32%_|____|45%_|____|60%_|15%_|____|____|____|
	 * </code>
	 *
	 * @param aAlgorithm
	 */
	void setBalancerAlgorithm(Integer aAlgorithm);

	/**
	 * Get the load balancer algorithm.
	 *
	 * @see setBalancerAlgorithm
	 * @return
	 */
	Integer getBalancerAlgorithm();

	/**
	 * Get a cluster by it name-space value.
	 *
	 * @param aNS
	 * @return
	 */
	ICluster getClusterByNamespace(String aNS);

	/**
	 * Update end-points CPU usage if matches the given connector id.
	 *
	 * @param aConnectorId
	 * @param aCpuUsage
	 */
	void updateCpuUsage(String aConnectorId, double aCpuUsage);

	/**
	 * Return TRUE if the given name-space matches an existing cluster
	 * name-space, FALSE otherwise.
	 *
	 * @param aNS
	 * @return
	 */
	boolean isNamespaceSupported(String aNS);

	/**
	 * Get the optimum end-point from the cluster that matches the given
	 * name-space value. The load balancer algorithm value is used.
	 *
	 * @param aNS
	 * @return
	 */
	IClusterEndPoint getOptimumServiceEndPoint(String aNS);
	
	/**
	 * Get the optimum end-point from a given cluster instance. The load balancer algorithm value is used.
	 *
	 * @param aCluster
	 * @return
	 */
	IClusterEndPoint getOptimumServiceEndPoint(ICluster aCluster);

	/**
	 * Remove all end-points were the client connector matches the given
	 * connector id.
	 *
	 * @param aConnectorId
	 * @return The removed end-points count.
	 */
	int removeConnectorEndPoints(String aConnectorId);
}
