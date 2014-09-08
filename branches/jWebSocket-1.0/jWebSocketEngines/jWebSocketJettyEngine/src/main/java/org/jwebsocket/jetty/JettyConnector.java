//	---------------------------------------------------------------------------
//	jWebSocket - Jetty Connector (Community Edition, CE)
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
package org.jwebsocket.jetty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.kit.WebSocketFrameType;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author Alexander Schulze
 */
public class JettyConnector extends BaseConnector {

	private static final Logger mLog = Logging.getLogger();
	private boolean mIsRunning = false;
	private CloseReason mCloseReason = CloseReason.TIMEOUT;
	private final Connection mConnection;
	private HttpServletRequest mRequest = null;

	/**
	 * creates a new TCP connector for the passed engine using the passed client
	 * socket. Usually connectors are instantiated by their engine only, not by
	 * the application.
	 *
	 * @param aEngine
	 * @param aRequest
	 * @param aProtocol
	 * @param aConnection
	 */
	public JettyConnector(WebSocketEngine aEngine, HttpServletRequest aRequest,
			String aProtocol, Connection aConnection) {
		super(aEngine);
		mConnection = aConnection;
		mRequest = aRequest;

		RequestHeader lHeader = new RequestHeader();

		// iterate throught URL args
		Map<String, String> lArgs = new FastMap<String, String>();
		Map<String, String[]> lReqArgs = aRequest.getParameterMap();
		for (String lArgName : lReqArgs.keySet()) {
			String[] lArgVals = lReqArgs.get(lArgName);
			if (lArgVals != null && lArgVals.length > 0) {
				lArgs.put(lArgName, lArgVals[0]);
			}
		}
		lHeader.put(RequestHeader.URL_ARGS, lArgs);

		// set default sub protocol if none passed
		if (aProtocol == null) {
			aProtocol = JWebSocketCommonConstants.WS_SUBPROT_DEFAULT;
		}
		lHeader.put(RequestHeader.WS_PROTOCOL, aProtocol);
		lHeader.put(RequestHeader.WS_PATH, aRequest.getRequestURI());

		// iterate throught header params
		Enumeration<String> lHeaderNames = aRequest.getHeaderNames();
		while (lHeaderNames.hasMoreElements()) {
			String lHeaderName = lHeaderNames.nextElement();
			if (lHeaderName != null) {
				lHeaderName = lHeaderName.toLowerCase();
				lHeader.put(lHeaderName, aRequest.getHeader(lHeaderName));
			}
		}

		lHeader.put(RequestHeader.WS_SEARCHSTRING, aRequest.getQueryString());

		//Setting client cookies
		Cookie[] lCookies = aRequest.getCookies();
		Map lCookiesMap = new FastMap().shared();
		for (Cookie lCookie : lCookies) {
			lCookiesMap.put(lCookie.getName(), lCookie.getValue());
		}
		lHeader.put(RequestHeader.WS_COOKIES, lCookiesMap);

		setHeader(lHeader);
	}

	@Override
	public void startConnector() {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting Jetty connector at port " + getRemotePort() + "...");
		}
		mIsRunning = true;

		if (mLog.isInfoEnabled()) {
			mLog.info("Started Jetty connector at port " + getRemotePort());
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
				mLog.debug("Stopping Jetty connector ("
						+ aCloseReason.name() + ") at port "
						+ getRemotePort() + "...");
			}
			super.stopConnector(aCloseReason);

			mConnection.disconnect();
			mCloseReason = aCloseReason;
			mIsRunning = false;
			if (mLog.isInfoEnabled()) {
				mLog.info("Stopped Jetty connector ("
						+ aCloseReason.name() + ") at port "
						+ getRemotePort() + ".");
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
				// mOutbound.sendMessage((byte) 0, );
				byte[] lBA = aDataPacket.getByteArray();
				mConnection.sendMessage(lBA, 0, lBA.length);
			} else {
				mConnection.sendMessage(aDataPacket.getUTF8());
			}
		} catch (IOException lEx) {
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
		return mRequest.getRemotePort();
	}

	@Override
	public InetAddress getRemoteHost() {
		InetAddress lAddr;
		try {
			lAddr = InetAddress.getByName(mRequest.getRemoteAddr());
		} catch (UnknownHostException lEx) {
			lAddr = null;
		}
		return lAddr;
	}

	@Override
	public String toString() {
		// TODO: weird results like... '0:0:0:0:0:0:0:1:61130'... on JDK 1.6u19
		// Windows 7 64bit
		String lRes = getRemoteHost().getHostAddress() + ":" + getRemotePort();
		// TODO: don't hard code. At least use JWebSocketConstants field here.
		String lUsername = getString("org.jwebsocket.plugins.system.username");
		if (lUsername != null) {
			lRes += " (" + lUsername + ")";
		}
		return lRes;
	}
}
