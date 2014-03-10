//	---------------------------------------------------------------------------
//	jWebSocket - Stress Stream (Community Edition, CE)
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
 *
 * @author Alexander Schulze
 */
public class StressStream extends TokenStream {

	private static final Logger mLog = Logging.getLogger();
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
	 *
	 * @param aTimeout
	 */
	@Override
	public final void startStream(long aTimeout) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting stress stream...");
		}
		super.startStream(aTimeout);

		mStressProcess = new StressProcess();
		mStressThread = new Thread(mStressProcess, "jWebSocket Streaming Plug-in, Stress Process");
		mStressThread.start();
	}

	/**
	 *
	 *
	 * @param aTimeout
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
		} catch (InterruptedException lEx) {
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
		@SuppressWarnings("SleepWhileInLoop")
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
