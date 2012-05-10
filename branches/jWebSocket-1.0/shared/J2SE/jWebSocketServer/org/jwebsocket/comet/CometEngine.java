// ---------------------------------------------------------------------------
// jWebSocket
// Copyright (c) 2012 jWebSocket.org, Innotrade GmbH
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.comet;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javolution.util.FastMap;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.comet.servlet.CometServlet;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.util.Tools;

/**
 * @author Osvaldo Aguilar Lauzurique @email osvaldo2627@hab.uci.cu
 */
public class CometEngine extends BaseEngine {

	private Tomcat mTomcat = null;
	private CometServlet mServlet;
	private static Logger mLog = Logging.getLogger();
	private Map<String, Queue<WebSocketPacket>> mPacketsQueue = new FastMap();
	
	private String mTomcatVersion = "7+";
	private String mDocumentRoot;

	public boolean isQueuePacketEmpty(String aIdConnector) {
		return mPacketsQueue.get(aIdConnector).isEmpty();
	}

	public CometServlet getServlet() {
		return mServlet;
	}

	public void setServlet(CometServlet aServlet) {
		this.mServlet = aServlet;
	}

	public CometEngine(EngineConfiguration aConfiguration) {
		super(aConfiguration);
		
		// load the ports
		Integer lPort = aConfiguration.getPort();
		Integer lSSLPort = aConfiguration.getSSLPort();
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
			String lBaseDir = JWebSocketConfig.getJWebSocketHome() + "webs";
			mTomcat.setBaseDir(lBaseDir);

			Context lCtx = mTomcat.addWebapp(lContext, mDocumentRoot);
			Tomcat.addServlet(lCtx, "jWebSocketServlet", new CometServlet()); // "org.jwebsocket.comet.servlet.CometServlet"
			lCtx.addServletMapping(lServlet, "jWebSocketServlet");

			mTomcat.start();

			// mTomcatServer.setStopAtShutdown(true);
			if (mLog.isDebugEnabled()) {
				mLog.debug("Starting embedded Tomcat Server '"
						+ mTomcatVersion + "'...");
			}

			mTomcat.start();
			// if (mLog.isDebugEnabled()) {
			//	mLog.debug("Joining embedded Tomcat server...");
			// }
			// mTomcatServer.join();
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

	public Map<String, Queue<WebSocketPacket>> getPacketsQueue() {
		return mPacketsQueue;
	}

	public void setPacketsQueue(Map<String, Queue<WebSocketPacket>> aPacketsQueue) {
		this.mPacketsQueue = aPacketsQueue;
	}

	@Override
	public void addConnector(WebSocketConnector aConnector) {
		super.addConnector(aConnector);
		mPacketsQueue.put(aConnector.getId(), new ConcurrentLinkedQueue());
	}

	@Override
	public void startEngine() throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting engine...");
		}

		super.startEngine();
	}

	@Override
	public void stopEngine(CloseReason aCloseReason) throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stoping engine...");
		}

		//@TODO check this
		mServlet.destroy();
		super.stopEngine(aCloseReason);
	}
}
