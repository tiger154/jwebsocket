//	---------------------------------------------------------------------------
//	jWebSocket - MongoDBCacheStorageProvider (Community Edition, CE)
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
package org.jwebsocket.cachestorage.mongodb;

import org.jwebsocket.api.IBasicCacheStorage;
import org.jwebsocket.api.ICacheStorageProvider;
import org.jwebsocket.storage.mongodb.MongoDBStorageBuilder;

/**
 *
 * @author Rolando Santamaria Maso, Alexander Schulze
 */
public class MongoDBCacheStorageProvider extends MongoDBCacheStorageBuilder implements ICacheStorageProvider {

	/**
	 *
	 */
	public MongoDBCacheStorageProvider() {
		super();
	}


	@Override
	public IBasicCacheStorage getCacheStorage(String aName) throws Exception {
		return this.getCacheStorage(MongoDBStorageBuilder.V2, aName);
	}

	@Override
	public void removeCacheStorage(String aName) throws Exception {
		super.removeCacheStorage(V2, aName);
	}
}
