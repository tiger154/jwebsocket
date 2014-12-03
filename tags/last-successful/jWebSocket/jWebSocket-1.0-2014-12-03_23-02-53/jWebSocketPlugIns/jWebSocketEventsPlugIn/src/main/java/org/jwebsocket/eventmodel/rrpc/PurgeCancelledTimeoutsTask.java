//	---------------------------------------------------------------------------
//	jWebSocket - PurgeCancelledTimeoutsTask (Community Edition, CE)
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
package org.jwebsocket.eventmodel.rrpc;

import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jwebsocket.util.JWSTimerTask;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class PurgeCancelledTimeoutsTask extends JWSTimerTask {

	private final Timer t;
	private static final Log logger = LogFactory.getLog(PurgeCancelledTimeoutsTask.class);

	/**
	 *
	 * @param t
	 */
	public PurgeCancelledTimeoutsTask(Timer t) {
		this.t = t;
	}

	@Override
	public void runTask() {
		if (logger.isDebugEnabled()) {
			logger.debug("Purging the timeout callbacks queue...");
		}
		t.purge();
	}
}
