//	---------------------------------------------------------------------------
//	jWebSocket - Statistics Stream (Community Edition, CE)
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

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;

/**
 * implements the StatisticStream, primarily for demonstration purposes but it
 * can also be used for client/server time synchronization. It implements an
 * internal thread which broadcasts the current system time of the server to the
 * registered clients once per second.
 *
 * @author Alexander Schulze
 */
public class StatisticStream extends TokenStream {

	private static final Logger mog = Logging.getLogger();

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
	 *
	 * @param aTimeout
	 */
	@Override
	public final void startStream(long aTimeout) {
		if (mog.isDebugEnabled()) {
			mog.debug("Starting Statistics stream...");
		}
		super.startStream(aTimeout);
	}

	/**
	 *
	 *
	 * @param aTimeout
	 */
	@Override
	public void stopStream(long aTimeout) {
		if (mog.isDebugEnabled()) {
			mog.debug("Stopping Statistics stream...");
		}
		super.stopStream(aTimeout);
	}
}
