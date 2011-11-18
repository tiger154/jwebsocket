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

	private C2SEventDefinition eventDefinition;
	private String requestId;
	private WebSocketConnector connector;

	/**
	 *
	 * @param requestId The client event arguments hash code
	 */
	public BeforeRouteResponseToken(String requestId) {
		super();
		this.requestId = requestId;
	}

	public WebSocketConnector getConnector() {
		return connector;
	}

	public void setConnector(WebSocketConnector connector) {
		this.connector = connector;
	}
	
	/**
	 * @return The event definition
	 */
	public C2SEventDefinition getEventDefinition() {
		return eventDefinition;
	}

	/**
	 * @param eventDefinition The event definition to set
	 */
	public void setEventDefinition(C2SEventDefinition eventDefinition) {
		this.eventDefinition = eventDefinition;
	}

	/**
	 * @return The client event arguments MD5
	 */
	public String getRequestId() {
		return requestId;
	}
}
