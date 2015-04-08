/*
 * Copyright 2015 kyberneees.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jwebsocket.plugins.loadbalancer;

import java.util.Set;
import javolution.util.FastSet;

/**
 *
 * @author kyberneees
 */
public class PriorityGroup implements Comparable<PriorityGroup> {

	private String mName, mDescription;
	private double mThreshold = 50.0;
	private final Set<EndPointPerformanceInfo> mEndPointsInfo = new FastSet<EndPointPerformanceInfo>();

	public PriorityGroup() {
	}

	public String getName() {
		return mName;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String aDescription) {
		this.mDescription = aDescription;
	}

	public void setName(String aName) {
		mName = aName;
	}

	public double getThreshold() {
		return mThreshold;
	}

	public void setThreshold(double aThreshold) {
		this.mThreshold = aThreshold;
	}

	public Set<EndPointPerformanceInfo> getEndPointsInfo() {
		return mEndPointsInfo;
	}

	@Override
	public int compareTo(PriorityGroup aPG) {
		if (getThreshold() < aPG.getThreshold()) {
			return -1;
		} else if (getThreshold() > aPG.getThreshold()) {
			return 1;
		} else {
			return 0;
		}
	}
}
