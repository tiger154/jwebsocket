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
//  ---------------------------------------------------------------------------F
package org.jwebsocket.eventmodel.event.test;

import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.token.Token;

/**
 * S2C event to plus two variables in the client side (x,y)
 * 
 * @author kyberneees
 */
public class S2CPlusXYEvent extends S2CEvent {

	private int x;
	private int y;

	/**
	 *
	 * @param x The 1 variable
	 * @param y The 2 variable
	 */
	public S2CPlusXYEvent(int x, int y) {
		super();
		setId("plusXY");
		setResponseType("integer");
		setTimeout(1000);

		setX(x);
		setY(y);
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void writeToToken(Token token){
		token.setInteger("x", getX());
		token.setInteger("y", getY());	
	}
}
