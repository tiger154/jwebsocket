//	---------------------------------------------------------------------------
//	jWebSocket - BaseFilter Implementation (Community Edition, CE)
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

import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketFilterChain;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.xml.FilterConfig;
import org.jwebsocket.kit.FilterResponse;

/**
 *
 * @author Alexander Schulze
 */
public class BaseFilter implements WebSocketFilter {
	// every filter has a backward reference to its filter chain

	private String mVersion = null;
	private WebSocketFilterChain mFilterChain = null;
	private FilterConfiguration mConfiguration = null;

	/**
	 *
	 * @param aConfiguration
	 */
	public BaseFilter(FilterConfiguration aConfiguration) {
		mConfiguration = aConfiguration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FilterConfiguration getFilterConfiguration() {
		return mConfiguration;
	}

	@Override
	public String toString() {
		return mConfiguration.getId();
	}

	@Override
	public void processPacketIn(FilterResponse aResponse,
			WebSocketConnector aConnector, WebSocketPacket aPacket) {
	}

	@Override
	public void processPacketOut(FilterResponse aResponse,
			WebSocketConnector aSource, WebSocketConnector aTarget,
			WebSocketPacket aPacket) {
	}

	/**
	 *
	 * @param aFilterChain
	 */
	@Override
	public void setFilterChain(WebSocketFilterChain aFilterChain) {
		mFilterChain = aFilterChain;
	}

	/**
	 * @return the filterChain
	 */
	@Override
	public WebSocketFilterChain getFilterChain() {
		return mFilterChain;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public WebSocketServer getServer() {
		WebSocketServer lServer = null;
		if (mFilterChain != null) {
			lServer = mFilterChain.getServer();
		}
		return lServer;
	}

	/**
	 * @return the id of the filter
	 */
	@Override
	public String getId() {
		return mConfiguration.getId();
	}

	/**
	 * @return the name space of the filter
	 */
	@Override
	public String getNS() {
		return mConfiguration.getNamespace();
	}

	@Override
	public boolean getEnabled() {
		return mConfiguration.getEnabled();
	}

	@Override
	public void setEnabled(boolean aEnabled) {
		Boolean lOldEnabled = mConfiguration.getEnabled();
		mConfiguration = new FilterConfig(mConfiguration.getId(),
				mConfiguration.getName(), mConfiguration.getPackageName(),
				mConfiguration.getJar(), mConfiguration.getNamespace(),
				mConfiguration.getServers(), mConfiguration.getSettings(), aEnabled);
		// notify filter for change of enabled status
		if (aEnabled != lOldEnabled) {
			processEnabled(aEnabled);
		}
	}

	@Override
	public void processEnabled(boolean aEnabled) {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getVersion() {
		return mVersion;
	}

	@Override
	public void setVersion(String aVersion) {
		mVersion = aVersion;
	}

	@Override
	public void systemStarting() throws Exception {
	}

	@Override
	public void systemStarted() throws Exception {
	}

	@Override
	public void systemStopping() throws Exception {
	}

	@Override
	public void systemStopped() throws Exception {
	}
}
