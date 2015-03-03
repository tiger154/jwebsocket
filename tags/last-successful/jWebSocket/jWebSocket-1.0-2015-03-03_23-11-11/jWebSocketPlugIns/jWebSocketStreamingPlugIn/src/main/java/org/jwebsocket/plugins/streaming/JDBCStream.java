//	---------------------------------------------------------------------------
//	jWebSocket - JDBC Stream (Community Edition, CE)
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * implements the JDBCStream, primarily for demonstration purposes but it can
 * also be used for client/server time synchronization. It implements an
 * internal thread which broadcasts the current system time of the server to the
 * registered clients once per second.
 *
 * @author Alexander Schulze
 */
public class JDBCStream extends TokenStream {

	private static final Logger mLog = Logging.getLogger();
	private Boolean mIsRunning = false;
	private final DBPollingProcess mDbPollingProcess = null;
	private final Thread mDbPollingThread = null;
	private final Connection mConnection = null;

	/**
	 *
	 *
	 * @param aStreamID
	 * @param aServer
	 */
	public JDBCStream(String aStreamID, TokenServer aServer) {
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
			mLog.debug("Starting JDBC stream...");
		}

		super.startStream(aTimeout);

		/*
		 dbPollingProcess = new DBPollingProcess();
		 dbPollingThread = new Thread(dbPollingProcess, , "jWebSocket Streaming Plug-in, DB Polling Process");
		 dbPollingThread.start();
		 */
		/*
		 try {
		 // load the class
		 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		 mConnection = DriverManager.getConnection(
		 "jdbc:sqlserver://host:15001;database=dbname;integratedSecurity=false;user=username;password=password;",
		 "username", "password");
		 } catch (Exception lEx) {
		 log.error("(run) " + lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		 }
		 */
	}

	/**
	 *
	 *
	 * @param aTimeout
	 */
	@Override
	public void stopStream(long aTimeout) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping JDBC stream...");
		}
		// long lStarted = new Date().getTime();
		mIsRunning = false;

		try {
			if (mConnection != null
					&& !mConnection.isClosed()) {
				// mLog.debug("Closing connection...");
				mConnection.close();
				// mLog.debug("Connection closed.");
			}
		} catch (SQLException lEx) {
			mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		/*
		 try {
		 dbPollingThread.join(aTimeout);
		 } catch (Exception lEx) {
		 log.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		 }
		 if (log.isDebugEnabled()) {
		 long lDuration = new Date().getTime() - lStarted;
		 if (dbPollingThread.isAlive()) {
		 log.warn("JDBC stream did not stopped after " + lDuration + "ms.");
		 } else {
		 log.debug("JDBC stream stopped after " + lDuration + "ms.");
		 }
		 }
		 */
		super.stopStream(aTimeout);
	}

	private class DBPollingProcess implements Runnable {

		@Override
		@SuppressWarnings("SleepWhileInLoop")
		public void run() {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Running JDBC stream...");
			}
			mIsRunning = true;
			while (mIsRunning) {
				try {
					Thread.sleep(1000);

					Token lToken = TokenFactory.createToken("event");
					lToken.setString("name", "stream");
					lToken.setString("msg", new Date().toString());
					lToken.setString("streamID", getStreamID());

					// log.debug("Time streamer queues '" + lData + "'...");
					put(lToken);
				} catch (InterruptedException lEx) {
					mLog.error("(run) " + lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
				}
			}
			if (mLog.isDebugEnabled()) {
				mLog.debug("JDBC stream stopped.");
			}
		}
	}
}
