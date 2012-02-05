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
public class S2CLedState extends S2CEvent {

	private Boolean mBlue;
	private Boolean mRed;
	private Boolean mGreen;
	private Boolean mYellow;

	public S2CLedState(Boolean aBlue, Boolean aRed, Boolean aGreen, Boolean aYellow) {
		super();
		this.setId("ledState");
		this.mBlue = aBlue;
		this.mRed = aRed;
		this.mGreen = aGreen;
		this.mYellow = aYellow;
	}

	public Boolean getBlue() {
		return mBlue;
	}

	public Boolean getRed() {
		return mRed;
	}

	public Boolean getGreen() {
		return mGreen;
	}

	public Boolean getYellow() {
		return mYellow;
	}

	@Override
	public void writeToToken(Token token) {
		token.setBoolean("blue", getBlue());
		token.setBoolean("red", getRed());
		token.setBoolean("green", getGreen());
		token.setBoolean("yellow", getYellow());
	}
}
