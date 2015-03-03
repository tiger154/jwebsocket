//	---------------------------------------------------------------------------
//	jWebSocket - JMS EndPoint MessageListener (Community Edition, CE)
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
package org.jwebsocket.jms.endpoint;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;

/**
 * Base implementation of the IJMSMessageListener interface for convenience.
 *
 * @author Alexander Schulze
 */
public class JMSEndPointMessageListener implements IJMSMessageListener {

	private static final Logger mLog = Logger.getLogger(JMSEndPointMessageListener.class);
	static long utid = 0;
	private JMSEndPointSender mSender;
	private final JMSEndPoint mJMSEndPoint;

	/**
	 *
	 * @param aJMSClient
	 */
	public JMSEndPointMessageListener(JMSEndPoint aJMSClient) {
		mJMSEndPoint = aJMSClient;
	}

	/**
	 *
	 * @param aTargetId
	 * @param aText
	 */
	public void sendText(String aTargetId, String aText) {
		mSender.sendText(aTargetId, aText);
	}

	/**
	 *
	 * @param aTargetId
	 * @param aPayload
	 */
	public void sendJSONviaGateway(String aTargetId, String aPayload) {
		aPayload = aPayload.replace("\"", "\\\"");
		utid++;
		sendText(getJMSEndPoint().getGatewayId(),
				"{\"ns\":\"org.jwebsocket.plugins.system\""
				+ ",\"type\":\"send\""
				+ ",\"utid\":" + utid
				+ ",\"format\":\"json\""
				+ ",\"sourceId\":\"" + mJMSEndPoint.getEndPointId() + "\""
				+ ",\"targetId\":\"" + aTargetId + "\""
				+ ",\"data\":\"" + aPayload + "\"}");
	}

	/**
	 *
	 * @param aTargetId
	 * @param aPayload
	 */
	public void sendXMLviaGateway(String aTargetId, String aPayload) {
		aPayload = aPayload
				.replace("\"", "\\\"")
				.replace("\t", "")
				.replace("\r", "")
				.replace("\n", "");
		utid++;
		sendText(getJMSEndPoint().getGatewayId(),
				"{\"ns\":\"org.jwebsocket.plugins.system\""
				+ ",\"type\":\"send\""
				+ ",\"utid\":" + utid
				+ ",\"format\":\"xml\""
				+ ",\"sourceId\":\"" + mJMSEndPoint.getEndPointId() + "\""
				+ ",\"targetId\":\"" + aTargetId + "\""
				+ ",\"data\":\"" + aPayload + "\"}");
	}

	/**
	 *
	 * @param aMessage
	 */
	@Override
	public void onMessage(Message aMessage) {
	}

	/**
	 *
	 * @param aMessage
	 */
	@Override
	public void onBytesMessage(BytesMessage aMessage) {
	}

	/**
	 *
	 * @param aMessage
	 */
	@Override
	public void onTextMessage(TextMessage aMessage) {
	}

	/**
	 *
	 * @param aMessage
	 */
	@Override
	public void onMapMessage(MapMessage aMessage) {
	}

	/**
	 *
	 * @param aMessage
	 */
	@Override
	public void onObjectMessage(ObjectMessage aMessage) {
	}

	/**
	 * @return the mSender
	 */
	@Override
	public JMSEndPointSender getSender() {
		return mSender;
	}

	/**
	 *
	 * @param aSender
	 */
	@Override
	public void setSender(JMSEndPointSender aSender) {
		mSender = aSender;
	}

	/**
	 *
	 * @return
	 */
	public JMSEndPoint getJMSEndPoint() {
		return mJMSEndPoint;
	}
}
