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
     * Available in MongoDB server from version 2.2
     */
    private boolean mServerExpirationSupported = false;

    /**
     *
     */
    public MongoDBCacheStorageProvider() {
        super();
    }

    /**
     * Indicates of the MongoDB server supports documents expiration.
     *
     * @return
     */
    public boolean isServerExpirationSupported() {
        return mServerExpirationSupported;
    }

    public void setServerExpirationSupported(boolean aIsServerExpirationSupported) {
        this.mServerExpirationSupported = aIsServerExpirationSupported;
    }

    @Override
    public IBasicCacheStorage getCacheStorage(String aName) throws Exception {
        if (isServerExpirationSupported()) {
            // TTL is default option
            return this.getCacheStorage(MongoDBCacheStorageBuilder.V3, aName);
        }
        return this.getCacheStorage(MongoDBStorageBuilder.V2, aName);
    }

    @Override
    public void removeCacheStorage(String aName) throws Exception {
        // V3 and V2 is same behavior 
        super.removeCacheStorage(V2, aName);
    }
}
