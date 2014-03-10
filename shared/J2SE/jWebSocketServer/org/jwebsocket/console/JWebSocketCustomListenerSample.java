//	---------------------------------------------------------------------------
//	jWebSocket - JWebSocketCustomListenerSample (Community Edition, CE)
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
package org.jwebsocket.console;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketServerListener;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.WebSocketServerEvent;
import org.jwebsocket.logging.Logging;

/**
 * This shows an example of a simple WebSocket listener
 *
 * @author Alexander Schulze
 */
public class JWebSocketCustomListenerSample implements WebSocketServerListener {

	private static Logger log = Logging.getLogger();

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processOpened(WebSocketServerEvent aEvent) {
		if (log.isDebugEnabled()) {
			log.debug("Client '" + aEvent.getConnector() + "' connected.");
		}
	}

	/**
	 *
	 * @param aEvent
	 * @param aPacket
	 */
	@Override
	public void processPacket(WebSocketServerEvent aEvent, WebSocketPacket aPacket) {
		if (log.isDebugEnabled()) {
			log.debug("Processing data packet '" + aPacket.getUTF8() + "'...");
		}
		aPacket.setUTF8("[echo from jWebSocket v" + JWebSocketServerConstants.VERSION_STR + "] " + aPacket.getUTF8());
		/*
		 StringBuilder lStrBuf = new StringBuilder();
		 for (int i = 0; i < 10000; i++) {
		 lStrBuf.append("<br>" + i + ": 1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		 }
		 aPacket.setUTF8(lStrBuf.toString());
		 */
		aEvent.sendPacket(aPacket);
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processClosed(WebSocketServerEvent aEvent) {
		if (log.isDebugEnabled()) {
			log.debug("Client '" + aEvent.getConnector() + "' disconnected.");
		}
	}
}
