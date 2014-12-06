//	---------------------------------------------------------------------------
//	jWebSocket IConnectorsPacketQueue (Community Edition, CE)
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
package org.jwebsocket.api;

import java.util.List;

/**
 * Component to store incoming or outgoing packets for non real-time client
 * connection types.
 *
 * @author Rolando Santamaria Maso
 */
public interface IConnectorsPacketQueue extends IInitializable {

	/**
	 * Dequeue all packets for a given connector
	 *
	 * @param aConnectorId
	 * @return
	 * @throws Exception
	 */
	List<String> dequeue(String aConnectorId) throws Exception;

	/**
	 * Enqueue a packet for a given connector
	 *
	 * @param aConnectorId
	 * @param aPacket
	 * @throws Exception
	 */
	void enqueue(String aConnectorId, String aPacket) throws Exception;

	/**
	 * Remove all enqueued packets for a given connector
	 *
	 * @param aConnectorId
	 * @throws Exception
	 */
	void clear(String aConnectorId) throws Exception;

	/**
	 * Remove all enqueued packets
	 *
	 * @throws Exception
	 */
	void clearAll() throws Exception;
}
