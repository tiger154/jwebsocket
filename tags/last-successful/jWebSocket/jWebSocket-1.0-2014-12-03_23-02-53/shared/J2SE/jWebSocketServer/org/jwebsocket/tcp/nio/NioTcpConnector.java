//	---------------------------------------------------------------------------
//	jWebSocket - NioTcpConnector (Community Edition, CE)
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
package org.jwebsocket.tcp.nio;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketProtocolAbstraction;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.tcp.nio.ssl.SSLHandler;

/**
 *
 * @author jang
 * @author Rolando Santamaria Maso
 */
public class NioTcpConnector extends BaseConnector {

	private static final Logger mLog = Logging.getLogger();
	private final InetAddress mRemoteAddress;
	private final int mRemotePort;
	private boolean mIsAfterWSHandshake;
	private int mWorkerId = -1;
	private boolean mIsAfterSSLHandshake;
	private SSLHandler mSSLHandler;

	/**
	 *
	 * @param aEngine
	 * @param aRemoteAddress
	 * @param aRemotePort
	 */
	public NioTcpConnector(NioTcpEngine aEngine, InetAddress aRemoteAddress,
			int aRemotePort) {
		super(aEngine);

		this.mRemoteAddress = aRemoteAddress;
		this.mRemotePort = aRemotePort;
		mIsAfterWSHandshake = false;
		mWorkerId = -1;
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		// supporting client "close" command
		String lClientCloseFlag = "connector_was_closed_by_client_demand";
		if (null != getVar(lClientCloseFlag)) {
			return;
		}
		if (aCloseReason.equals(CloseReason.CLIENT)) {
			setVar(lClientCloseFlag, true);
		}

		super.stopConnector(aCloseReason); 
	}

	/**
	 *
	 * @return
	 */
	public SSLHandler getSSLHandler() {
		return mSSLHandler;
	}

	/**
	 *
	 * @param aSSLHandler
	 */
	public void setSSLHandler(SSLHandler aSSLHandler) {
		this.mSSLHandler = aSSLHandler;
	}

	@Override
	public void sendPacket(WebSocketPacket aPacket) {
		sendPacketAsync(aPacket); // nio engine works asynchronously by default
	}

	@Override
	public IOFuture sendPacketAsync(WebSocketPacket aPacket) {
		try {
			checkBeforeSend(aPacket);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "sending packet to '" + getId() + "' connector!"));
			return null;
		}

		byte[] lProtocolPacket;
		if (isHixie()) {
			lProtocolPacket = new byte[aPacket.getByteArray().length + 2];
			lProtocolPacket[0] = 0x00;
			System.arraycopy(aPacket.getByteArray(), 0, lProtocolPacket, 1, aPacket.getByteArray().length);
			lProtocolPacket[lProtocolPacket.length - 1] = (byte) 0xFF;
		} else {
			lProtocolPacket = WebSocketProtocolAbstraction.rawToProtocolPacket(
					getVersion(), aPacket, WebSocketProtocolAbstraction.UNMASKED);
		}

		DataFuture lFuture = new DataFuture(this, ByteBuffer.wrap(lProtocolPacket));
		((NioTcpEngine) getEngine()).send(getId(), lFuture);
		return lFuture;
	}

	@Override
	public String generateUID() {
		return mRemoteAddress.getHostAddress() + '@' + mRemotePort;
	}

	@Override
	public InetAddress getRemoteHost() {
		return mRemoteAddress;
	}

	@Override
	public int getRemotePort() {
		return mRemotePort;
	}

	/**
	 *
	 */
	public void wsHandshakeValidated() {
		mIsAfterWSHandshake = true;
	}

	/**
	 *
	 * @return
	 */
	public boolean isAfterWSHandshake() {
		return mIsAfterWSHandshake;
	}

	/**
	 *
	 * @return
	 */
	public boolean isAfterSSLHandshake() {
		return mIsAfterSSLHandshake;
	}

	/**
	 * SSL session established successfully
	 */
	public void sslHandshakeValidated() {
		mIsAfterSSLHandshake = true;
		if (mLog.isDebugEnabled()) {
			mLog.debug("SSL session established successfully!");
		}
	}

	/**
	 *
	 * @param aPacket
	 */
	public void flushPacket(WebSocketPacket aPacket) {
		if (aPacket.size() > getMaxFrameSize()) {
			mLog.error(BaseEngine.getUnsupportedIncomingPacketSizeMsg(this, aPacket.size()));
			return;
		}
		try {
			super.processPacket(aPacket);
		} catch (Exception e) {
			mLog.error(e.getClass().getSimpleName()
					+ " in processPacket of connector "
					+ getClass().getSimpleName(), e);
		}
	}

	/**
	 *
	 * @return
	 */
	public int getWorkerId() {
		return mWorkerId;
	}

	/**
	 *
	 * @param aWorkerId
	 */
	public void setWorkerId(int aWorkerId) {
		this.mWorkerId = aWorkerId;
	}

	/**
	 *
	 */
	public void releaseWorker() {
		mWorkerId = -1;
	}
}
