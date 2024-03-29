//	---------------------------------------------------------------------------
//	jWebSocket - ActionJms (Community Edition, CE)
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
 * @author Johannes Smutny, Alexander Schulze
 */
public enum ActionJms {

	/**
	 * Listen, subscribe
	 */
	LISTEN("listenJms"),
	/**
	 * Listen message, subscribe
	 */
	LISTEN_MESSAGE("listenJmsMessage"),
	/**
	 * Un-subscribe
	 */
	UNLISTEN("unlistenJms"),
	/**
	 * send text
	 */
	SEND_TEXT("sendJmsText"),
	/**
	 * send JMS text message
	 */
	SEND_TEXT_MESSAGE("sendJmsTextMessage"),
	/**
	 * send JMS map
	 */
	SEND_MAP("sendJmsMap"),
	/**
	 * send JMS map message
	 */
	SEND_MAP_MESSAGE("sendJmsMapMessage"),
	/**
	 * identify, usually send as broadcast ping expecting pongs
	 */
	IDENTIFY("identify"),
	/**
	 * ping, expecting pong
	 */
	PING("ping"),
	/**
	 * test
	 */
	TEST("test"),
	/**
	 * isBrokerConnected
	 */
	IS_BROKER_CONNECTED("isBrokerConnected");
	private final String mValue;

	private ActionJms(String aValue) {
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

	/**
	 *
	 * @param aValue
	 * @return
	 */
	public static ActionJms get(String aValue) {
		if (null == aValue) {
			throw new IllegalArgumentException("Missing value");
		}

		for (ActionJms lNext : ActionJms.values()) {
			if (lNext.getValue().equals(aValue)) {
				return lNext;
			}
		}

		throw new IllegalArgumentException("Missing ActionJms for value: '" + aValue + "'");
	}
}
