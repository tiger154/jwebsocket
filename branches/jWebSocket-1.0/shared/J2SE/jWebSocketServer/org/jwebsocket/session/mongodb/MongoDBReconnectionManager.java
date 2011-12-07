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
package org.jwebsocket.session.mongodb;

import org.jwebsocket.cachestorage.mongodb.MongoDBCacheStorageBuilder;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.session.BaseReconnectionManager;
import org.jwebsocket.storage.mongodb.MongoDBStorageBuilder;

/**
 *
 * @author kyberneees
 */
public class MongoDBReconnectionManager extends BaseReconnectionManager {

	private MongoDBCacheStorageBuilder cacheStorageBuilder;
	private MongoDBStorageBuilder storageBuilder;
	private static Logger mLog = Logging.getLogger(MongoDBReconnectionManager.class);

	public MongoDBCacheStorageBuilder getCacheStorageBuilder() {
		return cacheStorageBuilder;
	}

	public void setCacheStorageBuilder(MongoDBCacheStorageBuilder cacheStorageBuilder) {
		this.cacheStorageBuilder = cacheStorageBuilder;
	}

	@Override
	public void initialize() throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Initializing...");
		}

		setReconnectionIndex(cacheStorageBuilder.getCacheStorage(MongoDBCacheStorageBuilder.V1, getCacheStorageName()));
		getReconnectionIndex().initialize();
		
		setSessionIdsTrash(storageBuilder.getStorage(MongoDBStorageBuilder.V1, getTrashStorageName()));
		getSessionIdsTrash().initialize();
	}

	@Override
	public void shutdown() throws Exception {
	}

	@Override
	public boolean isExpired(String aSessionId) {
		if (getReconnectionIndex().containsKey(aSessionId)) {
			return false;
		}

		return true;
	}
}
