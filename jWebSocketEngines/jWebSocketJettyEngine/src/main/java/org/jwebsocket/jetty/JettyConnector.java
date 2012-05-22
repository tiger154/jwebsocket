//	---------------------------------------------------------------------------
//	jWebSocket - Jetty Connector
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
package org.jwebsocket.jetty;

import java.net.InetAddress;
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
 * @author aschulze
 */
public class JettyConnector extends BaseConnector {

	private static Logger mLog = Logging.getLogger();
	private boolean mIsRunning = false;
	private CloseReason mCloseReason = CloseReason.TIMEOUT;
	private Connection mConnection;
	private HttpServletRequest mRequest = null;

	/**
	 * creates a new TCP connector for the passed engine using the passed client
	 * socket. Usually connectors are instantiated by their engine only, not by
	 * the application.
	 *
	 * @param aEngine
	 * @param aClientSocket
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
		for (int i = 0; i < lCookies.length; i++) {
			lCookiesMap.put(lCookies[i].getName(), lCookies[i].getValue());
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
			// call client stopped method of engine
			// (e.g. to release client from streams)
			WebSocketEngine lEngine = getEngine();
			if (lEngine != null) {
				lEngine.connectorStopped(this, aCloseReason);
			}

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
				// mOutbound.sendMessage((byte) 0, );
				byte[] lBA = aDataPacket.getByteArray();
				mConnection.sendMessage(lBA, 0, lBA.length);
			} else {
				mConnection.sendMessage(aDataPacket.getUTF8());
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
		return mRequest.getRemotePort();
	}

	@Override
	public InetAddress getRemoteHost() {
		InetAddress lAddr;
		try {
			lAddr = InetAddress.getByName(mRequest.getRemoteAddr());
		} catch (Exception lEx) {
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
