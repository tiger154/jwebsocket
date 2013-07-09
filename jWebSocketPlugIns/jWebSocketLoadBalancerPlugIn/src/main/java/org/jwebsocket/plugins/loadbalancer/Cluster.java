//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer Cluster (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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

import java.util.Set;
import javolution.util.FastSet;

/**
 * Manages the list of end points per cluster.
 *
 * @author aschulze
 */
public class Cluster {

	private Set<ClusterEndPoint> mEndpoints = new FastSet<ClusterEndPoint>();

	/**
	 * @return the mEndpoints
	 */
	public Set<ClusterEndPoint> getEndpoints() {
		return mEndpoints;
	}

	/**
	 * @param mEndpoints the mEndpoints to set
	 */
	public void setEndpoints(Set<ClusterEndPoint> aEndpoints) {
		mEndpoints = aEndpoints;
	}
}
