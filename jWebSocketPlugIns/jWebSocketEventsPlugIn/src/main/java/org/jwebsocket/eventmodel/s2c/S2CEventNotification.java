//	---------------------------------------------------------------------------
//	jWebSocket - S2CEventNotification (Community Edition, CE)
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
package org.jwebsocket.eventmodel.s2c;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.eventmodel.exception.InvalidConnectorIdentifier;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class S2CEventNotification {

	private S2CEvent mEvent;
	private S2CEventNotificationHandler mS2CNotificationHandler;

	/**
	 *
	 * @param plugInId The client plug-in identifier
	 * @param aEvent The event to fire in the client
	 * @param aS2CNotificationHandler
	 */
	public S2CEventNotification(String plugInId, S2CEvent aEvent, S2CEventNotificationHandler aS2CNotificationHandler) {
		aEvent.setPlugInId(plugInId);
		mEvent = aEvent;
		mS2CNotificationHandler = aS2CNotificationHandler;
	}

	/**
	 * Send the event to a giving client connector
	 *
	 * @param aConnector The client connector
	 * @param aOnResponse The server on-response callbacks
	 * @throws InvalidConnectorIdentifier
	 */
	public void to(WebSocketConnector aConnector, OnResponse aOnResponse) throws InvalidConnectorIdentifier {
		//Sending the event
		mS2CNotificationHandler.send(mEvent, aConnector, aOnResponse);
	}

	/**
	 * Send the event to a giving client connector
	 *
	 * @param aConnector The client connector
	 * @throws InvalidConnectorIdentifier
	 */
	public void to(WebSocketConnector aConnector) throws InvalidConnectorIdentifier {
		//Sending the event
		mS2CNotificationHandler.send(mEvent, aConnector, null);
	}

	/**
	 * Send the event to a giving client connector
	 *
	 * @param aConnectorId
	 * @param aOnResponse The server on-response callbacks
	 * @throws InvalidConnectorIdentifier
	 */
	public void to(String aConnectorId, OnResponse aOnResponse) throws InvalidConnectorIdentifier {
		//Sending the event
		mS2CNotificationHandler.send(mEvent, aConnectorId, aOnResponse);
	}

	/**
	 * Send the event to a giving client connector
	 *
	 * @param aConnectorId
	 * @throws InvalidConnectorIdentifier
	 */
	public void to(String aConnectorId) throws InvalidConnectorIdentifier {
		//Sending the event
		mS2CNotificationHandler.send(mEvent, aConnectorId, null);
	}
}
