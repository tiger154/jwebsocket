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
package org.jwebsocket.tomcat;

import java.io.File;
import java.util.Map;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.log4j.Logger;
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
 * @author Rolando Santamaria Maso
 */
public class TomcatEngine extends BaseEngine {

	private static final Logger mLog = Logging.getLogger();
	private boolean mIsRunning = false;
	private Tomcat mTomcat = null;
	private String mTomcatVersion = "7+";
	private String mDocumentRoot;
	private final String DOCUMENT_ROOT_CONFIG_KEY = "document_root";
	private final String MAX_THREADS_CONFIG_KEY = "max_threads";
	private final Integer DEFAULT_MAX_THREADS = 200;

	/**
	 *
	 * @param aConfiguration
	 */
	public TomcatEngine(EngineConfiguration aConfiguration) {

		super(aConfiguration);

		// load the ports
		Integer lPort = aConfiguration.getPort();
		Integer lSSLPort = aConfiguration.getSSLPort();

		// If ports are 0 use the WebSocket Servlet capabilities
		// of the Tomcat Engine and do not instantiate a separate engine here!
		// Caution! It is mandatory to load the jWebSocket Servlet in the
		// web.xml or webdefault.xml of the Tomcat server!
		if (null == lPort || 0 == lPort) {
			// fire the engine start event
			engineStarted();
			if (mLog.isDebugEnabled()) {
				mLog.debug("Running TomcatEngine not in embedded mode...");
			}
			return;
		}

		Map<String, Object> lSettings = aConfiguration.getSettings();
		if (null != lSettings) {
			Object lDocRoot = lSettings.get(DOCUMENT_ROOT_CONFIG_KEY);
			if (null != lDocRoot) {
				mDocumentRoot = JWebSocketConfig.expandEnvVarsAndProps(lDocRoot.toString());
			}
		}

		String lContext = aConfiguration.getContext();
		if (lContext == null) {
			lContext = "/";
		}
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Instantiating embedded Tomcat server"
						+ " at port " + lPort
						+ ", ssl-port " + lSSLPort
						+ ", context: '" + lContext + "'...");
			}

			mTomcat = new Tomcat();
			// setting the socket server hostname
			String lHostname = getConfiguration().getHostname();
			if (null != lHostname) {
				mTomcat.setHostname(lHostname);
			}
			mTomcatVersion = mTomcat.getServer().getInfo();
			mTomcat.setPort(lPort);
			mTomcat.setBaseDir(JWebSocketConfig.getConfigFolder("TomcatEngine"));

			// removing default Tomcat connector
			mTomcat.getService().removeConnector(mTomcat.getConnector());

			// getting maxThreads setting value
			String lMaxThreads = DEFAULT_MAX_THREADS.toString();
			if (getConfiguration().getSettings().containsKey(MAX_THREADS_CONFIG_KEY)) {
				lMaxThreads = getConfiguration().getSettings().get(MAX_THREADS_CONFIG_KEY).toString();
			}

			// creating plain connector
			Connector lPlainConnector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
			lPlainConnector.setPort(lPort);
			lPlainConnector.setProperty("maxThreads", lMaxThreads);

			mTomcat.getService().addConnector(lPlainConnector);

			// setting the SSL connector
			Connector lSSLConnector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
			lSSLConnector.setEnableLookups(false);
			lSSLConnector.setScheme("https");
			lSSLConnector.setSecure(true);
			lSSLConnector.setProperty("maxThreads", lMaxThreads);
			lSSLConnector.setPort(lSSLPort);
			lSSLConnector.setProperty("SSLEnabled", "true");
			lSSLConnector.setProperty("clientAuth", "false");
			lSSLConnector.setProperty("sslProtocol", "TLS");
			lSSLConnector.setProperty("keystoreFile",
					JWebSocketConfig.expandEnvVarsAndProps(getConfiguration().getKeyStore()));
			lSSLConnector.setProperty("keystorePass", getConfiguration().getKeyStorePassword());

			// registering the SSL connector
			mTomcat.getService().addConnector(lSSLConnector);

			final Context lCtx = mTomcat.addWebapp(lContext, mDocumentRoot);

			String lContextConfig = JWebSocketConfig.getConfigFolder("TomcatEngine/conf/context.xml");
			if (null != lContextConfig) {
				lCtx.setConfigFile(new File(lContextConfig).toURI().toURL());
			}

			// handling the global web.xml
			String lWebXML = JWebSocketConfig.getConfigFolder("TomcatEngine/conf/web.xml");
			if (null != lWebXML) {
				ContextConfig lContextListener = new ContextConfig();
				lCtx.addLifecycleListener(lContextListener);
				lContextListener.setDefaultWebXml(lWebXML);
			}

			if (mLog.isDebugEnabled()) {
				mLog.debug("Starting embedded Tomcat Server '"
						+ mTomcatVersion + "'...");
			}

			mTomcat.start();
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " instantiating Embedded Tomcat Server '"
					+ mTomcatVersion + "': "
					+ lEx.getMessage());
		}
		if (mLog.isDebugEnabled()) {
			mLog.debug("Tomcat Server '" + mTomcatVersion
					+ "' sucessfully instantiated at port "
					+ lPort + ", SSL port " + lSSLPort + "...");
		}
	}

	@Override
	public void startEngine() throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting Tomcat '" + mTomcatVersion + "' engine '" + getId() + "...");
		}

		mIsRunning = true;
		super.startEngine();

		if (mLog.isInfoEnabled()) {
			mLog.info("Tomcat '" + mTomcatVersion + "' engine '" + getId() + "' started.");
		}

		// fire the engine start event
		engineStarted();
	}

	@Override
	public void stopEngine(CloseReason aCloseReason) throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping Tomcat '" + mTomcatVersion + "' engine '" + getId() + "...");
		}

		// resetting "isRunning" causes engine listener to terminate
		mIsRunning = false;

		// inherited method stops all connectors
		super.stopEngine(aCloseReason);

		try {
			if (mTomcat != null) {
				mTomcat.stop();
				if (mLog.isDebugEnabled()) {
					mLog.debug("Tomcat '"
							+ mTomcatVersion
							+ " successfully stopped.");
				}
			} else {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Tomcat '"
							+ mTomcatVersion
							+ " not yet started, properly terminated.");
				}
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " stopping Tomcat Server '"
					+ mTomcatVersion + "': "
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
