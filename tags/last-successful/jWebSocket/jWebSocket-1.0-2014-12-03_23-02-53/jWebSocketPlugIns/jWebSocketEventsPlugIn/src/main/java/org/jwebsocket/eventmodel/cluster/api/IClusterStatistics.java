//	---------------------------------------------------------------------------
//	jWebSocket - IClusterStatistics (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.eventmodel.cluster.api;

/**
 *
 * @author Rolando Santamaria Maso
 */
public interface IClusterStatistics {

	/**
	 *
	 * @return The max concurrent connections number supported by the cluster
	 */
	Integer getMaxConnectionsSupported();

	/**
	 *
	 * @return The cluster load per cent
	 */
	Integer getLoadPerCent();

	/**
	 *
	 * @return The cluster current concurrent connections
	 */
	Integer getCurrentConnections();

	/**
	 *
	 * @return The cluster CPU usage
	 */
	Integer getCpuUsagePerCent();
}
