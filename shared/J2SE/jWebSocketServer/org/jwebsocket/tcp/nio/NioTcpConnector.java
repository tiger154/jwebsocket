//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket NIO Engine
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
package org.jwebsocket.tcp.nio;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketProtocolAbstraction;
import org.jwebsocket.logging.Logging;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import org.jwebsocket.kit.WebSocketFrameType;

public class NioTcpConnector extends BaseConnector {

	private static Logger mLog = Logging.getLogger(NioTcpConnector.class);
	private InetAddress mRemoteAddress;
	private int mRemotePort;
	private boolean mIsAfterHandshake;
	private byte[] mPacketBuffer;
	private int mPayloadLength = -1;
	private int mBufferPosition = -1;
	private WebSocketFrameType mFrameType = WebSocketFrameType.INVALID;
	private int mWorkerId;
	private DelayedPacketNotifier mDelayedPacketNotifier;

	public NioTcpConnector(NioTcpEngine aEngine, InetAddress aRemoteAddress,
			int aRemotePort) {
		super(aEngine);

		this.mRemoteAddress = aRemoteAddress;
		this.mRemotePort = aRemotePort;
		mIsAfterHandshake = false;
		mWorkerId = -1;
	}

	@Override
	public void sendPacket(WebSocketPacket aPacket) {
		sendPacketAsync(aPacket); // nio engine works asynchronously by default
	}

	@Override
	public IOFuture sendPacketAsync(WebSocketPacket aPacket) {
		byte[] lProtocolPacket;
		if (isHixie()) {
			lProtocolPacket = new byte[aPacket.getByteArray().length + 2];
			lProtocolPacket[0] = 0x00;
			System.arraycopy(aPacket.getByteArray(), 0, lProtocolPacket, 1, aPacket.getByteArray().length);
			lProtocolPacket[lProtocolPacket.length - 1] = (byte) 0xFF;
		} else {
			lProtocolPacket = WebSocketProtocolAbstraction.rawToProtocolPacket(getVersion(), aPacket);
		}

		DataFuture lFuture = new DataFuture(this, ByteBuffer.wrap(lProtocolPacket));
		((NioTcpEngine) getEngine()).send(getId(), lFuture);
		return lFuture;
	}

	@Override
	public String getId() {
		return String.valueOf(hashCode());
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

	public void handshakeValidated() {
		mIsAfterHandshake = true;
	}

	public boolean isAfterHandshake() {
		return mIsAfterHandshake;
	}

	public boolean isPacketBufferEmpty() {
		return mPacketBuffer == null;
	}

	public void extendPacketBuffer(byte[] aNewData, int aStart, int aCount) throws IOException {
		if (mPayloadLength == -1) {
			// packet buffer grows with new data
			if (mPacketBuffer == null) {
				mPacketBuffer = new byte[aCount];
				if (aCount > 0) {
					System.arraycopy(aNewData, aStart, mPacketBuffer, 0, aCount);
				}
			} else {
				byte[] newBuffer = new byte[mPacketBuffer.length + aCount];
				System.arraycopy(mPacketBuffer, 0, newBuffer, 0, mPacketBuffer.length);
				System.arraycopy(aNewData, aStart, newBuffer, mPacketBuffer.length, aCount);
				mPacketBuffer = newBuffer;
			}
		} else {
			// packet buffer was already created with the correct length
			System.arraycopy(aNewData, aStart, mPacketBuffer, mBufferPosition, aCount);
			mBufferPosition += aCount;
		}
		notifyWorker();
	}

	public byte[] getPacketBuffer() {
		return mPacketBuffer;
	}

	public void flushPacketBuffer() {
		// TODO: why does this need to be copied here?
		// why can't we use mPacketBuffer directly?
		byte[] lCopy = new byte[mPacketBuffer.length];
		System.arraycopy(mPacketBuffer, 0, lCopy, 0, mPacketBuffer.length);

		RawPacket lPacket = new RawPacket(lCopy);
		if (mFrameType != WebSocketFrameType.INVALID) {
			lPacket.setFrameType(mFrameType);
		}
		try {
			getEngine().processPacket(this, lPacket);
			// empty buffer for next packet
			mPacketBuffer = null;
			mPayloadLength = -1;
			mFrameType = WebSocketFrameType.INVALID;
			mWorkerId = -1;
			notifyWorker();
		} catch (Exception e) {
			mLog.error(e.getClass().getSimpleName()
					+ " in processPacket of connector "
					+ getClass().getSimpleName(), e);
		}
	}

	public void setPayloadLength(int aLength) {
		mPayloadLength = aLength;
		mPacketBuffer = new byte[aLength];
		mBufferPosition = 0;
	}

	public boolean isPacketBufferFull() {
		return mBufferPosition >= mPayloadLength;
	}

	public void setPacketType(WebSocketFrameType aPacketType) {
		this.mFrameType = aPacketType;
	}

	public int getWorkerId() {
		return mWorkerId;
	}

	public void setWorkerId(int aWorkerId) {
		this.mWorkerId = aWorkerId;
	}

	public DelayedPacketNotifier getDelayedPacketNotifier() {
		return mDelayedPacketNotifier;
	}

	public void setDelayedPacketNotifier(DelayedPacketNotifier delayedPacketNotifier) {
		this.mDelayedPacketNotifier = delayedPacketNotifier;
	}

	private void notifyWorker() throws IOException {
		if (mDelayedPacketNotifier != null) {
			mDelayedPacketNotifier.handleDelayedPacket();
			mDelayedPacketNotifier = null;
		}
	}
}
