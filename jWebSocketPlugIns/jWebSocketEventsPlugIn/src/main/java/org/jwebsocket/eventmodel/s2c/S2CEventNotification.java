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
package org.jwebsocket.eventmodel.s2c;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.eventmodel.exception.MissingTokenSender;

/**
 *
 * @author kyberneees
 */
public class S2CEventNotification {

	private S2CEvent event;
	private S2CEventNotificationHandler S2CNotificationHandler;

	/**
	 *
	 * @param plugInId The client plug-in identifier
	 * @param aEvent The event to fire in the client
	 */
	public S2CEventNotification(String plugInId, S2CEvent aEvent, S2CEventNotificationHandler aS2CNotificationHandler) {
		aEvent.setPlugInId(plugInId);
		event = aEvent;
		S2CNotificationHandler = aS2CNotificationHandler;
	}

	/**
	 * Send the event to a giving client connector
	 * 
	 * @param aConnector The client connector
	 * @param aOnResponse The server on-response callbacks 
	 */
	public void to(WebSocketConnector aConnector, OnResponse aOnResponse) throws MissingTokenSender {
		//Sending the event
		S2CNotificationHandler.send(event, aConnector, aOnResponse);
	}

	/**
	 * Send the event to a giving client connector
	 * 
	 * @param aConnector The client connector
	 * @param aOnResponse The server on-response callbacks 
	 */
	public void to(String aConnectorId, OnResponse aOnResponse) throws MissingTokenSender {
		//Sending the event
		S2CNotificationHandler.send(event, aConnectorId, aOnResponse);
	}
}
