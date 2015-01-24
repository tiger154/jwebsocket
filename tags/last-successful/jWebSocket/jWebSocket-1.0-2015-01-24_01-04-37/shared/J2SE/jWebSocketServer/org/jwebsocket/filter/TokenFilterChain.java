//	---------------------------------------------------------------------------
//	jWebSocket - TokenFilterChain (Community Edition, CE)
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
package org.jwebsocket.filter;

import java.util.List;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.kit.ChangeType;
import org.jwebsocket.kit.FilterResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 */
public class TokenFilterChain extends BaseFilterChain {

	private static Logger mLog = Logging.getLogger();

	/**
	 *
	 * @param aServer
	 */
	public TokenFilterChain(WebSocketServer aServer) {
		super(aServer);
	}

	/**
	 * @return the server
	 */
	@Override
	public TokenServer getServer() {
		return (TokenServer) super.getServer();
	}

	/**
	 *
	 * @param aFilter
	 */
	@Override
	public void addFilter(WebSocketFilter aFilter) {
		if (aFilter != null) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Adding token filter " + aFilter + "...");
			}
			super.addFilter(aFilter);
		}
	}

	/**
	 *
	 * @param aFilter
	 */
	@Override
	public void removeFilter(WebSocketFilter aFilter) {
		if (aFilter != null) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Removing token filter " + aFilter + "...");
			}
			super.removeFilter(aFilter);
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aDataPacket
	 * @return
	 */
	@Override
	public FilterResponse processPacketIn(WebSocketConnector aConnector, WebSocketPacket aDataPacket) {
		// FilterResponse lFilterResponse = new FilterResponse();
		// return lFilterResponse;
		return null;
	}

	/**
	 *
	 * @param aSource
	 * @param aTarget
	 * @param aDataPacket
	 * @return
	 */
	@Override
	public FilterResponse processPacketOut(WebSocketConnector aSource, WebSocketConnector aTarget, WebSocketPacket aDataPacket) {
		// FilterResponse lFilterResponse = new FilterResponse();
		// return lFilterResponse;
		return null;
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 * @return
	 */
	public FilterResponse processTokenIn(WebSocketConnector aConnector, Token aToken) {
		FilterResponse lFilterResponse = new FilterResponse();
		for (WebSocketFilter lFilter : getFilters()) {
			if (lFilter.getEnabled()) {
				try {
					((TokenFilter) lFilter).processTokenIn(lFilterResponse, aConnector, aToken);
				} catch (Exception lEx) {
					mLog.error(lEx.getClass().getSimpleName()
							+ " in incoming filter: " + lFilter.getId()
							+ ": " + lEx.getMessage());
				}
				if (lFilterResponse.isRejected()) {
					break;
				}
			}
		}
		return lFilterResponse;
	}

	/**
	 *
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 * @return
	 */
	public FilterResponse processTokenOut(WebSocketConnector aSource, WebSocketConnector aTarget, Token aToken) {
		FilterResponse lFilterResponse = new FilterResponse();
		for (WebSocketFilter lFilter : getFilters()) {
			if (lFilter.getEnabled()) {
				try {
					((TokenFilter) lFilter).processTokenOut(lFilterResponse, aSource, aTarget, aToken);
				} catch (Exception lEx) {
					mLog.error(lEx.getClass().getSimpleName()
							+ " in outgoing filter: " + lFilter.getId()
							+ ": " + lEx.getMessage());
				}
				if (lFilterResponse.isRejected()) {
					break;
				}
			}
		}
		return lFilterResponse;
	}

	/**
	 *
	 * @param aFilter
	 * @param aReasonOfChange
	 * @param aVersion
	 * @param aReason
	 * @return
	 */
	public Boolean reloadFilter(WebSocketFilter aFilter, Token aReasonOfChange, String aVersion, String aReason) {
		List<WebSocketFilter> lFilters = getFilters();

		for (int i = 0; i < lFilters.size(); i++) {
			if (lFilters.get(i).getId().equals(aFilter.getId())) {
				aFilter.setFilterChain(this);
				lFilters.get(i).setEnabled(false);
				((TokenFilter) lFilters.get(i)).createReasonOfChange(aReasonOfChange, ChangeType.UPDATED, aVersion, aReason);
				lFilters.set(i, aFilter);
				return true;
			}
		}
		return false;
	}
}
