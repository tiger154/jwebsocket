//	---------------------------------------------------------------------------
//	jWebSocket - Tomcat Connector
//	Copyright (c) 2012 Alexander Schulze, Innotrade GmbH
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

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
 * @author aschulze
 */
public class TomcatConnector extends BaseConnector {

	private static Logger mLog = Logging.getLogger();
	private boolean mIsRunning = false;
	private CloseReason mCloseReason = CloseReason.TIMEOUT;
	private WsOutbound mOutbound;
	private InetAddress mRemoteHost;
	private int mRemotePort;

	/**
	 * creates a new TCP connector for the passed engine using the passed client
	 * socket. Usually connectors are instantiated by their engine only, not by
	 * the application.
	 *
	 * @param aEngine
	 * @param aClientSocket
	 */
	public TomcatConnector(WebSocketEngine aEngine, WsOutbound aOutbound) {
		super(aEngine);
		// save the outbound object to send data to the client of this connection
		mOutbound = aOutbound;
	}

	@Override
	public void startConnector() {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting Tomcat connector at port " + getRemotePort() + "...");
		}
		mIsRunning = true;

		if (mLog.isInfoEnabled()) {
			mLog.info("Started Tomcat connector at port " + getRemotePort());
		}

		// call connectorStarted method of engine
		WebSocketEngine lEngine = getEngine();
		if (lEngine != null) {
			lEngine.connectorStarted(this);
		}
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		if (mIsRunning) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Stopping Tomcat connector ("
						+ aCloseReason.name() + ") at port "
						+ getRemotePort() + "...");
			}
			// call client stopped method of engine
			// (e.g. to release client from streams)
			WebSocketEngine lEngine = getEngine();
			if (lEngine != null) {
				lEngine.connectorStopped(this, aCloseReason);
			}
			try {
				mOutbound.close(0, null);
			} catch (IOException ex) {
			}
			mCloseReason = aCloseReason;
			mIsRunning = false;
			if (mLog.isInfoEnabled()) {
				mLog.info("Stopped Tomcat connector ("
						+ aCloseReason.name() + ") at port "
						+ getRemotePort() + ".");
			}
		}
	}

	@Override
	public void processPacket(WebSocketPacket aDataPacket) {
		// forward the data packet to the engine
		// the engine forwards the packet to all connected servers
		getEngine().processPacket(this, aDataPacket);
	}

	@Override
	public synchronized void sendPacket(WebSocketPacket aDataPacket) {
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
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " sending data packet: " + lEx.getMessage());
		}
		if (mLog.isDebugEnabled()) {
			mLog.debug("Packet '" + aDataPacket.getUTF8() + "' sent.");
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

	@Override
	public String toString() {
		String lRes = getRemoteHost().getHostAddress() + ":" + getRemotePort();
		// TODO: don't hard code. At least use JWebSocketConstants field here.
		String lUsername = getString("org.jwebsocket.plugins.system.username");
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
