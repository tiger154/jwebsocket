//	---------------------------------------------------------------------------
//	jWebSocket - Configuration
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
public enum ConfigurationJms {

	CF_PREFIX("connectionFactory:"), DESTINATION_PREFIX("destination:"), NAME("name"), CONNECTION_FACTORY_NAME("cfName"), PUB_SUB_DOMAIN(
			"pubSubDomain"), SESSION_ACKNOWLEDGE_MODE("sessionAckMode"), SESSION_TRANSACTED("sessionTransacted"), DELIVERY_PERSISTENT(
			"deliveryPersistent");

	private String mValue;

	private ConfigurationJms(String value) {
		this.mValue = value;
	}

	public String getValue() {
		return mValue;
	}

	public boolean equals(String aAction) {
		return mValue.equals(aAction);
	}

	@Override
	public String toString() {
		return mValue;
	}
}
