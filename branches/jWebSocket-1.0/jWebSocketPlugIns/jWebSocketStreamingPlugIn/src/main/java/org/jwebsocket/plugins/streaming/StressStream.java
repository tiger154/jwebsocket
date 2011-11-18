//	---------------------------------------------------------------------------
//	jWebSocket - stress Stream
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.plugins.streaming;

import java.util.Date;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * implements the stressStream, primarily for demonstration purposes but it can
 * also be used for client/server stress synchronization. It implements an
 * internal thread which broadcasts the current system stress of the server to
 * the registered clients once per second.
 * @author aschulze
 */
public class StressStream extends TokenStream {

	private static Logger mLog = Logging.getLogger(StressStream.class);
	private Boolean mIsRunning = false;
	private StressProcess mStressProcess = null;
	private Thread mStressThread = null;

	/**
	 *
	 *
	 * @param aStreamID
	 * @param aServer
	 */
	public StressStream(String aStreamID, TokenServer aServer) {
		super(aStreamID, aServer);
		startStream(-1);
	}

	/**
	 *
	 */
	@Override
	public void startStream(long aTimeout) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting stress stream...");
		}
		super.startStream(aTimeout);

		mStressProcess = new StressProcess();
		mStressThread = new Thread(mStressProcess);
		mStressThread.start();
	}

	/**
	 *
	 */
	@Override
	public void stopStream(long aTimeout) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping stress stream...");
		}
		long lStarted = new Date().getTime();
		mIsRunning = false;
		try {
			mStressThread.join(aTimeout);
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		if (mLog.isDebugEnabled()) {
			long lDuration = new Date().getTime() - lStarted;
			if (mStressThread.isAlive()) {
				mLog.warn("stress stream did not stopped after " + lDuration + "ms.");
			} else {
				mLog.debug("stress stream stopped after " + lDuration + "ms.");
			}
		}

		super.stopStream(aTimeout);
	}

	private class StressProcess implements Runnable {

		@Override
		public void run() {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Running stress stream...");
			}
			mIsRunning = true;
			Thread.currentThread().setName("jWebSocket StressStream");
			while (mIsRunning) {
				try {
					Thread.sleep(50);

					Token lToken = TokenFactory.createToken(BaseToken.TT_EVENT);
					lToken.setString("name", "stream");
					lToken.setString("msg", String.valueOf(new Date().getTime()));

					put(lToken);
				} catch (InterruptedException lEx) {
					mLog.error("(run) " + lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
				}
			}
			if (mLog.isDebugEnabled()) {
				mLog.debug("stress stream stopped.");
			}
		}
	}
}
