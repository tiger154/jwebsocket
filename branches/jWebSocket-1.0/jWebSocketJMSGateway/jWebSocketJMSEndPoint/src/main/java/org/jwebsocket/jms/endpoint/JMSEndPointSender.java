//  ---------------------------------------------------------------------------
//  jWebSocket - JMSEndPointSender (Community Edition, CE)
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
package org.jwebsocket.jms.endpoint;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Schulze
 */
public class JMSEndPointSender {

	
	// TODO: Introduce timeout management
	
	static final Logger mLog = Logger.getLogger(JMSEndPointSender.class);
	private final MessageProducer mProducer;
	private final Session mSession;
	private final String mEndPointId;

	/**
	 *
	 * @param aSession
	 * @param aProducer
	 * @param aEndPointId
	 */
	public JMSEndPointSender(Session aSession, MessageProducer aProducer,
			String aEndPointId) {
		mSession = aSession;
		mProducer = aProducer;
		mEndPointId = aEndPointId;
	}

	/**
	 *
	 * @param aTargetId
	 * @param aCorrelationID 
	 * @param aText
	 */
	public void sendText(String aTargetId, String aCorrelationID, final String aText) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending text: "
					+ "[content suppressed, length: " + aText.length() + " bytes]"
					+ "...");
		}
		Message lMsg;
		try {
			lMsg = mSession.createTextMessage(aText);
			if (null != aCorrelationID) {
				lMsg.setJMSCorrelationID(aCorrelationID);
			}
			lMsg.setStringProperty("targetId", aTargetId);
			lMsg.setStringProperty("sourceId", mEndPointId);
			mProducer.send(lMsg);
		} catch (JMSException lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " sending message.");
		}
	}

	/**
	 *
	 * @param aTargetId
	 * @param aText
	 */
	public void sendText(String aTargetId, final String aText) {
		sendText(aTargetId, null, aText);
	}

	/**
	 * @return the mJmsTemplate
	 */
	public MessageProducer getProducer() {
		return mProducer;
	}

	/**
	 * @return the EndPointId
	 */
	public String getEndPointId() {
		return mEndPointId;
	}
}

