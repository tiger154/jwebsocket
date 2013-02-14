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
import javax.servlet.http.HttpSession;
import javolution.util.FastMap;
import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.comet.CometEvent.EventType;
import org.apache.catalina.comet.CometProcessor;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.engines.ServletUtils;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.instance.JWebSocketInstance;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.storage.httpsession.HttpSessionStorage;
import org.jwebsocket.tcp.EngineUtils;
import org.jwebsocket.tomcat.ContextListener;
import org.jwebsocket.tomcat.TomcatEngine;
import org.jwebsocket.util.Tools;

/**
 * @author Osvaldo Aguilar Lauzurique <osvaldo2627@gmail.com>
 * @author kyberneees
 */
public class CometServlet extends HttpServlet implements CometProcessor {

	private static Logger mLog;
	private Map<String, Queue<WebSocketPacket>> mPacketsQueue = new FastMap<String, Queue<WebSocketPacket>>();
	private TomcatEngine mEngine;
	Map<String, String> mInternalConnectorIds = new FastMap<String, String>();

	public CometServlet() {
	}

	public boolean isRunningEmbedded() {
		return ContextListener.isRunningEmbedded();
	}

	public boolean isPacketQueueEmpty(String aConnectorId) {
		return mPacketsQueue.get(aConnectorId).isEmpty();
	}

	public Map<String, Queue<WebSocketPacket>> getPacketsQueue() {
		return mPacketsQueue;
	}

	@Override
	public void init() throws ServletException {
		if (JWebSocketInstance.STARTED == JWebSocketInstance.getStatus()) {
			mLog = Logging.getLogger();

			String lEngineId = getInitParameter(ServletUtils.SERVLET_ENGINE_CONFIG_KEY);
			if (null == lEngineId) {
				lEngineId = "tomcat0";
			}
			mEngine = (TomcatEngine) JWebSocketFactory.getEngine(lEngineId);

			super.init();
			if (mLog.isDebugEnabled()) {
				mLog.debug("Servlet successfully initialized.");
			}
		} else {
			throw new ServletException("The jWebSocket server is not started!");
		}
	}

	public void saveIntervalId(String aConnectorId, String aSessionId) {
		mInternalConnectorIds.put(aSessionId, aConnectorId);
	}

	public String getInternalId(String aSessionId) {
		return mInternalConnectorIds.get(aSessionId);
	}

	public void removeInternalId(String aSessionId) {
		mInternalConnectorIds.remove(aSessionId);
	}

	@Override
	public void event(CometEvent aEvent) throws IOException, ServletException {
		String lHttpSessionId = aEvent.getHttpServletRequest().getSession().getId();
		aEvent.setTimeout(mEngine.getConfiguration().getTimeout());
		String lConnectorId = getInternalId(lHttpSessionId);

		if (aEvent.getEventType() == EventType.BEGIN) {
			/**
			 *
			 */
		} else if (aEvent.getEventType() == EventType.ERROR) {
			if (null == lConnectorId) {
				return;
			}

			if (mEngine.getConnectors().containsKey(lConnectorId)) {
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
			}
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
			if (null != lConnectorId) {
				final CometConnector lConnector = ((CometConnector) mEngine.getConnectors().get(lConnectorId));
				if (aEvent.equals(lConnector.getEvent())) {
					Tools.getTimer().schedule(lConnector.getNewCloseTask(), 5000);
				}
			}
			// finally close event
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
		String lHttpSessionId = lSession.getId();
		String lConnectorId = getInternalId(lHttpSessionId);

		// client tries to open a connection
		if (0 == aState) {
			try {
				String lOrigin = aEvent.getHttpServletRequest().getScheme() + "//"
						+ aEvent.getHttpServletRequest().getServerName();

				if (EngineUtils.isOriginValid(lOrigin, mEngine.getConfiguration().getDomains())) {
					CometConnector lConnector;

					// stopping connector if previously exists (for browser reload support)
					if (null != lConnectorId && mEngine.getConnectors().containsKey(lConnectorId)) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Stopping connector '" + lConnectorId + "' due to web application reload...");
						}
						lConnector = (CometConnector) mEngine.getConnectors().get(lConnectorId);

						lConnector.cancelActiveCloseTask();
						lConnector.stopConnector(CloseReason.CLIENT);
					}

					if (mLog.isDebugEnabled()) {
						mLog.debug("Request for connection received...");
					}

					lConnector = new CometConnector(mEngine, this, aEvent);
					lConnector.setRequest(aEvent.getHttpServletRequest());
					
					// setting the session timeout
					if (ContextListener.isRunningEmbedded()) {
						aEvent.getHttpServletRequest().getSession()
								.setMaxInactiveInterval(mEngine.getConfiguration().getTimeout());
					}

					// registering the connector on the engine collection
					mEngine.addConnector(lConnector);

					// initializing utility collections
					saveIntervalId(lConnector.getId(), lHttpSessionId);
					getPacketsQueue().put(lConnector.getId(), new LinkedBlockingDeque<WebSocketPacket>());

					RequestHeader lHeader = new RequestHeader();
					lHeader.put(RequestHeader.WS_PROTOCOL, aSubProt);

					lConnector.setHeader(lHeader);
					lConnector.setReadyState(1);

					lConnector.getSession().setSessionId(lHttpSessionId);
					lConnector.getSession().setStorage(new HttpSessionStorage(lSession));

					// starting connector notification
					lConnector.startConnector();

					// sending conection state message
					String lMessage = createMessage(aSubProt, "connection", lConnector.getReadyState(), "");
					PrintWriter lWriter = aEvent.getHttpServletResponse().getWriter();
					lWriter.print(lMessage);
					lWriter.flush();
					lWriter.close();
				} else {
					mLog.error("Client origin '" + lOrigin + "' does not match allowed domains!");
					aEvent.close();
				}

			} catch (IOException lEx) {
				mLog.error("IOExeption sending openning message", lEx);
			}
		} else if (1 == aState && null != lConnectorId) {
			CometConnector lConnector = ((CometConnector) mEngine.getConnectors().get(lConnectorId));
			if (null == lConnector) {
				try {
					aEvent.close();
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "discarding invalid request!"));
				}
				return;
			}
			lConnector.cancelActiveCloseTask();
			lConnector.setEvent(aEvent);
			// refreshing the http request instance
			lConnector.setRequest(aEvent.getHttpServletRequest());

			if (mLog.isDebugEnabled()) {
				mLog.debug("Base client connection restarted for connector '" + lConnectorId + "'...");
			}
			lConnector.checkPacketQueue();
		}
	}

	private void handleDataMessage(int aState, Object aData, CometEvent aEvent) {
		String lConnectorId = getInternalId(aEvent.getHttpServletRequest().
				getSession().getId());
		if (null == lConnectorId) {
			try {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Discarding incoming data message...");
				}
				aEvent.close();
			} catch (Exception lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "discarding data message!"));
			}
			return;
		}

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

		try {
			StringBuffer lJSON = new StringBuffer();
			JSONProcessor.objectToJSONString(lMessage, lJSON);
			return lJSON.toString();
		} catch (Exception ex) {
			mLog.error(ex.toString());
		}

		return null;
	}
}