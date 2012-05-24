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
package org.jwebsocket.comet.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javolution.util.FastMap;
import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.comet.CometEvent.EventType;
import org.apache.catalina.comet.CometProcessor;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.comet.CometConnector;
import org.jwebsocket.comet.CometEngine;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;

/**
 * @author Osvaldo Aguilar Lauzurique @email osvaldo2627@hab.uci.cu
 */
public class CometServlet extends HttpServlet implements CometProcessor {
	
	private static Logger mLog;
	CometEngine mEngine;
	Map<String, Integer> mMonitoringClientDisconnect = new FastMap();
	
	public CometServlet() {
		mLog = Logging.getLogger();
	}
	
	@Override
	public void init() throws ServletException {
		/*
		 * try { JWebSocketFactory.start(); } catch (Exception e) {
		 * //mLog.debug("CometEvent begin raise with session id: '") }
		 */		
		mEngine = (CometEngine) JWebSocketFactory.getEngine("ce0");
		mEngine.setServlet(this);
	}
	
	@Override
	public void event(CometEvent aEvent) throws IOException, ServletException {
		if (aEvent.getEventType() == EventType.BEGIN) {
			
			if (mLog.isDebugEnabled()) {
				mLog.debug("CometEvent begin raise with session id: '"
						+ aEvent.getHttpServletRequest().getSession().getId() + "'...");
			}
			// input stream to read    
		} else if (aEvent.getEventType() == EventType.ERROR) {
			
			if (aEvent.getEventSubType() == CometEvent.EventSubType.TIMEOUT) {
				
				if (mLog.isDebugEnabled()) {
					mLog.debug("Connection timeout event fired ...");
				}
				connectionTimeOutMessage(mEngine.getConnectors().get(aEvent.getHttpServletRequest().getSession().getId()));
			} else {
				mLog.error("Unexpected error detected. Stopping CometCnnector..." + aEvent.toString());
			}
		} else if (aEvent.getEventType() == EventType.READ) {
			InputStream input = aEvent.getHttpServletRequest().getInputStream();
			byte[] buf = new byte[512];
			int c = 0;
			do {
				c = input.read(buf);
			} while (input.available() > 0);
			String lText = new String(buf, 0, c);
			
			ObjectMapper lMapper = new ObjectMapper();
			Map<String, Object> lInputMessage = lMapper.readValue(lText, Map.class);
			/*
			 * JSONObject lJson = null; try { lJson = new JSONObject(lText); }
			 * catch (Exception ex) { mLog.error("Unexpected error parsing input
			 * stream to json..." + lText + "..." + " from id" +
			 * aEvent.getHttpServletRequest().getSession().getId()); }
			 * Map<String, Object> lInputMessage =
			 * JSONProcessor.jsonObjectToMap(lJson);
			 */			
			messageHandler(lInputMessage, aEvent);
		} else if (aEvent.getEventType() == EventType.END) {
			
			String lConId = aEvent.getHttpServletRequest().getSession().getId();
			int num = mMonitoringClientDisconnect.get(lConId);
			if (num < 2) {
				num++;
				mMonitoringClientDisconnect.put(lConId, num);
			} else {
				CometConnector lConnector = ((CometConnector) mEngine.getConnectors().get(lConId));
				lConnector.stopConnector(CloseReason.CLIENT);
				mMonitoringClientDisconnect.remove(lConId);
			}
			aEvent.close();
		}		
	}
	
	public void messageHandler(Map<String, Object> message, CometEvent aEvent) {
		String lCometType = (String) message.get("cometType");
		String lSubProt = (String) (message.get("subPl"));
		int lReadyState = (Integer) message.get("readyState");
		Object lData = null;
		if (message.containsKey("data")) {
			lData = message.get("data");
		}
		mMonitoringClientDisconnect.put(aEvent.getHttpServletRequest().getSession().getId(), 0);
		if (lCometType.equals("connection")) {
			connectionIncomingMessage(lSubProt, lReadyState, aEvent);
		} else if (lCometType.equals("message")) {
			dataIncomingMessage(lReadyState, lData, aEvent);
		}
	}
	
	public void connectionTimeOutMessage(WebSocketConnector aConnector) {
		try {
			String lMessage = createMessage(aConnector.getHeader().getSubProtocol(), "connection", ((CometConnector) aConnector).getReadyState(), "");
			aConnector.sendPacket(new RawPacket(lMessage, "UTF-8"));
			((CometConnector) aConnector).setEvent(null);
			
		} catch (IOException e) {
			mLog.error("IOExeption sending close message", e);
		}
	}

	/**
	 * if the connection message is receive for the firs time, answer with the
	 * open ready state else just update de ready state or close the connection.
	 *
	 *
	 * @param aSubprot
	 * @param aState
	 * @param response
	 */
	private void connectionIncomingMessage(String aSubprot, int aState, CometEvent aEvent) {
		
		String message = "";
		if (aState == 0) {
			try {
				PrintWriter writer = aEvent.getHttpServletResponse().getWriter();
				message = createMessage(aSubprot, "connection", 1, "");
				writer.print(message);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				mLog.error("IOExeption sending openning message", e);
			}
		} else if (aState == 1) {
			String lSessionId = aEvent.getHttpServletRequest().getSession().getId();
			
			if (!mEngine.getConnectors().containsKey(lSessionId)) {
				if (mLog.isDebugEnabled()) {
					//@TODO fix this
					mLog.debug("Creating new connector with id: '" + lSessionId + "' ...");
				}
				
				CometConnector lConnector = new CometConnector(mEngine, aEvent);
				
				mEngine.addConnector(lConnector);
				mMonitoringClientDisconnect.put(lConnector.getId(), 0);
				
				RequestHeader lHeader = new RequestHeader();
				lHeader.put(RequestHeader.WS_PROTOCOL, aSubprot);
				
				lConnector.setHeader(lHeader);
				lConnector.setReadyState(1);
				lConnector.setAbleToSend(true);
				lConnector.startConnector();
				
				FastMap<String, WebSocketConnector> lConnectors = (FastMap<String, WebSocketConnector>) mEngine.getConnectors();
			} else {
				
				CometConnector lConnector = ((CometConnector) mEngine.getConnectors().get(lSessionId));
				lConnector.setEvent(aEvent);
				
				lConnector.setAbleToSend(true);
				lConnector.checkPaqueQueue();
				
			}
			
		}
		
	}
	
	private void dataIncomingMessage(int aState, Object aData, CometEvent aEvent) {
		String lSessionId = aEvent.getHttpServletRequest().getSession().getId();
		CometConnector lConnector = (CometConnector) mEngine.getConnectors().get(lSessionId);
		
		if (aState == 1) {
			//Processing incoming data packet
			lConnector.processPacket(new RawPacket(aData.toString()));
			//TODO fix this arter check
			if (!mEngine.getPacketsQueue().get(lSessionId).isEmpty()) {
				log("**** Here is a Package to send with the receive channel *****");
				lConnector.checkPaqueQueueByEvent(aEvent);
			} else {
				try {
					aEvent.close();
				} catch (Exception ex) {
					mLog.error("Unexpected error for release the incoming message event");
				}
				
			}
		} else if (aState == 3) {
			lConnector.stopConnector(CloseReason.CLIENT);
			try {
				aEvent.close();
			} catch (IOException ex) {
				mLog.error(ex.getMessage());
			}
		}
	}
	
	public static String createMessage(String subPro, String type, int readyState, String data) {
		
		Map<String, Object> lMessageMap = new FastMap();
		lMessageMap.put("subPl", subPro);
		lMessageMap.put("cometType", type);
		lMessageMap.put("readyState", readyState);
		lMessageMap.put("data", data);
		JSONObject json = null;
		try {
			json = JSONProcessor.mapToJsonObject(lMessageMap);
			
		} catch (Exception ex) {
			mLog.error(ex.toString());
		}
		
		if (mLog.isDebugEnabled()) {
			mLog.debug("json to string..." + json.toString());
		}
		
		return json.toString();
	}
}
