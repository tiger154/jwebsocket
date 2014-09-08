//	---------------------------------------------------------------------------
//	jWebSocket - Proxy Plug-In Client Listener (Community Edition, CE)
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
package org.jwebsocket.plugins.proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import jlibs.core.nio.ClientChannel;
import jlibs.core.nio.handlers.ClientHandler;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

class ClientListener implements ClientHandler {

	private final static Logger mLog = Logging.getLogger(ClientListener.class);
	private final String mId;
	private final ByteBuffer mInBuf, mOutBuf;
	private final boolean mIsSSL;
	private final ClientChannel mBuddy;
	private final ProxyTargets mTargetType = ProxyTargets.WEBSOCKET;

	ClientListener(String aId, ByteBuffer aInBuf, ByteBuffer aOutBuf,
			boolean aIsSSL, ClientChannel aBuddy) {
		this.mId = aId;
		this.mInBuf = aInBuf;
		this.mOutBuf = aOutBuf;
		this.mIsSSL = aIsSSL;
		this.mBuddy = aBuddy;
	}

	/**
	 * @return the mId
	 */
	public String getId() {
		return mId;
	}

	@Override
	public void onConnect(ClientChannel aClient) {
		try {
			if (mLog.isInfoEnabled()) {
				mLog.info(this + ": Connection established.");
			}
			if (mIsSSL) {
				aClient.enableSSL();
			}
			aClient.addInterest(ClientChannel.OP_READ);
		} catch (IOException ex) {
			cleanup(aClient, ex);
		}
	}

	@Override
	public void onConnectFailure(ClientChannel aClient, Exception ex) {
		cleanup(aClient, (IOException) ex);
	}

	@Override
	public void onIO(ClientChannel aClient) {
		try {
			if (aClient.isReadable()) {
				mInBuf.clear();
				int lRead = aClient.read(mInBuf);

				if (lRead == -1) {
					aClient.close();
					if ((mBuddy.interests() & ClientChannel.OP_WRITE) == 0) {
						mBuddy.close();
						if (mLog.isDebugEnabled()) {
							mLog.debug(getId() + ": Connection terminated.");
						}
					}
				} else if (lRead > 0) {
					// parse incoming data
					mLog.info(new String(mInBuf.array()));

					if (mBuddy.isOpen()) {
						mInBuf.flip();
						mBuddy.addInterest(SelectionKey.OP_WRITE);
					} else {
						aClient.close();
					}
				} else {
					aClient.addInterest(SelectionKey.OP_READ);
				}
			}
			if (aClient.isWritable()) {
				aClient.write(mOutBuf);
				if (mOutBuf.hasRemaining()) {
					aClient.addInterest(SelectionKey.OP_WRITE);
				} else {
					if (mBuddy.isOpen()) {
						mOutBuf.flip();
						mBuddy.addInterest(SelectionKey.OP_READ);
					} else {
						aClient.close();
					}
				}
			}
		} catch (IOException ex) {
			cleanup(aClient, ex);
		}
	}

	@Override
	public void onTimeout(ClientChannel aClient) {
		if (mBuddy.isOpen() && mBuddy.isTimeout()) {
			if (mLog.isInfoEnabled()) {
				mLog.info(mId + ": timed out.");
			}
			cleanup(aClient, null);
		}
	}

	@Override
	public String toString() {
		return mId;
	}

	private void cleanup(ClientChannel aClient, IOException ex) {
		if (ex != null) {
			mLog.error("Cleanup: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
		try {
			aClient.close();
		} catch (IOException ignore) {
			mLog.error("onAccept: " + ignore.getClass().getSimpleName() + ": " + ignore.getMessage());
		}
		try {
			mBuddy.close();
		} catch (IOException ignore) {
			mLog.error("onAccept: " + ignore.getClass().getSimpleName() + ": " + ignore.getMessage());
		}
		if (mLog.isInfoEnabled()) {
			mLog.info(this + ": Connection terminated.");
		}
	}

}
