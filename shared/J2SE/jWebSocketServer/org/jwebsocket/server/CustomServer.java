//	---------------------------------------------------------------------------
//	jWebSocket - Custom Server (abstract) (Community Edition, CE)
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
package org.jwebsocket.server;

import java.util.List;
import org.apache.log4j.Logger;
import org.jwebsocket.api.ServerConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServerListener;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.filter.TokenFilterChain;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.kit.WebSocketServerEvent;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.BasePlugInChain;

/**
 *
 * @author Alexander Schulze
 * @author jang
 */
public class CustomServer extends BaseServer {

	private static Logger mLog = Logging.getLogger(CustomServer.class);

	/**
	 * Creates a new instance of the CustomeServer. The custom server is a
	 * low-level data packet handler which is provided rather as an example
	 *
	 *
	 * @param aServerConfig
	 */
	public CustomServer(ServerConfiguration aServerConfig) {
		super(aServerConfig);
		mPlugInChain = new BasePlugInChain(this);
		mFilterChain = new TokenFilterChain(this);
	}

	@Override
	public void processPacket(WebSocketEngine aEngine, WebSocketConnector aConnector, WebSocketPacket aDataPacket) {
		if (mLog.isDebugEnabled()) {
			// don't show the content of the data packet here, 
			// potential plain passwords in log files may lead to security issues! 
			// mLog.debug("Processing data packet '" + aDataPacket.getUTF8() + "'...");
			mLog.debug("Processing data packet (content suppressed, length="
					+ aDataPacket.size() + " bytes)...");
		}
		RequestHeader lHeader = aConnector.getHeader();
		String lFormat = (lHeader != null ? lHeader.getFormat() : null);

		// the custom server here answers with a simple echo packet.
		// this section can be used as an example for your own protol handling.
		if (lFormat != null && JWebSocketCommonConstants.WS_FORMAT_TEXT.equals(lFormat)) {

			// send a modified echo packet back to sender.
			//
			// sendPacket(aConnector, aDataPacket);

			// you also could broadcast the packet here...
			// broadcastPacket(aDataPacket);

			// ...or forward it to your custom specific plug-in chain
			// PlugInResponse response = new PlugInResponse();
			// mPlugInChain.processPacket(response, aConnector, aDataPacket);

			// forward the token to the listener chain
			List<WebSocketServerListener> lListeners = getListeners();
			WebSocketServerEvent lEvent = new WebSocketServerEvent(aConnector, this);
			for (WebSocketServerListener lListener : lListeners) {
				if (lListener != null && lListener instanceof WebSocketServerListener) {
					((WebSocketServerListener) lListener).processPacket(lEvent, aDataPacket);
				}
			}
		}
	}

	/**
	 * removes a plugin from the plugin chain of the server.
	 *
	 * @param aPlugIn
	 */
	public void removePlugIn(WebSocketPlugIn aPlugIn) {
		mPlugInChain.removePlugIn(aPlugIn);
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing engine '" + aEngine.getId() + "' started...");
		}
		mPlugInChain.engineStarted(aEngine);
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing engine '" + aEngine.getId() + "' stopped...");
		}
		mPlugInChain.engineStopped(aEngine);
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing connector '" + aConnector.getId() + "' started...");
		}
		// notify plugins that a connector has started,
		// i.e. a client was sconnected.
		mPlugInChain.connectorStarted(aConnector);
		super.connectorStarted(aConnector);
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing connector '" + aConnector.getId() + "' stopped...");
		}
		// notify plugins that a connector has stopped,
		// i.e. a client was disconnected.
		mPlugInChain.connectorStopped(aConnector, aCloseReason);
		super.connectorStopped(aConnector, aCloseReason);
	}

	/**
	 * @return the mPlugInChain
	 */
	@Override
	public BasePlugInChain getPlugInChain() {
		return (BasePlugInChain) mPlugInChain;
	}
}
