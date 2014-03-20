//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer EndPointInfo (Community Edition, CE)
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

/**
 * Provide the allowed options for the status of an end point within a cluster.
 *
 * @author Alexander Schulze
 */
public enum EndPointStatus {

	/**
	 * disconnected and unavailable
	 */
	OFFLINE,
	/**
	 * connected and available
	 */
	ONLINE,
	/**
	 * end point to be shutdown, do not accept new clients
	 */
	SHUTTING_DOWN
}
