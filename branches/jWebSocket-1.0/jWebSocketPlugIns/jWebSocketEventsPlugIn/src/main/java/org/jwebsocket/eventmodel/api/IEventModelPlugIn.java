//	---------------------------------------------------------------------------
//	jWebSocket - IEventModelPlugIn (Community Edition, CE)
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

import java.util.Map;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.s2c.S2CEventNotification;

/**
 *
 * @author Rolando Santamaria Maso
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

	/**
	 *
	 * @return The server connectors list
	 */
	public Map<String, WebSocketConnector> getServerAllConnectors();

	/**
	 *
	 * @param em The EventModel singleton instance to set
	 */
	public void setEm(EventModel em);

	/**
	 * The client API is the plug-in interface for communication with the
	 * clients. Is used like WSDL to allow runtime client plug-ins generation.
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
