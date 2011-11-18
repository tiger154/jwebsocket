//  ---------------------------------------------------------------------------
//  jWebSocket - MemoryStorage
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
package org.jwebsocket.storage.memory;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javolution.util.FastMap;
import org.jwebsocket.api.IBasicStorage;

/**
 *
 * @param <K> 
 * @param <V> 
 * @author rbetancourt
 */
public class MemoryStorage<K, V> implements IBasicStorage<K, V> {

	private static FastMap<String, FastMap> container = new FastMap<String, FastMap>();
	private String name;
	private FastMap myMap;

	/**
	 * Create a new MemoryStorage instance
	 * @param name The name of the storage container
	 * */
	public MemoryStorage(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return
	 */
	public static FastMap<String, FastMap> getContainer() {
		return container;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc
	 * 
	 * @param newName 
	 * @throws Exception 
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
	 * 
	 * @param keys
	 * @return  
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
	 * 
	 * @throws Exception 
	 */
	@Override
	public void initialize() throws Exception {
		if (!getContainer().containsKey(name)) {
			getContainer().put(name, new FastMap<K, V>());
		}

		myMap = getContainer().get(name);
	}

	/**
	 * {@inheritDoc
	 * 
	 * @throws Exception 
	 */
	@Override
	public void shutdown() throws Exception {
	}

	/**
	 * {@inheritDoc
	 * 
	 * @return 
	 */
	@Override
	public int size() {
		return myMap.size();
	}

	/**
	 * {@inheritDoc
	 * 
	 * @return 
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
	 * 
	 * @param key 
	 * @return 
	 */
	@Override
	public boolean containsKey(Object key) {
		if (myMap.containsKey((String) key)) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc
	 * 
	 * @param value 
	 * @return 
	 */
	@Override
	public boolean containsValue(Object value) {
		if (myMap.containsValue(value)) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc
	 * 
	 * @param key 
	 * @return 
	 */
	@Override
	public V get(Object key) {
		return (V) myMap.get(key);
	}

	/**
	 * {@inheritDoc
	 * 
	 * @param key 
	 * @param value 
	 * @return 
	 */
	@Override
	public V put(K key, V value) {
		return (V) myMap.put(key, value);
	}

	/**
	 * {@inheritDoc
	 * 
	 * @param key 
	 * @return 
	 */
	@Override
	public V remove(Object key) {
		return (V) myMap.remove(key);
	}

	/**
	 * {@inheritDoc
	 * 
	 * @param m 
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		myMap.putAll(m);
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
	 * 
	 * @return 
	 */
	@Override
	public Set<K> keySet() {
		return myMap.keySet();
	}

	/**
	 * {@inheritDoc
	 * 
	 * @return 
	 */
	@Override
	public Collection<V> values() {
		return myMap.values();
	}

	/**
	 * {@inheritDoc
	 * 
	 * @return 
	 */
	@Override
	public Set<Entry<K, V>> entrySet() {
		return myMap.entrySet();
	}
}