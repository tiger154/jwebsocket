//	---------------------------------------------------------------------------
//	jWebSocket - TimeoutCallbackTask (Community Edition, CE)
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

	/**
	 *
	 * @param aConnectorId
	 * @param aNotificationId
	 * @param aNotificationHandler
	 */
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
