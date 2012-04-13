//	---------------------------------------------------------------------------
//	jWebSocket - Tomcat WebSocket Servlet, from Tomcat 7.0.27
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
package org.jwebsocket.tomcat;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author aschulze
 */
public class TomcatServlet extends WebSocketServlet {

	private static Logger mLog = Logging.getLogger();
	private HttpServletRequest mRequest;
	
	@Override
	protected void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Ignoring incoming POST request...");
		}
	}

	@Override
	protected void service(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException {
		mRequest = aRequest;
		super.service(aRequest, aResponse);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "jWebSocket Tomcat WebSocket Servlet";
	}

	@Override
	protected StreamInbound createWebSocketInbound(String aSubProtocol) {
		return new WrapperMessageInbound(mRequest, aSubProtocol);
	}

	private final class WrapperMessageInbound extends MessageInbound {

		private WebSocketConnector mConnector = null;
		private WebSocketEngine mEngine = null;
		private HttpServletRequest mRequest = null;
		private String mProtocol = null;

		public WrapperMessageInbound(HttpServletRequest aRequest, String aSubProtocol) {
			super();
			mRequest = aRequest;
			mProtocol = aSubProtocol;
		}

		@Override
		protected void onOpen(WsOutbound aOutbound) {
			// super.onOpen(aOutbound);

			mEngine = JWebSocketFactory.getEngine();

			if (mLog.isDebugEnabled()) {
				mLog.debug("Connecting Tomcat Client...");
			}
			mConnector = new TomcatConnector(mEngine, mRequest, mProtocol, aOutbound);
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
				// TODO implement binary Tomcat messages!
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
				// TODO implement binary Tomcat messages!
				WebSocketPacket lDataPacket = new RawPacket(aMessage.toString());
				mEngine.processPacket(mConnector, lDataPacket);
			}
		}
	}
}
