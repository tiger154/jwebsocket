//  ---------------------------------------------------------------------------
//  jWebSocket - JMSClientSender (Community Edition, CE)
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
package org.jwebsocket.jms.client;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Schulze
 */
public class JMSClientSender {

	static final Logger mLog = Logger.getLogger(JMSClientSender.class);
	private final MessageProducer mProducer;
	private final Session mSession;
	private final String mNodeId;

	/**
	 *
	 * @param aSession
	 * @param aProducer
	 * @param aNodeId
	 */
	public JMSClientSender(Session aSession, MessageProducer aProducer,
			String aNodeId) {
		mSession = aSession;
		mProducer = aProducer;
		mNodeId = aNodeId;
	}

	/**
	 *
	 * @param aText
	 */
	public void sendText(final String aText) {
		mLog.info("Sending text: " + aText + "...");
		Message lMsg;
		try {
			lMsg = mSession.createTextMessage(aText);
			lMsg.setJMSCorrelationID(mNodeId);
			mProducer.send(lMsg);
		} catch (JMSException lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " sending message.");
		}
	}

	/**
	 * @return the mJmsTemplate
	 */
	public MessageProducer getProducer() {
		return mProducer;
	}

	/**
	 * @return the mCorrelationId
	 */
	public String getCorrelationId() {
		return mNodeId;
	}
}
