//	---------------------------------------------------------------------------
//	jWebSocket - Time Stream (Community Edition, CE)
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

import java.util.Calendar;
import java.util.Date;

import java.util.GregorianCalendar;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * implements the TimeStream, primarily for demonstration purposes but it can
 * also be used for client/server time synchronization. It implements an
 * internal thread which broadcasts the current system time of the server to the
 * registered clients once per second.
 *
 * @author Alexander Schulze
 */
public class TimeStream extends TokenStream {

	private static final Logger log = Logging.getLogger();
	private Boolean mIsRunning = false;
	private TimerProcess mTimeProcess = null;
	private Thread mTimeThread = null;

	/**
	 *
	 *
	 * @param aStreamID
	 * @param aServer
	 */
	public TimeStream(String aStreamID, TokenServer aServer) {
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
		if (log.isDebugEnabled()) {
			log.debug("Starting Time stream...");
		}

		super.startStream(aTimeout);

		mTimeProcess = new TimerProcess();
		mTimeThread = new Thread(mTimeProcess, "jWebSocket Streaming Plug-in, Time Process");
		mTimeThread.start();
	}

	/**
	 *
	 *
	 * @param aTimeout
	 */
	@Override
	public void stopStream(long aTimeout) {
		if (log.isDebugEnabled()) {
			log.debug("Stopping Time stream...");
		}
		long lStarted = new Date().getTime();
		mIsRunning = false;
		try {
			mTimeThread.join(aTimeout);
		} catch (InterruptedException lEx) {
			log.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		if (log.isDebugEnabled()) {
			long lDuration = new Date().getTime() - lStarted;
			if (mTimeThread.isAlive()) {
				log.warn("Time stream did not stopped after " + lDuration + "ms.");
			} else {
				log.debug("Time stream stopped after " + lDuration + "ms.");
			}
		}

		super.stopStream(aTimeout);
	}

	private class TimerProcess implements Runnable {

		@Override
		@SuppressWarnings("SleepWhileInLoop")
		public void run() {
			if (log.isDebugEnabled()) {
				log.debug("Running time stream...");
			}
			mIsRunning = true;
			Thread.currentThread().setName("jWebSocket TimeStream");
			while (mIsRunning) {
				try {
					Thread.sleep(1000);

					Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.streaming", "event");
					GregorianCalendar lCal = new GregorianCalendar();
					lToken.setString("name", "stream");
					lToken.setString("msg", new Date(lCal.getTimeInMillis()).toString());
					lToken.setString("streamID", getStreamID());
					lToken.setInteger("year", lCal.get(Calendar.YEAR));
					lToken.setInteger("month", lCal.get(Calendar.MONTH));
					lToken.setInteger("day", lCal.get(Calendar.DAY_OF_MONTH));
					lToken.setInteger("hours", lCal.get(Calendar.HOUR_OF_DAY));
					lToken.setInteger("minutes", lCal.get(Calendar.MINUTE));
					lToken.setInteger("seconds", lCal.get(Calendar.SECOND));
					lToken.setInteger("millis", lCal.get(Calendar.MILLISECOND));

					// keep this her for demo purposes
					/*
					 * FastMap<String, Object> lRecord = new FastMap<String,
					 * Object>(); lRecord.put("string_field", "value");
					 * lRecord.put("int_field", 4711);
					 *
					 * lToken.setMap("record", lRecord);
					 */
					// log.debug("Time streamer queues '" + lData + "'...");
					put(lToken);
				} catch (InterruptedException lEx) {
					log.error("(run) " + lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("Time stream stopped.");
			}
		}
	}
}
