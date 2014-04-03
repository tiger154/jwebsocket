//	---------------------------------------------------------------------------
//	jWebSocket - IJMSResponseListener (Community Edition, CE)
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
package org.jwebsocket.jms.endpoint;

import javax.jms.Message;

/**
 * Interface for listeners to responses on low level JMS requests. A response is
 * assigned to a previous request by using the JMS Correlation ID. Messages with
 * the same correlation id like the request are associated as responses to that
 * request.
 *
 * @author Rolando Santamaria Maso, Alexander Schulze
 */
public interface IJMSResponseListener {

	/**
	 * Called when a response message arrives. The response is identified by the
	 * same correlation id like the request.
	 *
	 * @param aReponse
	 * @param aMessage
	 */
	void onReponse(String aReponse, Message aMessage);

	/**
	 * Called when the sent token processing has timed out on the remote
	 * endpoint. No response message can be given here in the arguments.
	 *
	 */
	void onTimeout();

}
