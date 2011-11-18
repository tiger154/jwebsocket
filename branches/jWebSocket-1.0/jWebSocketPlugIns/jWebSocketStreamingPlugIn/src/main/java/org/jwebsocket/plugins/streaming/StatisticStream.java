//	---------------------------------------------------------------------------
//	jWebSocket - Statistics Stream
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


import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;

/**
 * implements the StatisticStream, primarily for demonstration purposes but it can
 * also be used for client/server time synchronization. It implements an
 * internal thread which broadcasts the current system time of the server to
 * the registered clients once per second.
 * @author aschulze
 */
public class StatisticStream extends TokenStream {

	private static Logger mog = Logging.getLogger(StatisticStream.class);

	/**
	 *
	 *
	 * @param aStreamID
	 * @param aServer
	 */
	public StatisticStream(String aStreamID, TokenServer aServer) {
		super(aStreamID, aServer);
		startStream(-1);
	}

	/**
	 *
	 */
	@Override
	public void startStream(long aTimeout) {
		if (mog.isDebugEnabled()) {
			mog.debug("Starting Statistics stream...");
		}
		super.startStream(aTimeout);
	}

	/**
	 *
	 */
	@Override
	public void stopStream(long aTimeout) {
		if (mog.isDebugEnabled()) {
			mog.debug("Stopping Statistics stream...");
		}
		super.stopStream(aTimeout);
	}

}
