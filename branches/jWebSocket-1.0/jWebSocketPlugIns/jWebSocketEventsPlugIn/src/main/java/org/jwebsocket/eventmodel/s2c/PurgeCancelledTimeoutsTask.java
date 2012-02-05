//  ---------------------------------------------------------------------------
//  jWebSocket - PurgeCancelledTimeoutsTask
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
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
	private static Logger mLog = Logging.getLogger(PurgeCancelledTimeoutsTask.class);

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
