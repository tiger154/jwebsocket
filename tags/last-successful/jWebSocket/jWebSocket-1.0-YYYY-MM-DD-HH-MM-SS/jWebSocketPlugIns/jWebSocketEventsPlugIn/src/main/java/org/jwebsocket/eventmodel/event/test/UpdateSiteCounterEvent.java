//	---------------------------------------------------------------------------
//	jWebSocket - UpdateSiteCounterEvent (Community Edition, CE)
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
package org.jwebsocket.eventmodel.event.test;

import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.token.Token;

/**
 * S2C event to update the site visitor counter
 *
 * @author Rolando Santamaria Maso
 */
public class UpdateSiteCounterEvent extends S2CEvent {

	private Integer mCounter;

	/**
	 *
	 */
	public UpdateSiteCounterEvent() {
		setId("setVisitorCounter");
	}

	/**
	 * @return The counter value
	 */
	public Integer getCounter() {
		return mCounter;
	}

	/**
	 * @param aCounter The counter value to set
	 */
	public void setCounter(Integer aCounter) {
		this.mCounter = aCounter;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void writeToToken(Token aToken) {
		aToken.setString("counter", getCounter().toString());
	}
}
