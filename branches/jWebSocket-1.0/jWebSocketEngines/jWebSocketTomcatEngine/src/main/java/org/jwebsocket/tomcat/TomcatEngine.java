//	---------------------------------------------------------------------------
//	jWebSocket - Tomcat Engine
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
package org.jwebsocket.tomcat;

import java.util.Date;
import java.util.Map;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.util.Tools;

/**
 *
 * @author aschulze
 * @author kyberneees
 */
public class TomcatEngine extends BaseEngine {

	private static Logger mLog = Logging.getLogger();
	private boolean mIsRunning = false;
	private Tomcat mTomcat = null;
	private String mTomcatVersion = "7+";
	private String mDocumentRoot;

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
				mLog.debug("Running TomcatEngine in embedded mode...");
			}
			return;
		}

		Map<String, Object> lSettings = aConfiguration.getSettings();
		if (null != lSettings) {
			Object lDocRoot = lSettings.get("document_root");
			if (null != lDocRoot) {
				mDocumentRoot = Tools.expandEnvVarsAndProps(lDocRoot.toString());
			}
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
			if (mLog.isDebugEnabled()) {
				mLog.debug("Instantiating embedded Tomcat server"
						+ " at port " + lPort
						+ ", ssl-port " + lSSLPort
						+ ", context: '" + lContext
						+ "', servlet: '" + lServlet + "'...");
			}

			mTomcat = new Tomcat();
			mTomcatVersion = mTomcat.getServer().getInfo();
			mTomcat.setPort(lPort);
			mTomcat.setBaseDir(".");

			// removing default Tomcat connector
			mTomcat.getService().removeConnector(mTomcat.getConnector());

			// creating plain connector
			Connector lPlainConnector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
			lPlainConnector.setPort(lPort);
			mTomcat.getService().addConnector(lPlainConnector);

			// setting the SSL connector
			Connector lSSLConnector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
			lSSLConnector.setEnableLookups(false);
			lSSLConnector.setScheme("https");
			lSSLConnector.setSecure(true);
			lSSLConnector.setPort(lSSLPort);
			lSSLConnector.setProperty("maxHttpHeaderSize", "8192");
			lSSLConnector.setProperty("SSLEnabled", "true");
			lSSLConnector.setProperty("clientAuth", "false");
			lSSLConnector.setProperty("sslProtocol", "TLS");
			lSSLConnector.setProperty("keystoreFile",
					JWebSocketConfig.expandEnvAndJWebSocketVars(getConfiguration().getKeyStore()));
			lSSLConnector.setProperty("keystorePass",
					JWebSocketConfig.expandEnvAndJWebSocketVars(getConfiguration().getKeyStorePassword()));

			// registering the SSL connector
			mTomcat.getService().addConnector(lSSLConnector);

			Context lCtx = mTomcat.addWebapp(lContext, mDocumentRoot);

			// registering WebSocket and Comet servlets
			Tomcat.addServlet(lCtx, "jWebSocketServlet", "org.jwebsocket.tomcat.TomcatServlet");
			lCtx.addServletMapping(lServlet, "jWebSocketServlet");

			Tomcat.addServlet(lCtx, "jWebSocketCometServlet", "org.jwebsocket.tomcat.comet.CometServlet");
			lCtx.addServletMapping(lServlet + "Comet", "jWebSocketCometServlet");

			// setting the context session timeout
			lCtx.setSessionTimeout(getConfiguration().getTimeout());

			// mTomcatServer.setStopAtShutdown(true);
			if (mLog.isDebugEnabled()) {
				mLog.debug("Starting embedded Tomcat Server '"
						+ mTomcatVersion + "'...");
			}

			mTomcat.start();
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ "Instantiating Embedded Tomcat Server '"
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
			mLog.debug("Starting Tomcat '" + mTomcatVersion + "' engine '"
					+ getId()
					+ "...");
		}

		mIsRunning = true;
		super.startEngine();

		if (mLog.isInfoEnabled()) {
			mLog.info("Tomcat '"
					+ mTomcatVersion
					+ "' engine '"
					+ getId()
					+ "' started.");
		}

		// fire the engine start event
		engineStarted();
	}

	@Override
	public void stopEngine(CloseReason aCloseReason)
			throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping Tomcat '"
					+ mTomcatVersion
					+ "' engine '"
					+ getId() + "...");
		}

		// resetting "isRunning" causes engine listener to terminate
		mIsRunning = false;
		// inherited method stops all connectors

		long lStarted = new Date().getTime();
		int lNumConns = getConnectors().size();
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