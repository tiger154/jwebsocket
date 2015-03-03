//	---------------------------------------------------------------------------
//	jWebSocket - CleanExpiredSessionsTask (Community Edition, CE)
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

import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.IStorageProvider;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketSession;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.util.JWSTimerTask;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Rolando Santamaria Maso, Alexander Schulze
 */
public class CleanExpiredSessionsTask extends JWSTimerTask {

	private final IBasicStorage<String, Object> mSessionIdsTrash;
	private final IStorageProvider mStorageProvider;
	private static final Logger mLog = Logging.getLogger(CleanExpiredSessionsTask.class);

	/**
	 *
	 * @param aSessionIdsTrash
	 * @param aStorageProvider
	 */
	public CleanExpiredSessionsTask(IBasicStorage<String, Object> aSessionIdsTrash, IStorageProvider aStorageProvider) {
		mSessionIdsTrash = aSessionIdsTrash;
		mStorageProvider = aStorageProvider;
	}

	@Override
	public void runTask() {
		Iterator<String> lKeys = mSessionIdsTrash.keySet().iterator();
		while (lKeys.hasNext()) {
			final String lKey = lKeys.next();

			Long lExpirationTime = (Long) mSessionIdsTrash.get(lKey);
			if (null == lExpirationTime) {
				break;
			}
			if (lExpirationTime < System.currentTimeMillis()) {
				try {
					// extra check to avoid issues on servers cluster (DO NOT CHANGE)
					if (null != mSessionIdsTrash.remove(lKey)) {
						IBasicStorage<String, Object> lStorage = mStorageProvider.getStorage(lKey);

						if (null != lStorage) {
							final WebSocketSession lSession = new WebSocketSession(lKey);
							lSession.setStorage(lStorage);

							Tools.getThreadPool().submit(new Runnable() {
								@Override
								public void run() {
									// support for non realtime clients connection (REST clients)
									Map<String, WebSocketConnector> lConnectors = JWebSocketFactory
											.getTokenServer().getSharedSessionConnectors(lKey);
									if (!lConnectors.isEmpty()) {
										for (WebSocketConnector lConnector : lConnectors.values()) {
											lConnector.stopConnector(CloseReason.TIMEOUT);
										}
									}

									// notifying session stopped
									SystemPlugIn.stopSession(lSession);

									try {
										// removing the session data
										mStorageProvider.removeStorage(lKey);
										if (mLog.isDebugEnabled()) {
											mLog.debug("Expired session '" + lKey + "' data successfully cleaned!");
										}
									} catch (Exception lEx) {
										mLog.error(lEx.toString() + " removing expired session storage");
									}
								}
							});
						}
					}
				} catch (Exception ex) {
					mLog.error(ex.toString() + " cleaning expired '" + lKey + "' session data");
				}
			}
		}
	}
}
