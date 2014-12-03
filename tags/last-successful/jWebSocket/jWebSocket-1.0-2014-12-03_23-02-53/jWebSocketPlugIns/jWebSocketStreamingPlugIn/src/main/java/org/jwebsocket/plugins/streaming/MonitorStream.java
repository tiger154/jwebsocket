//	---------------------------------------------------------------------------
//	jWebSocket - Monitor Stream (Community Edition, CE)
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

import java.io.File;
import java.util.Date;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * implements the jWebSocket monitor stream for demonstration purposes. Reads
 * certain system parameters in a predefined interval of 1 second and reports it
 * to all registered clients.
 *
 * @author Alexander Schulze
 */
public class MonitorStream extends TokenStream {

	private static final Logger mLog = Logging.getLogger();
	private Boolean mIsRunning = false;
	private MonitorProcess mMonitorProcess = null;
	private Thread mMonitorThread = null;

	/**
	 * creates a new instance of the monitor stream.
	 *
	 * @param aStreamID The unique ID of the stream.
	 * @param aServer The Token Server associated with this stream.
	 */
	public MonitorStream(String aStreamID, TokenServer aServer) {
		super(aStreamID, aServer);
		startStream(-1);
	}

	/**
	 * starts the internal monitor thread to check for certain system parameters
	 * in a predefined interval of 1 second.
	 *
	 * @param aTimeout
	 */
	@Override
	public final void startStream(long aTimeout) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting Monitor stream...");
		}

		super.startStream(aTimeout);

		mMonitorProcess = new MonitorProcess();
		mMonitorThread = new Thread(mMonitorProcess, "jWebSocket Streaming Plug-in, Monitor Process");
		mMonitorThread.start();
	}

	/**
	 * stops the monitor thread.
	 *
	 * @param aTimeout
	 */
	@Override
	public void stopStream(long aTimeout) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping Monitor stream...");
		}
		long lStarted = new Date().getTime();
		mIsRunning = false;
		try {
			mMonitorThread.join(aTimeout);
		} catch (InterruptedException lEx) {
			mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		if (mLog.isDebugEnabled()) {
			long lDuration = new Date().getTime() - lStarted;
			if (mMonitorThread.isAlive()) {
				mLog.warn("Monitor stream did not stopped after " + lDuration + "ms.");
			} else {
				mLog.debug("Monitor stream stopped after " + lDuration + "ms.");
			}
		}

		super.stopStream(aTimeout);
	}

	private class MonitorProcess implements Runnable {

		@Override
		@SuppressWarnings("SleepWhileInLoop")
		public void run() {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Running monitor stream...");
			}
			mIsRunning = true;
			Thread.currentThread().setName("jWebSocket MonitorStream");

			while (mIsRunning) {
				try {
					Thread.sleep(1000);

					Token lToken = TokenFactory.createToken(BaseToken.TT_EVENT);
					lToken.setString("name", "stream");
					lToken.setString("streamID", getStreamID());

					// collect some data to monitor
					Runtime lRT = Runtime.getRuntime();
					lToken.setInteger("totalMem", (int) (lRT.totalMemory() >> 10));
					lToken.setInteger("freeMem", (int) (lRT.totalMemory() >> 10));

					TokenServer lServer = getServer();
					lToken.setInteger("clientCount", lServer.getAllConnectors().size());

					File lFile = new File(".");
					lToken.setInteger("freeDisk", (int) (lFile.getFreeSpace() >> 10));
					lToken.setInteger("totalDisk", (int) (lFile.getTotalSpace() >> 10));
					lToken.setInteger("usableDisk", (int) (lFile.getUsableSpace() >> 10));

					// : further tags to be continued....
					put(lToken);
				} catch (InterruptedException lEx) {
					mLog.error("(run) " + lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
				}
			}

			if (mLog.isDebugEnabled()) {
				mLog.debug("Monitor stream stopped.");
			}
		}
	}
}
