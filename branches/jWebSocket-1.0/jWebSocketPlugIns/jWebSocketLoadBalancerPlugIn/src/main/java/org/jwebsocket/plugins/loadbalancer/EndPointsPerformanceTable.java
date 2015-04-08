//	---------------------------------------------------------------------------
//	jWebSocket Load Balancer EndPointsPerformanceTable (Community Edition, CE)
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * Contains the endpoints custom information for LB algorithms 4 and 5.
 *
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 */
public class EndPointsPerformanceTable {

	private double mDefaultPerformanceFactor = 1.0;
	private final Map<String, EndPointPerformanceInfo> mEndPoints = new FastMap<String, EndPointPerformanceInfo>().shared();
	private final List<PriorityGroup> mPriorityGroups = new FastList<PriorityGroup>();

	public double getDefaultPerformanceFactor() {
		return mDefaultPerformanceFactor;
	}

	public void setDefaultPerformanceFactor(double aDefaultPerformanceFactor) {
		mDefaultPerformanceFactor = aDefaultPerformanceFactor;
	}

	public List<EndPointPerformanceInfo> getEndPoints() {
		return new ArrayList<EndPointPerformanceInfo>(mEndPoints.values());
	}

	public void setEndPoints(List<EndPointPerformanceInfo> aEndPoints) {
		Set<PriorityGroup> lPGs = new HashSet<PriorityGroup>();
		for (Iterator<EndPointPerformanceInfo> lIt = aEndPoints.iterator(); lIt.hasNext();) {
			EndPointPerformanceInfo lEndPointInfo = lIt.next();
			mEndPoints.put(lEndPointInfo.getEndPointId(), lEndPointInfo);

			if (null != lEndPointInfo.getPriorityGroup()) {
				lPGs.add(lEndPointInfo.getPriorityGroup());
			}
		}

		PriorityQueue<PriorityGroup> lPQ = new PriorityQueue<PriorityGroup>();
		for (PriorityGroup lPG : lPGs) {
			lPQ.add(lPG);
		}
		while (!lPQ.isEmpty()) {
			mPriorityGroups.add(lPQ.poll());
		}
	}

	public double getEndPointPerformanceFactor(String aEndPointId) {
		if (mEndPoints.containsKey(aEndPointId)) {
			return mEndPoints.get(aEndPointId).getPerformanceFactor();
		}

		return getDefaultPerformanceFactor();
	}

	public List<PriorityGroup> getPriorityGroups() {
		return mPriorityGroups;
	}
}
