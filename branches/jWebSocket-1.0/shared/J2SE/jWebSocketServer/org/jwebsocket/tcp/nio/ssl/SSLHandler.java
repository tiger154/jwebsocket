//	---------------------------------------------------------------------------
//	jWebSocket - SSLHandler
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
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

/**
 * Concept from http://www.java-gaming.org/index.php?PHPSESSID=1omilg2ptvh0a138gcfsnjqki1&topic=21984.msg181208#msg181208
 * 
 * @author kyberneees
 */
public abstract class SSLHandler {

	final ByteBuffer mWrapSrc, mUnwrapSrc;
	final ByteBuffer mWrapDst, mUnwrapDst;
	final SSLEngine mEngine;
	private boolean mAfterHandshake = false;

	public SSLHandler(SSLEngine aEngine, int aBufferSize) {
		mWrapSrc = ByteBuffer.allocateDirect(aBufferSize);
		mWrapDst = ByteBuffer.allocateDirect(aBufferSize);

		mUnwrapSrc = ByteBuffer.allocateDirect(aBufferSize);
		mUnwrapDst = ByteBuffer.allocateDirect(aBufferSize);


		mEngine = aEngine;
	}

	public abstract void onInboundData(ByteBuffer aDecrypted);

	public abstract void onOutboundData(ByteBuffer aEncrypted);

	public abstract void onHandshakeFailure(Exception aCause);

	public abstract void onHandshakeSuccess();

	public abstract void onClosed();

	public synchronized void send(final ByteBuffer aData) {
		mWrapSrc.put(aData);
		execute();
	}

	public synchronized void processSSLPacket(final ByteBuffer aData) {
		mUnwrapSrc.put(aData);
		execute();
	}

	public void execute() {
		while (this.step()) {
			continue;
		}
	}

	private boolean step() {
		switch (mEngine.getHandshakeStatus()) {
			case NOT_HANDSHAKING:
				boolean lContinue = false; {
				if (mWrapSrc.position() > 0) {
					lContinue |= this.wrap();
				}
				if (mUnwrapSrc.position() > 0) {
					lContinue |= this.unwrap();
				}
			}
			return lContinue;

			case NEED_WRAP:
				if (!this.wrap()) {
					return false;
				}
				break;

			case NEED_UNWRAP:
				if (!this.unwrap()) {
					return false;
				}
				break;

			case NEED_TASK:
				Runnable lTask;
				while ((lTask = mEngine.getDelegatedTask()) != null) {
					lTask.run();
				}
				execute();
				return false;

			case FINISHED:
				throw new IllegalStateException("FINISHED");
		}

		return true;
	}

	private boolean wrap() {
		SSLEngineResult lResult;

		try {
			mWrapSrc.flip();
			lResult = mEngine.wrap(mWrapSrc, mWrapDst);
			mWrapSrc.compact();
		} catch (SSLException lEx) {
			this.onHandshakeFailure(lEx);
			return false;
		}

		switch (lResult.getStatus()) {
			case OK:
				if (mWrapDst.position() > 0) {
					mWrapDst.flip();
					this.onOutboundData(mWrapDst);
					mWrapDst.compact();
				}
				break;

			case BUFFER_UNDERFLOW:
				// try again later
				break;

			case BUFFER_OVERFLOW:
				throw new IllegalStateException("failed to wrap");

			case CLOSED:
				this.onClosed();
				return false;
		}

		return true;
	}

	private boolean unwrap() {
		SSLEngineResult lResult;

		try {
			mUnwrapSrc.flip();
			lResult = mEngine.unwrap(mUnwrapSrc, mUnwrapDst);
			mUnwrapSrc.compact();
		} catch (SSLException lEx) {
			this.onHandshakeFailure(lEx);
			return false;
		}

		switch (lResult.getStatus()) {
			case OK:
				if (mUnwrapDst.position() > 0) {
					mUnwrapDst.flip();
					this.onInboundData(mUnwrapDst);
					mUnwrapDst.compact();
				}
				if (lResult.getHandshakeStatus().equals(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)
						&& !mAfterHandshake) {
					this.onHandshakeSuccess();
					mAfterHandshake = true;
				}
				break;

			case CLOSED:
				this.onClosed();
				return false;

			case BUFFER_OVERFLOW:
				throw new IllegalStateException("failed to unwrap");

			case BUFFER_UNDERFLOW:
				return false;
		}

		return true;
	}
}