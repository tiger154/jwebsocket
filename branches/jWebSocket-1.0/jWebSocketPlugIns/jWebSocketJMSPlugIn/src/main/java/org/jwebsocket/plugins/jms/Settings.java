//	---------------------------------------------------------------------------
//	jWebSocket - Settings for JMS Gateway Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.jms;

import java.util.UUID;

/**
 *
 * @author aschulze
 */
public class Settings {

	private String mEndPointId = UUID.randomUUID().toString();
	private String mBrokerURI = null;
	private String mGatewayTopic = null;
	private String mAdvisoryTopic = null;

	/**
	 * @return the mEndPointId
	 */
	public String getEndPointId() {
		return mEndPointId;
	}

	/**
	 * @param mEndPointId the mEndPointId to set
	 */
	public void setEndPointId(String mEndPointId) {
		this.mEndPointId = mEndPointId;
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
	 * @return the mAdvisoryTopic
	 */
	public String getAdvisoryTopic() {
		return mAdvisoryTopic;
	}

	/**
	 * @param mAdvisoryTopic the mAdvisoryTopic to set
	 */
	public void setAdvisoryTopic(String mAdvisoryTopic) {
		this.mAdvisoryTopic = mAdvisoryTopic;
	}

	/**
	 * @return the mGatewayTopic
	 */
	public String getGatewayTopic() {
		return mGatewayTopic;
	}

	/**
	 * @param aGatewayTopic
	 */
	public void setGatewayTopic(String aGatewayTopic) {
		mGatewayTopic = aGatewayTopic;
	}
}
