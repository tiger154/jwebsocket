//	---------------------------------------------------------------------------
//	jWebSocket - Tomcat WebSocket Servlet Wrapper
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
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.storage.httpsession.HttpSessionStorage;

/**
 *
 * @author aschulze
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

	public static boolean verifyOrigin(String aOrigin) {
		// TODO: implement correct origin check here!
		boolean lVerified = true;
		return lVerified;
	}

	public TomcatWrapper(HttpServletRequest aRequest, String aSubProtocol) {
		super();
		// TODO: we need to fix this hardcoded solution
		mEngine = JWebSocketFactory.getEngine("tomcat0");
		mRequest = aRequest;
		mSession = aRequest.getSession();
		if (null != mEngine) {
			EngineConfiguration lConfig = mEngine.getConfiguration();
			if (null != lConfig) {
				int lTimeout = mEngine.getConfiguration().getTimeout();
				if (lTimeout >= 1000) {
					mSession.setMaxInactiveInterval(lTimeout);
				}
			}
		}

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
		if (aSubProtocol == null) {
			aSubProtocol = JWebSocketCommonConstants.WS_SUBPROT_DEFAULT;
		}
		lHeader.put(RequestHeader.WS_PROTOCOL, aSubProtocol);
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

	@Override
	protected void onOpen(WsOutbound aOutbound) {
		// super.onOpen(aOutbound);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Connecting Tomcat Client...");
		}
		mConnector = new TomcatConnector(mEngine, aOutbound);
		mConnector.getSession().setSessionId(mSession.getId());
		mConnector.getSession().setStorage(new HttpSessionStorage(mSession));

		mConnector.setHeader(mHeader);
		mConnector.setSubprot(mHeader.getSubProtocol());
		mConnector.setRemotePort(mRemotePort);
		mConnector.setRemoteHost(mRemoteHost);
		mEngine.addConnector(mConnector);

		mConnector.startConnector();
	}

	@Override
	public void onUpgradeComplete() {
		super.onUpgradeComplete();

	}

	@Override
	protected void onClose(int aStatus) {
		// super.onClose(status);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Disconnecting Tomcat Client...");
		}
		if (mConnector != null) {
			// inherited BaseConnector.stopConnector
			// calls mEngine connector stopped
			mConnector.stopConnector(CloseReason.CLIENT);
			mEngine.removeConnector(mConnector);
		}
	}

	@Override
	protected void onBinaryMessage(ByteBuffer aMessage) throws IOException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Message (binary) from Tomcat client...");
		}
		if (mConnector != null) {
			// TODO: implement binary Tomcat messages!
			WebSocketPacket lDataPacket = new RawPacket(aMessage.array());
			mEngine.processPacket(mConnector, lDataPacket);
		}
	}

	@Override
	protected void onTextMessage(CharBuffer aMessage) throws IOException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Message (text) from Tomcat client...");
		}
		if (mConnector != null) {
			WebSocketPacket lDataPacket = new RawPacket(aMessage.toString());
			mEngine.processPacket(mConnector, lDataPacket);
		}
	}
}
