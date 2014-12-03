//	---------------------------------------------------------------------------
//	jWebSocket - Filter API (Community Edition, CE)
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

import org.jwebsocket.kit.FilterResponse;

/**
 *
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 */
public interface WebSocketFilter extends ISystemLifecycle {

	/**
	 *
	 * @param aResponse
	 * @param aConnector
	 * @param aPacket
	 */
	void processPacketIn(FilterResponse aResponse, WebSocketConnector aConnector, WebSocketPacket aPacket);

	/**
	 *
	 * @param aResponse
	 * @param aSource
	 * @param aTarget
	 * @param aPacket
	 */
	void processPacketOut(FilterResponse aResponse, WebSocketConnector aSource, WebSocketConnector aTarget, WebSocketPacket aPacket);

	/**
	 *
	 * @param aFilterChain
	 */
	public void setFilterChain(WebSocketFilterChain aFilterChain);

	/**
	 * @return the filterChain
	 */
	public WebSocketFilterChain getFilterChain();

	/**
	 * Returns the filter configuration object based on the configuration file
	 * values
	 *
	 * @return the filter configuration object
	 */
	public FilterConfiguration getFilterConfiguration();

	/**
	 * @return the Id of the filter
	 */
	public String getId();

	/**
	 * @return the name space of the filter
	 */
	public String getNS();

	/**
	 * return the version of the plug-in.
	 *
	 * @return
	 */
	String getVersion();

	/**
	 * set the version of the filter.
	 *
	 * @param aVersion
	 */
	void setVersion(String aVersion);

	/**
	 * return the enabled status of the filter.
	 *
	 * @return
	 */
	boolean getEnabled();

	/**
	 * set the enabled status of the filter.
	 *
	 * @param aEnabled
	 */
	void setEnabled(boolean aEnabled);

	/**
	 * notifies the filter about a change in enabled status.
	 *
	 * @param aEnabled
	 */
	void processEnabled(boolean aEnabled);

	/**
	 *
	 * @return
	 */
	WebSocketServer getServer();
}
