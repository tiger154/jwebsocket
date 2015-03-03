//	---------------------------------------------------------------------------
//	jWebSocket - BeforeProcessEvent (Community Edition, CE)
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
package org.jwebsocket.eventmodel.event.em;

import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 * Fired before process events from the client
 *
 * @author Rolando Santamaria Maso
 */
public class BeforeProcessEvent extends C2SEvent {

	private C2SEvent mEvent;

	/**
	 * @return The event from the client to process
	 */
	public C2SEvent getEvent() {
		return mEvent;
	}

	/**
	 * @param aEvent The event from the client to process
	 */
	public void setEvent(C2SEvent aEvent) {
		this.mEvent = aEvent;
	}
}
