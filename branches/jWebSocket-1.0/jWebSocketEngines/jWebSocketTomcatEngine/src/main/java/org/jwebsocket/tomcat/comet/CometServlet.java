// ---------------------------------------------------------------------------
// jWebSocket - CometServlet (Community Edition, CE)
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
package org.jwebsocket.tomcat.comet;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * @author Osvaldo Aguilar Lauzurique Lauzurique
 * @author Rolando Santamaria Maso
 */
public class CometServlet extends HttpServlet implements CometProcessor {

	private static Logger mLog;
	private final Map<String, Queue<WebSocketPacket>> mPacketsQueue = new FastMap<String, Queue<WebSocketPacket>>();
	private TomcatEngine mEngine;
	Map<String, String> mInternalConnectorIds = new FastMap<String, String>();

	/**
	 *
	 */
	public CometServlet() {
	}

	/**
	 *
	 * @return
	 */
	public boolean isRunningEmbedded() {
		return ContextListener.isRunningEmbedded();
	}

	/**
	 *
	 * @param aConnectorId
	 * @return
	 */
	public boolean isPacketQueueEmpty(String aConnectorId) {
		return mPacketsQueue.get(aConnectorId).isEmpty();
	}

	/**
	 *
	 * @return
	 */
	public Map<String, Queue<WebSocketPacket>> getPacketsQueue() {
		return mPacketsQueue;
	}

	/**
	 *
	 * @throws ServletException
	 */
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
			if (!mServerStartupTimeoutConsumed) {
				try {
					// waiting 3 seconds for the server startup
					log("The request has been paused because the jWebSocket server is not started."
							+ " Waiting for 3 seconds the jWebSocket server startup...");
					Thread.sleep(3000);
				} catch (InterruptedException lEx) {
					throw new ServletException(lEx);
				}
				mServerStartupTimeoutConsumed = true;
				init();
			}

			String lErrMsg = "The jWebSocket server startup is taking too long or has failed. "
					+ "Request cannot be processed in this moment!";
			log(lErrMsg);
			throw new ServletException(lErrMsg);
		}
	}
	boolean mServerStartupTimeoutConsumed = false;

	/**
	 *
	 * @param aConnectorId
	 * @param aSessionId
	 */
	public void saveIntervalId(String aConnectorId, String aSessionId) {
		mInternalConnectorIds.put(aSessionId, aConnectorId);
	}

	/**
	 *
	 * @param aSessionId
	 * @return
	 */
	public String getInternalId(String aSessionId) {
		return mInternalConnectorIds.get(aSessionId);
	}

	/**
	 *
	 * @param aSessionId
	 */
	public void removeInternalId(String aSessionId) {
		mInternalConnectorIds.remove(aSessionId);
	}

	/**
	 *
	 * @param aEvent
	 * @throws IOException
	 * @throws ServletException
	 */
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

	/**
	 *
	 * @param aMessage
	 * @param aEvent
	 */
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
				} catch (IOException lEx) {
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
			} catch (IOException lEx) {
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
				} catch (IOException ex) {
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

	/**
	 *
	 * @param aSubProt
	 * @param aType
	 * @param aReadyState
	 * @param aData
	 * @return
	 */
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
