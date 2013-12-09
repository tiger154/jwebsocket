//  ---------------------------------------------------------------------------
//  jWebSocket - PurgeCancelledTimeoutsTask (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.eventmodel.s2c;

import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author kyberneees
 */
public class PurgeCancelledTimeoutsTask extends TimerTask {

	private Timer mTimer;
	private static final Logger mLog = Logging.getLogger();

	/**
	 *
	 * @param aTimer
	 */
	public PurgeCancelledTimeoutsTask(Timer aTimer) {
		this.mTimer = aTimer;
	}

	@Override
	public void run() {
		int lPurged = mTimer.purge(); // Keep the timer cleaned up
		if (lPurged > 0 && mLog.isDebugEnabled()) {
			mLog.debug("Purged " + lPurged + " items from timeout callbacks queue.");
		}
	}
}
