//	---------------------------------------------------------------------------
//	jWebSocket BaseCluster class for LBP (Community Edition, CE)
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

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import org.jwebsocket.plugins.loadbalancer.api.ICluster;
import org.jwebsocket.plugins.loadbalancer.api.IClusterEndPoint;

/**
 *
 * @author Rolando Santamaria Maso
 */
public abstract class BaseCluster implements ICluster {

	@Override
	public IClusterEndPoint getGroupQuickerEndPoint(final EndPointsPerformanceTable aPI) {
		List<PriorityGroup> lPGs = aPI.getPriorityGroups();
		// ordering best candidate endpoint with a prority queue
		PriorityQueue<IClusterEndPoint> lEndPointsPQ = new PriorityQueue<IClusterEndPoint>(
				new Comparator<IClusterEndPoint>() {

					@Override
					public int compare(IClusterEndPoint lEP1, IClusterEndPoint lEP2) {
						double lEP1Cpu = lEP1.getCpuUsage() / aPI.getEndPointPowerFactor(lEP1.getEndPointId());
						double lEP2Cpu = lEP2.getCpuUsage() / aPI.getEndPointPowerFactor(lEP2.getEndPointId());

						if (lEP1Cpu < lEP2Cpu) {
							return -1;
						} else if (lEP1Cpu > lEP2Cpu) {
							return 1;
						} else {
							return 0;
						}
					}
				});
		// getting ordered priority groups endpoints
		for (PriorityGroup lPG : lPGs) {
			for (EndPointPerformanceInfo lEPI : lPG.getEndPointsInfo()) {
				IClusterEndPoint lEP = getEndPoint(lEPI.getId());
				if (null != lEP) {
					lEndPointsPQ.add(lEP);
				}
			}

			if (!lEndPointsPQ.isEmpty()) {
				IClusterEndPoint lEP = lEndPointsPQ.poll();
				if (lEP.getCpuUsage() / aPI.getEndPointPowerFactor(lEP.getEndPointId()) <= lPG.getThreshold()) {
					return lEP;
				}
			}
		}

		// non endpoint satisfy the requirement
		return null;
	}
}
