//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.event.test;

import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.token.Token;

/**
 * S2C event to plus two variables in the client side (x,y)
 * 
 * @author kyberneees
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
	public void writeToToken(Token aToken){
		aToken.setInteger("x", getX());
		aToken.setInteger("y", getY());	
	}
}
