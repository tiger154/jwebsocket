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
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author aschulze
 */
public class TomcatEngine extends BaseEngine {

	private static Logger mLog = Logging.getLogger();
	private boolean mIsRunning = false;
	private Tomcat mTomcat = null;

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
				mLog.debug("Using Tomcat"
						+ " configured by Tomcat.xml...");
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
 			if (mLog.isDebugEnabled()) {
				mLog.debug("Instantiating embedded Tomcat server"
						+ " at port " + lPort
						+ ", ssl-port " + lSSLPort
						+ ", context: '" + lContext
						+ "', servlet: '" + lServlet + "'...");
			}
			
			mTomcat = new Tomcat();
			mTomcat.setPort(lPort);
			mTomcat.setBaseDir(".");

			Context lCtx = mTomcat.addWebapp(lContext, "C:/svn/jWebSocketDev/branches/jWebSocket-1.0/jWebSocketClient/web");
			Tomcat.addServlet(lCtx, "jWebSocketServlet", "org.jwebsocket.tomcat.TomcatServlet");
			lCtx.addServletMapping(lServlet, "jWebSocketServlet");

			mTomcat.start();


			// mTomcatServer.setStopAtShutdown(true);
			if (mLog.isDebugEnabled()) {
				mLog.debug("Starting embedded Tomcat Server '"
						+ mTomcat.getServer().getInfo() + "'...");
			}

			mTomcat.start();
			// if (mLog.isDebugEnabled()) {
			//	mLog.debug("Joining embedded Tomcat server...");
			// }
			// mTomcatServer.join();
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ "Instantiating Embedded Tomcat Server '"
					+ mTomcat.getServer().getInfo() + "': "
					+ lEx.getMessage());
		}
		if (mLog.isDebugEnabled()) {
			mLog.debug("Tomcat Server '" + mTomcat.getServer().getInfo()
					+ "' sucessfully instantiated at port "
					+ lPort + ", SSL port " + lSSLPort + "...");
		}
	}

	@Override
	public void startEngine()
			throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting Tomcat '" + mTomcat.getServer().getInfo() + "' engine '"
					+ getId()
					+ "...");
		}

		super.startEngine();

		if (mLog.isInfoEnabled()) {
			mLog.info("Tomcat '"
					+ mTomcat.getServer().getInfo()
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
					+ mTomcat.getServer().getInfo()
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
							+ mTomcat.getServer().getInfo()
							+ " successfully stopped.");
				}
			} else {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Tomcat '"
							+ mTomcat.getServer().getInfo()
							+ " not yet started, properly terminated.");
				}
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " stopping Tomcat Server '"
					+ mTomcat.getServer().getInfo() + "': "
					+ lEx.getMessage());
		}

		/*
		 * // now wait until all connectors have been closed properly // or
		 * timeout exceeds... try { while (getConnectors().size() > 0 && new
		 * Date().getTime() - lStarted < 10000) { Thread.sleep(250); } } catch
		 * (Exception lEx) { mLog.error(lEx.getClass().getSimpleName() + ": " +
		 * lEx.getMessage()); } if (mLog.isDebugEnabled()) { long lDuration =
		 * new Date().getTime() - lStarted; int lRemConns =
		 * getConnectors().size(); if (lRemConns > 0) { mLog.warn(lRemConns + "
		 * of " + lNumConns + " Tomcat connectors '" + getId() + "' did not stop
		 * after " + lDuration + "ms."); } else { mLog.debug(lNumConns + "
		 * Tomcat connectors '" + getId() + "' stopped after " + lDuration +
		 * "ms."); } }
		 */

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
