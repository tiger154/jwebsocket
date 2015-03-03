//	---------------------------------------------------------------------------
//	jWebSocket - BaseClientTokenPlugIn (Community Edition, CE)
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
package org.jwebsocket.client.plugins;

import java.util.Iterator;
import javolution.util.FastList;
import org.jwebsocket.api.*;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
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
