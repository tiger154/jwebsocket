//	---------------------------------------------------------------------------
//	jWebSocket - InternalConnector Implementation (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
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
package org.jwebsocket.connectors;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import javolution.util.FastList;
import org.jwebsocket.api.IInternalConnectorListener;
import org.jwebsocket.api.WebSocketConnectorStatus;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;

/**
 *
 * @author kyberneees
 */
public class InternalConnector extends BaseConnector {

	private Collection<IInternalConnectorListener> mListeners = new FastList<IInternalConnectorListener>().shared();
	static RequestHeader mHeader = new RequestHeader() {
		@Override
		public String getFormat() {
			return JWebSocketCommonConstants.WS_FORMAT_JSON;
		}
	};

	public InternalConnector(WebSocketEngine aEngine) {
		super(aEngine);
	}

	@Override
	public Integer getMaxFrameSize() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean isHixie() {
		return false;
	}

	@Override
	public boolean isHybi() {
		return false;
	}

	@Override
	public void startConnector() {
		if (null == getSession().getSessionId()) {
			getSession().setSessionId(Tools.getMD5(UUID.randomUUID().toString()));
		}

		getEngine().addConnector(this);
		setStatus(WebSocketConnectorStatus.UP);

		// notifying open event
		Iterator<IInternalConnectorListener> lIt = mListeners.iterator();
		while (lIt.hasNext()) {
			lIt.next().processOpened();
		}

		// notifying connector started
		if (getEngine() != null) {
			getEngine().connectorStarted(this);
		}
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		setStatus(WebSocketConnectorStatus.DOWN);
		getEngine().removeConnector(this);

		// notifying close event
		Iterator<IInternalConnectorListener> lIt = mListeners.iterator();
		while (lIt.hasNext()) {
			lIt.next().processClosed(aCloseReason);
		}

		super.stopConnector(aCloseReason);
	}

	@Override
	public InetAddress getRemoteHost() {
		try {
			return InetAddress.getLocalHost();
		} catch (Exception lEx) {
			return null;
		}
	}

	@Override
	public int getRemotePort() {
		return 0;
	}

	@Override
	public RequestHeader getHeader() {
		return mHeader;
	}

	@Override
	public boolean supportTokens() {
		return true;
	}

	@Override
	public void sendPacket(WebSocketPacket aDataPacket) {
		processPacket(aDataPacket);
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	@Override
	public IOFuture sendPacketAsync(WebSocketPacket aDataPacket) {
		throw new UnsupportedOperationException("Not supported on InternalConnector implementation!");
	}

	public void handleIncomingPacket(WebSocketPacket aPacket) {
		Iterator<IInternalConnectorListener> lIt = mListeners.iterator();
		// notifying welcome event
		while (lIt.hasNext()) {
			lIt.next().processPacket(aPacket);
		}
	}

	public void handleIncomingToken(Token aToken) {
		Iterator<IInternalConnectorListener> lIt = mListeners.iterator();
		if ((JWebSocketServerConstants.NS_BASE + ".plugins.system").equals(aToken.getNS())
				&& "welcome".equals(aToken.getType())) {
			// notifying welcome event
			while (lIt.hasNext()) {
				lIt.next().processWelcome(aToken);
			}
		} else {
			// notifying processToken event
			while (lIt.hasNext()) {
				lIt.next().processToken(aToken);
			}
		}
	}

	public void addListener(IInternalConnectorListener aListener) {
		mListeners.add(aListener);
	}

	public void removeListener(IInternalConnectorListener aListener) {
		mListeners.remove(aListener);
	}
}
