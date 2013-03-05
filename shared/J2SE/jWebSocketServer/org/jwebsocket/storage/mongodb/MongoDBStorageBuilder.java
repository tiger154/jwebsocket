//  ---------------------------------------------------------------------------
//  jWebSocket - MongoDBStorageBuilder (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import org.jwebsocket.api.IBasicStorage;

/**
 * Create MongoDBStorage instances
 *
 * @author kyberneees
 */
public class MongoDBStorageBuilder {

	private Mongo mCon;
	private String mDatabaseName;
	private String mCollectionName;
	/**
	 *
	 */
	public static final String V1 = "v1";
	/**
	 *
	 */
	public static final String V2 = "v2";
	private DBCollection mCollection = null;
	private DB mDatabase = null;

	/**
	 *
	 * @return The Mongo database connection
	 */
	public Mongo getCon() {
		return mCon;
	}

	/**
	 *
	 * @param aCon The Mongo database connection to set
	 */
	public void setCon(Mongo aCon) {
		this.mCon = aCon;
	}

	/**
	 *
	 * @param aVersion
	 * @param aName
	 * @return
	 * @throws Exception
	 */
	public IBasicStorage<String, Object> getStorage(String aVersion, String aName) throws Exception {
		IBasicStorage<String, Object> lStorage = null;
		if (aVersion.equals(V1)) {
			lStorage = new MongoDBStorageV1<String, Object>(aName, mDatabase);
			lStorage.initialize();
		} else if (aVersion.equals(V2)) {
			lStorage = new MongoDBStorageV2<String, Object>(aName, mCollection);
			lStorage.initialize();
		}

		return lStorage;
	}

	/**
	 *
	 * @param aVersion
	 * @param aName
	 * @throws Exception
	 */
	public void removeStorage(String aVersion, String aName) throws Exception {
		if (aVersion.equals(V1)) {
			mDatabase.getCollection(aName).drop();
		} else if (aVersion.equals(V2)) {
			mCollection.remove(new BasicDBObject().append("ns", aName));
		}
	}

	/**
	 * @return the databaseName
	 */
	public String getDatabaseName() {
		return mDatabaseName;
	}

	/**
	 * @param aDatabaseName the databaseName to set
	 */
	public void setDatabaseName(String aDatabaseName) {
		this.mDatabaseName = aDatabaseName;

		//Getting the temporal database instance to improve performance
		mDatabase = mCon.getDB(aDatabaseName);
	}

	/**
	 * @return The database collection name for storages of version 2
	 */
	public String getCollectionName() {
		return mCollectionName;
	}

	/**
	 * @param aCollectionName The database collection name for storages of
	 * version 2
	 */
	public void setCollectionName(String aCollectionName) {
		this.mCollectionName = aCollectionName;

		//Getting the temporal collection instance to improve performance
		mCollection = mCon.getDB(mDatabaseName).getCollection(aCollectionName);
	}
}