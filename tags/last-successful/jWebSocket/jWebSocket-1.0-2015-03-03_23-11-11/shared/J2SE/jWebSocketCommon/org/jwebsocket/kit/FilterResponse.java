//	---------------------------------------------------------------------------
//	jWebSocket - FilterResponse (Community Edition, CE)
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
package org.jwebsocket.kit;

/**
 * Implements the response class to return results from the filter chain to the
 * server.
 *
 * @author Alexander Schulze
 */
public class FilterResponse {

	private boolean isRejected = false;

	/**
	 * Returns if a filter in the filter chain has rejected a message.
	 *
	 * @return the chainAborted
	 */
	public Boolean isRejected() {
		return isRejected;
	}

	/**
	 * Signals that a message has to be rejected and that the filter chain was
	 * aborted.
	 */
	public void rejectMessage() {
		this.isRejected = true;
	}

	/**
	 * Signals that a message may be relayed to further filters, the server or
	 * clients, depending on its direction.
	 */
	public void relayMessage() {
		this.isRejected = false;
	}
}
