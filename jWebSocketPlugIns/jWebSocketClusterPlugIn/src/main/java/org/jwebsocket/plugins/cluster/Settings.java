//	---------------------------------------------------------------------------
//	jWebSocket - Settings for Cluster Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.cluster;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author aschulze
 */
public class Settings {

	private String mNodeId = UUID.randomUUID().toString();
	private String mBrokerURI = null;
	private String mClusterTopic = null;
	private String mAdvisoryTopic = null;
	private List<String> mClusterNodes = null;

	/**
	 * @return the NodeId
	 */
	public String getNodeId() {
		return mNodeId;
	}

	/**
	 * @param aNodeId the NodeId to set
	 */
	public void setNodeId(String aNodeId) {
		this.mNodeId = aNodeId;
	}

	/**
	 * @return the BrokerURI
	 */
	public String getBrokerURI() {
		return mBrokerURI;
	}

	/**
	 * @param mBrokerURI the BrokerURI to set
	 */
	public void setBrokerURI(String mBrokerURI) {
		this.mBrokerURI = mBrokerURI;
	}

	/**
	 * @return the ClusterTopic
	 */
	public String getClusterTopic() {
		return mClusterTopic;
	}

	/**
	 * @param aClusterTopic the ClusterTopic to set
	 */
	public void setClusterTopic(String aClusterTopic) {
		this.mClusterTopic = aClusterTopic;
	}

	/**
	 * @return the AdvisoryTopic
	 */
	public String getAdvisoryTopic() {
		return mAdvisoryTopic;
	}

	/**
	 * @param aAdvisoryTopic the AdvisoryTopic to set
	 */
	public void setAdvisoryTopic(String aAdvisoryTopic) {
		this.mAdvisoryTopic = aAdvisoryTopic;
	}

	/**
	 * @return the mClusterNodes
	 */
	public List<String> getClusterNodes() {
		return mClusterNodes;
	}

	/**
	 * @param aClusterNodes the ClusterNodes to set
	 */
	public void setClusterNodes(List<String> aClusterNodes) {
		this.mClusterNodes = aClusterNodes;
	}
}
