//	---------------------------------------------------------------------------
//	jWebSocket - IJMSMessageListener interface (Community Edition, CE)
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

/**
 * Interface for listeners to incoming messages.
 *
 * @author Alexander Schulze
 */
public interface IJMSMessageListener {

	/**
	 * Returns the reference to the assigned sender object to easily answer
	 * messages.
	 *
	 * @return
	 */
	JMSEndPointSender getSender();

	/**
	 * Sets the sender object to easily answer messages.
	 *
	 * @param aSender
	 */
	void setSender(JMSEndPointSender aSender);

	/**
	 * Listener for unclassified incoming messages, here the type of the message
	 * is not yet parsed.
	 *
	 * @param aMessage
	 */
	void onMessage(Message aMessage);

	/**
	 * Listener for incoming binary byte messages.
	 *
	 * @param aMessage
	 */
	void onBytesMessage(BytesMessage aMessage);

	/**
	 * Listener for incoming text messages.
	 *
	 * @param aMessage
	 */
	void onTextMessage(TextMessage aMessage);

	/**
	 * Listener for incoming map messages.
	 *
	 * @param aMessage
	 */
	void onMapMessage(MapMessage aMessage);

	/**
	 * Listener for incoming object messages.
	 *
	 * @param aMessage
	 */
	void onObjectMessage(ObjectMessage aMessage);
}
