//	---------------------------------------------------------------------------
//	jWebSocket - Tomcat WebSocket Servlet Wrapper (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javolution.util.FastMap;
import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.storage.httpsession.HttpSessionStorage;

/**
 *
 * @author aschulze
 * @author kyberneees
 */
public class TomcatWrapper extends MessageInbound {

	private static Logger mLog = Logging.getLogger();
	private TomcatConnector mConnector = null;
	private static WebSocketEngine mEngine = null;
	private HttpServletRequest mRequest = null;
	private HttpSession mSession = null;
	private RequestHeader mHeader;
	private InetAddress mRemoteHost;
	private int mRemotePort;
	private boolean mIsSecure;

	/**
	 *
	 * @param aSubProtocols
	 * @return
	 */
	public static String selectSubProtocol(List<String> aSubProtocols) {
		// TODO: implement correct algorithm here!
		String lSubProt;
		if (null == aSubProtocols || aSubProtocols.size() <= 0) {
			lSubProt = JWebSocketCommonConstants.WS_SUBPROT_JSON;
		} else {
			lSubProt = aSubProtocols.get(0);
		}
		return lSubProt;
	}

	/**
	 *
	 * @param aEngine
	 * @param aRequest
	 * @param aSubProtocol
	 */
	public TomcatWrapper(TomcatEngine aEngine, HttpServletRequest aRequest, String aSubProtocol) {
		super();

		mEngine = aEngine;
		// setting the max frame size
		setByteBufferMaxSize(aEngine.getMaxFrameSize());
		mRequest = aRequest;
		mIsSecure = aRequest.isSecure();
		mSession = aRequest.getSession();

		mRemotePort = mRequest.getRemotePort();
		InetAddress lAddr;
		try {
			lAddr = InetAddress.getByName(mRequest.getRemoteAddr());
		} catch (Exception lEx) {
			lAddr = null;
		}
		mRemoteHost = lAddr;

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
		if (null == aSubProtocol) {
			aSubProtocol = JWebSocketCommonConstants.WS_SUBPROT_DEFAULT;
		}
		lHeader.put(RequestHeader.WS_PROTOCOL, aSubProtocol);
		lHeader.put(RequestHeader.WS_PATH, aRequest.getRequestURI());

		// iterate throught header params
		Enumeration<String> lHeaderNames = aRequest.getHeaderNames();
		while (lHeaderNames.hasMoreElements()) {
			String lHeaderName = lHeaderNames.nextElement();
			if (null != lHeaderName) {
				lHeaderName = lHeaderName.toLowerCase();
				lHeader.put(lHeaderName, aRequest.getHeader(lHeaderName));
			}
		}

		lHeader.put(RequestHeader.WS_SEARCHSTRING, aRequest.getQueryString());

		// Setting client cookies
		Cookie[] lCookies = aRequest.getCookies();
		Map lCookiesMap = new FastMap().shared();
		if (null != lCookies) {
			for (int lIdx = 0; lIdx < lCookies.length; lIdx++) {
				lCookiesMap.put(lCookies[lIdx].getName(), lCookies[lIdx].getValue());
			}
		}
		lHeader.put(RequestHeader.WS_COOKIES, lCookiesMap);

		mHeader = lHeader;
	}

	/**
	 *
	 * @param aOutbound
	 */
	@Override
	protected void onOpen(WsOutbound aOutbound) {
		// super.onOpen(aOutbound);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Connecting Tomcat ("
					+ (mIsSecure ? "SSL" : "plain")
					+ ") client...");
		}

		// supporting max connections reached strategy
		if (mEngine.getConnectors().size() == mEngine.getConfiguration().getMaxConnections()) {
			String lMessage = "Client(" + mRemoteHost.getHostAddress() + ") not accepted due to max connections reached.";
			if (mEngine.getConfiguration().getOnMaxConnectionStrategy().equals("close")) {
				if (mLog.isDebugEnabled()) {
					mLog.debug(lMessage + " Connection closed!");
				}
				try {
					aOutbound.close(0, null);
				} catch (IOException ex) {
				}
			} else if (mEngine.getConfiguration().getOnMaxConnectionStrategy().equals("reject")) {
				if (mLog.isDebugEnabled()) {
					mLog.debug(lMessage + " Connection rejected!");
				}
				try {
					aOutbound.close(CloseReason.SERVER_REJECT_CONNECTION.getCode(), null);
				} catch (IOException ex) {
				}
			}

			return;
		}
		mConnector = new TomcatConnector(mEngine, mRequest, aOutbound);
		mConnector.setSSL(mIsSecure);
		mConnector.getSession().setSessionId(mSession.getId());
		mConnector.getSession().setStorage(new HttpSessionStorage(mSession));
		mConnector.setHeader(mHeader);
		mConnector.setSubprot(mHeader.getSubProtocol());
		mConnector.setRemotePort(mRemotePort);
		mConnector.setRemoteHost(mRemoteHost);
		mEngine.addConnector(mConnector);

		mConnector.startConnector();
	}

	/**
	 *
	 * @param aStatus
	 */
	@Override
	protected void onClose(int aStatus) {
		// super.onClose(status);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Disconnecting Tomcat Client...");
		}
		if (null != mConnector) {
			mConnector.stopConnector(CloseReason.CLIENT);
		}
	}

	/**
	 *
	 * @param aMessage
	 * @throws IOException
	 */
	@Override
	protected void onBinaryMessage(ByteBuffer aMessage) throws IOException {
		if (aMessage.remaining() > mConnector.getMaxFrameSize()) {
			mLog.error(BaseEngine.getUnsupportedIncomingPacketSizeMsg(mConnector, aMessage.remaining()));
			return;
		}
		if (mLog.isDebugEnabled()) {
			mLog.debug("Message (binary) from Tomcat client...");
		}
		if (null != mConnector) {
			// TODO: implement binary Tomcat messages!
			WebSocketPacket lDataPacket = new RawPacket(aMessage.array());
			mConnector.processPacket(lDataPacket);
		}
	}

	/**
	 *
	 * @param aMessage
	 * @throws IOException
	 */
	@Override
	protected void onTextMessage(CharBuffer aMessage) throws IOException {
		if (aMessage.remaining() > mConnector.getMaxFrameSize()) {
			mLog.error(BaseEngine.getUnsupportedIncomingPacketSizeMsg(mConnector, aMessage.length()));
			return;
		}
		if (mLog.isDebugEnabled()) {
			mLog.debug("Message (text) from Tomcat client...");
		}
		if (null != mConnector) {
			WebSocketPacket lDataPacket = new RawPacket(aMessage.toString());
			mConnector.processPacket(lDataPacket);
		}
	}
}
