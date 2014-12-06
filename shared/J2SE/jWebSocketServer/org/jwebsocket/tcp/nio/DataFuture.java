//	---------------------------------------------------------------------------
//	jWebSocket - NIO Engine, DataFuture (Community Edition, CE)
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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.async.IOFutureListener;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author Alexander Schulze
 */
public class DataFuture implements IOFuture {

	private static final Logger mLog = Logging.getLogger();
	private final List<IOFutureListener> mListeners;
	private boolean mDone;
	private boolean mSuccess;
	private Throwable mCause;
	private final WebSocketConnector mConnector;
	private ByteBuffer mData;

	/**
	 *
	 * @param aConnector
	 * @param aData
	 */
	public DataFuture(WebSocketConnector aConnector, ByteBuffer aData) {
		this.mConnector = aConnector;
		this.mData = aData;
		mListeners = new ArrayList<IOFutureListener>();
	}

	@Override
	public WebSocketConnector getConnector() {
		return mConnector;
	}

	@Override
	public boolean isDone() {
		return mDone;
	}

	@Override
	public boolean isCancelled() {
		return false;  // not implemented
	}

	@Override
	public boolean isSuccess() {
		return mSuccess;
	}

	@Override
	public Throwable getCause() {
		return mCause;
	}

	@Override
	public boolean cancel() {
		return false;  // not implemented
	}

	@Override
	public boolean setSuccess() {
		mSuccess = true;
		mDone = true;
		notifyListeners();
		return mSuccess;
	}

	@Override
	public boolean setFailure(Throwable lCause) {
		if (!mSuccess && !mDone) {
			this.mCause = lCause;
			mSuccess = false;
			mDone = true;
			notifyListeners();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean setProgress(long aAmount, long aCurrent, long aTotal) {
		return false;  // not implemented
	}

	@Override
	public void addListener(IOFutureListener aListener) {
		if (mDone) {
			try {
				aListener.operationComplete(this);
			} catch (Exception lEx) {
				mLog.info("Exception while notifying IOFuture listener", lEx);
			}
		}
		mListeners.add(aListener);
	}

	@Override
	public void removeListener(IOFutureListener aListener) {
		mListeners.remove(aListener);
	}

	/**
	 *
	 * @return
	 */
	public ByteBuffer getData() {
		return mData;
	}

	/**
	 *
	 * @param aData
	 */
	public void setData(ByteBuffer aData) {
		mData = aData;
	}

	private void notifyListeners() {
		try {
			for (IOFutureListener listener : mListeners) {
				listener.operationComplete(this);
			}
		} catch (Exception e) {
			mLog.info("Exception while notifying IOFuture listener", e);
		}
	}
}
