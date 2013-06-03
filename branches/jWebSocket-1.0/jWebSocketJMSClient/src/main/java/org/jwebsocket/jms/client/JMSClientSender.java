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
import javax.jms.Session;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 *
 * @author alexanderschulze
 */
public class JMSClientSender {

	private final JmsTemplate mJmsTemplate;
	private String mCorrelationId;

	/**
	 *
	 * @param aJmsTemplate
	 * @param aCorrelationId
	 */
	public JMSClientSender(JmsTemplate aJmsTemplate, String aCorrelationId) {
		mJmsTemplate = aJmsTemplate;
		mCorrelationId = aCorrelationId;
	}

	/**
	 *
	 * @param aJSON
	 */
	public void send(final String aJSON) {
		System.out.println("Sending JSON " + aJSON + "...");
		mJmsTemplate.send(new MessageCreator() {
			@Override
			public Message createMessage(Session aSession) throws JMSException {
				Message lMsg = aSession.createTextMessage(aJSON);
				lMsg.setJMSCorrelationID(mCorrelationId);
				return lMsg;
			}
		});
	}

	/**
	 * @return the mJmsTemplate
	 */
	public JmsTemplate getJmsTemplate() {
		return mJmsTemplate;
	}

	/**
	 * @return the mCorrelationId
	 */
	public String getCorrelationId() {
		return mCorrelationId;
	}

}
