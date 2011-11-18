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

import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ResponseEvent;

/**
 * A IListener is a component listening custom events external or locals.
 * <p>
 * <tt>external</tt>: Events from the client (WebSocketEvent)
 * <br>
 * <tt>local</tt>: Events from the system or others components
 * 
 * @author kyberneees
 */
public interface IListener {

	/**
	 * Process custom events. 
	 * <p>
	 * For specific events use a new methods signature like this:
	 * <br>
	 * <tt>public void processEvent(SpecificEventClass aEvent, ResponseEvent aResponseEvent);</tt>
	 * <br>
	 * or:
	 * <br>
	 * <tt>public void processEvent(SpecificWebSocketEventClass aEvent, WebSocketResponseEvent aResponseEvent);</tt>
	 * 
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Event aEvent, ResponseEvent aResponseEvent);
}
