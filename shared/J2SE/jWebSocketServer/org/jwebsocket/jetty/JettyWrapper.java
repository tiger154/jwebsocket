//	---------------------------------------------------------------------------
//	jWebSocket - Jetty WebSocket Servlet Wrapper
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

import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.eclipse.jetty.websocket.WebSocket.OnControl;
import org.eclipse.jetty.websocket.WebSocket.OnFrame;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;

// LOOK AT THIS: http://www.maths.tcd.ie/~dacurran/4ict12/assignment3/
// Jetty Home: http://www.eclipse.org/jetty/
// Jetty M1 JavaDoc: http://www.jarvana.com/jarvana/browse/org/eclipse/jetty/aggregate/jetty-all-server/8.0.0.M1/
// SSL Tutorial: http://docs.codehaus.org/display/JETTY/How+to+configure+SSL
// " http://www.opennms.org/wiki/Standalone_HTTPS_with_Jetty

/*
aschulze-dt1:~ alexanderschulze$ keytool -keystore keystore -alias jWebSocket -genkey -keyalg RSA
Enter keystore password:
Re-enter new password:
What is your first and last name?
[Unknown]:  Alexander Schulze
What is the name of your organizational unit?
[Unknown]:  jWebSocket
What is the name of your organization?
[Unknown]:  Innotrade GmbH
What is the name of your City or Locality?
[Unknown]:  Herzogenrath
What is the name of your State or Province?
[Unknown]:  NRW
What is the two-letter country code for this unit?
[Unknown]:  DE
Is CN=Alexander Schulze, OU=jWebSocket, O=Innotrade GmbH, L=Herzogenrath, ST=NRW, C=DE correct?
[no]:  yes

Enter key password for <jWebSocket>
(RETURN if same as keystore password):
aschulze-dt1:~ alexanderschulze$
 */
/**
 *
 * @author alexanderschulze
 */
public class JettyWrapper implements WebSocket,
		OnTextMessage, OnBinaryMessage, OnFrame, OnControl {

	private static Logger mLog = Logging.getLogger();
	private WebSocketConnector mConnector = null;
	private WebSocketEngine mEngine = null;
	private HttpServletRequest mRequest = null;
	private String mProtocol = null;

	public JettyWrapper(HttpServletRequest aRequest, String aProtocol) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Jetty Wrapper with subprotocol '"
					+ aProtocol + "'...");
		}
		mEngine = JWebSocketFactory.getEngine();
		mRequest = aRequest;
		mProtocol = aProtocol;

	}

	@Override
	public boolean onFrame(byte b, byte b1, byte[] bytes, int i, int i1) {
		return false;
	}

	@Override
	public void onHandshake(FrameConnection aFC) {
	}

	@Override
	public boolean onControl(byte b, byte[] bytes, int i, int i1) {
		return false;
	}

	@Override
	public void onOpen(Connection aConnection) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Connecting Jetty Client...");
		}
		mConnector = new JettyConnector(mEngine, mRequest, mProtocol, aConnection);
		mEngine.addConnector(mConnector);
		// inherited BaseConnector.startConnector
		// calls mEngine connector started

		// TODO: Check Jetty 8.0.0.M2 for connection issues
		// need to call startConnector in a separate thread 
		// because Jetty does not allow to send a welcome message 
		// during it's onConnect listener.
		new Thread() {

			@Override
			public void run() {
				mConnector.startConnector();
			}
		}.start();
	}

	@Override
	public void onClose(int i, String string) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Disconnecting Jetty Client...");
		}
		if (mConnector != null) {
			// inherited BaseConnector.stopConnector
			// calls mEngine connector stopped
			mConnector.stopConnector(CloseReason.CLIENT);
			mEngine.removeConnector(mConnector);
		}
	}

	@Override
	public void onMessage(String aMessage) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Message (text) from Jetty client...");
		}
		if (mConnector != null) {
			WebSocketPacket lDataPacket = new RawPacket(aMessage);
			mEngine.processPacket(mConnector, lDataPacket);
		}
	}

	@Override
	public void onMessage(byte[] aMessage, int i, int i1) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Message (binary) from Jetty client...");
		}
		if (mConnector != null) {
			WebSocketPacket lDataPacket = new RawPacket(aMessage);
			mEngine.processPacket(mConnector, lDataPacket);
		}
	}
}
