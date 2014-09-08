// ---------------------------------------------------------------------------
// jWebSocket - S2CJoystickPosition (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
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
package org.jwebsocket.plugins.arduino.event.s2c;

import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.token.Token;

/**
 *
 * @author Dariel Noa (dnoa@hab.uci.cu, UCI, Artemisa)
 */
public class S2CJoystickPosition extends S2CEvent {

	private final Integer mX;
	private final Integer mY;

	/**
	 *
	 * @param aX
	 * @param aY
	 */
	public S2CJoystickPosition(Integer aX, Integer aY) {
		super();
		this.setId("joystickPosition");
		this.mX = aX;
		this.mY = aY;
	}

	/**
	 *
	 * @return
	 */
	public Integer getmX() {
		return mX;
	}

	/**
	 *
	 * @return
	 */
	public Integer getmY() {
		return mY;
	}

	@Override
	public void writeToToken(Token token) {
		token.setInteger("x", mX);
		token.setInteger("y", mY);

	}
}
