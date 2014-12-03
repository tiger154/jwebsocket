//	---------------------------------------------------------------------------
//	jWebSocket - Jetty Engine (Community Edition, CE)
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
package org.jwebsocket.jetty;

import java.net.InetSocketAddress;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author Alexander Schulze
 */
public class JettyEngine extends BaseEngine {

	private static final Logger mLog = Logging.getLogger();
	private boolean mIsRunning = false;
	private Server mJettyServer = null;

	/**
	 *
	 * @param aConfiguration
	 */
	public JettyEngine(EngineConfiguration aConfiguration) {

		super(aConfiguration);

		// load the ports
		Integer lPort = aConfiguration.getPort();
		Integer lSSLPort = aConfiguration.getSSLPort();

		// If ports are 0 use the WebSocket Servlet capabilities
		// of the Jetty Engine and do not instantiate a separate engine here!
		// Caution! It is mandatory to load the jWebSocket Servlet in the
		// web.xml or webdefault.xml of the Jetty server!
		if (null == lPort || 0 == lPort) {
			// fire the engine start event
			engineStarted();
			if (mLog.isDebugEnabled()) {
				mLog.debug("Using Jetty "
						+ Server.getVersion()
						+ "' configured by jetty.xml...");
			}
			return;
		}

		String lContext = aConfiguration.getContext();
		if (lContext == null) {
			lContext = "/";
		}
		String lServlet = aConfiguration.getServlet();
		if (lServlet == null) {
			lServlet = "/*";
		}
		try {
			// create Jetty server
			if (mLog.isDebugEnabled()) {
				mLog.debug("Instantiating Jetty server '"
						+ Server.getVersion() + "' at "
						+ "port " + lPort
						+ ", ssl-port " + lSSLPort
						+ ", context: '" + lContext
						+ "', servlet: '" + lServlet + "'...");
			}

			// setting the socket server hostname
			String lHostname = getConfiguration().getHostname();
			if (null != lHostname) {
				mJettyServer = new Server(InetSocketAddress.createUnresolved(lHostname, lPort));
			} else {
				mJettyServer = new Server(lPort);
			}

			SslSelectChannelConnector lSSLConnector = new SslSelectChannelConnector();
			String lKeyStore = JWebSocketConfig.expandEnvVarsAndProps(getConfiguration().getKeyStore());
			if (mLog.isDebugEnabled()) {
				mLog.debug("Loading SSL cert from keystore '" + lKeyStore + "'...");
			}
			lSSLConnector.setPort(lSSLPort);
			lSSLConnector.getSslContextFactory().setKeyStorePath(lKeyStore);
			lSSLConnector.getSslContextFactory().setKeyStorePassword(getConfiguration().getKeyStorePassword());
			mJettyServer.addConnector(lSSLConnector);

			ServletContextHandler lServletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
			lServletContext.setContextPath(lContext);
			mJettyServer.setHandler(lServletContext);

			ServletHolder lServletHolder = new ServletHolder(new JettyServlet(this));
			lServletContext.addServlet(lServletHolder, lServlet);

			mJettyServer.setStopAtShutdown(true);
			if (mLog.isDebugEnabled()) {
				mLog.debug("Starting embedded Jetty Server '"
						+ Server.getVersion() + "'...");
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " Instantiating Embedded Jetty Server '"
					+ Server.getVersion() + "': "
					+ lEx.getMessage());
		}
	}

	@Override
	public void startEngine() throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting Jetty '" + Server.getVersion() + "' engine '"
					+ getId()
					+ "...");
		}

		try {
			mJettyServer.start();

			if (mLog.isDebugEnabled()) {
				mLog.debug("Jetty Server '" + Server.getVersion()
						+ "' sucessfully instantiated at port "
						+ getConfiguration().getPort() + ", SSL port "
						+ getConfiguration().getSSLPort() + "...");
			}

			super.startEngine();

			if (mLog.isInfoEnabled()) {
				mLog.info("Jetty '"
						+ Server.getVersion()
						+ "' engine '"
						+ getId()
						+ "' started.");
			}

			// fire the engine start event
			engineStarted();
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " Starting Embedded Jetty Server '"
					+ Server.getVersion() + "': "
					+ lEx.getMessage());
		}
	}

	@Override
	public void stopEngine(CloseReason aCloseReason)
			throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping Jetty '"
					+ Server.getVersion()
					+ "' engine '"
					+ getId() + "...");
		}

		// resetting "isRunning" causes engine listener to terminate
		mIsRunning = false;
		super.stopEngine(aCloseReason);

		try {
			if (mJettyServer != null) {
				mJettyServer.stop();
				if (mLog.isDebugEnabled()) {
					mLog.debug("Jetty '"
							+ Server.getVersion()
							+ " successfully stopped.");
				}
			} else {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Jetty '"
							+ Server.getVersion()
							+ " not yet started, properly terminated.");
				}
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " stopping Jetty Server '"
					+ Server.getVersion() + "': "
					+ lEx.getMessage());
		}

		// fire the engine stopped event
		engineStopped();
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Detected new connector at port "
					+ aConnector.getRemotePort() + ".");
		}
		super.connectorStarted(aConnector);
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Detected stopped connector at port "
					+ aConnector.getRemotePort() + ".");
		}
		super.connectorStopped(aConnector, aCloseReason);
	}

	/*
	 * Returns {@code true} if the TCP engine is running or {@code false}
	 * otherwise. The alive status represents the state of the TCP engine
	 * listener thread.
	 */
	@Override
	public boolean isAlive() {
		return mIsRunning;
	}
}
