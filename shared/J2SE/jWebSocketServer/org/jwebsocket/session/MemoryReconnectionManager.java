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

import java.util.Timer;
import org.jwebsocket.cachestorage.memory.MemoryCacheStorage;
import org.jwebsocket.storage.memory.MemoryStorage;

/**
 *
 * @author kyberneees, acshulze
 */
public class MemoryReconnectionManager extends BaseReconnectionManager {

	private Timer mTimer;

	public MemoryReconnectionManager() {
		super();
	}
	
	@Override
	public boolean isExpired(String aSessionId) {
		if (getReconnectionIndex().containsKey(aSessionId)) {
			return false;
		}

		return true;
	}
	
	@Override
	public void initialize() throws Exception {
		setReconnectionIndex(new MemoryCacheStorage<String, Object>(getCacheStorageName()));
		getReconnectionIndex().initialize();

		setSessionIdsTrash(new MemoryStorage<String, Object>(getTrashStorageName()));
		getSessionIdsTrash().initialize();
		
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new CleanExpiredMemorySessionsTask(getSessionIdsTrash()), 0, 600000);
	}

	@Override
	public void shutdown() throws Exception {
	}

}
