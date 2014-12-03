//	---------------------------------------------------------------------------
//	jWebSocket - FilterChain API (Community Edition, CE)
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
import org.jwebsocket.kit.FilterResponse;

/**
 *
 * @author Alexander Schulze
 * @author Marcos Antonio Gonzalez Huerta
 * @author Rolando Santamaria Maso
 */
public interface WebSocketFilterChain extends ISystemLifecycle {

	/**
	 *
	 * @param aFilter
	 */
	void addFilter(WebSocketFilter aFilter);

	/**
	 *
	 * @param aPosition
	 * @param aFilter
	 */
	void addFilter(Integer aPosition, WebSocketFilter aFilter);

	/**
	 *
	 * @param aFilter
	 */
	void removeFilter(WebSocketFilter aFilter);

	/**
	 *
	 * @return
	 */
	List<WebSocketFilter> getFilters();

	/**
	 *
	 * @param aId
	 * @return
	 */
	WebSocketFilter getFilterById(String aId);

	/**
	 *
	 * @param aSource
	 * @param aPacket
	 * @return
	 */
	FilterResponse processPacketIn(WebSocketConnector aSource, WebSocketPacket aPacket);

	/**
	 *
	 * @param aSource
	 * @param aTarget
	 * @param aPacket
	 * @return
	 */
	FilterResponse processPacketOut(WebSocketConnector aSource, WebSocketConnector aTarget, WebSocketPacket aPacket);

	/**
	 *
	 * @return
	 */
	WebSocketServer getServer();
}
