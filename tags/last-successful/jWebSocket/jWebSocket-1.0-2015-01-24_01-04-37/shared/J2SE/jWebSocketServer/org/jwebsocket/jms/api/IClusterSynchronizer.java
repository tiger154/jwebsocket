//	---------------------------------------------------------------------------
//	jWebSocket - IClusterSynchronizer interface (Community Edition, CE)
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
package org.jwebsocket.jms.api;

import org.jwebsocket.api.IInitializable;

/**
 * Load balancer instances require to be synchronized in order to allow only one
 * load balancer instance to process a message.
 *
 * @author Rolando Santamaria Maso
 */
public interface IClusterSynchronizer extends IInitializable {

	/**
	 * Returns TRUE if the calling node is allowed to process a message, FALSE
	 * otherwise. Multiples nodes can request permission to process the same
	 * message, but only one should be granted.
	 *
	 * @param aMessageId A custom message id
	 * @return
	 */
	boolean getWorkerTurn(String aMessageId);
}
