//	---------------------------------------------------------------------------
//	jWebSocket - NioSSLHandler
//	Copyright (c) 2011 Innotrade GmbH, jWebSocket.org, Author: Jan Gnezda
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
package org.jwebsocket.tcp.nio.ssl;

import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.Map;
import java.util.Queue;
import javax.net.ssl.SSLEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.tcp.nio.*;

/**
 * Concept from http://www.java-gaming.org/index.php?PHPSESSID=1omilg2ptvh0a138gcfsnjqki1&topic=21984.msg181208#msg181208
 * 
 * @author kyberneees
 */
public class NioSSLHandler extends SSLHandler {

	final NioTcpConnector mConnector;
	final Map<String, Queue<DataFuture>> mWritesQueue;
	final DelayedPacketsQueue mDelayedPacketsQueue;
	final Selector mSelector;

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
		if (mConnector.isAfterSSLHandshake()) {
			byte[] lArray = new byte[aDecrypted.remaining()];
			aDecrypted.get(lArray);
			
			System.out.println(new String(lArray));
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
	}

	@Override
	public void onHandshakeSuccess() {
		mConnector.sslHandshakeValidated();
	}

	@Override
	public void onClosed() {
		mConnector.getEngine().connectorStopped(mConnector, CloseReason.BROKEN);
	}
}
