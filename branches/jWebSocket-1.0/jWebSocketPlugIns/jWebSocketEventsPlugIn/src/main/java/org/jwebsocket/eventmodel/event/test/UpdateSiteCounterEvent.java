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
 * S2C event to update the site visitor counter
 * 
 * @author kyberneees
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
