//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.cachestorage.memory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.jwebsocket.api.IBasicCacheStorage;

/**
 *
 * @author kyberneees
 */
public class MemoryCacheStorage<K, V> implements IBasicCacheStorage<K, V> {

	class Element<V> {

		private int expTime;
		private Long insertionTime;
		private V value;

		public Element(V value, Long insertionTime, int expTime) {
			this.expTime = expTime;
			this.value = value;
			this.insertionTime = insertionTime;
		}

		public int getExpTime() {
			return expTime;
		}

		public void setExpTime(int expTime) {
			this.expTime = expTime;
		}

		public V getValue() {
			return value;
		}

		public void setValue(V value) {
			this.value = value;
		}

		public Long getInsertionTime() {
			return insertionTime;
		}

		public void setInsertionTime(Long insertionTime) {
			this.insertionTime = insertionTime;
		}
	}
	private static FastMap<String, FastMap> container = new FastMap<String, FastMap>();
	private String name;
	private FastMap<K, Element<V>> myMap;

	/**
	 * Create a new MemoryStorage instance
	 * @param name The name of the storage container
	 * */
	public MemoryCacheStorage(String name) {
		this.name = name;

	}

	public static FastMap<String, FastMap> getContainer() {
		return container;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public synchronized void setName(String newName) throws Exception {
		if (getContainer().containsKey(name)) {
			FastMap value = getContainer().remove(name);
			if (myMap != null) {
				getContainer().put(newName, myMap);
			} else {
				getContainer().put(newName, value);
			}
		}

		this.name = newName;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public Map<K, V> getAll(Collection<K> keys) {
		FastMap<K, V> map = new FastMap<K, V>();
		for (K key : keys) {
			map.put((K) key, get((K) key));
		}

		return map;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void initialize() throws Exception {
		if (!getContainer().containsKey(name)) {
			getContainer().put(name, new FastMap<K, Element<V>>());
		}

		myMap = getContainer().get(name);
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
	public int size() {
		return myMap.size();
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean isEmpty() {
		if (myMap.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean containsKey(Object key) {
		if (myMap.containsKey((String) key)) {
			if (isValid((K)key, myMap.get(key))){
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param r The Element object
	 * @return TRUE if the element is not expired, FALSE otherwise
	 */
	private boolean isValid(K key, Element<V> e) {
		Integer expTime = e.getExpTime();
		if (expTime < 1) {
			return true;
		}

		if (e.getInsertionTime() + expTime >= System.currentTimeMillis() / 1000) {
			return true;
		}
		//Useful to keep the collection up to date with only non-expired values
		myMap.remove(key);

		return false;
	}
	
	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean containsValue(Object value) {
		Iterator<K> keys = myMap.keySet().iterator();
		while (keys.hasNext()){
			K key = keys.next();
			Element<V> e = myMap.get(key);
			if (e.getValue().equals(value) && isValid((K)key, e)){
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public V get(Object key) {
		Element<V> e = myMap.get(key);
		if (e != null && isValid((K)key, e)){
			return e.getValue();
		}
		
		return null;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public V put(K key, V value) {
		return put(key, value, 0);
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public V put(K key, V value, int expTime) {
		Element<V> e = new Element<V>(value, (Long) (System.currentTimeMillis() / 1000), expTime);
		myMap.put(key, e);
		
		return e.getValue();
	}

	/**
	 * {@inheritDoc
	 * 
	 * @param key
	 * @return  
	 */
	@Override
	public V remove(Object key) {
		return (V) myMap.remove(key).getValue();
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		Iterator keys = m.keySet().iterator();
		while (keys.hasNext()){
			K key = (K)keys.next();
			put(key, m.get(key));
		}
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void clear() {
		myMap.clear();
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public Set<K> keySet() {
		Set<K> result = new FastSet<K>();
		Iterator<K> keys = myMap.keySet().iterator();
		while (keys.hasNext()){
			K key = keys.next();
			Element<V> e = myMap.get(key);
			if (isValid(key, e)){
				result.add(key);
			}
		}
		
		return result;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public Collection<V> values() {
		Set<V> result = new FastSet<V>();
		Set<K> keys = keySet();
		
		if (!keys.isEmpty())
		for (K k: keys){
			result.add(myMap.get(k).getValue());
		}
		
		return result;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public Set<Entry<K, V>> entrySet() {
		return getAll(keySet()).entrySet();
	}
}
