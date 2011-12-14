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
package org.jwebsocket.eventmodel.api;

import org.jwebsocket.api.IInitializable;
import org.jwebsocket.eventmodel.core.EventModel;
import java.util.Map;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.s2c.S2CEventNotification;
import org.jwebsocket.token.ITokenizable;

/**
 *
 * @author kyberneees
 */
public interface IEventModelPlugIn extends IListener, IInitializable, ITokenizable {

	/**
	 *
	 * @return The plug-in identifier
	 */
	public String getId();

	/**
	 *
	 * @param id The plug-in identifier to set
	 */
	public void setId(String id);

	/**
	 *
	 * @return The EventModel singleton instance
	 */
	public EventModel getEm();

	public Map<String, WebSocketConnector> getServerAllConnectors();
	
	/**
	 *
	 * @param em The EventModel singleton instance to set
	 */
	public void setEm(EventModel em);

	/**
	 * The client API is the plug-in interface for communication with the clients.
	 * Is used like WSDL to allow runtime client plug-ins generation.
	 *
	 * @return The plug-in client API
	 */
	public Map<String, Class<? extends Event>> getClientAPI();

	/**
	 *
	 * @param clientAPI The client API to set
	 */
	public void setClientAPI(Map<String, Class<? extends Event>> clientAPI);

	/**
	 * (s2c calls) Notify events from the server to client(s) . 
	 *
	 * @param aEvent The S2CEvent to be send
	 * @return
	 */
	public S2CEventNotification notifyS2CEvent(S2CEvent aEvent);
}
