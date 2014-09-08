//	---------------------------------------------------------------------------
//	jWebSocket - Jetty WebSocket Servlet Wrapper (Community Edition, CE)
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
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.instance.JWebSocketInstance;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.storage.httpsession.HttpSessionStorage;

// LOOK AT THIS: http://www.maths.tcd.ie/~dacurran/4ict12/assignment3/
// Jetty Home: http://www.eclipse.org/jetty/
// Jetty M1 JavaDoc: http://www.jarvana.com/jarvana/browse/org/eclipse/jetty/aggregate/jetty-all-server/8.0.0.M1/
// SSL Tutorial: http://docs.codehaus.org/display/JETTY/How+to+configure+SSL
// " http://www.opennms.org/wiki/Standalone_HTTPS_with_Jetty
/**
 *
 * @author Alexander Schulze
 */
public class JettyWrapper implements WebSocket,
		OnTextMessage, OnBinaryMessage, OnFrame, OnControl {

	private static final Logger mLog = Logging.getLogger();
	private WebSocketConnector mConnector = null;
	private WebSocketEngine mEngine = null;
	private HttpServletRequest mRequest = null;
	private String mProtocol = null;

	/**
	 *
	 * @param aEngine
	 * @param aRequest
	 * @param aProtocol
	 */
	public JettyWrapper(JettyEngine aEngine, HttpServletRequest aRequest, String aProtocol) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Jetty Wrapper with subprotocol '"
					+ aProtocol + "'...");
		}
		mEngine = aEngine;
		mRequest = aRequest;
		mProtocol = aProtocol;
	}

	/**
	 *
	 * @param b
	 * @param b1
	 * @param bytes
	 * @param i
	 * @param i1
	 * @return
	 */
	@Override
	public boolean onFrame(byte b, byte b1, byte[] bytes, int i, int i1) {
		return false;
	}

	/**
	 *
	 * @param aFC
	 */
	@Override
	public void onHandshake(FrameConnection aFC) {
	}

	/**
	 *
	 * @param b
	 * @param bytes
	 * @param i
	 * @param i1
	 * @return
	 */
	@Override
	public boolean onControl(byte b, byte[] bytes, int i, int i1) {
		return false;
	}

	/**
	 *
	 * @param aConnection
	 */
	@Override
	public void onOpen(Connection aConnection) {
		// closing if server is not ready
		if (JWebSocketInstance.STARTED != JWebSocketInstance.getStatus()) {
			aConnection.close();
			return;
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug("Connecting Jetty Client...");
		}
		mConnector = new JettyConnector(mEngine, mRequest, mProtocol, aConnection);
		mConnector.getSession().setSessionId(mRequest.getSession().getId());
		if (JWebSocketConfig.isWebApp()) {
			mConnector.getSession().setStorage(new HttpSessionStorage(mRequest.getSession()));
		}

		mEngine.addConnector(mConnector);

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

	/**
	 *
	 * @param i
	 * @param string
	 */
	@Override
	public void onClose(int i, String string) {
		if (mConnector != null) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Disconnecting Jetty Client...");
			}

			mConnector.stopConnector(CloseReason.CLIENT);
		}
	}

	/**
	 *
	 * @param aMessage
	 */
	@Override
	public void onMessage(String aMessage) {
		if (aMessage.length() > mConnector.getMaxFrameSize()) {
			mLog.error(BaseEngine.getUnsupportedIncomingPacketSizeMsg(mConnector, aMessage.length()));
			return;
		}
		if (mConnector != null) {
			WebSocketPacket lDataPacket = new RawPacket(aMessage);
			mConnector.processPacket(lDataPacket);
		}
	}

	/**
	 *
	 * @param aMessage
	 * @param i
	 * @param i1
	 */
	@Override
	public void onMessage(byte[] aMessage, int i, int i1) {
		if (aMessage.length > mConnector.getMaxFrameSize()) {
			mLog.error(BaseEngine.getUnsupportedIncomingPacketSizeMsg(mConnector, aMessage.length));
			return;
		}
		if (mConnector != null) {
			WebSocketPacket lDataPacket = new RawPacket(aMessage);
			mConnector.processPacket(lDataPacket);
		}
	}
}
