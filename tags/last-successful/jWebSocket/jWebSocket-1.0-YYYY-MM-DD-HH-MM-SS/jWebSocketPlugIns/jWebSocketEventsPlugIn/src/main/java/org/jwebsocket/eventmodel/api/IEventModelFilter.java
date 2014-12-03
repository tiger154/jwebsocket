//	---------------------------------------------------------------------------
//	jWebSocket - IEventModelFilter (Community Edition, CE)
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

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.core.EventModel;

/**
 *
 * @author Rolando Santamaria Maso
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
	 * Filter the server response associated to a previous client event
	 * notification
	 *
	 * @param aConnector The WebSocketConnector that execute the c2s call
	 * @param aEvent The server response event
	 * @throws Exception
	 */
	public void afterCall(WebSocketConnector aConnector, C2SResponseEvent aEvent) throws Exception;
}
