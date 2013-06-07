//	---------------------------------------------------------------------------
//	jWebSocket - Settings for Mail Plug-in (Community Edition, CE)
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
	private String mPubTopic = null;
	private String mSubTopic = null;
	private List<String> mClusterNodes = null;

	/**
	 * @return the mNodeId
	 */
	public String getNodeId() {
		return mNodeId;
	}

	/**
	 * @param mNodeId the mNodeId to set
	 */
	public void setNodeId(String mNodeId) {
		this.mNodeId = mNodeId;
	}

	/**
	 * @return the mBrokerURI
	 */
	public String getBrokerURI() {
		return mBrokerURI;
	}

	/**
	 * @param mBrokerURI the mBrokerURI to set
	 */
	public void setBrokerURI(String mBrokerURI) {
		this.mBrokerURI = mBrokerURI;
	}

	/**
	 * @return the mPubTopic
	 */
	public String getPubTopic() {
		return mPubTopic;
	}

	/**
	 * @param mPubTopic the mPubTopic to set
	 */
	public void setPubTopic(String mPubTopic) {
		this.mPubTopic = mPubTopic;
	}

	/**
	 * @return the mSubTopic
	 */
	public String getSubTopic() {
		return mSubTopic;
	}

	/**
	 * @param mSubTopic the mSubTopic to set
	 */
	public void setSubTopic(String mSubTopic) {
		this.mSubTopic = mSubTopic;
	}

	/**
	 * @return the mClusterNodes
	 */
	public List<String> getClusterNodes() {
		return mClusterNodes;
	}

	/**
	 * @param mClusterNodes the mClusterNodes to set
	 */
	public void setClusterNodes(List<String> mClusterNodes) {
		this.mClusterNodes = mClusterNodes;
	}
}
