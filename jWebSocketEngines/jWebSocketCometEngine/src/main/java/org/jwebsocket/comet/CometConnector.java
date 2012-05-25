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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import javolution.util.FastMap;
import org.apache.catalina.comet.CometEvent;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.comet.servlet.CometServlet;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;

/**
 *
 * @author Osvaldo Aguilar Lauzurique @email osvaldo2627@hab.uci.cu
 */
public class CometConnector extends BaseConnector {

	private CometEvent mEvent;
	private int mReadyState = 0;
	private boolean mAbleToSend = false;
	private static Logger mLog = Logging.getLogger();

	/**
	 *
	 * @param aAbleToSend
	 */
	public void setAbleToSend(boolean aAbleToSend) {
		this.mAbleToSend = aAbleToSend;
	}

	/**
	 *
	 * @return
	 */
	public synchronized boolean isAbleToSend() {
		return mAbleToSend;
	}

	/**
	 *
	 * @param aEngine
	 * @param aEvent
	 */
	public CometConnector(WebSocketEngine aEngine, CometEvent aEvent) {
		super(aEngine);

		mEvent = aEvent;
	}

	/**
	 *
	 * @return
	 */
	public int getReadyState() {
		return mReadyState;
	}

	/**
	 *
	 * @param mReadyState
	 */
	public void setReadyState(int mReadyState) {
		this.mReadyState = mReadyState;
	}

	/**
	 *
	 * @return
	 */
	public CometEvent getEvent() {
		return mEvent;
	}

	/**
	 *
	 * @param aEvent
	 */
	public void setEvent(CometEvent aEvent) {
		this.mEvent = aEvent;
	}

	@Override
	public int getRemotePort() {
		return mEvent.getHttpServletRequest().getRemotePort();
	}

	@Override
	public InetAddress getRemoteHost() {
		InetAddress remoteInetAddress = null;
		try {
			remoteInetAddress = InetAddress.getByName(mEvent.getHttpServletRequest().getRemoteAddr());
		} catch (UnknownHostException ex) {
			mLog.error(ex.getMessage());
		}
		return remoteInetAddress;
	}

	@Override
	public String generateUID() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getId() {
		return mEvent.getHttpServletRequest().getSession().getId();
	}

	@Override
	public void startConnector() {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting connector '" + getId() + "'...");
		}

		super.startConnector();
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping connector '" + getId() + "'...");
		}

		if (aCloseReason.equals(CloseReason.SERVER) || aCloseReason.equals(CloseReason.SHUTDOWN)) {
			String lCloseMessage = CometServlet.createMessage(getSubprot(), "connection", 3, "");
			try {
				__sendPacket(new RawPacket(lCloseMessage));
			} catch (IOException ex) {
				mLog.error("Error intentando cerrar la conexion");
			}
		}

		getEngine().getConnectors().remove(mEvent.getHttpServletRequest().getSession().getId());
		try {
			mEvent.close();
		} catch (Exception ex) {
			mLog.error("Error cerrando la conexion cuando se detiene el connector");
		}

		super.stopConnector(aCloseReason);
	}

	@Override
	public synchronized void sendPacket(WebSocketPacket aDataPacket) {
		CometEngine lEngine = (CometEngine) getEngine();
		lEngine.getPacketsQueue().get(getId()).add(aDataPacket);
		checkPaqueQueue();
	}

	private void __sendPacket(WebSocketPacket aDataPacket) throws IOException {
		WebSocketPacket lPacket = __setupCometMessageResponse(getReadyState(), "connection", aDataPacket);
		PrintWriter lWriter = mEvent.getHttpServletResponse().getWriter();
		lWriter.write(lPacket.getString());
		lWriter.flush();
		lWriter.close();
	}

	private WebSocketPacket __setupCometMessageResponse(int readyState, String CometType, WebSocketPacket aDataPackage) {

		String lStringMessage = aDataPackage.getString();
		String lJson = null;
		try {
			Map<String, Object> lMessage = new FastMap();

			lMessage.put("cometType", CometType);
			lMessage.put("readyState", readyState);
			lMessage.put("subPl", getSubprot());
			lMessage.put("data", "");

			lJson = JSONProcessor.mapToJsonObject(lMessage).toString();
			if (lStringMessage.indexOf("\"data\":\"\"") == -1) {
				lJson = lJson.replace("\"data\":\"\"", "\"data\":" + lStringMessage);
			}
		} catch (Exception e) {
			mLog.error("Error configurando el mensaje para enviar");
		}
		return new RawPacket(lJson.toString());
	}

	/**
	 *
	 * @param aEvent
	 */
	public synchronized void checkPaqueQueueByEvent(CometEvent aEvent) {

		CometEngine lEngine = (CometEngine) getEngine();

		if (lEngine.getPacketsQueue().get(getId()).isEmpty()) {
			WebSocketPacket lPacket = lEngine.getPacketsQueue().get(getId()).poll();
			lPacket = __setupCometMessageResponse(getReadyState(), "message", lPacket);
			try {
				PrintWriter lWriter = aEvent.getHttpServletResponse().getWriter();
				lWriter.write(lPacket.getString());
				lWriter.flush();
				lWriter.close();
			} catch (IOException ex) {
				mLog.error("Unexpected error try to send data by the incoming data..." + ex.getMessage());
			}

		}
	}

	/**
	 *
	 */
	public synchronized void checkPaqueQueue() {

		if (isAbleToSend()) {

			CometEngine lEngine = (CometEngine) getEngine();
			if (!lEngine.getPacketsQueue().get(getId()).isEmpty()) {
				try {
					WebSocketPacket lPacket = lEngine.getPacketsQueue().get(getId()).poll();
					setAbleToSend(false);
					__sendPacket(lPacket);
				} catch (Exception ex) {
					mLog.error("Error tratando de enviar los mensajes pendientes de la cola");
				}
			}
		} else {
			//mLog.error("************************* Esto es solo un invento para ver si alguna vez no esta disponible *******************************");
		}
	}
}
