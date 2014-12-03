//	---------------------------------------------------------------------------
//	jWebSocket - IRRPCManager (Community Edition, CE)
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
package org.jwebsocket.eventmodel.api;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.exception.MissingTokenSender;
import org.jwebsocket.eventmodel.rrpc.RRPC;

/**
 *
 * @author Rolando Santamaria Maso
 */
public interface IRRPCManager {

	/**
	 *
	 * @param aConnectorId
	 * @param aRRPC
	 * @throws MissingTokenSender
	 */
	void send(String aConnectorId, RRPC aRRPC) throws MissingTokenSender;

	/**
	 *
	 * @param aConnectorId
	 * @param aRRPC
	 * @param aOnResponse
	 * @throws MissingTokenSender
	 */
	void send(String aConnectorId, RRPC aRRPC, IRRPCOnResponseCallback aOnResponse) throws MissingTokenSender;

	/**
	 *
	 * @param aConnector
	 * @param aRRPC
	 * @param aOnResponse
	 * @throws MissingTokenSender
	 */
	void send(WebSocketConnector aConnector, RRPC aRRPC, IRRPCOnResponseCallback aOnResponse) throws MissingTokenSender;

	/**
	 *
	 * @param aConnector
	 * @param aRRPC
	 * @throws MissingTokenSender
	 */
	void send(WebSocketConnector aConnector, RRPC aRRPC) throws MissingTokenSender;
}
