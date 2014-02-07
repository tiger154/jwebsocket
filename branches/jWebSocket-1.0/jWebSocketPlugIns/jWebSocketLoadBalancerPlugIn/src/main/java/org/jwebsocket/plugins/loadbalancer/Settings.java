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

import java.util.Map;
import javolution.util.FastMap;

/**
 * Load balancer settings.
 *
 * @author aschulze
 * @author kyberneees
 * @author rbetancourt
 */
public class Settings {

	/**
	 * List of (service) clusters.
	 */
	private Map<String, Cluster> mClusters = new FastMap<String, Cluster>();
	/**
	 * Default message delivery timeout, default value '5000 milliseconds '.
	 */
	private long mMessageTimeout = 5000;
	/**
	 * Load balancer algorithm, default value '1 = round robin '.
	 */
	private int mBalancerAlgorithm = 1;

	/**
	 * @return the clusters
	 */
	public Map<String, Cluster> getClusters() {
		return mClusters;
	}

	/**
	 * @param aClusters clusters to set.
	 */
	public void setClusters(Map aClusters) {
		mClusters = aClusters;
	}

	/**
	 * @return the message timeout.
	 */
	public long getMessageTimeout() {
		return mMessageTimeout;
	}

	/**
	 * @param mMessageTimeout the message timeout to set.
	 */
	public void setMessageTimeout(long aMessageTimeout) {
		this.mMessageTimeout = aMessageTimeout;
	}

	/**
	 * @return the load balancer algorithm (1,2 or 3).
	 */
	public int getBalancerAlgorithm() {
		return mBalancerAlgorithm;
	}

	/**
	 * @param mBalancerAlgorithm the load balancer algorithm to set.
	 */
	public void setBalancerAlgorithm(int aBalancerAlgorithm) {
		this.mBalancerAlgorithm = aBalancerAlgorithm;
	}
}
