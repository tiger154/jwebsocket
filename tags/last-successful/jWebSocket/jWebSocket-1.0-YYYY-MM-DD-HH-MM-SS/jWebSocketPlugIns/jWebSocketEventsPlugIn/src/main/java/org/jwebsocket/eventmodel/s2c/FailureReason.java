//	---------------------------------------------------------------------------
//	jWebSocket - FailureReason (Community Edition, CE)
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

/**
 *
 * @author Rolando Santamaria Maso
 */
public enum FailureReason {

	/**
	 * The connector gets STOPPED before answer
	 */
	CONNECTOR_STOPPED,
	/**
	 * The s2c event notification is not supported on the client side by the
	 * targeted plug-in
	 */
	EVENT_NOT_SUPPORTED_BY_CLIENT,
	/**
	 * The client response has invalid type or the "isValid" method in the
	 * OnResponse callback has returned FALSE
	 */
	INVALID_RESPONSE,
	/**
	 * The client response take more time than allowed
	 */
	TIMEOUT
}
