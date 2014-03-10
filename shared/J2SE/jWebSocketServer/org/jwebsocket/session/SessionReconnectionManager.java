//	---------------------------------------------------------------------------
//	jWebSocket - SessionReconnectionManager (Community Edition, CE)
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
package org.jwebsocket.session;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class SessionReconnectionManager extends BaseReconnectionManager {

	private final static Logger mLog = Logging.getLogger();

	@Override
	public void initialize() throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Initializing session reconnection manager...");
		}

		setSessionIdsTrash(getStorageProvider().getStorage(getTrashStorageName()));
		getSessionIdsTrash().initialize();

		super.initialize();
	}

	@Override
	public void shutdown() throws Exception {
		getSessionIdsTrash().shutdown();
	}
}
