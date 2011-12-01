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
import org.jwebsocket.logging.Logging;

/**
 *
 * @author kyberneees,aschulze
 */
public class CleanExpiredSessionsTask extends TimerTask {

	private IBasicStorage<String, Object> mSessionIdsTrash;
	private IStorageProvider mStorageProvider;
	private static Logger mLog = Logging.getLogger(CleanExpiredSessionsTask.class);

	public CleanExpiredSessionsTask(IBasicStorage<String, Object> aSessionIdsTrash,
			IStorageProvider aStorageProvider) {
		this.mSessionIdsTrash = aSessionIdsTrash;
		this.mStorageProvider = aStorageProvider;
	}

	@Override
	public void run() {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Cleaning expired sessions...");
		}
		Iterator<String> lKeys = mSessionIdsTrash.keySet().iterator();
		while (lKeys.hasNext()) {
			String lKey = lKeys.next();
			if (((Long) (mSessionIdsTrash.get(lKey)) < System.currentTimeMillis())) {
				try {
					mStorageProvider.removeStorage(lKey);
					mSessionIdsTrash.remove(lKey);
				} catch (Exception lEx) {
					mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
				}
			}
		}
	}
	// TODO: create something similar to clean-up session index (reconnection manager)
}
