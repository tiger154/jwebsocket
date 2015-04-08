//	---------------------------------------------------------------------------
//	jWebSocket EndPointPerformanceInfo class for LBP (Community Edition, CE)
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

/**
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 */
public class EndPointPerformanceInfo {

	private String mEndPointId;
	private double mPerformanceFactor;
	private PriorityGroup mPriorityGroup;

	public EndPointPerformanceInfo() {
		mPriorityGroup = new PriorityGroup();
		mPriorityGroup.setName("UnassignedGroup");
		mPriorityGroup.setDescription("Default priority group for LB endpoints!");
		mPriorityGroup.setThreshold(100);
	}

	public String getEndPointId() {
		return mEndPointId;
	}

	public void setEndPointId(String aEndPointId) {
		this.mEndPointId = aEndPointId;
	}

	public double getPerformanceFactor() {
		return mPerformanceFactor;
	}

	public void setPerformanceFactor(double aPerformanceFactor) {
		this.mPerformanceFactor = aPerformanceFactor;
	}

	public PriorityGroup getPriorityGroup() {
		return mPriorityGroup;
	}

	public void setPriorityGroup(PriorityGroup aPriorityGroup) {
		this.mPriorityGroup = aPriorityGroup;
		aPriorityGroup.getEndPointsInfo().add(this);
	}
}
