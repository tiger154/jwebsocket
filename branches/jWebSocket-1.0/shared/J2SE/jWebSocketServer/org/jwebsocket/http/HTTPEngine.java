//	---------------------------------------------------------------------------
//	jWebSocket - Tomcat Engine (Community Edition, CE)
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
package org.jwebsocket.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;

/**
 * Suppose to be implemented by final HTTP based engines
 *
 * @author Rolando Santamaria Maso
 */
public abstract class HTTPEngine extends BaseEngine {

	private static final Logger mLog = Logging.getLogger();
	private boolean mIsRunning = false;
	private IConnectorsManager mConnectorsManager;
	public static final String NS = "org.jwebsocket.engine.http";

	/**
	 *
	 * @param aConfiguration
	 */
	public HTTPEngine(EngineConfiguration aConfiguration) {
		super(aConfiguration);
	}

	public IConnectorsManager getConnectorsManager() {
		return mConnectorsManager;
	}

	public void setConnectorsManager(IConnectorsManager aConnectorsManager) {
		mConnectorsManager = aConnectorsManager;
		mConnectorsManager.setEngine(this);
	}

	@Override
	public Map<String, WebSocketConnector> getConnectors() {
		try {
			return mConnectorsManager.getAll();
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}
	}

	@Override
	public void startEngine() throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting HTTP engine '" + getId() + "...");
		}

		mIsRunning = true;
		super.startEngine();

		if (mLog.isInfoEnabled()) {
			mLog.info("HTTP engine '" + getId() + "' started.");
		}

		// fire the engine start event
		engineStarted();
	}

	@Override
	public void stopEngine(CloseReason aCloseReason) throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping HTTP engine '" + getId() + "...");
		}

		mIsRunning = false;

		try {
			mConnectorsManager.shutdown();
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "stopping connectors manager"));
		}

		// fire the engine stopped event
		engineStopped();
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		try {
			mConnectorsManager.remove(aConnector.getId());
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}

		// notify servers that a connector has stopped
		for (final WebSocketServer lServer : getServers().values()) {
			lServer.connectorStopped(aConnector, aCloseReason);
		}
	}

	@Override
	public boolean isAlive() {
		return mIsRunning;
	}

	@Override
	public WebSocketConnector getConnectorById(String aConnectorId) {
		try {
			return mConnectorsManager.getConnectorById(aConnectorId);
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}
	}

	@Override
	public Map<String, WebSocketConnector> getSharedSessionConnectors(String aSessionId) {
		try {
			return mConnectorsManager.getSharedSession(aSessionId);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "getting connectors that share the session"));
			return new HashMap<String, WebSocketConnector>();
		}
	}

	@Override
	public Long getConnectorsCount() {
		return mConnectorsManager.count();
	}

	@Override
	public Iterator<WebSocketConnector> getConnectorsIterator() {
		return mConnectorsManager.getIterator();
	}
}
