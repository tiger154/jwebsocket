//	---------------------------------------------------------------------------
//	jWebSocket - Settings for Load Balancer Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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

import java.util.Map;
import javolution.util.FastMap;

/**
 *
 * @author aschulze
 */
public class Settings {

	// list of (service) clusters
	private Map<String, Cluster> mClusters = new FastMap<String, Cluster>();
	private long mShutdownTimeout = 0;
	private long mMessageTimeout=0;

	/**
	 * @return the clusters
	 */
	public Map<String, Cluster> getClusters() {
		return mClusters;
	}

	/**
	 * @param aClusters
	 */
	public void setClusters(Map aClusters) {
		mClusters = aClusters;
	}

	/**
	 * @return the mShutdownTimeout
	 */
	public long getShutdownTimeout() {
		return mShutdownTimeout;
	}

	/**
	 * @param mShutdownTimeout the mShutdownTimeout to set
	 */
	public void setShutdownTimeout(long aShutdownTimeout) {
		this.mShutdownTimeout = aShutdownTimeout;
	}

	/**
	 * @return the mMessageTimeout
	 */
	public long getMessageTimeout() {
		return mMessageTimeout;
	}

	/**
	 * @param mMessageTimeout the mMessageTimeout to set
	 */
	public void setMessageTimeout(long aMessageTimeout) {
		this.mMessageTimeout = aMessageTimeout;
	}
}
