//  ---------------------------------------------------------------------------
//  jWebSocket - MongoDBStorageV1 (Community Edition, CE)
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

import com.mongodb.*;
import java.util.Set;
import javolution.util.FastSet;
import org.jwebsocket.storage.BaseStorage;

/**
 * This class uses MongoDB servers to persist the information. <br> Each storage
 * represents a database collection. Please see for MongoDB collections number
 * limit.
 *
 * @param <K>
 * @param <V>
 * @author rbetancourt
 */
public class MongoDBStorageV1<K, V> extends BaseStorage<K, V> {

	private DB mDatabase;
	private String mName;
	private DBCollection mCollection;

	/**
	 * Create a new MongoDBStorage instance
	 *
	 * @param aName The name of the storage container
	 * @param aDatabase The MongoDB database instance
	 */
	public MongoDBStorageV1(String aName, DB aDatabase) {
		this.mDatabase = aDatabase;
		this.mName = aName;
		mCollection = aDatabase.getCollection(aName);
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void initialize() throws Exception {
		mCollection.ensureIndex(new BasicDBObject().append("k", 1),
				new BasicDBObject().append("unique", true));
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
	 * @param newName
	 */
	@Override
	public void setName(String newName) throws Exception {
		mDatabase.createCollection(newName, null);
		DBCollection lNewCollection = mDatabase.getCollection(newName);

		DBCursor lRecords = mCollection.find();
		while (lRecords.hasNext()) {
			lNewCollection.insert(lRecords.next());
		}

		mCollection.drop();
		mCollection = lNewCollection;
		mName = newName;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public int size() {
		return (int) mCollection.count();
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean isEmpty() {
		return mCollection.count() == 0;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKey
	 */
	@Override
	public boolean containsKey(Object aKey) {
		DBObject lValue = mCollection.findOne(new BasicDBObject().append("k", (String) aKey));
		if (lValue != null) {
			return true;
		}
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
		if (lRecord != null) {
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
		return (V) mCollection.findOne(new BasicDBObject().append("k", aKey)).get("v");
	}

	@Override
	public V put(K aKey, V aValue) {
		BasicDBObject lRecord = new BasicDBObject();
		lRecord.append("k", aKey);
		DBCursor lCursor = mCollection.find(lRecord);
		if (!lCursor.hasNext()) {
			lRecord.append("v", aValue);
			mCollection.insert(lRecord);
		} else {
			DBObject lExistingRecord = lCursor.next();
			lExistingRecord.put("v", aValue);
			mCollection.save(lExistingRecord);
		}
		return aValue;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKey
	 */
	@Override
	public V remove(Object aKey) {
		if (containsKey(aKey)) {
			V lValue = get(aKey);
			mCollection.remove(new BasicDBObject().append("k", aKey));
			return lValue;
		} else {
			throw new IndexOutOfBoundsException();
		}
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
	 */
	@Override
	public Set<K> keySet() {
		Set<K> lKeySet = new FastSet<K>();
		DBCursor lCursor = mCollection.find();
		while (lCursor.hasNext()) {
			lKeySet.add((K) lCursor.next().get("k"));
		}
		return lKeySet;
	}
}
