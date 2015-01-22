//	---------------------------------------------------------------------------
//	jWebSocket - JMS Sender (Community Edition, CE)
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
package org.jwebsocket.plugins.jms.gateway;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.log4j.Logger;
import org.jwebsocket.jms.endpoint.JMSLogging;

/**
 *
 * @author Alexander Schulze
 */
public class JMSSender {

	static final Logger mLog = Logger.getLogger(JMSSender.class);
	private final MessageProducer mProducer;
	private final Session mSession;
	private final String mEndPointId;

	/**
	 *
	 * @param aSession
	 * @param aProducer
	 * @param aEndPointId
	 */
	public JMSSender(Session aSession, MessageProducer aProducer,
			String aEndPointId) {
		mSession = aSession;
		mProducer = aProducer;
		mEndPointId = aEndPointId;
	}

	/**
	 *
	 * @param aTargetId
	 * @param aText
	 * @throws javax.jms.JMSException
	 */
	public void sendText(final String aTargetId, final String aText) throws JMSException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending text to '" + aTargetId + "': "
					+ (JMSLogging.isFullTextLogging() ? aText
							: "[content suppressed, length="
							+ (null != aText ? aText.length() : "0") + " bytes]")
					+ "...");
		}
		Message lMsg;
		try {
			lMsg = mSession.createTextMessage(aText);
			// source of message to the MQ is the gateway id
			lMsg.setStringProperty("sourceId", mEndPointId);
			// target is the endpoint id of the MQ target endpoint
			lMsg.setStringProperty("targetId", aTargetId);
			mProducer.send(lMsg);
		} catch (JMSException lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " sending message: " + lEx.getMessage());
			throw new JMSException(lEx.getMessage());
		}
	}

	/**
	 * @return the Producer
	 */
	public MessageProducer getProducer() {
		return mProducer;
	}

	/**
	 * @return the EndPointId (source id of this JMS gateway!)
	 */
	public String getEndPointId() {
		return mEndPointId;
	}
}
