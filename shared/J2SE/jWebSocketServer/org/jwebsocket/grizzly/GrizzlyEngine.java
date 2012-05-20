//	---------------------------------------------------------------------------
//	jWebSocket - Grizzly Engine
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.grizzly;

import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.Date;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
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
	private Integer mSessionTimeout = 3000;
	private boolean mIsRunning = false;
	private HttpServer mGrizzlyServer = null;
	private HttpServer mGrizzlySSLServer;

	public GrizzlyEngine(EngineConfiguration aConfiguration) {

		super(aConfiguration);
		try {
			// load the ports from the configuration
			mGrizzlyPort = aConfiguration.getPort();
			mGrizzlySSLPort = aConfiguration.getSSLPort();

			String lEngineContext = aConfiguration.getContext();
			String lEngineApp = aConfiguration.getServlet();
			mSessionTimeout = aConfiguration.getTimeout();


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

//			mGrizzlyServer = WebSocketServer.createServer(mGrizzlyPort);
//			mGrizzlyServer.register("jWebSocketEngine", new GrizzlyWebSocketApplication(this));

			mGrizzlyServer = HttpServer.createSimpleServer(lEngineApp, mGrizzlyPort);
			mGrizzlyServer.getListener("grizzly").registerAddOn(new WebSocketAddOn());

			// HttpHandler lHttpHandler = new	StaticHttpHandler(mDemoRootDirectory);
			// mGrizzlyServer.getServerConfiguration().addHttpHandler(lHttpHandler, mDemoContext);

			// Create encrypted (SSL) server socket for wss:// protocol
			if (mGrizzlySSLPort > 0) {
				// create unencrypted server socket for ws:// protocol
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
						SSLContext lSSLContext = SSLContext.getInstance("TLS");
						KeyManagerFactory lKMF = KeyManagerFactory.getInstance("SunX509");
						KeyStore lKeyStore = KeyStore.getInstance("JKS");

						String lKeyStorePath = JWebSocketConfig.getJWebSocketHome() + "conf/" + JWebSocketConfig.expandEnvAndJWebSocketVars(mKeyStore);
						if (lKeyStorePath != null) {
							char[] lPassword = mKeyStorePassword.toCharArray();
							URL lURL = JWebSocketConfig.getURLFromPath(lKeyStorePath);
							lKeyStore.load(new FileInputStream(lURL.getPath()), lPassword);
							lKMF.init(lKeyStore, lPassword);

							lSSLContext.init(lKMF.getKeyManagers(), null, new java.security.SecureRandom());

							mGrizzlySSLServer = HttpServer.createSimpleServer("/var/www", mGrizzlySSLPort);

							mGrizzlySSLServer.getListener("grizzly").registerAddOn(new WebSocketAddOn());

							mGrizzlySSLServer.getListener("grizzly").setSecure(true);

							SSLEngineConfigurator lSSLEngineConfigurator = new SSLEngineConfigurator(lSSLContext, false, false, false);

							lSSLEngineConfigurator.setEnabledProtocols(new String[]{"TLSv1", "SSLv3"});
							lSSLEngineConfigurator.setProtocolConfigured(true);
							
							String[] lEnabledCipherSuites = {"SSL_RSA_WITH_RC4_128_SHA", "TLS_KRB5_WITH_RC4_128_SHA"};
							
							//cipherSuites 	null means 'use SSLEngine's default.'
							lSSLEngineConfigurator.setEnabledCipherSuites(lEnabledCipherSuites);
							lSSLEngineConfigurator.setCipherConfigured(true);

							mGrizzlySSLServer.getListener("grizzly").
									setSSLEngineConfig(lSSLEngineConfigurator);
						} else {
							mLog.error("SSL engine could not be instantiated: "
									+ "KeyStore '" + mKeyStore + "' not found.");
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
			WebSocketApplication lGrizzlyApplication = new GrizzlyWebSocketApplication(this);
			// Registering grizzly jWebSocket Wrapper Application into grizzly WebSocketEngine
			WebSocketEngine.getEngine().register(lGrizzlyApplication);

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
		//The BaseEngine removes the connector from the list
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

	private SSLEngineConfigurator createSSLConfig() throws Exception {
		final SSLContextConfigurator sslContextConfigurator =
				new SSLContextConfigurator();
		/*
		 * final ClassLoader cl = SuspendTest.class.getClassLoader();
		 *
		 * // override system properties final URL cacertsUrl =
		 * cl.getResource("ssltest-cacerts.jks"); if (cacertsUrl != null) {
		 * sslContextConfigurator.setTrustStoreFile(cacertsUrl.getFile());
		 * sslContextConfigurator.setTrustStorePass("changeit"); }
		 *
		 * // override system properties final URL keystoreUrl =
		 * cl.getResource("ssltest-keystore.jks");
		 *
		 *
		 * if (keystoreUrl != null) {
		 * sslContextConfigurator.setKeyStoreFile(keystoreUrl.getFile());
		 * sslContextConfigurator.setKeyStorePass("changeit"); }
		 */
		sslContextConfigurator.setKeyStoreFile(mKeyStore);
		sslContextConfigurator.setKeyStorePass(mKeyStorePassword);

		return new SSLEngineConfigurator(sslContextConfigurator.createSSLContext(),
				false, false, false);
	}
}
