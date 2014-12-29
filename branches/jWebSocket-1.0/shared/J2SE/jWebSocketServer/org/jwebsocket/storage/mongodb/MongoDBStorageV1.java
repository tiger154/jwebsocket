//	---------------------------------------------------------------------------
//	jWebSocket - MongoDBStorageV1 (Community Edition, CE)
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

import com.mongodb.*;
import java.util.Set;
import javolution.util.FastSet;
import org.jwebsocket.storage.BaseStorage;
import org.springframework.util.Assert;

/**
 * This class uses MongoDB servers to persist the information. <br> Each storage
 * represents a database collection. Please see for MongoDB collections number
 * limit.
 *
 * @param <K>
 * @param <V>
 * @author Rolando Betancourt Toucet
 */
public class MongoDBStorageV1<K, V> extends BaseStorage<K, V> {

	private final DB mDatabase;
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
	}

	/**
	 * {@inheritDoc
	 *
	 * @throws java.lang.Exception
	 */
	@Override
	public void initialize() throws Exception {
		Assert.notNull(mName, "The 'name', argument cannot be null!");
		Assert.notNull(mDatabase, "The 'database', argument cannot be null!");

		mCollection = mDatabase.getCollection(mName);
		mCollection.createIndex(new BasicDBObject().append("k", 1),
				new BasicDBObject().append("unique", true));
	}

	/**
	 * {@inheritDoc
	 *
	 * @return
	 */
	@Override
	public String getName() {
		return mName;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param newName
	 * @throws java.lang.Exception
	 */
	@Override
	public void setName(String newName) throws Exception {
		Assert.isTrue(null != mName, "The 'newName', argument cannot be null!");

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
	 *
	 * @return
	 */
	@Override
	public int size() {
		return (int) mCollection.count();
	}

	/**
	 * {@inheritDoc
	 *
	 * @return
	 */
	@Override
	public boolean isEmpty() {
		return mCollection.count() == 0;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public boolean containsKey(Object aKey) {
		DBObject lValue = mCollection.findOne(new BasicDBObject().append("k", (String) aKey));
		return (lValue != null);
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aValue
	 * @return
	 */
	@Override
	public boolean containsValue(Object aValue) {
		DBObject lRecord = mCollection.findOne(new BasicDBObject().append("v", aValue));
		return (lRecord != null);
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKey
	 * @return
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
	 * @return
	 */
	@Override
	public V remove(Object aKey) {
		if (containsKey(aKey)) {
			V lValue = get(aKey);
			mCollection.remove(new BasicDBObject().append("k", aKey));
			return lValue;
		}

		return null;
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
	 * @return
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
