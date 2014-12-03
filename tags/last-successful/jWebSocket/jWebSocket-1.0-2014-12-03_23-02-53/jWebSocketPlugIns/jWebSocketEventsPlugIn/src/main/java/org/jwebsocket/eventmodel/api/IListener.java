//	---------------------------------------------------------------------------
//	jWebSocket - IListener (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
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
 * @author Rolando Santamaria Maso
 */
public interface IListener {

	/**
	 * Process custom events.
	 * <p>
	 * For specific events use a new methods signature like this:
	 * <br>
	 * <tt>public void processEvent(SpecificEventClass aEvent, ResponseEvent
	 * aResponseEvent);</tt>
	 * <br>
	 * or:
	 * <br>
	 * <tt>public void processEvent(SpecificWebSocketEventClass aEvent,
	 * WebSocketResponseEvent aResponseEvent);</tt>
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Event aEvent, ResponseEvent aResponseEvent);
}
