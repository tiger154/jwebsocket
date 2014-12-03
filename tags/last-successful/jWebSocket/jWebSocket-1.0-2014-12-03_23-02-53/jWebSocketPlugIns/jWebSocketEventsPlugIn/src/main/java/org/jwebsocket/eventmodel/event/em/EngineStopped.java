//	---------------------------------------------------------------------------
//	jWebSocket - EngineStopped (Community Edition, CE)
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

import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 * An engine gets stopped
 *
 * @author Rolando Santamaria Maso
 */
public class EngineStopped extends C2SEvent {

	private WebSocketEngine mEngine;

	/**
	 * @return the engine
	 */
	public WebSocketEngine getEngine() {
		return mEngine;
	}

	/**
	 * @param aEngine the engine to set
	 */
	public void setEngine(WebSocketEngine aEngine) {
		this.mEngine = aEngine;
	}
}
