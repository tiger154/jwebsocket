//	---------------------------------------------------------------------------
//	jWebSocket - MongoDBStorageV2 (Community Edition, CE)
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

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.Set;
import javolution.util.FastSet;
import org.jwebsocket.storage.BaseStorage;

/**
 * This class uses MongoDB servers to persist the information. <br> All the
 * cache storages entries are located in the same database collection.
 *
 * @param <K>
 * @param <V>
 * @author Rolando Santamaria Maso
 */
public class MongoDBStorageV2<K, V> extends BaseStorage<K, V> {

	private String mName;
	private final DBCollection mCollection;

	/**
	 * Create a new MongoDBStorage instance
	 *
	 * @param aName The name of the storage container
	 * @param aCollection The MongoDB database collection instance
	 */
	public MongoDBStorageV2(String aName, DBCollection aCollection) {
		mName = aName;
		mCollection = aCollection;
	}

	@Override
	public void initialize() throws Exception {
		mCollection.createIndex(new BasicDBObject().append("ns", 1).append("k", 1),
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
	 * @param aNewName
	 * @throws java.lang.Exception
	 */
	@Override
	public void setName(String aNewName) throws Exception {
		mCollection.update(new BasicDBObject().append("ns", mName),
				new BasicDBObject().append("$set", new BasicDBObject().append("ns", aNewName)));

		mName = aNewName;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public boolean containsKey(Object aKey) {
		DBObject lRecord = mCollection.findOne(new BasicDBObject().append("ns", mName).append("k", (String) aKey));
		return (lRecord != null);
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aValue
	 * @return
	 */
	@Override
	public boolean containsValue(Object aValue) {
		DBObject lRecord = mCollection.findOne(new BasicDBObject().append("ns", mName).append("v", aValue));
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
		DBObject lRecord = mCollection.findOne(new BasicDBObject()
				.append("ns", mName).append("k", aKey));

		if (null != lRecord) {
			return (V) lRecord.get("v");
		}
		return null;
	}

	@Override
	public V put(K aKey, V aValue) {
		BasicDBObject lRecord = new BasicDBObject();
		lRecord.append("ns", mName);
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
			mCollection.remove(new BasicDBObject().append("ns", mName).append("k", aKey));
			return lValue;
		}

		return null;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void clear() {
		mCollection.remove(new BasicDBObject().append("ns", mName));
	}

	/**
	 * {@inheritDoc
	 *
	 * @return
	 */
	@Override
	public Set<K> keySet() {
		Set<K> lKeySet = new FastSet<K>();
		DBCursor lCursor = mCollection.find(new BasicDBObject().append("ns", mName));
		while (lCursor.hasNext()) {
			lKeySet.add((K) lCursor.next().get("k"));
		}
		return lKeySet;
	}
}
