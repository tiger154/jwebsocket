//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket CGI Client
//	Copyright (c) 2012 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.client.plugins;

import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class ClientTokenPlugIn {

	BaseTokenClient mClient = null;
	TokenListener mListener = null;

	private class TokenListener implements WebSocketClientTokenListener {

		ClientTokenPlugIn mPlugIn;

		public TokenListener(ClientTokenPlugIn aPlugIn) {
			mPlugIn = aPlugIn;
		}

		@Override
		public void processToken(WebSocketClientEvent aEvent, Token aToken) {
			mPlugIn.processToken(aEvent, aToken);
		}

		@Override
		public void processOpening(WebSocketClientEvent aEvent) {
			mPlugIn.processOpening(aEvent);
		}

		@Override
		public void processOpened(WebSocketClientEvent aEvent) {
			mPlugIn.processOpened(aEvent);
		}

		@Override
		public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
			mPlugIn.processPacket(aEvent, aPacket);
		}

		@Override
		public void processClosed(WebSocketClientEvent aEvent) {
			mPlugIn.processClosed(aEvent);
		}

		@Override
		public void processReconnecting(WebSocketClientEvent aEvent) {
			mPlugIn.processReconnecting(aEvent);
		}
	}

	public ClientTokenPlugIn(BaseTokenClient aClient) {
		mListener = new TokenListener(this);
		mClient = aClient;
		mClient.addListener(mListener);
	}

	public final BaseTokenClient getClient() {
		return mClient;
	}

	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
	}

	public void processOpening(WebSocketClientEvent aEvent) {
	}

	public void processOpened(WebSocketClientEvent aEvent) {
	}

	public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
	}

	public void processClosed(WebSocketClientEvent aEvent) {
	}

	public void processReconnecting(WebSocketClientEvent aEvent) {
	}
}
