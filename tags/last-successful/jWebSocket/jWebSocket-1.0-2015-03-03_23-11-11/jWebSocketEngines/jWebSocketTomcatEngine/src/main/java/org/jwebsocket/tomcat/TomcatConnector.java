//	---------------------------------------------------------------------------
//	jWebSocket - Tomcat Connector (Community Edition, CE)
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
package org.jwebsocket.tomcat;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketFrameType;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 */
public class TomcatConnector extends BaseConnector {

	private static final Logger mLog = Logging.getLogger();
	private boolean mIsRunning = false;
	private CloseReason mCloseReason = CloseReason.TIMEOUT;
	private final WsOutbound mOutbound;
	private InetAddress mRemoteHost;
	private int mRemotePort;
	private final HttpServletRequest mRequest;

	/**
	 * creates a new TCP connector for the passed engine using the passed client
	 * socket. Usually connectors are instantiated by their engine only, not by
	 * the application.
	 *
	 * @param aEngine
	 * @param aRequest
	 * @param aOutbound
	 */
	public TomcatConnector(WebSocketEngine aEngine, HttpServletRequest aRequest, WsOutbound aOutbound) {
		super(aEngine);

		// save the outbound object to send data to the client of this connection
		mOutbound = aOutbound;
		mRequest = aRequest;
	}

	@Override
	public void startConnector() {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting Tomcat connector '" + getId() + "' at port " + getRemotePort() + "...");
		}
		mIsRunning = true;

		super.startConnector();

		if (mLog.isInfoEnabled()) {
			mLog.info("Tomcat connector '" + getId() + "' at port " + getRemotePort() + " started.");
		}
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		if (mIsRunning) {
			int lRemotePort = getRemotePort();
			if (mLog.isDebugEnabled()) {
				mLog.debug("Stopping Tomcat connector '" + getId() + "' ("
						+ aCloseReason.name() + ") at port "
						+ lRemotePort + "...");
			}
			super.stopConnector(aCloseReason);
			try {
				mOutbound.close(0, null);
			} catch (IOException ex) {
			}
			mCloseReason = aCloseReason;
			mIsRunning = false;
			if (mLog.isInfoEnabled()) {
				mLog.info("Tomcat connector '" + getId() + "' at port " + lRemotePort + " stopped.");
			}
		}
	}

	@Override
	public synchronized void sendPacket(WebSocketPacket aDataPacket) {
		try {
			checkBeforeSend(aDataPacket);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "sending packet to '" + getId() + "' connector!"));
			return;
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending packet '" + aDataPacket.getUTF8() + "'...");
		}

		try {
			if (aDataPacket.getFrameType() == WebSocketFrameType.BINARY) {
				byte[] lBA = aDataPacket.getByteArray();
				mOutbound.writeBinaryMessage(ByteBuffer.wrap(lBA));
			} else {
				mOutbound.writeTextMessage(CharBuffer.wrap(aDataPacket.getUTF8().toCharArray()));
			}
			if (mLog.isDebugEnabled()) {
				mLog.debug("Packet sent!");
			}
		} catch (IOException lEx) {
			// DO NOT NOTIFY. The connection with the client has been broken.
			// mLog.error(lEx.getClass().getSimpleName() + " sending data packet: " + lEx.getMessage());
		}
	}

	@Override
	public IOFuture sendPacketAsync(WebSocketPacket aDataPacket) {
		throw new UnsupportedOperationException("Underlying connector:"
				+ getClass().getName() + " doesn't support asynchronous send operation");
	}

	@Override
	public String generateUID() {
		String lUID = getRemoteHost() + "@" + getRemotePort();
		return lUID;
	}

	@Override
	public int getRemotePort() {
		return mRemotePort;
	}

	@Override
	public InetAddress getRemoteHost() {
		return mRemoteHost;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String toString() {
		String lRes = getRemoteHost().getHostAddress() + ":" + getRemotePort();
		// TODO: don't hard code. At least use JWebSocketConstants field here.
		String lUsername = getUsername();
		if (lUsername != null) {
			lRes += " (" + lUsername + ")";
		}
		return lRes;
	}

	/**
	 * @param mRemoteHost the mRemoteHost to set
	 */
	public void setRemoteHost(InetAddress mRemoteHost) {
		this.mRemoteHost = mRemoteHost;
	}

	/**
	 * @param mRemotePort the mRemotePort to set
	 */
	public void setRemotePort(int mRemotePort) {
		this.mRemotePort = mRemotePort;
	}
}
