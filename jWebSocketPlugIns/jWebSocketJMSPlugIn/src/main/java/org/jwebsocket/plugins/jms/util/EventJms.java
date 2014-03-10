//	---------------------------------------------------------------------------
//	jWebSocket - EventJms (Community Edition, CE)
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
 * @author Alexander Schulze
 */
public enum EventJms {

	/**
	 *
	 */
	HANDLE_TEXT("handleJmsText"),
	/**
	 *
	 */
	HANDLE_TEXT_MESSAGE("handleJmsTextMessage"),
	/**
	 *
	 */
	HANDLE_MAP("handleJmsMap"),
	/**
	 *
	 */
	HANDLE_MAP_MESSAGE("handleJmsMapMessage");
	private final String mValue;

	private EventJms(String value) {
		this.mValue = value;
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
