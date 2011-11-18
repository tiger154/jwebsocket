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

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.core.EventModel;

/**
 *
 * @author kyberneees
 */
public interface IEventModelFilter {

	/**
	 *
	 * @return The filter identifier
	 */
	public String getId();

	/**
	 * Filters identifiers must to be unique to avoid access conflicts 
	 * 
	 * @param id The filter identifier to set
	 */
	public void setId(String id);

	/**
	 *
	 * @return The EventModel singleton instance
	 */
	public EventModel getEm();

	/**
	 * 
	 * @param em The EventModel singleton instance to set
	 */
	public void setEm(EventModel em);

	/**
	 * Filter the client event notifications before it was processed
	 * 
	 * @param aConnector The WebSocketConnector that execute the c2s call
	 * @param aEvent The client notified event
	 * @throws Exception
	 */
	public void beforeCall(WebSocketConnector aConnector, C2SEvent aEvent) throws Exception;

	/**
	 * Filter the server response associated to a previous client 
	 * event notification
	 *
	 * @param aConnector The WebSocketConnector that execute the c2s call
	 * @param aEvent The server response event
	 * @throws Exception
	 */
	public void afterCall(WebSocketConnector aConnector, C2SResponseEvent aEvent) throws Exception;
	
}
