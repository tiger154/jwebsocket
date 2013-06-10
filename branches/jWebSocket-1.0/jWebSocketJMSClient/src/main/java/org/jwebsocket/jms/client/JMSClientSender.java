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

/**
 *
 * @author alexanderschulze
 */
public class JMSClientSender {

	private final MessageProducer mProducer;
	private final Session mSession;
	private final String mCorrelationId;

	/**
	 *
	 * @param aProducer
	 * @param aCorrelationId
	 */
	public JMSClientSender(Session aSession, MessageProducer aProducer,
			String aCorrelationId) {
		mSession = aSession;
		mProducer = aProducer;
		mCorrelationId = aCorrelationId;
	}

	/**
	 *
	 * @param aJSON
	 */
	public void send(final String aJSON) {
		System.out.println("Sending JSON " + aJSON + "...");
		Message lMsg;
		try {
			lMsg = mSession.createTextMessage(aJSON);
			lMsg.setJMSCorrelationID(mCorrelationId);
			mProducer.send(lMsg);
		} catch (JMSException lEx) {
			System.out.println(lEx.getClass().getSimpleName() + " sending message.");
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
		return mCorrelationId;
	}
}
