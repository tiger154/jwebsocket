//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
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

	private String connectorId;
	private String notificationId;
	private S2CEventNotificationHandler nh;
	private static Logger mLog = Logging.getLogger(TimeoutCallbackTask.class);

	public TimeoutCallbackTask(String connectorId, String notificationId, S2CEventNotificationHandler nh) {
		this.connectorId = connectorId;
		this.notificationId = notificationId;
		this.nh = nh;
	}

	@Override
	public void run() {
		//Execute only if the OnResponse callback was not called before
		if (nh.getCallsMap().containsKey(connectorId)
				&& nh.getCallsMap().get(connectorId).containsKey(notificationId)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Calling the failure method because of a timeout reason."
						+ " Notification: " + connectorId + ":" + notificationId);
			}

			//Getting the OnResponse callback
			OnResponse aOnResponse = nh.getCallsMap().get(connectorId).remove(notificationId);

			//Cleaning if empty
			if (nh.getCallsMap().get(connectorId).isEmpty()) {
				nh.getCallsMap().remove(connectorId);
			}

			aOnResponse.failure(FailureReason.TIMEOUT, connectorId);
		}
	}
}
