//	---------------------------------------------------------------------------
//	jWebSocket - ILoadBalancerSynchronizer interface (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
 * load balancer instance to process client messages.
 *
 * @author kyberneees
 */
public interface ILoadBalancerSynchronizer extends IInitializable {

	/**
	 * Returns TRUE if the calling load balancer is allowed to process the
	 * client message, FALSE otherwise. Multiples LB's instances are subscribed
	 * to messages, but only one should be able to process it.
	 *
	 * @param aMessageId The client message id
	 * @return
	 */
	boolean getLoadBalancerTurn(String aMessageId);
}
