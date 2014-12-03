//	---------------------------------------------------------------------------
//	jWebSocket - FieldJms (Community Edition, CE)
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
package org.jwebsocket.plugins.jms.util;

/**
 *
 * @author Johannes Smutny
 */
public enum FieldJms {

	/**
	 * connectionFactoryName
	 */
	CONNECTION_FACTORY_NAME("connectionFactoryName"),
	/**
	 * destinationName
	 */
	DESTINATION_NAME("destinationName"),
	/**
	 * pubSubDomain
	 */
	PUB_SUB_DOMAIN("pubSubDomain"),
	/**
	 * text
	 */
	TEXT("text"),
	/**
	 * map
	 */
	MAP("map"),
	/**
	 * JMSCorrelationID
	 */
	JMS_HEADER_CORRELATION_ID("JMSCorrelationID"),
	/**
	 * JMSReplyTo
	 */
	JMS_HEADER_REPLY_TO("JMSReplyTo"),
	/**
	 * JMSType
	 */
	JMS_HEADER_TYPE("JMSType"),
	/**
	 * JMSDestination
	 */
	JMS_HEADER_DESTINATION("JMSDestination"),
	/**
	 * JMSMessageID
	 */
	JMS_HEADER_MESSAGE_ID("JMSMessageID"),
	/**
	 * JMSDeliveryMode
	 */
	JMS_HEADER_DELIVERY_MODE("JMSDeliveryMode"),
	/**
	 * JMSTimestamp
	 */
	JMS_HEADER_TIMESTAMP("JMSTimestamp"),
	/**
	 * JMSRedelivered
	 */
	JMS_HEADER_REDELIVERED("JMSRedelivered"),
	/**
	 * JMSExpiration
	 */
	JMS_HEADER_EXPIRATION("JMSExpiration"),
	/**
	 * JMSPriority
	 */
	JMS_HEADER_PRIORITY("JMSPriority"),
	/**
	 * msgPayLoad
	 */
	MESSSAGE_PAYLOAD("msgPayLoad"),
	/**
	 * name
	 */
	NAME("name"),
	/**
	 * code
	 */
	CODE("code"),
	/**
	 * msg
	 */
	MSG("msg"),
	/**
	 * event
	 */
	EVENT("event"),
	/**
	 * jmsHeaderProperties
	 */
	JMS_HEADER_PROPERTIES("jmsHeaderProperties"),
	/**
	 * destinationIdentifier
	 */
	DESTINATION_IDENTIFIER("destinationIdentifier");
	private final String mValue;

	private FieldJms(String aValue) {
		this.mValue = aValue;
	}

	/**
	 *
	 * @return
	 */
	public String getValue() {
		return mValue;
	}

	/**
	 *
	 * @param aField
	 * @return
	 */
	public boolean equals(String aField) {
		return mValue.equals(aField);
	}

	@Override
	public String toString() {
		return mValue;
	}
}
