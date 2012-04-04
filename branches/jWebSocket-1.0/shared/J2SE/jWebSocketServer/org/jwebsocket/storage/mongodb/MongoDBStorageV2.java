//  ---------------------------------------------------------------------------
//  jWebSocket - MongoDBStorage
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.storage.mongodb;

import org.jwebsocket.api.IBasicStorage;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javolution.util.FastMap;
import javolution.util.FastSet;

/**
 * This class uses MongoDB servers to persist the information. 
 * <br>
 * All the cache storages entries are located in the same database collection. 
 * 
 * @author kyberneees
 */
public class MongoDBStorageV2<K, V> implements IBasicStorage<K, V> {
	
	private String mName;
	private DBCollection mCollection;

	/**
	 * Create a new MongoDBStorage instance
	 *
	 * @param aName The name of the storage container
	 * @param aCollection The MongoDB database collection instance
	 */
	public MongoDBStorageV2(String aName, DBCollection aCollection) {
		this.mName = aName;
		mCollection = aCollection;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void initialize() throws Exception {
		mCollection.ensureIndex(new BasicDBObject().append("ns", 1).append("k", 1),
				new BasicDBObject().append("unique", true));
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void shutdown() throws Exception {
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
	 */
	@Override
	public void setName(String aNewName) throws Exception {
		mCollection.update(new BasicDBObject().append("ns", mName),
				new BasicDBObject().append("$set", new BasicDBObject().append("ns", aNewName)));

		mName = aNewName;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public Map<K, V> getAll(Collection<K> aKeys) {
		FastMap<K, V> lMap = new FastMap<K, V>();
		for (K lKey : aKeys) {
			lMap.put((K) lKey, get(lKey));
		}
		return lMap;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public int size() {
		return keySet().size();
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean isEmpty() {
		return keySet().isEmpty();
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean containsKey(Object aKey) {
		DBObject lRecord = mCollection.findOne(new BasicDBObject().append("ns", mName).append("k", (String) aKey));
		if (lRecord != null) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean containsValue(Object aValue) {
		DBObject lRecord = mCollection.findOne(new BasicDBObject().append("ns", mName).append("v", aValue));
		if (lRecord != null) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public V get(Object aKey) {
		return (V) mCollection.findOne(new BasicDBObject().append("ns", mName).append("k", aKey)).get("v");
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
	 */
	@Override
	public V remove(Object aKey) {
		if (containsKey(aKey)) {
			V lValue = get(aKey);
			mCollection.remove(new BasicDBObject().append("ns", mName).append("k", aKey));
			return lValue;
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> aMap) {
		for (K lKey : aMap.keySet()) {
			put(lKey, aMap.get(lKey));
		}
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

	/**
	 * {@inheritDoc
	 */
	@Override
	public Collection<V> values() {
		List<V> lValues = new ArrayList<V>();
		DBCursor lCursor = mCollection.find(new BasicDBObject().append("ns", mName));
		while (lCursor.hasNext()) {
			lValues.add((V) lCursor.next().get("v"));
		}
		return lValues;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public Set<Entry<K, V>> entrySet() {
		return getAll(keySet()).entrySet();
	}
}
