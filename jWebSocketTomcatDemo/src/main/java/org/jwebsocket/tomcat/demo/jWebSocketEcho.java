//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2012, Innotrade GmbH jwebsocket.org
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
package org.jwebsocket.tomcat.demo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;
import org.jwebsocket.config.JWebSocketCommonConstants;

/**
 *
 * @author aschulze
 */
public class jWebSocketEcho extends WebSocketServlet {

	private TomcatHandler mHandler = null;

	private class TomcatHandler extends MessageInbound {

		private WsOutbound mOutbound = null;

		public TomcatHandler(HttpServletRequest aRequest, String aSubProtocol) {
			super();
		}

		@Override
		protected void onOpen(WsOutbound aOutbound) {
			mOutbound = aOutbound;
		}
		
		@Override
		protected void onClose(int aStatus) {
		}

		@Override
		protected void onBinaryMessage(ByteBuffer aMessage) throws IOException {
			// interpret incoming binary message here
			mOutbound.writeBinaryMessage(aMessage);
		}

		@Override
		protected void onTextMessage(CharBuffer aMessage) throws IOException {
			// interpret incoming text message here
			mOutbound.writeTextMessage(aMessage);
		}
	}

	@Override
	protected StreamInbound createWebSocketInbound(String aSubProtocol, HttpServletRequest aRequest) {
		if (null == aSubProtocol) {
			aSubProtocol = JWebSocketCommonConstants.WS_SUBPROT_JSON;
		}
		mHandler = new TomcatHandler(aRequest, aSubProtocol);
		return mHandler;
	}

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected String selectSubProtocol(List<String> aSubProtocols) {
		// TODO: implement correct algorithm here!
		String lSubProt;
		if (null == aSubProtocols || aSubProtocols.size() <= 0) {
			lSubProt = JWebSocketCommonConstants.WS_SUBPROT_JSON;
		} else {
			lSubProt = aSubProtocols.get(0);
		}
		return lSubProt;
	}

	@Override
	protected boolean verifyOrigin(String aOrigin) {
		// TODO: implement correct origin check here!
		boolean lVerified = true;
		return lVerified;
	}

	@Override
	protected void service(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		// save the request, since this is not available anymore in the createWebSocketInbound method
		super.service(aRequest, aResponse);
	}
}
