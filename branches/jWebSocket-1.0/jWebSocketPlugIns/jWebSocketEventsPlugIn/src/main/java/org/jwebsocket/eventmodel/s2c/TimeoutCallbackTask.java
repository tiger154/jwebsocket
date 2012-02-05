//  ---------------------------------------------------------------------------
//  jWebSocket - TimeoutCallbackTask
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

import java.util.TimerTask;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;

/**
 *
 * @author kyberneees
 */
public class TimeoutCallbackTask extends TimerTask {

	private String mConnectorId;
	private String mNotificationId;
	private S2CEventNotificationHandler mNotificationHandler;
	private static Logger mLog = Logging.getLogger(TimeoutCallbackTask.class);

	public TimeoutCallbackTask(String aConnectorId, String aNotificationId, S2CEventNotificationHandler aNotificationHandler) {
		this.mConnectorId = aConnectorId;
		this.mNotificationId = aNotificationId;
		this.mNotificationHandler = aNotificationHandler;
	}

	@Override
	public void run() {
		//Execute only if the OnResponse callback was not called before
		if (mNotificationHandler.getCallbacks().containsKey(mConnectorId)
				&& mNotificationHandler.getCallbacks().get(mConnectorId).containsKey(mNotificationId)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Calling the failure method because of a timeout reason."
						+ " Notification: " + mConnectorId + ":" + mNotificationId);
			}

			//Getting the OnResponse callback
			OnResponse lCallback = mNotificationHandler.getCallbacks().get(mConnectorId).remove(mNotificationId);

			//Cleaning if empty
			if (mNotificationHandler.getCallbacks().get(mConnectorId).isEmpty()) {
				mNotificationHandler.getCallbacks().remove(mConnectorId);
			}

			lCallback.failure(FailureReason.TIMEOUT, mConnectorId);
		}
	}
}
