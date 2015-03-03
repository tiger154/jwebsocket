//	---------------------------------------------------------------------------
//	jWebSocket - S2CEventNotSupportedOnClient (Community Edition, CE)
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

import org.jwebsocket.eventmodel.annotation.ImportFromToken;
import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 * The target event to notify on the client-side is not supported
 *
 * @author Rolando Santamaria Maso
 */
public class S2CEventNotSupportedOnClient extends C2SEvent {

	/**
	 * @return The request identifier
	 */
	public String getReqId() {
		return getArgs().getString("req_id");
	}

	/**
	 * @param aReqId The request identifier
	 */
	@ImportFromToken(key = "req_id")
	public void setReqId(String aReqId) {
		getArgs().setString("req_id", aReqId);
	}
}
