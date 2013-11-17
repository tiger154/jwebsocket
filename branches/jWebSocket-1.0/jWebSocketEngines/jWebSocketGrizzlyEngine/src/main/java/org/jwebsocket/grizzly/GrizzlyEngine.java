//	---------------------------------------------------------------------------
//	jWebSocket - Grizzly Engine (Community Edition, CE)
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
package org.jwebsocket.grizzly;

import java.util.Date;
import javax.net.ssl.SSLContext;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.tcp.nio.Util;

/**
 *
 * @author vbarzana, aschulze
 */
public class GrizzlyEngine extends BaseEngine {

	private static Logger mLog = Logging.getLogger();
	private static Integer mGrizzlyPort = 80;
	private static Integer mGrizzlySSLPort = 443;
	private String mKeyStore = JWebSocketServerConstants.JWEBSOCKET_KEYSTORE;
	private String mKeyStorePassword = JWebSocketServerConstants.JWEBSOCKET_KS_DEF_PWD;
	private final String DOCUMENT_ROOT_CONFIG_KEY = "document_root";
	private Integer mSessionTimeout = 3000;
	private Integer mTimeout = 3000;
	private boolean mIsRunning = false;
	private HttpServer mGrizzlyServer = null;
	private HttpServer mGrizzlySSLServer;

	/**
	 *
	 * @param aConfiguration
	 */
	public GrizzlyEngine(EngineConfiguration aConfiguration) {

		super(aConfiguration);
		try {
			// load the ports from the configuration
			mGrizzlyPort = aConfiguration.getPort();
			mGrizzlySSLPort = aConfiguration.getSSLPort();
			mKeyStore = aConfiguration.getKeyStore();
			mKeyStorePassword = aConfiguration.getKeyStorePassword();
			mTimeout = aConfiguration.getTimeout();
			mSessionTimeout = aConfiguration.getTimeout();
			
			if(mTimeout.equals(0)){
				mTimeout = 3000;
				mSessionTimeout = 3000;
			}
			String lEngineContext = aConfiguration.getContext();
			String lEngineApp = aConfiguration.getServlet();

			if (mGrizzlySSLPort == 0) {
				mGrizzlySSLPort = JWebSocketCommonConstants.DEFAULT_SSLPORT;
			}
			if (mGrizzlyPort == 0) {
				mGrizzlyPort = JWebSocketCommonConstants.DEFAULT_PORT;
			}

			if (lEngineContext == null) {
				lEngineContext = "/jWebSocket";
			}

			if (lEngineApp == null) {
				lEngineApp = "/jWebSocket";
			}

			if (mLog.isDebugEnabled()) {
				mLog.debug("Instantiating Grizzly server '"
						+ "port " + mGrizzlyPort
						+ ", ssl-port " + mGrizzlySSLPort
						+ ", context: '" + lEngineContext
						+ "', servlet: '" + lEngineApp + "'...");
			}

			// setting the socket server hostname
//			String lHostname = getConfiguration().getHostname();
//			if (null != lHostname) {
//				mGrizzlyServer = WebSocketServer.createServer(lHostname, mGrizzlyPort);
//			} else {
//				mGrizzlyServer = WebSocketServer.createServer(mGrizzlyPort);
//			}
//			mGrizzlyServer.register("jWebSocketEngine", new GrizzlyWebSocketApplication(this));
			String lDocumentRoot = JWebSocketConfig.getJWebSocketHome() + "web/";
			if (getConfiguration().getSettings().containsKey(DOCUMENT_ROOT_CONFIG_KEY)) {
				lDocumentRoot = getConfiguration().getSettings().get(DOCUMENT_ROOT_CONFIG_KEY).toString();
			}
			mGrizzlyServer = HttpServer.createSimpleServer(lDocumentRoot, mGrizzlyPort);
			final WebSocketAddOn lWebSocketAddon = new WebSocketAddOn();
			lWebSocketAddon.setTimeoutInSeconds(mTimeout);
			for (NetworkListener lListener : mGrizzlyServer.getListeners()) {
				lListener.registerAddOn(lWebSocketAddon);
			}

			// Create encrypted (SSL) server socket for wss:// protocol
			if (mGrizzlySSLPort > 0) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Trying to initiate SSL on port " + mGrizzlySSLPort + "...");
				}
				if (mKeyStore != null && !mKeyStore.isEmpty()
						&& mKeyStorePassword != null && !mKeyStorePassword.isEmpty()) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Using keystore " + mKeyStore + "...");
						mLog.debug("Starting Grizzly SSL engine '"
								+ getId()
								+ "' at port " + mGrizzlySSLPort + ","
								+ " with default timeout "
								+ (mSessionTimeout > 0 ? mSessionTimeout + "ms" : "infinite")
								+ "...");
					}
					try {
						String lKeyStorePath = JWebSocketConfig.expandEnvAndJWebSocketVars(mKeyStore);
						SSLContext lSSLContext = Util.createSSLContext(lKeyStorePath, mKeyStorePassword);

						mGrizzlySSLServer = HttpServer.createSimpleServer(lDocumentRoot, mGrizzlySSLPort);

//						mGrizzlySSLServer.getListener("grizzly").registerAddOn(new WebSocketAddOn());
//						mGrizzlySSLServer.getListener("grizzly").setSecure(true);

						SSLEngineConfigurator lSSLEngineConfigurator = new SSLEngineConfigurator(lSSLContext, false, false, false);
						lSSLEngineConfigurator.setEnabledProtocols(new String[]{"TLSv1", "SSLv3"});
//						lSSLEngineConfigurator.setEnabledProtocols(new String[]{"TLS"});
						lSSLEngineConfigurator.setProtocolConfigured(true);

//						String[] lEnabledCipherSuites = {"SSL_RSA_WITH_RC4_128_SHA", "TLS_KRB5_WITH_RC4_128_SHA"};
						//cipherSuites 	null means 'use SSLEngine's default.'
//						lSSLEngineConfigurator.setEnabledCipherSuites(lEnabledCipherSuites);
						lSSLEngineConfigurator.setCipherConfigured(true);
						lSSLEngineConfigurator.setNeedClientAuth(false);
						WebSocketAddOn lSSLWebSocketAddon = new WebSocketAddOn();
						lSSLWebSocketAddon.setTimeoutInSeconds(mTimeout);
						
						for (NetworkListener lListener : mGrizzlySSLServer.getListeners()) {
							lListener.registerAddOn(lSSLWebSocketAddon);
							lListener.setSecure(true);
							lListener.setSSLEngineConfig(lSSLEngineConfigurator);
						}

					} catch (Exception lEx) {
						mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating SSL engine"));
					}
				} else {
					mLog.error("SSL engine could not be instantiated due to missing configuration,"
							+ " please set sslport, keystore and password options.");
				}
			}

			// The WebSocketApplication will control the incoming and
			//outgoing flow, connection, listeners, etc...
			final WebSocketApplication lGrizzlyApplication = new GrizzlyWebSocketApplication(this);
			// Registering grizzly jWebSocket Wrapper Application into grizzly WebSocketEngine
			WebSocketEngine.getEngine().register("/jWebSocket", "/jWebSocket", lGrizzlyApplication);

		} catch (Exception lEx) {
			mLog.error(lEx.getMessage());
		}
	}

	@Override
	public void startEngine()
			throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting Grizzly engine '" + getId() + "...");
		}

		super.startEngine();

		try {
			mGrizzlyServer.start();
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " instantiating Embedded Grizzly Server: " + lEx.getMessage());
		}
		try {
			mGrizzlySSLServer.start();
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " instantiating Embedded Grizzly SSL Server: " + lEx.getMessage());
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Grizzly Server '"
					+ "' sucessfully instantiated at port "
					+ mGrizzlyPort + ", SSL port " + mGrizzlySSLPort + "...");
		}

		mIsRunning = true;

		if (mLog.isInfoEnabled()) {
			mLog.info("Grizzly engine '" + getId() + "' started.");
		}

		// fire the engine start event
		engineStarted();
	}

	@Override
	public void stopEngine(CloseReason aCloseReason)
			throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping Grizzly ' engine '"
					+ getId() + "...");
		}

		// resetting "isRunning" causes engine listener to terminate
		mIsRunning = false;
		// inherited method stops all connectors

		long lStarted = new Date().getTime();
		int lNumConns = getConnectors().size();
		super.stopEngine(aCloseReason);

		try {
			if (mGrizzlyServer != null) {
				mGrizzlyServer.stop();
				if (mLog.isDebugEnabled()) {
					mLog.debug("Grizzly successfully stopped.");
				}
			} else {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Grizzly not yet started, properly terminated.");
				}
				return;
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " stopping Grizzly Server: "
					+ lEx.getMessage());
		}

		// now wait until all connectors have been closed properly
		// or timeout exceeds...
		try {
			while (getConnectors().size() > 0 && new Date().getTime() - lStarted < 10000) {
				Thread.sleep(250);
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		if (mLog.isDebugEnabled()) {
			long lDuration = new Date().getTime() - lStarted;
			int lRemConns = getConnectors().size();
			if (lRemConns > 0) {
				mLog.warn(lRemConns + " of " + lNumConns
						+ " Grizzly connectors '" + getId()
						+ "' did not stop after " + lDuration + "ms.");
			} else {
				mLog.debug(lNumConns
						+ " Grizzly connectors '" + getId()
						+ "' stopped after " + lDuration + "ms.");
			}
		}
		// fire the engine stopped event
		engineStopped();
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Detected new Grizzly connector at port "
					+ aConnector.getRemotePort() + ".");
		}
		getConnectors().put(aConnector.getId(), aConnector);
		super.connectorStarted(aConnector);
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Detected stopped Grizzly connector at port "
					+ aConnector.getRemotePort() + ".");
		}

		super.connectorStopped(aConnector, aCloseReason);
	}

	/*
	 * Returns {@code true} if Grizzly engine is running or {@code false}
	 * otherwise. The alive status represents the state of the Grizzly engine
	 * listener thread.
	 */
	@Override
	public boolean isAlive() {
		return mIsRunning;
	}
}
