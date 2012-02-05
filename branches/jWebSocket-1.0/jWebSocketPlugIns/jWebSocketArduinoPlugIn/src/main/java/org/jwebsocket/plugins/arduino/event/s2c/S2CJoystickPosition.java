// ---------------------------------------------------------------------------
// jWebSocket - < Description/Name of the Module >
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.plugins.arduino.event.s2c;

import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.token.Token;

/**
 *
 * @author Dariel Noa (dnoa@hab.uci.cu, UCI, Artemisa)
 */
public class S2CJoystickPosition extends S2CEvent {

	private Integer mX;
	private Integer mY;

	public S2CJoystickPosition(Integer aX, Integer aY) {
		super();
		this.setId("joystickPosition");
		this.mX = aX;
		this.mY = aY;
	}

	public Integer getmX() {
		return mX;
	}

	public void setmX(Integer mX) {
		this.mX = mX;
	}

	public Integer getmY() {
		return mY;
	}

	public void setmY(Integer mY) {
		this.mY = mY;
	}

	@Override
	public void writeToToken(Token token) {
		token.setInteger("x", mX);
		token.setInteger("y", mY);

	}
}
