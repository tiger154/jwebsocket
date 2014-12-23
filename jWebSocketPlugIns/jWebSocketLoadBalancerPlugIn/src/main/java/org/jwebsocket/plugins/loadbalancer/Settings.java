//	---------------------------------------------------------------------------
//	jWebSocket - Settings for Load Balancer Plug-in (Community Edition, CE)
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

import org.jwebsocket.plugins.loadbalancer.api.IClusterManager;

/**
 * Load balancer settings.
 *
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 * @author Rolando Betancourt Toucet
 */
public class Settings {

	/**
	 * The clusters manager.
	 */
	private IClusterManager mClusters;
	/**
	 * Default message delivery timeout, default value 5000 milliseconds.
	 */
	private long mMessageTimeout = 5000;
	
	private boolean mIncludeWorkerServiceId =false;

	public boolean isIncludeWorkerServiceId() {
		return mIncludeWorkerServiceId;
	}

	public void setIncludeWorkerServiceId(boolean aIncludeWorkerServiceId) {
		mIncludeWorkerServiceId = aIncludeWorkerServiceId;
	}

	public IClusterManager getClusterManager() {
		return mClusters;
	}

	public void setClusterManager(IClusterManager aCM) {
		mClusters = aCM;
	}

	/**
	 * @return the message timeout.
	 */
	public long getMessageTimeout() {
		return mMessageTimeout;
	}

	/**
	 * @param aMessageTimeout the message timeout to set.
	 */
	public void setMessageTimeout(long aMessageTimeout) {
		this.mMessageTimeout = aMessageTimeout;
	}
}
