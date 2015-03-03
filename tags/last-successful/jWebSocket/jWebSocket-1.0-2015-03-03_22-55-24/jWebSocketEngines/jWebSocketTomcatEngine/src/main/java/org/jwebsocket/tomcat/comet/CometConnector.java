// ---------------------------------------------------------------------------
// jWebSocket - CometConnector (Community Edition, CE)
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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimerTask;
import javax.servlet.http.HttpServletRequest;
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
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.JWSTimerTask;

/**
 *
 * @author Osvaldo Aguilar Lauzurique Lauzurique
 * @author Rolando Santamaria Maso
 */
public class CometConnector extends BaseConnector {

	private CometEvent mEvent;
	private int mReadyState = 0;
	private static final Logger mLog = Logging.getLogger();
	private final int mRemotePort;
	private InetAddress mRemoteHost;
	private final CometServlet mServlet;
	private HttpServletRequest mRequest;
	private TimerTask mCloseTask;

	class CloseTimerTask extends JWSTimerTask {

		private final CometConnector mConnector;

		public CloseTimerTask(CometConnector aConnector) {
			mConnector = aConnector;
		}

		@Override
		public void runTask() {
			mConnector.setReadyState(3);
			mConnector.stopConnector(CloseReason.BROKEN);
		}
	}

	/**
	 *
	 */
	public void cancelActiveCloseTask() {
		if (null != mCloseTask) {
			mCloseTask.cancel();
			mCloseTask = null;
		}
	}

	/**
	 *
	 * @return
	 */
	public TimerTask getNewCloseTask() {
		mCloseTask = new CloseTimerTask(this);

		return mCloseTask;
	}

	/**
	 *
	 * @param aEngine
	 * @param aServlet
	 * @param aEvent
	 */
	public CometConnector(WebSocketEngine aEngine, CometServlet aServlet, CometEvent aEvent) {
		super(aEngine);

		mEvent = aEvent;
		mServlet = aServlet;

		mRemotePort = mEvent.getHttpServletRequest().getRemotePort();
		try {
			mRemoteHost = InetAddress.getByName(mEvent.getHttpServletRequest().getRemoteAddr());
		} catch (UnknownHostException ex) {
			// never happen
		}
	}

	/**
	 *
	 * @param aRequest
	 */
	public void setRequest(HttpServletRequest aRequest) {
		mRequest = aRequest;
	}

	/**
	 *
	 * @return
	 */
	public HttpServletRequest getRequest() {
		return mRequest;
	}

	/**
	 *
	 * @return
	 */
	public CometServlet getServlet() {
		return mServlet;
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
		setReadyState(2);

		if (!aCloseReason.equals(CloseReason.CLIENT)) {
			// creating message
			String lCloseMessage = CometServlet.createMessage(getSubprot(), "connection", 3, aCloseReason.name());
			List<WebSocketPacket> lPackets = new LinkedList<WebSocketPacket>();
			lPackets.add(new RawPacket(lCloseMessage));

			try {
				sendPacketsInConnectionMessage(lPackets);
				mEvent.close();
			} catch (IOException lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "stopping connector '" + getId() + "' ..."));
			}
		}
		// removing internal connector id
		mServlet.removeInternalId(getSession().getSessionId());

		// removing delayed packets for connector
		mServlet.getPacketsQueue().remove(getId());

		setReadyState(3);
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
		try {
			Map<String, Object> lMessage = new FastMap();

			lMessage.put("cometType", CometType);
			lMessage.put("readyState", readyState);
			lMessage.put("subPl", getSubprot());
			lMessage.put("data", aDataPacketList);

			Token lToken = TokenFactory.createToken();
			lToken.setMap(lMessage);

			return JSONProcessor.tokenToPacket(lToken);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "creating internal communication packet"));
		}
		return new RawPacket("");
	}

	private List<WebSocketPacket> getAvailablePackets(Queue<WebSocketPacket> aPacketsQueue) {
		List<WebSocketPacket> lAvailable = new LinkedList<WebSocketPacket>();
		while (!aPacketsQueue.isEmpty()) {
			lAvailable.add(aPacketsQueue.poll());
		}

		return lAvailable;
	}

	/**
	 *
	 * @param aEvent
	 */
	public synchronized void checkPacketQueueByEvent(CometEvent aEvent) {
		try {
			if (!mServlet.isPacketQueueEmpty(getId())) {
				List<WebSocketPacket> lDelayedPackets = getAvailablePackets(mServlet.getPacketsQueue().get(getId()));
				WebSocketPacket lPacket = __setupCometMessageResponse(getReadyState(), "message", lDelayedPackets);

				PrintWriter lWriter = aEvent.getHttpServletResponse().getWriter();
				lWriter.write(lPacket.getString());
				lWriter.flush();
				lWriter.close();
			} else {
				aEvent.close();
			}
		} catch (IOException lEx) {
			// TODO: process exception
		}
	}

	/**
	 *
	 */
	public synchronized void checkPacketQueue() {
		if (null != mEvent) {
			if (!mServlet.isPacketQueueEmpty(getId())) {
				try {
					List<WebSocketPacket> lPackets = getAvailablePackets(mServlet.getPacketsQueue().get(getId()));
					sendPacketsInConnectionMessage(lPackets);
					mEvent.close();
					mEvent = null;
				} catch (IOException lEx) {
					// DO NOT NOTIFY. The connection with the client has been broken.
					// mLog.error(lEx.getClass().getSimpleName() + " sending data packet: " + lEx.getMessage());
				}
			}
		}
	}
}
