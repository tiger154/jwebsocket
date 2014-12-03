//	---------------------------------------------------------------------------
//	jWebSocket - Grizzly Connector (Community Edition, CE)
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
package org.jwebsocket.grizzly;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
 * @author Alexander Schulze, Victor Antonio Barzana Crespo
 */
public class GrizzlyConnector extends BaseConnector {

	private static final Logger mLog = Logging.getLogger();
	private boolean mRunning = false;
	private final WebSocket mConnection;
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

			setStatus(WebSocketConnectorStatus.UP);
			sendPacket(lClose);
			setStatus(lStatus);
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
		} catch (UnknownHostException lEx) {
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
