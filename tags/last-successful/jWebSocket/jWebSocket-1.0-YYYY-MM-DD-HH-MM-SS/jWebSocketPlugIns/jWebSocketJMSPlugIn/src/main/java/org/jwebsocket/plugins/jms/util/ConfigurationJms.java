//	---------------------------------------------------------------------------
//	jWebSocket - ConfigurationJms (Community Edition, CE)
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
public enum ConfigurationJms {

	/**
	 * connectionFactory
	 */
	CF_PREFIX("connectionFactory:"),
	/**
	 * destination
	 */
	DESTINATION_PREFIX("destination:"),
	/**
	 * name
	 */
	NAME("name"),
	/**
	 * cfName
	 */
	CONNECTION_FACTORY_NAME("cfName"),
	/**
	 * pubSubDomain
	 */
	PUB_SUB_DOMAIN("pubSubDomain"),
	/**
	 * sessionAckMode
	 */
	SESSION_ACKNOWLEDGE_MODE("sessionAckMode"),
	/**
	 * sessionTransacted
	 */
	SESSION_TRANSACTED("sessionTransacted"),
	/**
	 * deliveryPersistent
	 */
	DELIVERY_PERSISTENT("deliveryPersistent");
	private final String mValue;

	private ConfigurationJms(String aValue) {
		mValue = aValue;
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
	 * @param aAction
	 * @return
	 */
	public boolean equals(String aAction) {
		return mValue.equals(aAction);
	}

	@Override
	public String toString() {
		return mValue;
	}
}
