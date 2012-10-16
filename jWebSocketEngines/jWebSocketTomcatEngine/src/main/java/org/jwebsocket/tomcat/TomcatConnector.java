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
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IEmbeddedAuthentication;
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
public class TomcatConnector extends BaseConnector implements IEmbeddedAuthentication {

	private static Logger mLog = Logging.getLogger();
	private boolean mIsRunning = false;
	private CloseReason mCloseReason = CloseReason.TIMEOUT;
	private WsOutbound mOutbound;
	private InetAddress mRemoteHost;
	private int mRemotePort;
	private HttpServletRequest mRequest;

	/**
	 * creates a new TCP connector for the passed engine using the passed client
	 * socket. Usually connectors are instantiated by their engine only, not by
	 * the application.
	 *
	 * @param aEngine
	 * @param aClientSocket
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
			mLog.debug("Starting Tomcat connector at port " + getRemotePort() + "...");
		}
		mIsRunning = true;

		super.startConnector();
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		if (mIsRunning) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Stopping Tomcat connector ("
						+ aCloseReason.name() + ") at port "
						+ getRemotePort() + "...");
			}
			super.stopConnector(aCloseReason);
			try {
				mOutbound.close(0, null);
			} catch (IOException ex) {
			}
			mCloseReason = aCloseReason;
			mIsRunning = false;
			if (mLog.isInfoEnabled()) {
				mLog.info("Tomcat connector stopped!");
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
		} catch (Exception lEx) {
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

	@Override
	public String getAuthenticationType() {
		return mRequest.getAuthType();
	}

	@Override
	public boolean hasAuthority(String aAuthority) {
		return mRequest.isUserInRole(aAuthority);
	}

	@Override
	public boolean isAuthenticated() {
		return getUsername() != null;
	}

	@Override
	public String getUsername() {
		if (null != super.getUsername()) {
			return super.getUsername();
		}

		return mRequest.getRemoteUser();
	}
}
