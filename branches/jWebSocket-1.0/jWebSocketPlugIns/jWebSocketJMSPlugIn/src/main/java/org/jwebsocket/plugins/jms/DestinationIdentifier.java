//	---------------------------------------------------------------------------
//	jWebSocket - DestinationIdentifier
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
package org.jwebsocket.plugins.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Session;

import org.jwebsocket.plugins.jms.util.FieldJms;
import org.jwebsocket.token.Token;

/**
 * Class that represents an identifier for a jms destination. This class
 * identifies a destination by the name of the connection factory by which the
 * destination gets connected to, the name of the destination and a flag which
 * indicates whether the destination adheres to the publish-subscribe or the
 * point-to-point domain.
 * 
 * @author Johannes Smutny
 * 
 */
public class DestinationIdentifier {

	private final Boolean mPubSubDomain;
	private Boolean mDeliveryPersistent = Boolean.TRUE;
	private Boolean mSessionTransacted = Boolean.FALSE;
	private final String mDestinationName;
	private final String mConnectionFactoryName;
	private Integer mSessionAcknowledgeMode = Session.AUTO_ACKNOWLEDGE;

	private DestinationIdentifier(String aConnectionFactoryName, String aDestinationName, Boolean aPubSubDomain,
			Boolean aSessionTransacted, Integer aSessionAcknowledgeMode, Boolean deliveryPersistent) {
		this.mConnectionFactoryName = aConnectionFactoryName;
		this.mDestinationName = aDestinationName;
		this.mPubSubDomain = aPubSubDomain;
		if (null != aSessionTransacted)
			this.mSessionTransacted = aSessionTransacted;
		if (null != aSessionAcknowledgeMode) {
			if (aSessionAcknowledgeMode != Session.AUTO_ACKNOWLEDGE
					&& aSessionAcknowledgeMode != Session.CLIENT_ACKNOWLEDGE
					&& aSessionAcknowledgeMode != Session.DUPS_OK_ACKNOWLEDGE)
				throw new IllegalArgumentException("wrong arg for session Ack mode: " + aSessionAcknowledgeMode);
			this.mSessionAcknowledgeMode = aSessionAcknowledgeMode;
		}
		if (null != deliveryPersistent)
			this.mDeliveryPersistent = deliveryPersistent;
	}

	private DestinationIdentifier(String aConnectionFactoryName, String aDestinationName, Boolean aPubSubDomain) {
		this.mConnectionFactoryName = aConnectionFactoryName;
		this.mDestinationName = aDestinationName;
		this.mPubSubDomain = aPubSubDomain;
	}

	public Boolean isPubSubDomain() {
		return mPubSubDomain;
	}

	public String getDestinationName() {
		return mDestinationName;
	}

	public String getConnectionFactoryName() {
		return mConnectionFactoryName;
	}

	public Boolean isSessionTransacted() {
		return mSessionTransacted;
	}

	public int getSessionAcknowledgeMode() {
		return mSessionAcknowledgeMode;
	}

	public Boolean getDeliveryPersistent() {
		return mDeliveryPersistent;
	}

	public static DestinationIdentifier valueOf(String aConnectionFactoryName, String aDestinationName,
			Boolean aPubSubDomain, Boolean aSessionTransacted, Integer aSessionAcknowledgeMode,
			Boolean deliveryPersistent) {
		return new DestinationIdentifier(aConnectionFactoryName, aDestinationName, aPubSubDomain, aSessionTransacted,
				aSessionAcknowledgeMode, deliveryPersistent);
	}

	public static DestinationIdentifier valueOf(String aConnectionFactoryName, String aDestinationName,
			Boolean aPubSubDomain) {
		return new DestinationIdentifier(aConnectionFactoryName, aDestinationName, aPubSubDomain);
	}

	public static DestinationIdentifier valueOf(Token aToken) {
		String lConnectionFactoryName = aToken.getString(FieldJms.CONNECTION_FACTORY_NAME.getValue());
		String lDestinationName = aToken.getString(FieldJms.DESTINATION_NAME.getValue());
		Boolean lPubSubDomain = aToken.getBoolean(FieldJms.PUB_SUB_DOMAIN.getValue());
		return valueOf(lConnectionFactoryName, lDestinationName, lPubSubDomain);
	}

	public boolean isMissingData() {
		return null == mConnectionFactoryName || mConnectionFactoryName.trim().length() == 0
				|| null == mDestinationName || mDestinationName.trim().length() == 0 || null == mPubSubDomain;
	}

	public Token setDestinationIdentifier(Token aToken) {
		if (null == aToken)
			return null;
		aToken.setMap(FieldJms.DESTINATION_IDENTIFIER.getValue(), getJSONMap());
		return aToken;
	}

	public Map<String, Object> getJSONMap() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(FieldJms.CONNECTION_FACTORY_NAME.getValue(), mConnectionFactoryName);
		result.put(FieldJms.DESTINATION_NAME.getValue(), mDestinationName);
		result.put(FieldJms.PUB_SUB_DOMAIN.getValue(), mPubSubDomain);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mConnectionFactoryName == null) ? 0 : mConnectionFactoryName.hashCode());
		result = prime * result + ((mDestinationName == null) ? 0 : mDestinationName.hashCode());
		result = prime * result + ((mPubSubDomain == null) ? 0 : mPubSubDomain.hashCode());
		result = prime * result + mSessionAcknowledgeMode;
		result = prime * result + ((mSessionTransacted == null) ? 0 : mSessionTransacted.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DestinationIdentifier other = (DestinationIdentifier) obj;
		if (mConnectionFactoryName == null) {
			if (other.mConnectionFactoryName != null)
				return false;
		} else if (!mConnectionFactoryName.equals(other.mConnectionFactoryName))
			return false;
		if (mDestinationName == null) {
			if (other.mDestinationName != null)
				return false;
		} else if (!mDestinationName.equals(other.mDestinationName))
			return false;
		if (mPubSubDomain == null) {
			if (other.mPubSubDomain != null)
				return false;
		} else if (!mPubSubDomain.equals(other.mPubSubDomain))
			return false;
		if (mSessionAcknowledgeMode != other.mSessionAcknowledgeMode)
			return false;
		if (mSessionTransacted == null) {
			if (other.mSessionTransacted != null)
				return false;
		} else if (!mSessionTransacted.equals(other.mSessionTransacted))
			return false;
		return true;
	}

}
