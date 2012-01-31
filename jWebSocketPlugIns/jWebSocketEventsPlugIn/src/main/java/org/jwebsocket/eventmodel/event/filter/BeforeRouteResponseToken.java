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
package org.jwebsocket.eventmodel.event.filter;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.observable.Event;

/**
 * Fired before send the response token to the client
 *
 * @author kyberneees
 */
public class BeforeRouteResponseToken extends Event {

	private C2SEventDefinition mEventDefinition;
	private String mRequestId;
	private WebSocketConnector mConnector;

	/**
	 *
	 * @param aRequestId The client event arguments hash code
	 */
	public BeforeRouteResponseToken(String aRequestId) {
		super();
		this.mRequestId = aRequestId;
	}

	public WebSocketConnector getConnector() {
		return mConnector;
	}

	public void setConnector(WebSocketConnector aConnector) {
		this.mConnector = aConnector;
	}
	
	/**
	 * @return The event definition
	 */
	public C2SEventDefinition getEventDefinition() {
		return mEventDefinition;
	}

	/**
	 * @param aEventDefinition The event definition to set
	 */
	public void setEventDefinition(C2SEventDefinition aEventDefinition) {
		this.mEventDefinition = aEventDefinition;
	}

	/**
	 * @return The client event arguments MD5
	 */
	public String getRequestId() {
		return mRequestId;
	}
}
