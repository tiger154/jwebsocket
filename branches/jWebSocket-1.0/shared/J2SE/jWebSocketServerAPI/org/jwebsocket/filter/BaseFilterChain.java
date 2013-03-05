//	---------------------------------------------------------------------------
//	jWebSocket - BaseFilterChain Implementation (Community Edition, CE)
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
package org.jwebsocket.filter;

import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.api.*;
import org.jwebsocket.kit.FilterResponse;

/**
 *
 * @author aschulze
 * @author Marcos Antonio González Huerta (markos0886, UCI)
 */
public class BaseFilterChain implements WebSocketFilterChain {

	private List<WebSocketFilter> mFilters = new FastList<WebSocketFilter>();
	private WebSocketServer mServer = null;

	/**
	 *
	 * @param aServer
	 */
	public BaseFilterChain(WebSocketServer aServer) {
		mServer = aServer;
	}

	/**
	 * @return the server
	 */
	@Override
	public WebSocketServer getServer() {
		return mServer;
	}

	// TODO: Filters are currently organized in a map, which does not allow to specify an order. This needs to be changed!
	/**
	 *
	 * @param aFilter
	 */
	@Override
	public void addFilter(WebSocketFilter aFilter) {
		mFilters.add(aFilter);
		aFilter.setFilterChain(this);
	}

	/**
	 *
	 * @param aFilter
	 */
	@Override
	public void removeFilter(WebSocketFilter aFilter) {
		mFilters.remove(aFilter);
		aFilter.setFilterChain(null);
	}

	/**
	 *
	 * @return
	 */
	@Override
	public List<WebSocketFilter> getFilters() {
		return mFilters;
	}

	/**
	 *
	 * @param aId
	 * @return
	 */
	@Override
	public WebSocketFilter getFilterById(String aId) {
		if (aId != null) {
			for (WebSocketFilter lFilter : mFilters) {
				FilterConfiguration lConfig = lFilter.getFilterConfiguration();
				if (lConfig != null && aId.equals(lConfig.getId())) {
					return lFilter;
				}
			}
		}
		return null;
	}

	/**
	 *
	 * @param aConnector
	 * @param aPacket
	 * @return
	 */
	@Override
	public FilterResponse processPacketIn(WebSocketConnector aConnector, WebSocketPacket aPacket) {
		FilterResponse lResponse = new FilterResponse();
		for (WebSocketFilter lFilter : mFilters) {
			lFilter.processPacketIn(lResponse, aConnector, aPacket);
			if (lResponse.isRejected()) {
				break;
			}
		}
		return lResponse;
	}

	/**
	 *
	 * @param aSource
	 * @param aTarget
	 * @param aPacket
	 * @return
	 */
	@Override
	public FilterResponse processPacketOut(WebSocketConnector aSource, WebSocketConnector aTarget, WebSocketPacket aPacket) {
		FilterResponse lResponse = new FilterResponse();
		for (WebSocketFilter lFilter : mFilters) {
			lFilter.processPacketOut(lResponse, aSource, aTarget, aPacket);
			if (lResponse.isRejected()) {
				break;
			}
		}
		return lResponse;
	}

	/**
	 *
	 * @param aPosition
	 * @param aFilter
	 */
	@Override
	public void addFilter(Integer aPosition, WebSocketFilter aFilter) {
		mFilters.add(aPosition, aFilter);
		aFilter.setFilterChain(this);
	}
}
