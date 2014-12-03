//	---------------------------------------------------------------------------
//	jWebSocket - SSLHandler (Community Edition, CE)
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
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

/**
 * Concept:
 * http://www.java-gaming.org/index.php?PHPSESSID=1omilg2ptvh0a138gcfsnjqki1&topic=21984.msg181208#msg181208
 *
 * @author Rolando Santamaria Maso
 */
public abstract class SSLHandler {

	private ByteBuffer mWrapSrc, mUnwrapSrc;
	private ByteBuffer mWrapDst, mUnwrapDst;
	private final SSLEngine mEngine;
	private boolean mAfterHandshake = false;
	private int mBufferSize;

	/**
	 *
	 * @param aEngine
	 * @param aBufferSize
	 */
	public SSLHandler(SSLEngine aEngine, int aBufferSize) {
		mWrapSrc = ByteBuffer.allocate(aBufferSize);
		mWrapDst = ByteBuffer.allocate(aBufferSize);

		mUnwrapSrc = ByteBuffer.allocate(aBufferSize);
		mUnwrapDst = ByteBuffer.allocate(aBufferSize);

		mBufferSize = aBufferSize;
		mEngine = aEngine;
	}

	/**
	 * Handles incoming decrypted packets
	 *
	 * @param aDecrypted
	 */
	public abstract void onInboundData(ByteBuffer aDecrypted);

	/**
	 * Handles outgoing encrypted (SSL) packets
	 *
	 * @param aEncrypted
	 */
	public abstract void onOutboundData(ByteBuffer aEncrypted);

	/**
	 * Handles SSL handshake failure
	 *
	 * @param aCause
	 */
	public abstract void onHandshakeFailure(Exception aCause);

	/**
	 * Handles SSl handshake success
	 */
	public abstract void onHandshakeSuccess();

	/**
	 * Handles SSL handshake close
	 */
	public abstract void onClosed();

	/**
	 * Encrypts outgoing packet to be sent to the client
	 *
	 * @param aData
	 */
	public synchronized void send(final ByteBuffer aData) {
		if (mAfterHandshake) {
			mWrapSrc = ByteBuffer.allocate(mBufferSize);
			mWrapDst = ByteBuffer.allocate(mBufferSize);
		}

		mWrapSrc.put(aData);
		execute();

		if (mAfterHandshake) {
			//release buffer's memory
			mWrapSrc = null;
			mWrapDst = null;
		}
	}

	/**
	 * Decrypt incoming SSL encrypted packet and execute "onInboundData"
	 * callback
	 *
	 * @param aData
	 */
	public synchronized void processSSLPacket(final ByteBuffer aData) {
		if (mAfterHandshake) {
			mUnwrapSrc = ByteBuffer.allocate(mBufferSize);
			mUnwrapDst = ByteBuffer.allocate(mBufferSize);
		}

		mUnwrapSrc.put(aData);
		execute();
	}

	/**
	 * Execute SSL work-flow
	 */
	private void execute() {
		while (this.step()) {
			continue;
		}
	}

	private boolean step() {
		switch (mEngine.getHandshakeStatus()) {
			case NOT_HANDSHAKING:
				boolean lContinue = false; {
				if (null != mWrapSrc && mWrapSrc.position() > 0) {
					lContinue |= this.wrap();
				}
				if (null != mUnwrapSrc && mUnwrapSrc.position() > 0) {
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

		//Use NOT_HANDSHAKING inside "unwrap" method 
		//instead of FINISHED inside the "wrap" method
		if (lResult.getHandshakeStatus().equals(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)
				&& !mAfterHandshake) {
			this.onHandshakeSuccess();
			mAfterHandshake = true;

			//release buffer's memory
			mWrapDst = null;
			mWrapSrc = null;
		}

		switch (lResult.getStatus()) {
			case OK:
				if (mUnwrapDst.position() > 0) {
					mUnwrapDst.flip();
					if (mAfterHandshake) {
						//Ensure that limit is bigger than 1 
						if (mUnwrapDst.limit() > 1) {
							this.onInboundData(mUnwrapDst);

							//release buffer's memory
							mUnwrapDst = null;
							mUnwrapSrc = null;
							break;
						}
					}
					mUnwrapDst.compact();
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