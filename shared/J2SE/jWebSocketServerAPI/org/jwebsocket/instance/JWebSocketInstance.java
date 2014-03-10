//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket instance manager (Community Edition, CE)
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
package org.jwebsocket.instance;

import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Alexander Schulze
 */
public class JWebSocketInstance {

	/**
	 *
	 */
	public final static int STOPPED = 0;
	/**
	 *
	 */
	public final static int STARTING = 1;
	/**
	 *
	 */
	public final static int STARTED = 2;
	/**
	 *
	 */
	public final static int STOPPING = 3;
	/**
	 *
	 */
	public final static int SHUTTING_DOWN = 4;
	private static volatile int mStatus = STOPPED;

	/**
	 * @return the mStatus
	 */
	public static int getStatus() {
		return mStatus;
	}

	/**
	 *
	 * @param aStatus
	 */
	public static void setStatus(int aStatus) {
		mStatus = aStatus;
	}

	/**
	 * Returns TRUE if the given jWebSocket server version is compatible, FALSE
	 * otherwise.
	 *
	 * @param aVersion
	 * @return
	 */
	public static boolean isVersionCompatible(String aVersion) {
		return Tools.compareVersions(JWebSocketServerConstants.VERSION_STR, aVersion) == 0;
	}
}
