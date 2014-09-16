//	---------------------------------------------------------------------------
//	jWebSocket - TimeoutOutputStreamNIOWriter (Community Edition, CE)
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
package org.jwebsocket.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.util.JWSTimerTask;
import org.jwebsocket.util.Tools;

/**
 * This works OK, the only pending question is that the write method of the
 * native OutputStream never gets locked as expected.
 *
 * Advises: - Notify the connector stopped event in a thread pool instead of the
 * same thread - Check the connection state before send a packet
 *
 * @author Rolando Santamaria Maso
 * @author Alexander Schulze
 */
public class TimeoutOutputStreamNIOWriter {

	private static final Logger mLog = Logging.getLogger();
	private final static int DEFAULT_TIME_OUT_TERMINATION_THREAD = 1000;
	/**
	 * Singleton Timer instance to control all timeout tasks
	 */
	private static int mTimeout = DEFAULT_TIME_OUT_TERMINATION_THREAD;
	// can be set to "true" for heavy debugging purposes
	private static final boolean mIsDebug = false;
	// the size of this executor service should be adjusted to the maximum
	// of expected client send operations that concurrently might get 
	// to a timeout case.
	private static ExecutorService mPool = null;
	private final static Object mPoolSync = new Object();
	private OutputStream mOut = null;
	private InputStream mIn = null;
	private WebSocketConnector mConnector = null;
	private static boolean mStarted = false;
	private static Timer mTimer;

	/**
	 *
	 * @param aNumWorkers
	 * @param aTimeout
	 */
	public static void start(int aNumWorkers, int aTimeout) {
		mTimer = Tools.getTimer();
		mPool = Executors.newFixedThreadPool(aNumWorkers, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable aRunnable) {
				return new Thread(aRunnable, "jWebSocket TCP-Engine NIO writer");
			}
		});
		mTimeout = aTimeout;
		mStarted = true;
	}

	/**
	 *
	 */
	public static void stop() {
		synchronized (mPoolSync) {
			if (mStarted) {
				mPool.shutdownNow();
				mStarted = false;
			}
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aIn
	 * @param aOut
	 */
	public TimeoutOutputStreamNIOWriter(WebSocketConnector aConnector,
			InputStream aIn, OutputStream aOut) {
		mConnector = aConnector;
		mIn = aIn;
		mOut = aOut;
	}

	/**
	 *
	 * @return
	 */
	public static ExecutorService getPool() {
		return mPool;
	}

	/**
	 *
	 * @param aTimeout
	 */
	public static void setTimeout(int aTimeout) {
		mTimeout = aTimeout;
	}

	/**
	 *
	 * @return
	 */
	public static int getTimeout() {
		return mTimeout;
	}

	/**
	 * Write operation thread to execute write operations in non-blocking mode.
	 */
	class SendOperation implements Runnable {

		private final WebSocketPacket mPacket;
		private boolean mSent = false;

		public InputStream getIn() {
			return mIn;
		}

		public OutputStream getOut() {
			return mOut;
		}

		public boolean isDone() {
			return mSent;
		}

		public SendOperation(WebSocketPacket aDataPacket) {
			mPacket = aDataPacket;
		}

		@Override
		public void run() {
			// @TODO This always is being executed quickly even when the connector get's stopped
			// this sends the packet to the socket output stream
			if (mIsDebug && mLog.isDebugEnabled()) {
				mLog.debug("Physically sending packet to '" + mConnector.getId() + "' under timeout control...");
			}

			// sending packet in blocking mode
			((TCPConnector) mConnector)._sendPacket(mPacket);

			// this cancels the timeout task in case 
			// the send operation did not block for the given timeout
			if (mIsDebug && mLog.isDebugEnabled()) {
				mLog.debug("Cancelling timeout control for '" + mConnector.getId() + "' because packet had been sent properly...");
			}
			// setting the sent flag to true
			mSent = true;
		}
	}

	/**
	 * Send a data packet with timeout control.
	 *
	 * @param aDataPacket
	 */
	public void sendPacket(WebSocketPacket aDataPacket) {
		if (mIsDebug && mLog.isDebugEnabled()) {
			mLog.debug("Scheduling send operation to '" + mConnector.getId() + "'...");
		}

		synchronized (mPoolSync) {
			if (mPool.isTerminated() || mPool.isShutdown()) {
				mLog.warn("Sender thread pool is terminated or shutdown already, skipping send operation.");
				return;
			}

			// create a timer task to send the packet 
			final SendOperation lSend = new SendOperation(aDataPacket);

			// create a timeout timer task to cancel the send operation in case of disconnection
			mTimer.schedule(new JWSTimerTask() {
				@Override
				public void runTask() {
					try {
						if (!lSend.isDone()) {
							// close the outbound stream to fire exception
							// timed out write operation
							if (mIsDebug && mLog.isDebugEnabled()) {
								mLog.debug("Closing stream to '" + mConnector.getId() + "' connector due to timeout...");
							}
							lSend.getIn().close();
							lSend.getOut().close();
						}
					} catch (IOException lEx) {
						// TODO check this
					}
				}
			}, mTimeout);

			// finally execute the send operation
			mPool.execute(lSend);
		}
	}
}
