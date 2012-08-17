// ---------------------------------------------------------------------------
// jWebSocket - CometServlet
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
package org.jwebsocket.tomcat.comet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javolution.util.FastMap;
import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.comet.CometEvent.EventType;
import org.apache.catalina.comet.CometProcessor;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.console.JWebSocketServer;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.instance.JWebSocketInstance;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.storage.httpsession.HttpSessionStorage;
import org.jwebsocket.tomcat.TomcatEngine;
import org.jwebsocket.tomcat.TomcatWrapper;
import org.jwebsocket.util.Tools;

/**
 * @author Osvaldo Aguilar Lauzurique @email osvaldo2627@hab.uci.cu
 * @author kyberneees
 */
public class CometServlet extends HttpServlet implements CometProcessor {

	private boolean mRunningEmbedded = false;
	private static Logger mLog;
	private Map<String, Integer> mMonitoringClientDisconnect = new FastMap<String, Integer>();
	private Map<String, Queue<WebSocketPacket>> mPacketsQueue = new FastMap<String, Queue<WebSocketPacket>>();
	private TomcatEngine mEngine;

	public CometServlet() {
	}

	public boolean isRunningEmbedded() {
		return mRunningEmbedded;
	}

	public boolean isPacketQueueEmpty(String aConnectorId) {
		return mPacketsQueue.get(aConnectorId).isEmpty();
	}

	public Map<String, Queue<WebSocketPacket>> getPacketsQueue() {
		return mPacketsQueue;
	}

	private void checkHeaders(HttpServletRequest aRequest, HttpServletResponse aResponse) {
		String lAllowedMethods = aResponse.getHeader("Allow");

		if (null == lAllowedMethods) {
			lAllowedMethods = "GET, HEAD, POST, TRACE, OPTIONS";
		}
		aResponse.setHeader("Allow", lAllowedMethods);

		String lOrigin = aRequest.getHeader("Origin");
		String lMethod = aRequest.getHeader("Access-Control-Request-Method");
		String lHeaders = aRequest.getHeader("Access-Control-Request-Headers");
		if (null != lOrigin) {
			aResponse.setHeader("Access-Control-Allow-Origin", lOrigin);
		}
		if (null != lMethod) {
			aResponse.setHeader("Access-Control-Allow-Methods", lMethod);
		} else {
			aResponse.setHeader("Access-Control-Allow-Methods", lAllowedMethods);
		}
		if (null != lHeaders) {
			aResponse.setHeader("Access-Control-Allow-Headers", lHeaders);
		} else {
			aResponse.setHeader("Access-Control-Allow-Headers", "origin, content-type");
		}
	}

	@Override
	protected void doOptions(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		checkHeaders(aRequest, aResponse);
	}

	@Override
	public void init() throws ServletException {
		if (JWebSocketInstance.STOPPED == JWebSocketInstance.getStatus()) {
			log("Starting jWebSocket application server...");
			// running in embedded mode
			// starting the jWebSocket application server
			JWebSocketServer.main(new String[0]);
			log("jWebSocket application server started!");
		}
		mEngine = (TomcatEngine) JWebSocketFactory.getEngine("tomcat0");
		mLog = Logging.getLogger();
		
		super.init();
		if (mLog.isDebugEnabled()) {
			mLog.debug("CometServlet successfully initialized.");
		}
	}

	/**
	 * Generate comet connector's UIDs
	 *
	 * @param aEvent
	 * @return a connector UID
	 */
	public static String generateUID(CometEvent aEvent) {
		return Tools.getMD5(aEvent.getHttpServletRequest().getSession().getId() + "comet");
	}

	@Override
	public void event(CometEvent aEvent) throws IOException, ServletException {
		String lConnectorId = generateUID(aEvent);
		aEvent.setTimeout(mEngine.getConfiguration().getTimeout());

		if (aEvent.getEventType() == EventType.BEGIN) {
			/**
			 *
			 */
		} else if (aEvent.getEventType() == EventType.ERROR) {
			CloseReason lReason = CloseReason.CLIENT;
			if (aEvent.getEventSubType() == CometEvent.EventSubType.TIMEOUT) {
				lReason = CloseReason.TIMEOUT;
				if (mLog.isDebugEnabled()) {
					mLog.debug("Stopping CometConnector '" + lConnectorId + "' due to timeout reason...");
				}
			} else {
				mLog.error("Unexpected error detected. Stopping CometConnector '" + lConnectorId + "' ...");
			}
			CometConnector lConnector = ((CometConnector) mEngine.getConnectors().get(lConnectorId));
			lConnector.stopConnector(lReason);
			mMonitoringClientDisconnect.remove(lConnectorId);
		} else if (aEvent.getEventType() == EventType.READ) {
			InputStream lIn = aEvent.getHttpServletRequest().getInputStream();
			byte[] lBuffer = new byte[mEngine.getConfiguration().getMaxFramesize()];

			int lIndex = 0;
			do {
				lIndex = lIn.read(lBuffer);
			} while (lIn.available() > 0);
			String lData = new String(lBuffer, 0, lIndex);

			ObjectMapper lMapper = new ObjectMapper();
			Map<String, Object> lInputMessage = lMapper.readValue(lData, Map.class);

			// process incoming message
			processMessage(lInputMessage, aEvent);
		} else if (aEvent.getEventType() == EventType.END) {

			int lCode = mMonitoringClientDisconnect.get(lConnectorId);
			if (lCode < 2) {
				lCode++;
				mMonitoringClientDisconnect.put(lConnectorId, lCode);
			} else {
				CometConnector lConnector = ((CometConnector) mEngine.getConnectors().get(lConnectorId));
				lConnector.stopConnector(CloseReason.CLIENT);
				mMonitoringClientDisconnect.remove(lConnectorId);
			}
			aEvent.close();
		}
	}

	public void processMessage(Map<String, Object> aMessage, CometEvent aEvent) {
		String lCometType = (String) aMessage.get("cometType");
		String lSubProt = (String) (aMessage.get("subPl"));
		int lReadyState = (Integer) aMessage.get("readyState");

		Object lData = null;
		if (aMessage.containsKey("data")) {
			lData = aMessage.get("data");
		}

		String lConnectorId = generateUID(aEvent);
		mMonitoringClientDisconnect.put(lConnectorId, 0);

		if (lCometType.equals("connection")) {
			handleConnectionMessage(lSubProt, lReadyState, aEvent);
		} else if (lCometType.equals("message")) {
			handleDataMessage(lReadyState, lData, aEvent);
		}
	}

	/**
	 * if the connection message is received for the first time, answer with the
	 * open ready state else just update the ready state or close the
	 * connection.
	 *
	 * @param aSubProt
	 * @param aState
	 * @param response
	 */
	private void handleConnectionMessage(String aSubProt, int aState, CometEvent aEvent) {
		HttpSession lSession = aEvent.getHttpServletRequest().getSession();
		String lConnectorId = generateUID(aEvent);

		// client tries to open a connection
		if (aState == 0) {
			try {
				String lOrigin = aEvent.getHttpServletRequest().getScheme() + "//"
						+ aEvent.getHttpServletRequest().getServerName();

				if (TomcatWrapper.verifyOrigin(lOrigin, mEngine.getConfiguration().getDomains())) {
					String lMessage = createMessage(aSubProt, "connection", 1, "");

					// stopping connector if previously exists (for browser reload support)
					if (mEngine.getConnectors().containsKey(lConnectorId)) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Stopping connector '" + lConnectorId + "' due to web application reload...");
						}
						mEngine.getConnectors().get(lConnectorId).stopConnector(CloseReason.CLIENT);
					}

					PrintWriter lWriter = aEvent.getHttpServletResponse().getWriter();
					lWriter.print(lMessage);
					lWriter.flush();
					lWriter.close();

					if (mLog.isDebugEnabled()) {
						mLog.debug("Request for connection received...");
					}

					CometConnector lConnector = new CometConnector(mEngine, aEvent);
					mEngine.addConnector(lConnector);
					lConnector.setServlet(this);
					getPacketsQueue().put(lConnectorId, new LinkedBlockingDeque<WebSocketPacket>());

					mMonitoringClientDisconnect.put(lConnector.getId(), 0);

					RequestHeader lHeader = new RequestHeader();
					lHeader.put(RequestHeader.WS_PROTOCOL, aSubProt);

					lConnector.setHeader(lHeader);
					lConnector.setReadyState(1);

					lConnector.getSession().setSessionId(lConnectorId);
					lConnector.getSession().setStorage(new HttpSessionStorage(lSession));

					// starting connector notification
					lConnector.startConnector();
				} else {
					mLog.error("Client origin '" + lOrigin + "' does not match allowed domains!");
					aEvent.close();
				}

			} catch (IOException lEx) {
				mLog.error("IOExeption sending openning message", lEx);
			}
		} else if (aState == 1) {
			CometConnector lConnector = ((CometConnector) mEngine.getConnectors().get(lConnectorId));
			lConnector.setEvent(aEvent);

			if (mLog.isDebugEnabled()) {
				mLog.debug("Base client connection restarted for connector '" + lConnectorId + "'...");
			}
			lConnector.checkPacketQueue();
		}
	}

	private void handleDataMessage(int aState, Object aData, CometEvent aEvent) {
		String lConnectorId = generateUID(aEvent);
		CometConnector lConnector = (CometConnector) mEngine.getConnectors().get(lConnectorId);

		if (aState == 1) {
			// processing incoming data packet
			lConnector.processPacket(new RawPacket(aData.toString()));

			// looking for pending packets on the queue (performance requirement)
			if (!isPacketQueueEmpty(lConnectorId)) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Sending a pending packet in the received request response...");
				}
				lConnector.checkPacketQueueByEvent(aEvent);
			} else {
				try {
					aEvent.close();
				} catch (Exception ex) {
					mLog.error("Unexpected error closing comet event!");
				}
			}
		} else if (aState == 3) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Closing connector '" + lConnectorId + "' due to the client order...");
			}
			lConnector.stopConnector(CloseReason.CLIENT);
			try {
				aEvent.close();
			} catch (IOException lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "stopping connector '" + lConnectorId + "' ..."));
			}
		}
	}

	public static String createMessage(String aSubProt, String aType, int aReadyState, String aData) {
		Map<String, Object> lMessage = new FastMap<String, Object>();
		lMessage.put("subPl", aSubProt);
		lMessage.put("cometType", aType);
		lMessage.put("readyState", aReadyState);
		lMessage.put("data", aData);

		JSONObject lJSON;
		try {
			lJSON = JSONProcessor.mapToJsonObject(lMessage);
			return lJSON.toString();
		} catch (Exception ex) {
			mLog.error(ex.toString());
		}

		return null;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		if (mRunningEmbedded && JWebSocketInstance.STARTED == JWebSocketInstance.getStatus()) {
			JWebSocketFactory.stop();
		}
	}
}