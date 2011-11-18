//	---------------------------------------------------------------------------
//	jWebSocket - Monitor Stream
//	Copyright (c) 2010 jWebSocket.org by Innotrade GmbH, Alexander Schulze.
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

import java.io.File;
import java.util.Date;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * implements the jWebSocket monitor stream for demonstration purposes.
 * Reads certain system parameters in a predefined interval of 1 second and
 * reports it to all registered clients.
 * @author aschulze
 */
public class MonitorStream extends TokenStream {

	private static Logger mLog = Logging.getLogger(MonitorStream.class);
	private Boolean mIsRunning = false;
	private MonitorProcess mMonitorProcess = null;
	private Thread mMonitorThread = null;

	/**
	 * creates a new instance of the monitor stream.
	 * @param aStreamID The unique ID of the stream.
	 * @param aServer The Token Server associated with this stream.
	 */
	public MonitorStream(String aStreamID, TokenServer aServer) {
		super(aStreamID, aServer);
		startStream(-1);
	}

	/**
	 * starts the internal monitor thread to check for certain system
	 * parameters in a predefined interval of 1 second.
	 */
	@Override
	public void startStream(long aTimeout) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting Monitor stream...");
		}

		super.startStream(aTimeout);

		mMonitorProcess = new MonitorProcess();
		mMonitorThread = new Thread(mMonitorProcess);
		mMonitorThread.start();
	}

	/**
	 * stops the monitor thread.
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
		} catch (Exception lEx) {
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
