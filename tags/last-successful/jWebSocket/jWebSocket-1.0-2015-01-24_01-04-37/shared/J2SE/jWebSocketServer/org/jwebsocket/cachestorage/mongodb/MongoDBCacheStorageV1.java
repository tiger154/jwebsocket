//	---------------------------------------------------------------------------
//	jWebSocket - MongoDBCacheStorageV1 (Community Edition, CE)
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

import com.mongodb.*;
import java.util.Set;
import javolution.util.FastSet;
import org.jwebsocket.api.IBasicCacheStorage;
import org.jwebsocket.storage.BaseStorage;

/**
 * This class uses MongoDB servers to persist the information. <br> Each cache
 * storage represents a database collection. Please see for MongoDB collections
 * number limit.
 *
 * @param <K>
 * @param <V>
 * @author Rolando Santamaria Maso
 */
public class MongoDBCacheStorageV1<K, V> extends BaseStorage<K, V> implements IBasicCacheStorage<K, V> {

	private final DB mDatabase;
	private String mName;
	private DBCollection mCollection;

	/**
	 *
	 * @param aName
	 * @param aDatabase
	 */
	public MongoDBCacheStorageV1(String aName, DB aDatabase) {
		this.mDatabase = aDatabase;
		this.mName = aName;
		mCollection = aDatabase.getCollection(aName);
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKey
	 * @param aValue
	 */
	@Override
	public V put(K aKey, V aValue, int expTime) {
		mCollection.insert(new BasicDBObject()
				.append("k", aKey)
				.append("v", aValue)
				.append("it", (Long) (System.currentTimeMillis() / 1000))
				.append("et", expTime));

		return aValue;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKey
	 * @param aValue
	 */
	@Override
	public V put(K aKey, V aValue) {
		return put(aKey, aValue, 0);
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public String getName() {
		return mName;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aNewName
	 * @throws java.lang.Exception
	 */
	@Override
	public void setName(String aNewName) throws Exception {
		mDatabase.createCollection(aNewName, null);
		DBCollection newCollection = mDatabase.getCollection(aNewName);

		DBCursor lRecords = mCollection.find();
		while (lRecords.hasNext()) {
			newCollection.insert(lRecords.next());
		}

		mCollection.drop();
		mCollection = newCollection;
		mName = aNewName;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void clear() {
		mCollection.drop();
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aObj
	 */
	@Override
	public boolean containsKey(Object aObj) {
		DBObject lRecord = mCollection.findOne(new BasicDBObject().append("k", aObj));

		if (lRecord != null && isValid(lRecord)) {
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param r The DBObject record
	 * @return TRUE if the record is not expired, FALSE otherwise
	 */
	private boolean isValid(DBObject aRecord) {
		Integer lExpTime = (Integer) aRecord.get("et");
		if (lExpTime < 1) {
			return true;
		}

		if (((Long) aRecord.get("it")) + lExpTime >= System.currentTimeMillis() / 1000) {
			return true;
		}
		//Useful to keep the collection up to date with only non-expired values
		mCollection.remove(aRecord);

		return false;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aValue
	 */
	@Override
	public boolean containsValue(Object aValue) {
		DBObject lRecord = mCollection.findOne(new BasicDBObject().append("v", aValue));

		if (lRecord != null && isValid(lRecord)) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKey
	 */
	@Override
	public V get(Object aKey) {
		DBObject lRecord = mCollection.findOne(new BasicDBObject().append("k", aKey));

		if (lRecord != null) {
			if (isValid(lRecord)) {
				return (V) lRecord.get("v");
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public Set<K> keySet() {
		Set<K> lKeySet = new FastSet<K>();
		DBCursor lCursor = mCollection.find();
		DBObject lRecord;

		while (lCursor.hasNext()) {
			lRecord = lCursor.next();
			if (isValid(lRecord)) {
				lKeySet.add((K) lRecord.get("k"));
			}
		}

		return lKeySet;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKey
	 */
	@Override
	public V remove(Object aKey) {
		DBObject lRecord = mCollection.findOne(new BasicDBObject().append("k", aKey));

		if (lRecord != null) {
			mCollection.remove(lRecord);
			if (isValid(lRecord)) {
				return (V) lRecord.get("v");
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc
	 *
	 * @throws java.lang.Exception
	 */
	@Override
	public void initialize() throws Exception {
		mCollection.createIndex(new BasicDBObject().append("k", 1),
				new BasicDBObject().append("unique", true));
	}
}
