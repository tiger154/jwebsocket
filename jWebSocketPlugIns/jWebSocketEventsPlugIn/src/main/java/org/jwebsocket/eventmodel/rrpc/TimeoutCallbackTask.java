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
package org.jwebsocket.eventmodel.rrpc;

import javolution.util.FastMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jwebsocket.eventmodel.api.IRRPCOnResponseCallback;
import org.jwebsocket.util.JWSTimerTask;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class TimeoutCallbackTask extends JWSTimerTask {

	private final String connectorId;
	private final String upcid;
	private final FastMap<String, FastMap<String, IRRPCOnResponseCallback>> callsMap;
	private static final Log logger = LogFactory.getLog(TimeoutCallbackTask.class);

	/**
	 *
	 * @param connectorId
	 * @param upcid
	 * @param callsMap
	 */
	public TimeoutCallbackTask(String connectorId, String upcid, FastMap<String, FastMap<String, IRRPCOnResponseCallback>> callsMap) {
		this.connectorId = connectorId;
		this.upcid = upcid;
		this.callsMap = callsMap;
	}

	@Override
	public void runTask() {
		//Execute only if the OnResponse callback was not called before
		if (callsMap.containsKey(connectorId)
				&& callsMap.get(connectorId).containsKey(upcid)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Calling the failure method because of a timeout reason."
						+ " Notification: " + connectorId + ":" + upcid);
			}

			//Getting the callback
			IRRPCOnResponseCallback aOnResponse = callsMap.get(connectorId).remove(upcid);

			//Cleaning if empty
			if (callsMap.get(connectorId).isEmpty()) {
				callsMap.remove(connectorId);
			}

			aOnResponse.failure(FailureReason.TIMEOUT, connectorId);
		}
	}
}
