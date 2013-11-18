//	---------------------------------------------------------------------------
//	jWebSocket - BaseClientTokenPlugIn
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

import java.util.Iterator;
import javolution.util.FastList;
import org.jwebsocket.api.*;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 * @author kyberneees
 */
public class BaseClientTokenPlugIn implements WebSocketClientTokenPlugIn {

	WebSocketTokenClient mClient = null;
	WebSocketClientTokenListener mListener = null;
	String mNS;
	FastList<WebSocketClientTokenPlugInListener> mListeners = new FastList<WebSocketClientTokenPlugInListener>();

	@Override
	public void addListener(WebSocketClientTokenPlugInListener aListener) {
		mListeners.add(aListener);
	}

	@Override
	public void removeListener(WebSocketClientTokenPlugInListener aListener) {
		mListeners.remove(aListener);
	}

	private class TokenListener implements WebSocketClientTokenListener {

		WebSocketClientTokenPlugIn mPlugIn;

		public TokenListener(WebSocketClientTokenPlugIn aPlugIn) {
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

	/**
	 *
	 * @param aClient
	 * @param aNS
	 */
	public BaseClientTokenPlugIn(WebSocketTokenClient aClient, String aNS) {
		mListener = new TokenListener(this);
		mClient = aClient;
		mClient.addListener(mListener);
		mNS = aNS;
	}

	@Override
	public WebSocketTokenClient getTokenClient() {
		return mClient;
	}

	@Override
	public String getNS() {
		return mNS;
	}

	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		if (getNS().equals(aToken.getNS())) {
			for (Iterator<WebSocketClientTokenPlugInListener> lIt = mListeners.iterator(); lIt.hasNext();) {
				WebSocketClientTokenPlugInListener lListener = lIt.next();
				try {
					lListener.processToken(aToken);
				} catch (Exception lEx) {
				}
			}
		}
	}

	@Override
	public void processOpening(WebSocketClientEvent aEvent) {
	}

	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
	}

	@Override
	public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
	}

	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
	}

	@Override
	public void processReconnecting(WebSocketClientEvent aEvent) {
	}
}
