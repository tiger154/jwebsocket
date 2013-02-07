//	---------------------------------------------------------------------------
//	jWebSocket - TimeoutOutputStreamNIOWriter
//	Copyright (c) 2011 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.util.Tools;

/**
 * This works OK, the only pending question is that the write method of the
 * native OutputStream never gets locked as expected.
 *
 * Advises: - Notify the connector stopped event in a thread pool instead of the
 * same thread - Check the connection state before send a packet
 *
 * @author kyberneees
 * @author aschulze
 */
public class TimeoutOutputStreamNIOWriter {

	private static Logger mLog = Logging.getLogger();
	private final static int DEFAULT_TIME_OUT_TERMINATION_THREAD = 1000;
	/**
	 * Singleton Timer instance to control all timeout tasks
	 */
	private static int mTimeout = DEFAULT_TIME_OUT_TERMINATION_THREAD;
	private static Timer mTimer;
	// can be set to "true" for heavy debugging purposes
	private static boolean mIsDebug = false;
	// the size of this executor service should be adjusted to the maximum
	// of expected client send operations that concurrently might get 
	// to a timeout case.
	private static ExecutorService mPool = null;
	private OutputStream mOut = null;
	private InputStream mIn = null;
	private WebSocketConnector mConnector = null;

	public static void start(int aNumWorkers, int aTimeout) {
		if (null == mTimer) {
			mTimer = new Timer("jWebSocket TCP-Engine SendScheduler");
			Tools.getTimer().schedule(new PurgeCancelledWriterTasks(mTimer), 0, 2000);
			mPool = Executors.newFixedThreadPool(aNumWorkers, new ThreadFactory() {

				@Override
				public Thread newThread(Runnable aRunnable) {
					return new Thread(aRunnable, "[pool]TCPEngine NIO writer thread");
				}
			});
			mTimeout = aTimeout;
		}
	}

	public static void stop() {
		if (null != mTimer) {
			mPool.shutdownNow();
			mTimer.cancel();
			mTimer.purge();
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aTimeout
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

	public static void setTimeout(int aTimeout) {
		mTimeout = aTimeout;
	}

	public static int getTimeout() {
		return mTimeout;
	}

	/**
	 *
	 * @return
	 */
	public static Timer getTimer() {
		return mTimer;
	}

	/**
	 * Write operation thread to execute write operations in non-blocking mode.
	 */
	class SendOperation implements Runnable {

		private int mTimeout;
		private WebSocketPacket mPacket;
		private TimerTask mTimeoutTask;

		public InputStream getIn() {
			return mIn;
		}

		public OutputStream getOut() {
			return mOut;
		}

		public int getTimeout() {
			return mTimeout;
		}

		public SendOperation(WebSocketPacket aDataPacket) {
			this.mPacket = aDataPacket;
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
			mTimeoutTask.cancel();
		}
	}

	class TimeoutTimerTask extends TimerTask {

		private SendOperation mSendOperation;

		public TimeoutTimerTask(SendOperation aSendOperation) {
			mSendOperation = aSendOperation;
			mSendOperation.mTimeoutTask = this;
		}

		@Override
		public void run() {
			try {
				// close the outbound stream to fire exception
				// timed out write operation
				if (mIsDebug && mLog.isDebugEnabled()) {
					mLog.debug("Closing stream to '" + mConnector.getId() + "' connector due to timeout...");
				}
				mSendOperation.getIn().close();
				mSendOperation.getOut().close();

				// communication channel is broken
				// mConnector.stopConnector(CloseReason.BROKEN);
			} catch (IOException ex) {
				// TODO check this
			}
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
		// create a timer task to send the packet
		SendOperation lSend = new SendOperation(aDataPacket);
		// create a timeout timer task to cancel the send operation in case of disconnection
		TimerTask lTask = new TimeoutTimerTask(lSend);
		// schedule the watcher for the send operation
		mTimer.schedule(lTask, mTimeout);
		// finally execute the send operation
		mPool.execute(lSend);
	}
}
