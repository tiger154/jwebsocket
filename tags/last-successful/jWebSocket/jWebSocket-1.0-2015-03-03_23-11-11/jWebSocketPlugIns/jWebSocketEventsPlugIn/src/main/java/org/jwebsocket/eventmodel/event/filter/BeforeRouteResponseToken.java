//	---------------------------------------------------------------------------
//	jWebSocket - BeforeRouteResponseToken (Community Edition, CE)
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
package org.jwebsocket.eventmodel.event.filter;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.observable.Event;

/**
 * Fired before send the response token to the client
 *
 * @author Rolando Santamaria Maso
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

	/**
	 *
	 * @return
	 */
	public WebSocketConnector getConnector() {
		return mConnector;
	}

	/**
	 *
	 * @param aConnector
	 */
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
