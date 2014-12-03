//	---------------------------------------------------------------------------
//	jWebSocket - NioSSLHandler (Community Edition, CE)
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
package org.jwebsocket.tcp.nio.ssl;

import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.Map;
import java.util.Queue;
import javax.net.ssl.SSLEngine;
import org.apache.log4j.Logger;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.tcp.nio.*;

/**
 * Concept:
 * http://www.java-gaming.org/index.php?PHPSESSID=1omilg2ptvh0a138gcfsnjqki1&topic=21984.msg181208#msg181208
 *
 * @author Rolando Santamaria Maso
 */
public class NioSSLHandler extends SSLHandler {

	final NioTcpConnector mConnector;
	final Map<String, Queue<DataFuture>> mWritesQueue;
	final DelayedPacketsQueue mDelayedPacketsQueue;
	final Selector mSelector;
	private static final Logger mLog = Logging.getLogger();

	/**
	 *
	 * @param aConnector
	 * @param aWritesQueue
	 * @param aDelayedPacketsQueue
	 * @param aSelector
	 * @param aEngine
	 * @param aBufferSize
	 */
	public NioSSLHandler(NioTcpConnector aConnector, Map<String, Queue<DataFuture>> aWritesQueue,
			DelayedPacketsQueue aDelayedPacketsQueue, Selector aSelector, SSLEngine aEngine, int aBufferSize) {
		super(aEngine, aBufferSize);
		mConnector = aConnector;
		mWritesQueue = aWritesQueue;
		mDelayedPacketsQueue = aDelayedPacketsQueue;
		mSelector = aSelector;
	}

	@Override
	public void onInboundData(ByteBuffer aDecrypted) {
		byte[] lArray = new byte[aDecrypted.remaining()];
		aDecrypted.get(lArray);
		final ReadBean lBean = new ReadBean(mConnector.getId(), lArray);
		mDelayedPacketsQueue.addDelayedPacket(new IDelayedPacketNotifier() {
			@Override
			public ReadBean getBean() {
				return lBean;
			}

			@Override
			public NioTcpConnector getConnector() {
				return mConnector;
			}
		});
	}

	@Override
	public void onOutboundData(ByteBuffer aEncrypted) {
		byte[] lArray = new byte[aEncrypted.remaining()];
		aEncrypted.get(lArray);
		mWritesQueue.get(mConnector.getId()).add(new DataFuture(
				mConnector,
				ByteBuffer.wrap(lArray)));

		mSelector.wakeup();
	}

	@Override
	public void onHandshakeFailure(Exception cause) {
		mConnector.getEngine().connectorStopped(mConnector, CloseReason.BROKEN);
		if (mLog.isDebugEnabled()) {
			mLog.debug("SSL handshake failure!");
		}
	}

	@Override
	public void onHandshakeSuccess() {
		mConnector.sslHandshakeValidated();
	}

	@Override
	public void onClosed() {
		if (mLog.isDebugEnabled()) {
			mLog.debug("SSL session has been closed on connector '" + mConnector.getRemoteHost()
					+ "@" + mConnector.getRemotePort() + "'!");
		}
		mConnector.getEngine().connectorStopped(mConnector, CloseReason.BROKEN);
	}
}
