//	---------------------------------------------------------------------------
//	jWebSocket - Settings for JMS Gateway Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
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
import org.jwebsocket.config.JWebSocketServerConstants;

/**
 *
 * @author Alexander Schulze
 */
public class Settings {

	private String mEndPointId = UUID.randomUUID().toString();
	private String mBrokerURI = null;
	private String mGatewayTopic = null;
	private String mAdvisoryTopic = null;
	private String mHostname = JWebSocketServerConstants.DEFAULT_HOSTNAME;

	private Boolean mLoggerActive = false;
	private Boolean mBroadcastTransportlisterEvents = false;
	private Boolean mBroadcastAdvisoryEvents = false;

	/**
	 * @return the Endpoint Id
	 */
	public String getEndPointId() {
		return mEndPointId;
	}

	/**
	 * @param aEndPointId
	 */
	public void setEndPointId(String aEndPointId) {
		mEndPointId = aEndPointId;
	}

	/**
	 * @return the Broker URI
	 */
	public String getBrokerURI() {
		return mBrokerURI;
	}

	/**
	 * @param aBrokerURI
	 */
	public void setBrokerURI(String aBrokerURI) {
		mBrokerURI = aBrokerURI;
	}

	/**
	 * @return the Advisory Topic
	 */
	public String getAdvisoryTopic() {
		return mAdvisoryTopic;
	}

	/**
	 * @param aAdvisoryTopic
	 */
	public void setAdvisoryTopic(String aAdvisoryTopic) {
		mAdvisoryTopic = aAdvisoryTopic;
	}

	/**
	 * @return the Gateway Topic
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

	/**
	 * @return the mLoggerActive
	 */
	public Boolean getLoggerActive() {
		return mLoggerActive;
	}

	/**
	 * @param aLoggerActive the aLoggerActive to set
	 */
	public void setLoggerActive(Boolean aLoggerActive) {
		mLoggerActive = aLoggerActive;
	}

	/**
	 * @return the mBroadcastTransportlisterEvents
	 */
	public Boolean getBroadcastTransportlisterEvents() {
		return mBroadcastTransportlisterEvents;
	}

	/**
	 * @param aBroadcastTransportlisterEvents
	 */
	public void setBroadcastTransportlisterEvents(Boolean aBroadcastTransportlisterEvents) {
		mBroadcastTransportlisterEvents = aBroadcastTransportlisterEvents;
	}

	/**
	 * @return the mBroadcastAdvisoryEvents
	 */
	public Boolean getBroadcastAdvisoryEvents() {
		return mBroadcastAdvisoryEvents;
	}

	/**
	 * @param aBroadcastAdvisoryEvents the mBroadcastAdvisoryEvents to set
	 */
	public void setBroadcastAdvisoryEvents(Boolean aBroadcastAdvisoryEvents) {
		mBroadcastAdvisoryEvents = aBroadcastAdvisoryEvents;
	}

	/**
	 * 
	 * @return 
	 */
	public String getHostname() {
		return mHostname;
	}

	/**
	 * 
	 * @param aHostname 
	 */
	public void setHostname(String aHostname) {
		mHostname = aHostname;
	}
}
