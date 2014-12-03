//	---------------------------------------------------------------------------
//	jWebSocket - MongoDBStorageProvider (Community Edition, CE)
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
package org.jwebsocket.storage.mongodb;

import com.mongodb.Mongo;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.IStorageProvider;

/**
 *
 * @author Rolando Santamaria Maso, Alexander Schulze
 */
public class MongoDBStorageProvider extends MongoDBStorageBuilder implements IStorageProvider {

	/**
	 *
	 */
	public MongoDBStorageProvider() {
		super();
	}

	/**
	 * {@inheritDoc
	 * @throws java.lang.Exception
	 */
	@Override
	public IBasicStorage<String, Object> getStorage(String aName) throws Exception {
		return this.getStorage(MongoDBStorageBuilder.V2, aName);
	}

	@Override
	public void removeStorage(String aName) throws Exception {
		super.removeStorage(MongoDBStorageBuilder.V2, aName);
	}

	/**
	 *
	 * @param aConnection
	 * @param aDatabaseName
	 * @param aStorageName
	 * @param aCollectionName
	 * @return
	 * @throws Exception
	 */
	public static IBasicStorage getInstance(Mongo aConnection, String aDatabaseName,
			String aCollectionName, String aStorageName) throws Exception {
		MongoDBStorageBuilder lBuilder = new MongoDBStorageBuilder();
		lBuilder.setConnection(aConnection);
		lBuilder.setDatabaseName(aDatabaseName);
		lBuilder.setCollectionName(aCollectionName);
		lBuilder.initialize();

		return lBuilder.getStorage(MongoDBStorageBuilder.V2, aStorageName);
	}
}
