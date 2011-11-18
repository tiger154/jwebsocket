//	---------------------------------------------------------------------------
//	jWebSocket - FieldJms
//	Copyright (c) 2011, Innotrade GmbH - jWebSocket.org, Alexander Schulze
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.jms.util;

/**
 * 
 * @author Johannes Smutny
 */
public enum FieldJms {

	CONNECTION_FACTORY_NAME("connectionFactoryName"), DESTINATION_NAME("destinationName"), PUB_SUB_DOMAIN(
			"pubSubDomain"), TEXT("text"), MAP("map"), JMS_HEADER_CORRELATION_ID("JMSCorrelationID"), JMS_HEADER_REPLY_TO(
			"JMSReplyTo"), JMS_HEADER_TYPE("JMSType"), JMS_HEADER_DESTINATION("JMSDestination"), JMS_HEADER_MESSAGE_ID(
			"JMSMessageID"), JMS_HEADER_DELIVERY_MODE("JMSDeliveryMode"), JMS_HEADER_TIMESTAMP("JMSTimestamp"), JMS_HEADER_REDELIVERED(
			"JMSRedelivered"), JMS_HEADER_EXPIRATION("JMSExpiration"), JMS_HEADER_PRIORITY("JMSPriority"), MESSSAGE_PAYLOAD(
			"msgPayLoad"), NAME("name"), CODE("code"), MSG("msg"), EVENT("event"), JMS_HEADER_PROPERTIES(
			"jmsHeaderProperties"), DESTINATION_IDENTIFIER("destinationIdentifier");
	private String mValue;

	private FieldJms(String value) {
		this.mValue = value;
	}

	public String getValue() {
		return mValue;
	}

	public boolean equals(String aField) {
		return mValue.equals(aField);
	}

	@Override
	public String toString() {
		return mValue;
	}
}
