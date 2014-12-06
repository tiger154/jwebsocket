//	---------------------------------------------------------------------------
//	jWebSocket - Base Engine Implementation (Community Edition, CE)
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
package org.jwebsocket.engines;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javolution.util.FastMap;
import org.jwebsocket.api.*;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;

/**
 * Provides the basic implementation of the jWebSocket engines. The
 * {@literal BaseEngine} is supposed to be used as ancestor for the engine
 * implementations like e.g. the {@literal TCPEngine} or the {@literal NettyEngine}.
 *
 * @author Alexander Schulze
 */
public abstract class BaseEngine implements WebSocketEngine {

	private final Map<String, WebSocketServer> mServers
			= new FastMap<String, WebSocketServer>().shared();
	private final FastMap<String, WebSocketConnector> mConnectors
			= new FastMap<String, WebSocketConnector>().shared();
	private int mSessionTimeout = JWebSocketCommonConstants.DEFAULT_TIMEOUT;
	private EngineConfiguration mConfiguration;
	private Runnable mSystemStoppingNotificationStragety;

	/**
	 *
	 * @param aConfiguration
	 */
	public BaseEngine(EngineConfiguration aConfiguration) {
		mConfiguration = aConfiguration;
	}

	@Override
	public void setEngineConfiguration(EngineConfiguration aConfiguration) {
		mConfiguration = aConfiguration;
	}

	@Override
	public void startEngine() throws WebSocketException {
		// this method will be overridden by engine implementations.
		// The implementation will notify server that the engine has started
		// Don't do this here: engineStarted();
	}

	@Override
	public WebSocketConnector getConnectorById(String aConnectorId) {
		return getConnectors().get(aConnectorId);
	}

	@Override
	public void stopEngine(CloseReason aCloseReason) throws WebSocketException {
		// stop all connectors of this engine
		for (final WebSocketConnector lConnector : mConnectors.values()) {
			try {
				lConnector.stopConnector(aCloseReason);
			} catch (Exception lEx) {
			}
		}
		// this method will be overridden by engine implementations.
		// The implementation will notify server that the engine has stopped
		// Don't do this here: engineStopped();
	}

	@Override
	public void engineStarted() {
		// notify servers that the engine has started
		for (final WebSocketServer lServer : mServers.values()) {
			lServer.engineStarted(this);
		}
	}

	@Override
	public void engineStopped() {
		// notify servers that the engine has stopped
		for (final WebSocketServer lServer : mServers.values()) {
			lServer.engineStopped(this);
		}
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		// notify servers that a connector has started
		for (final WebSocketServer lServer : mServers.values()) {
			lServer.connectorStarted(aConnector);
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector,
			CloseReason aCloseReason) {
		// once a connector stopped remove it from the list of connectors
		// FastMap ensures that the entry is being kept in shared mode
		getConnectors().remove(aConnector.getId());

		// notify servers that a connector has stopped
		for (final WebSocketServer lServer : mServers.values()) {
			lServer.connectorStopped(aConnector, aCloseReason);
		}
	}

	@Override
	public boolean isAlive() {
		return false;
	}

	@Override
	public void processPacket(WebSocketConnector aConnector,
			WebSocketPacket aDataPacket) {
		Map<String, WebSocketServer> lServers = getServers();
		for (final WebSocketServer lServer : lServers.values()) {
			lServer.processPacket(this, aConnector, aDataPacket);
		}
	}

	@Override
	public void sendPacket(WebSocketConnector aConnector,
			WebSocketPacket aDataPacket) {
		aConnector.sendPacket(aDataPacket);
	}

	@Override
	public void broadcastPacket(WebSocketConnector aSource,
			WebSocketPacket aDataPacket) {
		for (final WebSocketConnector lConnector : mConnectors.values()) {
			lConnector.sendPacket(aDataPacket);
		}
	}

	@Override
	public void addConnector(WebSocketConnector aConnector) {
		mConnectors.put(aConnector.getId(), aConnector);
	}

	@Override
	public void removeConnector(WebSocketConnector aConnector) {
		mConnectors.remove(aConnector.getId());
	}

	@Override
	@Deprecated
	public int getSessionTimeout() {
		return mSessionTimeout;
	}

	@Override
	@Deprecated
	public void setSessionTimeout(int aSessionTimeout) {
		this.mSessionTimeout = aSessionTimeout;
	}

	@Override
	@Deprecated
	public int getMaxFrameSize() {
		return mConfiguration.getMaxFramesize();
	}

	@Override
	public Map<String, WebSocketConnector> getConnectors() {
		return mConnectors;
	}

	@Override
	public WebSocketConnector getConnectorByRemotePort(int aRemotePort) {
		for (WebSocketConnector lConnector : mConnectors.values()) {
			if (lConnector.getRemotePort() == aRemotePort) {
				return lConnector;
			}
		}
		return null;
	}

	@Override
	public Map<String, WebSocketServer> getServers() {
		return mServers; // (FastMap) (servers.unmodifiable());
	}

	@Override
	public void addServer(WebSocketServer aServer) {
		this.mServers.put(aServer.getId(), aServer);
	}

	@Override
	public void removeServer(WebSocketServer aServer) {
		this.mServers.remove(aServer.getId());
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return mConfiguration.getId();
	}

	@Override
	public EngineConfiguration getConfiguration() {
		return mConfiguration;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public Integer getMaxConnections() {
		return mConfiguration.getMaxConnections();
	}

	/**
	 *
	 * @param aConnector
	 * @param aPacketSize
	 * @return
	 */
	public static String getUnsupportedIncomingPacketSizeMsg(WebSocketConnector aConnector, int aPacketSize) {
		return "Incoming packet from connector '" + aConnector.getId() + "'"
				+ " cannot be processed!"
				+ " Supported packet size: " + aConnector.getMaxFrameSize()
				+ ", received: " + aPacketSize;
	}

	@Override
	public void systemStarting() throws Exception {
	}

	@Override
	public void systemStarted() throws Exception {
	}

	@Override
	public void setSystemStoppingNotificationStrategy(Runnable aStrategy) {
		mSystemStoppingNotificationStragety = aStrategy;
	}

	@Override
	public void systemStopping() throws Exception {
		if (getConfiguration().isNotifySystemStopping()) {
			// executing notification strategy
			if (null != mSystemStoppingNotificationStragety) {
				mSystemStoppingNotificationStragety.run();
			}
		}

		// close all internal connectors in engine
		for (final WebSocketConnector lConnector : mConnectors.values()) {
			if (lConnector.isInternal()) {
				try {
					lConnector.stopConnector(CloseReason.SERVER);
				} catch (Exception ex) {
				}
			}
		}

	}

	@Override
	public void systemStopped() throws Exception {
	}

	@Override
	public Map<String, WebSocketConnector> getSharedSessionConnectors(String aSessionId) {
		Map<String, WebSocketConnector> lShared = new HashMap<String, WebSocketConnector>();

		Collection<WebSocketConnector> lConnectors = getConnectors().values();
		for (WebSocketConnector lConnector : lConnectors) {
			if (aSessionId.equals(lConnector.getSession().getSessionId())) {
				lShared.put(lConnector.getId(), lConnector);
			}
		}

		return lShared;
	}

	@Override
	public Long getConnectorsCount() {
		return new Long(getConnectors().size());
	}

	@Override
	public Iterator<WebSocketConnector> getConnectorsIterator() {
		return getConnectors().values().iterator();
	}
}
