//	---------------------------------------------------------------------------
//	jWebSocket - Grizzly Connector
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.grizzly;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Map;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.util.MimeHeaders;
import org.glassfish.grizzly.websockets.WebSocket;
import org.jwebsocket.api.WebSocketConnectorStatus;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.*;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author ashulze, vbarzana
 */
public class GrizzlyConnector extends BaseConnector {

	private static Logger mLog = Logging.getLogger();
	private boolean mRunning = false;
	private WebSocket mConnection;
	private HttpRequestPacket mRequest = null;
	private String mProtocol = null;
	/**
	 *
	 */
	public static final String GRIZZLY_LOG = "Grizzly";
	/**
	 *
	 */
	public static final String GRIZZLY_SSL_LOG = "Grizzly-SSL";
	private String mLogInfo = GRIZZLY_LOG;

	/**
	 * Creates a new Grizzly connector for the passed engine using the passed
	 * client socket. Usually connectors are instantiated by their engine only,
	 * not by the application.
	 *
	 * @param aEngine
	 * @param aRequest
	 * @param socket
	 * @param aProtocol
	 */
	public GrizzlyConnector(WebSocketEngine aEngine, HttpRequestPacket aRequest,
			String aProtocol, WebSocket socket) {
		super(aEngine);
		mConnection = socket;
		mRequest = aRequest;
		mProtocol = aProtocol;

		RequestHeader lHeader = new RequestHeader();

		if (null != aRequest.getQueryString()) {
			try {
				// Parsing URL arguments
				Map<String, String> lArguments = jWebSocketGrizzlyTools.getUrlParameters(aRequest.getQueryString());

				// Setting the URL args to the connector header
				lHeader.put(RequestHeader.URL_ARGS, lArguments);

			} catch (UnsupportedEncodingException ex) {
				mLog.error(ex.getMessage());
			}
		}

		// set default sub protocol if none passed
		if (aProtocol == null) {
			aProtocol = JWebSocketCommonConstants.WS_SUBPROT_DEFAULT;
		}
		lHeader.put(RequestHeader.WS_PROTOCOL, aProtocol);

		setSubprot(aProtocol);

		lHeader.put(RequestHeader.WS_PATH, aRequest.getRequestURI());

		// iterate throught header params
		MimeHeaders lHeaderNames = aRequest.getHeaders();
		for (String lHeaderName : lHeaderNames.names()) {
			if (lHeaderName != null) {
				lHeaderName = lHeaderName.toLowerCase();
				lHeader.put(lHeaderName, aRequest.getHeader(lHeaderName));
			}
		}

		// TODO: check with Alex what is exactly search string
		lHeader.put(RequestHeader.WS_SEARCHSTRING, aRequest.getQueryString());

		setHeader(lHeader);

		setSSL(aRequest.isSecure());
		mLogInfo = isSSL() ? GRIZZLY_SSL_LOG : GRIZZLY_LOG;
	}

	/**
	 *
	 * @return
	 */
	public boolean isRunning() {
		return mRunning;
	}

	@Override
	public String getUsername() {
		return super.getUsername();
	}

	@Override
	public void startConnector() {
		mRunning = true;

		int lTimeout = -1;
		int lPort = getRemotePort();
		String lNodeStr = getNodeId();
		if (lNodeStr != null) {
			lNodeStr = " (unid: " + lNodeStr + ")";
		} else {
			lNodeStr = "";
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting " + mLogInfo + " connector" + lNodeStr + " on port "
					+ lPort + " with timeout "
					+ (lTimeout > 0 ? lTimeout + "ms" : "infinite") + "");
		}

		super.startConnector();
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping " + mLogInfo
					+ " connector (" + aCloseReason.name()
					+ ") at port " + getRemotePort() + "...");
		}

		if (CloseReason.CLIENT != aCloseReason) {
			WebSocketPacket lClose;
			lClose = new RawPacket(WebSocketFrameType.CLOSE,
					WebSocketProtocolAbstraction.calcCloseData(1000, aCloseReason.name()));
			WebSocketConnectorStatus lStatus = getStatus();
			try {
				// to ensure that the close packet can be sent at all!
				setStatus(WebSocketConnectorStatus.UP);
				sendPacketInTransaction(lClose);
			} catch (WebSocketException ex) {
				mLog.error("Could not send close notification packet "
						+ ex.getMessage());
			} finally {
				setStatus(lStatus);
			}
		}

		mConnection.close();
		mRunning = false;

		super.stopConnector(aCloseReason);

		if (mLog.isInfoEnabled()) {
			mLog.info("Stopped Grizzly connector ("
					+ aCloseReason.name() + ") at port "
					+ getRemotePort() + ".");
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
			if (mConnection.isConnected()) {
				if (aDataPacket.getFrameType() == WebSocketFrameType.BINARY
						|| aDataPacket.getFrameType() == WebSocketFrameType.PING) {
					mConnection.send(aDataPacket.getByteArray());
				} else if (aDataPacket.getFrameType() == WebSocketFrameType.TEXT) {
					mConnection.send(aDataPacket.getUTF8());
				} else {
					mConnection.send(aDataPacket.getByteArray());
				}
				if (mLog.isDebugEnabled()) {
					mLog.debug("Packet '" + aDataPacket.getUTF8() + "' sent.");
				}
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " sending data packet: " + lEx.getMessage());
		}
	}

	@Override
	public void sendPacketInTransaction(WebSocketPacket aDataPacket) throws WebSocketException {
		if (WebSocketConnectorStatus.UP == getStatus()) {
			boolean lSendSuccess = false;
			String lExMsg = null;
			try {
				if (mConnection.isConnected()) {
					mConnection.send(aDataPacket.getUTF8());
					lSendSuccess = true;
				} else {
					mLog.error("Trying to send transaction to closed connection: "
							+ getId() + ", " + aDataPacket.getUTF8());
				}
			} catch (Exception lEx) {
				lExMsg = lEx.getMessage();
			}
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
		return mRequest.getRemotePort();
	}

	@Override
	public InetAddress getRemoteHost() {
		InetAddress lAddr;
		try {
			lAddr = InetAddress.getByName(mRequest.getRemoteAddress());
		} catch (Exception lEx) {
			lAddr = null;
		}
		return lAddr;
	}

	/**
	 *
	 * @return
	 */
	public HttpRequestPacket getRequest() {
		return mRequest;
	}

	/**
	 *
	 * @return
	 */
	public WebSocket getConnection() {
		return mConnection;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String toString() {
		String lRes = getRemoteHost().getHostAddress() + ":" + getRemotePort();
		String lUsername = getUsername();
		if (lUsername != null) {
			lRes += " (" + lUsername + ")";
		}

		return lRes;
	}
}
