//	---------------------------------------------------------------------------
//	jWebSocket - Grizzly WebSocket Servlet Wrapper
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.*;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.tcp.EngineUtils;
import org.jwebsocket.util.Tools;

/**
 *
 * @author vbarzana
 * @author kyberneees
 */
public class GrizzlyWebSocketApplication extends WebSocketApplication {

	private static Logger mLog = Logging.getLogger();
	private static Map<WebSocket, GrizzlyConnector> mConnectors;
	private GrizzlyEngine mEngine = null;
	private HttpRequestPacket mRequest = null;
	private String mProtocol = null;
	private Map mCookies = null;

	/**
	 * The application listener for grizzly WebSocket
	 *
	 * @param aEngine
	 */
	public GrizzlyWebSocketApplication(GrizzlyEngine aEngine) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Grizzly Wrapper with subprotocol '" + mProtocol + "'...");
		}
		mEngine = aEngine;
		mConnectors = new FastMap<WebSocket, GrizzlyConnector>().shared();
	}

	@Override
	protected void handshake(HandShake aHandshake) throws HandshakeException {
		super.handshake(aHandshake);
	}

	@Override
	public List<String> getSupportedExtensions() {
		List<String> lExts = super.getSupportedExtensions();
		return lExts;
	}

	@Override
	public List<String> getSupportedProtocols(List<String> aSubProtocol) {
		// List<String> lProts = super.getSupportedProtocols(aSubProtocol);
		List<String> lProts = new FastList<String>();
		lProts.add(aSubProtocol.get(0));
		return lProts;
	}

	/**
	 * This method analyzes if the incoming connection is for this application,
	 * otherwise it rejects the connection.
	 *
	 * @param aRequest
	 * @return aIsApplicationRequest
	 */
	@Override
	public boolean isApplicationRequest(HttpRequestPacket aRequest) {

		// The jWebSocket context from the engine configuration
		String lContext = mEngine.getConfiguration().getContext();
		// The jWebSocket servlet from the engine configuration
		String lServlet = mEngine.getConfiguration().getServlet();

		boolean isApp = (lContext + lServlet).equals(aRequest.getRequestURI());

		if (isApp) {
			mRequest = aRequest;

			mCookies = new FastMap();
			// parsing cookies
			mCookies.put(RequestHeader.WS_COOKIES, mRequest.getHeader(RequestHeader.WS_COOKIES));
			EngineUtils.parseCookies(mCookies);
			Map lCookies = (Map) mCookies.get(RequestHeader.WS_COOKIES);
			Object lSessionId = lCookies.get(JWebSocketCommonConstants.SESSIONID_COOKIE_NAME);

			if (null == lSessionId) {
				lSessionId = Tools.getMD5(UUID.randomUUID().toString());
				mRequest.getResponse().addHeader("Set-Cookie", JWebSocketCommonConstants.SESSIONID_COOKIE_NAME + "=" + lSessionId + "; HttpOnly");
				lCookies.put(JWebSocketCommonConstants.SESSIONID_COOKIE_NAME, lSessionId);
			}
		}

		return isApp;
	}

	@Override
	public void onConnect(WebSocket aWebSocket) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Connecting Grizzly Client...");
		}

		GrizzlyConnector lConnector = new GrizzlyConnector(mEngine, mRequest, mProtocol, aWebSocket);
		lConnector.getHeader().put(RequestHeader.WS_COOKIES, mCookies.get(RequestHeader.WS_COOKIES));

		// setting the connector id
		lConnector.getSession().setSessionId(lConnector.getHeader().getCookies().
				get(JWebSocketCommonConstants.SESSIONID_COOKIE_NAME).
				toString());

		// registering the connector
		mConnectors.put(aWebSocket, lConnector);

		// inherited BaseConnector.startConnector
		// calls mEngine connector started
		lConnector.startConnector();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onClose(WebSocket aWebSocket, DataFrame aFrame) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Disconnecting Grizzly Client...");
		}
		GrizzlyConnector lConnector = mConnectors.get(aWebSocket);

		if (lConnector != null) {
			lConnector.stopConnector(CloseReason.CLIENT);
			mEngine.connectorStopped(lConnector, CloseReason.CLIENT);
		}

		mConnectors.remove(aWebSocket);
	}

	@Override
	public void onFragment(WebSocket aWebSocket, String aFragment, boolean aLast) {
		super.onFragment(aWebSocket, aFragment, aLast);

		GrizzlyConnector lConnector = mConnectors.get(aWebSocket);
		if (lConnector != null) {
			mEngine.processPacket(lConnector, new RawPacket(aFragment));
		}
	}

	@Override
	public void onFragment(WebSocket aWebSocket, byte[] aFragment, boolean aLast) {
		super.onFragment(aWebSocket, aFragment, aLast);

		GrizzlyConnector lConnector = mConnectors.get(aWebSocket);
		if (lConnector != null) {
			mEngine.processPacket(lConnector, new RawPacket(aFragment));
		}
	}

	@Override
	public void onMessage(WebSocket aWebSocket, String aData) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Message (text) from Grizzly client...");
		}
		GrizzlyConnector lConnector = mConnectors.get(aWebSocket);
		if (lConnector != null) {
			mEngine.processPacket(lConnector, new RawPacket(aData));
		}
	}

	@Override
	public void onMessage(WebSocket aWebSocket, byte[] aBytes) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Message (binary) from Grizzly client...");
		}
		GrizzlyConnector lConnector = mConnectors.get(aWebSocket);
		if (lConnector != null) {
			mEngine.processPacket(lConnector, new RawPacket(aBytes));
		}
	}

	@Override
	public void onPing(WebSocket aWebSocket, byte[] aBytes) {
		super.onPing(aWebSocket, aBytes);
		GrizzlyConnector lConnector = mConnectors.get(aWebSocket);
		if (lConnector != null) {
			lConnector.processPing(new RawPacket(aBytes));
		}
	}

	@Override
	public void onPong(WebSocket aWebSocket, byte[] aBytes) {
		super.onPong(aWebSocket, aBytes);
		GrizzlyConnector lConnector = mConnectors.get(aWebSocket);
		if (lConnector != null) {
			lConnector.processPong(new RawPacket(aBytes));
		}
	}
}