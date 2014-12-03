//	---------------------------------------------------------------------------
//	jWebSocket - S2CPlusXYEvent (Community Edition, CE)
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
 * S2C event to plus two variables in the client side (x,y)
 *
 * @author Rolando Santamaria Maso
 */
public class S2CPlusXYEvent extends S2CEvent {

	private int mX;
	private int mY;

	/**
	 *
	 * @param aX The 1 variable
	 * @param aY The 2 variable
	 */
	public S2CPlusXYEvent(int aX, int aY) {
		super();
		setId("plusXY");
		setResponseType("integer");
		setTimeout(5000);

		this.mX = aX;
		this.mY = aY;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return mX;
	}

	/**
	 * @param aX the x to set
	 */
	public void setX(int aX) {
		this.mX = aX;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return mY;
	}

	/**
	 * @param aY the y to set
	 */
	public void setY(int aY) {
		this.mY = aY;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void writeToToken(Token aToken) {
		aToken.setInteger("x", getX());
		aToken.setInteger("y", getY());
	}
}
