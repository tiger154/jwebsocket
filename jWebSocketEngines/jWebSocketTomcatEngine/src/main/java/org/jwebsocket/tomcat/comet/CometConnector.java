// ---------------------------------------------------------------------------
// jWebSocket - CometConnector
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
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javolution.util.FastMap;
import org.apache.catalina.comet.CometEvent;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;

/**
 *
 * @author Osvaldo Aguilar Lauzurique @email osvaldo2627@hab.uci.cu
 * @author kyberneees
 */
public class CometConnector extends BaseConnector {

	private CometEvent mEvent;
	private int mReadyState = 0;
	private static Logger mLog = Logging.getLogger();
	private String mId;
	private int mRemotePort;
	private InetAddress mRemoteHost;
	private CometServlet mServlet;

	public CometConnector(WebSocketEngine aEngine, CometEvent aEvent) {
		super(aEngine);

		mEvent = aEvent;
		mId = CometServlet.generateUID(aEvent);
		mRemotePort = mEvent.getHttpServletRequest().getRemotePort();
		try {
			mRemoteHost = InetAddress.getByName(mEvent.getHttpServletRequest().getRemoteAddr());
		} catch (UnknownHostException ex) {
			// never happen
		}
	}

	public CometServlet getServlet() {
		return mServlet;
	}

	public void setServlet(CometServlet aServlet) {
		this.mServlet = aServlet;
	}

	public int getReadyState() {
		return mReadyState;
	}

	public void setReadyState(int mReadyState) {
		this.mReadyState = mReadyState;
	}

	public CometEvent getEvent() {
		return mEvent;
	}

	public void setEvent(CometEvent aEvent) {
		this.mEvent = aEvent;
	}

	@Override
	public int getRemotePort() {
		return mRemotePort;
	}

	@Override
	public InetAddress getRemoteHost() {
		return mRemoteHost;
	}

	@Override
	public String generateUID() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getId() {
		return mId;
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
			// creating message
			String lCloseMessage = CometServlet.createMessage(getSubprot(), "connection", 3, "");
			List<WebSocketPacket> lPackets = new LinkedList<WebSocketPacket>();
			lPackets.add(new RawPacket(lCloseMessage));

			try {
				sendPacketsInConnectionMessage(lPackets);
				mEvent.close();
			} catch (IOException lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "stopping connector '" + getId() + "' ..."));
			}
		}
		mServlet.getPacketsQueue().remove(getId());
		getEngine().getConnectors().remove(getId());

		super.stopConnector(aCloseReason);
	}

	@Override
	public synchronized void sendPacket(WebSocketPacket aDataPacket) {
		mServlet.getPacketsQueue().get(getId()).add(aDataPacket);
		checkPacketQueue();
	}

	private void sendPacketsInConnectionMessage(List<WebSocketPacket> aDataPacketList) throws IOException {
		WebSocketPacket lPacket = __setupCometMessageResponse(getReadyState(), "connection", aDataPacketList);
		PrintWriter lWriter = mEvent.getHttpServletResponse().getWriter();
		lWriter.write(lPacket.getString());
		lWriter.flush();
		lWriter.close();
	}

	private WebSocketPacket __setupCometMessageResponse(int readyState, String CometType,
			List<WebSocketPacket> aDataPacketList) {
		String lJson = null;
		try {
			Map<String, Object> lMessage = new FastMap();

			lMessage.put("cometType", CometType);
			lMessage.put("readyState", readyState);
			lMessage.put("subPl", getSubprot());
			lMessage.put("data", aDataPacketList);

			lJson = JSONProcessor.mapToJsonObject(lMessage).toString();
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "creating internal communication packet"));
		}
		return new RawPacket(lJson.toString());
	}

	private List<WebSocketPacket> getAvailablePackets(Queue<WebSocketPacket> aPacketsQueue) {
		List<WebSocketPacket> lAvailable = new LinkedList<WebSocketPacket>();
		while (!aPacketsQueue.isEmpty()) {
			lAvailable.add(aPacketsQueue.poll());
		}

		return lAvailable;
	}

	public synchronized void checkPacketQueueByEvent(CometEvent aEvent) {
		String lConnectorId = CometServlet.generateUID(aEvent);
		try {
			if (!mServlet.getPacketsQueue().get(lConnectorId).isEmpty()) {
				List<WebSocketPacket> lDelayedPackets = getAvailablePackets(mServlet.getPacketsQueue().get(getId()));
				WebSocketPacket lPacket = __setupCometMessageResponse(getReadyState(), "message", lDelayedPackets);

				PrintWriter lWriter = aEvent.getHttpServletResponse().getWriter();
				lWriter.write(lPacket.getString());
				lWriter.flush();
				lWriter.close();
			} else {
				aEvent.close();
			}
		} catch (Exception lEx) {
		}
	}

	public synchronized void checkPacketQueue() {
		if (null != mEvent) {
			if (!mServlet.getPacketsQueue().get(getId()).isEmpty()) {
				try {
					List<WebSocketPacket> lPackets = getAvailablePackets(mServlet.getPacketsQueue().get(getId()));
					sendPacketsInConnectionMessage(lPackets);
					mEvent.close();
					mEvent = null;
				} catch (Exception lEx) {
					mLog.error(Logging.getSimpleExceptionMessage(lEx, "unexpected error sending pending packet to the client"));
				}
			}
		}
	}
}