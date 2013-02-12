//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2011 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.session;

import java.util.Iterator;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.IStorageProvider;
import org.jwebsocket.kit.WebSocketSession;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.util.Tools;

/**
 *
 * @author kyberneees, aschulze
 */
public class CleanExpiredSessionsTask extends TimerTask {

	private IBasicStorage<String, Object> mSessionIdsTrash;
	private IStorageProvider mStorageProvider;
	private static Logger mLog = Logging.getLogger(CleanExpiredSessionsTask.class);

	public CleanExpiredSessionsTask(IBasicStorage<String, Object> aSessionIdsTrash, IStorageProvider aStorageProvider) {
		mSessionIdsTrash = aSessionIdsTrash;
		mStorageProvider = aStorageProvider;
	}

	@Override
	public void run() {
		Iterator<String> lKeys = mSessionIdsTrash.keySet().iterator();
		while (lKeys.hasNext()) {
			String lKey = lKeys.next();
			if (((Long) (mSessionIdsTrash.get(lKey)) < System.currentTimeMillis())) {
				try {
					if (null != mSessionIdsTrash.remove(lKey)) { // protection for clustering (DO NOT CHANGE)
						IBasicStorage<String, Object> lStorage = mStorageProvider.getStorage(lKey);
						mStorageProvider.removeStorage(lKey);

						if (null != lStorage) {
							final WebSocketSession lSession = new WebSocketSession(lKey);
							lSession.setStorage(lStorage);

							if (mLog.isDebugEnabled()) {
								mLog.debug("Expired '" + lKey + "' session data cleaned!");
							}

							Tools.getThreadPool().submit(new Runnable() {
								@Override
								public void run() {
									SystemPlugIn.stopSession(lSession);
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
	// TODO: check if this task has a name for rdebug purposes
}
