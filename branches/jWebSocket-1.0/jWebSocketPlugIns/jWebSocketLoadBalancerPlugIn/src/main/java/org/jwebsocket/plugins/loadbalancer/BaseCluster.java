//	---------------------------------------------------------------------------
//	jWebSocket BaseCluster class for LBP (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2015 Innotrade GmbH (jWebSocket.org)
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
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import org.jwebsocket.plugins.loadbalancer.api.ICluster;
import org.jwebsocket.plugins.loadbalancer.api.IClusterEndPoint;

/**
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 */
public abstract class BaseCluster implements ICluster {

	@Override
	public IClusterEndPoint getGroupQuickerEndPoint(final EndPointsPerformanceTable aPI) {
		List<PriorityGroup> lPGs = aPI.getPriorityGroups();
		// ordering best candidate endpoint with a prority queue
		final Comparator<IClusterEndPoint> lComparator = new Comparator<IClusterEndPoint>() {

			@Override
			public int compare(IClusterEndPoint lEP1, IClusterEndPoint lEP2) {
				double lEP1Cpu = lEP1.getCpuUsage() / aPI.getEndPointPerformanceFactor(lEP1.getConnectorId());
				double lEP2Cpu = lEP2.getCpuUsage() / aPI.getEndPointPerformanceFactor(lEP2.getConnectorId());

				if (lEP1Cpu < lEP2Cpu) {
					return -1;
				} else if (lEP1Cpu > lEP2Cpu) {
					return 1;
				} else {
					return 0;
				}
			}
		};

		IClusterEndPoint lAvailable = null;
		for (Iterator<PriorityGroup> it = lPGs.iterator(); it.hasNext();) {
			PriorityGroup lPG = it.next();
			PriorityQueue<IClusterEndPoint> lEndPointsPQ = new PriorityQueue<IClusterEndPoint>(lPG.getEndPointsInfo().size(), lComparator);
			// inserting endpoints into priority queue to order them
			for (EndPointPerformanceInfo lEPI : lPG.getEndPointsInfo()) {
				IClusterEndPoint lEP = getEndPointByConnectorId(lEPI.getEndPointId());
				if (null != lEP) {
					lEndPointsPQ.add(lEP);
				}
			}
			// getting group best candidate if exists
			if (!lEndPointsPQ.isEmpty()) {
				IClusterEndPoint lEP = lEndPointsPQ.poll();
				lAvailable = lEP;
				if (lEP.getCpuUsage() / aPI.getEndPointPerformanceFactor(lEP.getEndPointId()) <= lPG.getThreshold()) {
					return lEP;
				}
			}
		}

		// non endpoint satisfy the requirement
		return lAvailable;
	}

	@Override
	public IClusterEndPoint getEndPointByConnectorId(String aConnectorId) {
		Iterator<IClusterEndPoint> lIt = getEndPoints();
		while (lIt.hasNext()) {
			IClusterEndPoint lEP = lIt.next();
			if (lEP.getConnectorId().equals(aConnectorId)) {
				return lEP;
			}
		}

		return null;
	}
}
