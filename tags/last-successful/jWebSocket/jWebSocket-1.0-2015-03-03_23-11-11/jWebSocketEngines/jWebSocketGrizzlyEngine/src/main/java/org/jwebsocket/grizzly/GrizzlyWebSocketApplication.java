//	---------------------------------------------------------------------------
//	jWebSocket - Grizzly WebSocket Servlet Wrapper (Community Edition, CE)
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.*;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.instance.JWebSocketInstance;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.tcp.EngineUtils;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Victor Antonio Barzana Crespo
 * @author Rolando Santamaria Maso
 */
public class GrizzlyWebSocketApplication extends WebSocketApplication {

	private static final Logger mLog = Logging.getLogger();
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

	/**
	 *
	 * @param handler
	 * @param requestPacket
	 * @param listeners
	 * @return
	 */
	@Override
	public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
		if (isApplicationRequest(requestPacket)) {
			return super.createSocket(handler, requestPacket, listeners); //To change body of generated methods, choose Tools | Templates.
		}
		return null;
	}

	/**
	 *
	 * @param aHandshake
	 * @throws HandshakeException
	 */
	@Override
	protected void handshake(HandShake aHandshake) throws HandshakeException {
		super.handshake(aHandshake);
	}

	/**
	 *
	 * @return
	 */
	@Override
	public List<Extension> getSupportedExtensions() {
		List<Extension> lExts = super.getSupportedExtensions();
		return lExts;
	}

	/**
	 *
	 * @param aSubProtocol
	 * @return
	 */
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
		String lOrigin = aRequest.getHeader("origin");
		if (!EngineUtils.isOriginValid(lOrigin, mEngine.getConfiguration().getDomains())) {
			mLog.error("Client origin '" + lOrigin + "' does not match allowed domains!");
			return false;
		}

		// The jWebSocket context from the engine configuration
		// String lContext = mEngine.getConfiguration().getContext();
		// The jWebSocket servlet from the engine configuration
		// String lServlet = mEngine.getConfiguration().getServlet();
		// boolean isApp = (lContext + lServlet).equals(aRequest.getRequestURI());
		boolean isApp = true; // all request URIs must be allowed

		if (mEngine.getMaxConnections() == mEngine.getConnectors().size()) {
			// max connections reached
			// Grizzly only supports "close" as max connections reached strategy 
			if (mLog.isDebugEnabled()) {
				mLog.debug("Incoming connection on  port '" + aRequest.getRemotePort() + "' "
						+ "has been closed due to maximum number of connections reached!");
			}
			aRequest.getConnection().close();
		}

		if (isApp) {
			mRequest = aRequest;

			// Getting the protocol from the request
			mProtocol = aRequest.getHeader("sec-websocket-protocol");
			mCookies = new FastMap();
			// parsing cookies
			mCookies.put(RequestHeader.WS_COOKIES, mRequest.getHeader(RequestHeader.WS_COOKIES));
			EngineUtils.parseCookies(mCookies);
			Map lCookies = (Map) mCookies.get(RequestHeader.WS_COOKIES);
			Object lSessionId = lCookies.get(JWebSocketCommonConstants.SESSIONID_COOKIE_NAME);
			String lPath = aRequest.getRequestURI();

			if (null == lSessionId) {
				lSessionId = Tools.getMD5(UUID.randomUUID().toString());
				mRequest.getResponse().addHeader("Set-Cookie", JWebSocketCommonConstants.SESSIONID_COOKIE_NAME + "="
						+ lSessionId + "; Path=" + lPath + "; HttpOnly");
				lCookies.put(JWebSocketCommonConstants.SESSIONID_COOKIE_NAME, lSessionId);
			}
		}

		return isApp;
	}

	/**
	 *
	 * @param aWebSocket
	 */
	@Override
	public void onConnect(WebSocket aWebSocket) {
		// closing if server is not ready
		if (JWebSocketInstance.STARTED != JWebSocketInstance.getStatus()) {
			aWebSocket.close();
			return;
		}

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
	 *
	 * @param aWebSocket
	 * @param aFrame
	 */
	@Override
	public void onClose(WebSocket aWebSocket, DataFrame aFrame) {
		GrizzlyConnector lConnector = mConnectors.get(aWebSocket);

		if (lConnector != null) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Disconnecting Grizzly Client...");
			}
			lConnector.stopConnector(CloseReason.CLIENT);
		}
	}

	/**
	 *
	 * @param aWebSocket
	 * @param aData
	 */
	@Override
	public void onMessage(WebSocket aWebSocket, String aData) {
		GrizzlyConnector lConnector = mConnectors.get(aWebSocket);
		if (lConnector != null) {
			if (aData.length() > lConnector.getMaxFrameSize()) {
				mLog.error(BaseEngine.getUnsupportedIncomingPacketSizeMsg(lConnector, aData.length()));
				return;
			}
			if (mLog.isDebugEnabled()) {
				mLog.debug("Processing (text) message from '" + lConnector.getId() + "' connector...");
			}
			lConnector.processPacket(new RawPacket(aData));
		}
	}

	/**
	 *
	 * @param aWebSocket
	 * @param aBytes
	 */
	@Override
	public void onMessage(WebSocket aWebSocket, byte[] aBytes) {
		GrizzlyConnector lConnector = mConnectors.get(aWebSocket);
		if (lConnector != null) {
			if (aBytes.length > lConnector.getMaxFrameSize()) {
				mLog.error(BaseEngine.getUnsupportedIncomingPacketSizeMsg(lConnector, aBytes.length));
				return;
			}
			if (mLog.isDebugEnabled()) {
				mLog.debug("Processing (binary) message from '" + lConnector.getId() + "' connector...");
			}
			lConnector.processPacket(new RawPacket(aBytes));
		}
	}

	/**
	 *
	 * @param aWebSocket
	 * @param aBytes
	 */
	@Override
	public void onPing(WebSocket aWebSocket, byte[] aBytes) {
		super.onPing(aWebSocket, aBytes);
		GrizzlyConnector lConnector = mConnectors.get(aWebSocket);
		if (lConnector != null) {
			lConnector.processPing(new RawPacket(aBytes));
		}
	}

	/**
	 *
	 * @param aWebSocket
	 * @param aBytes
	 */
	@Override
	public void onPong(WebSocket aWebSocket, byte[] aBytes) {
		super.onPong(aWebSocket, aBytes);
		GrizzlyConnector lConnector = mConnectors.get(aWebSocket);
		if (lConnector != null) {
			lConnector.processPong(new RawPacket(aBytes));
		}
	}
}
