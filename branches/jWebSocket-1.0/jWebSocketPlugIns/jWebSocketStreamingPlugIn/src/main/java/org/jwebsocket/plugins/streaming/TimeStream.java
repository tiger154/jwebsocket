//	---------------------------------------------------------------------------
//	jWebSocket - Time Stream
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
 * @author aschulze
 */
public class TimeStream extends TokenStream {

	private static Logger log = Logging.getLogger(TimeStream.class);
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
	 */
	@Override
	public void startStream(long aTimeout) {
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
		} catch (Exception lEx) {
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
