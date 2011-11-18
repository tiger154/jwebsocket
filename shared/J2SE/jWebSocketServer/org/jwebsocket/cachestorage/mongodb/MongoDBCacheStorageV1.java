//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.cachestorage.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.jwebsocket.api.IBasicCacheStorage;

/**
 * This class uses MongoDB servers to persist the information. 
 * <br>
 * Each cache storage represents a database collection. Please see for 
 * MongoDB collections number limit.
 *
 * @author kyberneees
 */
public class MongoDBCacheStorageV1<K, V> implements IBasicCacheStorage<K, V> {

	private DB db;
	private String name;
	private DBCollection myCollection;

	public MongoDBCacheStorageV1(String name, DB db) {
		this.db = db;
		this.name = name;
		myCollection = db.getCollection(name);
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public V put(K key, V value, int expTime) {
		myCollection.insert(new BasicDBObject()
				.append("k", key)
				.append("v", value)
				.append("it", (Long)(System.currentTimeMillis() / 1000))
				.append("et", expTime));

		return value;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public V put(K k, V v) {
		return put(k, v, 0);
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (K key : m.keySet()) {
			put(key, m.get(key));
		}
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public Map<K, V> getAll(Collection<K> keys) {
		FastMap<K, V> map = new FastMap<K, V>();
		for (K key : keys) {
			map.put((K) key, get(key));
		}
		
		return map;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void setName(String newName) throws Exception {
		db.createCollection(newName, null);
		DBCollection newCollection = db.getCollection(newName);
		
		DBCursor records = myCollection.find();
		while (records.hasNext()){
			newCollection.insert(records.next());
		}
		
		myCollection.drop();
		myCollection = newCollection;
		name = newName;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void clear() {
		myCollection.drop();
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean containsKey(Object o) {
		DBObject r = myCollection.findOne(new BasicDBObject().append("k", o));

		if (r != null && isValid(r)) {
			return true;
		}
		
		return false;
	}

	/**
	 * 
	 * @param r The DBObject record
	 * @return TRUE if the record is not expired, FALSE otherwise
	 */
	private boolean isValid(DBObject r) {
		Integer expTime = (Integer)r.get("et");
		if (expTime < 1){
			return true;
		}
		
		if (((Long)r.get("it")) + expTime >= System.currentTimeMillis() / 1000) {
			return true;
		}
		//Useful to keep the collection up to date with only non-expired values
		myCollection.remove(r);
		
		return false;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean containsValue(Object o) {
		DBObject r = myCollection.findOne(new BasicDBObject().append("v", o));

		if (r != null && isValid(r)) {
			return true;
		}
		
		return false;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public Set<Entry<K, V>> entrySet() {
		return getAll(keySet()).entrySet();
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public V get(Object o) {
		DBObject r = myCollection.findOne(new BasicDBObject().append("k", o));

		if (r != null) {
			if (isValid(r)){
				return (V)r.get("v");
			}
		}
		
		return null;
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
	public Set<K> keySet() {
		Set<K> s = new FastSet<K>();
		DBCursor cur = myCollection.find();
		
		while (cur.hasNext()) {
			DBObject r = cur.next();
			if (isValid(r)){
				s.add((K) r.get("k"));
			}
		}
		
		return s;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public V remove(Object o) {
		DBObject r = myCollection.findOne(new BasicDBObject().append("k", o));
		myCollection.remove(r);
		
		if (r != null && isValid(r)){
			return (V)r.get("v");
		}
		
		return null;
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
	public Collection<V> values() {
		Set<K> keys = keySet();
		FastList<V> values = new FastList <V>();
		DBObject r;
		
		for (K key : keys){
			r = myCollection.findOne(new BasicDBObject().append("k", key));
			values.add((V)r.get("v"));
		}
		
		return values;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void initialize() throws Exception {
		myCollection.ensureIndex(new BasicDBObject().append("k", 1),
				new BasicDBObject().append("unique", true));
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void shutdown() throws Exception {
	}
}
