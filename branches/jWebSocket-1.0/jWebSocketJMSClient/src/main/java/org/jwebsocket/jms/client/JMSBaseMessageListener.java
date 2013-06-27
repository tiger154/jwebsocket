//  ---------------------------------------------------------------------------
//  jWebSocket - JMS BaseMessageListener (Community Edition, CE)
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

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

/**
 * Base implementation of the IJMSMessageListener interface for convenience.
 *
 * @author Alexander Schulze
 */
public class JMSBaseMessageListener implements IJMSMessageListener {

	private JMSClientSender mSender;
	private JMSClient mJMSClient;

	/**
	 *
	 * @param aJMSClient
	 */
	public JMSBaseMessageListener(JMSClient aJMSClient) {
		mJMSClient = aJMSClient;
	}

	/*
	 public JMSBaseMessageListener(JMSClientSender aSender) {
	 mSender = aSender;
	 }
	 */
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
	public JMSClientSender getSender() {
		return mSender;
	}

	/**
	 *
	 * @param aSender
	 */
	@Override
	public void setSender(JMSClientSender aSender) {
		mSender = aSender;
	}

	/**
	 *
	 * @return
	 */
	public JMSClient getJMSClient() {
		return mJMSClient;
	}
}
