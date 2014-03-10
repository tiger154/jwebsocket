//	---------------------------------------------------------------------------
//	jWebSocket - ClientCacheAspectStatus (Community Edition, CE)
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
package org.jwebsocket.eventmodel.event.system;

import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 * Indicate to the server if the client cache aspect is enabled
 * <br>
 * This event must to be fired at the beginning of the connection, is used to
 * keep updated the client cache if the server cache change
 *
 * @author Rolando Santamaria Maso
 */
public class ClientCacheAspectStatus extends C2SEvent {

	private boolean enabled;

	/**
	 * @return <tt>TRUE</tt> if the client cache aspect is enabled,
	 * <tt>FALSE</tt> otherwise
	 */
	public boolean isEnabled() {
		return getArgs().getBoolean("enabled");
	}
}
