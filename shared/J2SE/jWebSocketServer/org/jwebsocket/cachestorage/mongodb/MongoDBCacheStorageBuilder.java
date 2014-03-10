//	---------------------------------------------------------------------------
//	jWebSocket - MongoDBCacheStorageBuilder (Community Edition, CE)
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

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import org.jwebsocket.api.IBasicCacheStorage;
import org.jwebsocket.api.IInitializable;
import org.springframework.util.Assert;

/**
 * Create MongoDBCacheStorage instances
 *
 * @author Rolando Santamaria Maso
 */
public class MongoDBCacheStorageBuilder implements IInitializable {

	private Mongo mConnection;
	private String mDatabaseName;
	private String mCollectionName;
	private String mUsername, mPassword;
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
	 * @return
	 */
	public String getUsername() {
		return mUsername;
	}

	/**
	 *
	 * @param aUsername
	 */
	public void setUsername(String aUsername) {
		this.mUsername = aUsername;
	}

	/**
	 *
	 * @return
	 */
	public String getPassword() {
		return mPassword;
	}

	/**
	 *
	 * @param aPassword
	 */
	public void setPassword(String aPassword) {
		mPassword = aPassword;
	}

	/**
	 *
	 * @return The Mongo database connection
	 */
	public Mongo getConnection() {
		return mConnection;
	}

	/**
	 *
	 * @param aConnection The Mongo database connection to set
	 */
	public void setConnection(Mongo aConnection) {
		mConnection = aConnection;
	}

	/**
	 *
	 * @param aVersion
	 * @param aName The cache storage name to build
	 * @return The cache storage ready to use.
	 * @throws Exception
	 */
	public IBasicCacheStorage<String, Object> getCacheStorage(String aVersion, String aName) throws Exception {
		IBasicCacheStorage<String, Object> lCache = null;
		if (aVersion.equals(V1)) {
			lCache = new MongoDBCacheStorageV1<String, Object>(aName, mDatabase);
			lCache.initialize();
		} else if (aVersion.equals(V2)) {
			lCache = new MongoDBCacheStorageV2<String, Object>(aName, mCollection);
			lCache.initialize();
		}

		return lCache;
	}

	/**
	 *
	 * @param aVersion
	 * @param aName
	 * @throws Exception
	 */
	public void removeCacheStorage(String aVersion, String aName) throws Exception {
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
	 *
	 * @param aDatabaseName
	 */
	public void setDatabaseName(String aDatabaseName) {
		mDatabaseName = aDatabaseName;
	}

	/**
	 * @return The database collection name for cache storages of version 2
	 */
	public String getCollectionName() {
		return mCollectionName;
	}

	/**
	 * @param aCollectionName The database collection name for cache storages of
	 * version 2
	 */
	public void setCollectionName(String aCollectionName) {
		mCollectionName = aCollectionName;
	}

	@Override
	public void initialize() throws Exception {
		Assert.notNull(mConnection, "The 'connection' property cannot be null!");
		Assert.notNull(mDatabaseName, "The 'databaseName' property cannot be null!");
		Assert.notNull(mCollectionName, "The 'collectionName' property cannot be null!");

		//Getting the temporal database instance to improve performance
		mDatabase = mConnection.getDB(mDatabaseName);
		if (null != mUsername) {
			// authenticating on database
			Assert.isTrue(mDatabase.authenticate(mUsername, mPassword.toCharArray()),
					"Invalid credentials!");
		}

		//Getting the temporal collection instance to improve performance
		mCollection = mDatabase.getCollection(mCollectionName);
	}

	@Override
	public void shutdown() throws Exception {
	}
}
