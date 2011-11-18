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
package org.jwebsocket.storage.memcached;

import java.security.InvalidParameterException;
import java.util.Map.Entry;
import org.jwebsocket.api.IBasicStorage;
import java.util.Collection;
import java.util.Set;
import java.util.Map;
import javolution.util.FastMap;
import net.spy.memcached.MemcachedClient;
import javolution.util.FastSet;
import java.util.Arrays;

/**
 * 
 * @param <K> 
 * @param <V> 
 * @author kyberneees
 */
public class MemcachedStorage<K extends Object, V extends Object> implements IBasicStorage<K, V> {

	private MemcachedClient memcachedClient;
	private String name;
	private final static String KEYS_LOCATION = ".KEYS::1234567890";
	private final static String KEY_SEPARATOR = "::-::";
	private final static int NOT_EXPIRE = 0;

	/**
	 * 
	 * @param name
	 * @param memcachedClient
	 */
	public MemcachedStorage(String name, MemcachedClient memcachedClient) {
		this.name = name;
		this.memcachedClient = memcachedClient;
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void initialize() throws Exception {
		//Key index support
		if (null == get(name + KEYS_LOCATION)) {
			memcachedClient.set(name + KEYS_LOCATION, NOT_EXPIRE, "");
		}
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void clear() {
		for (Object key : keySet()) {
			remove((K) key);
		}
		//Removing the index
		memcachedClient.set(name + KEYS_LOCATION, NOT_EXPIRE, "");
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<K> keySet() {
		String index = (String) get(name + KEYS_LOCATION);
		if (index.length() == 0) {
			return new FastSet<K>();
		} else {
			String[] keys = index.split(KEY_SEPARATOR);
			FastSet set = new FastSet();
			set.addAll(Arrays.asList(keys));

			return set;
		}
	}

	/**
	 * 
	 * @return
	 */
	public Collection<V> values() {
		return getAll(keySet()).values();
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean containsKey(Object key) {
		return keySet().contains((K) key);
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean containsValue(Object value) {
		return values().contains((V) value);
	}

	/**
	 * 
	 * @param keys
	 * @return
	 */
	public Map<K, V> getAll(Collection<K> keys) {
		FastMap<K, V> m = new FastMap<K, V>();
		for (K key : keys) {
			m.put(key, get(key));
		}

		return m;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public V get(Object key) {
		V myObj = null;
		myObj = (V) memcachedClient.get(key.toString());

		return myObj;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public V remove(Object key) {
		V myObj = get(key);
		memcachedClient.delete(key.toString());

		//Key index update
		String index = (String) get(name + KEYS_LOCATION);
		index = index.replace(key.toString() + KEY_SEPARATOR, "");
		memcachedClient.set(name + KEYS_LOCATION, NOT_EXPIRE, index);

		return myObj;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public V put(K key, V value) {
		memcachedClient.set(key.toString(), NOT_EXPIRE, value);

		//Key index update
		if (!keySet().contains(key)) {
			String index = (String) get(name + KEYS_LOCATION);
			index = index + key.toString() + KEY_SEPARATOR;
			memcachedClient.set(name + KEYS_LOCATION, NOT_EXPIRE, index);
		}

		return value;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return keySet().isEmpty();
	}

	/**
	 * 
	 * @param m
	 */
	public void putAll(Map<? extends K, ? extends V> m) {
		for (K key : m.keySet()) {
			put(key, m.get(key));
		}
	}

	/**
	 * 
	 * @return
	 */
	public MemcachedClient getMemcachedClient() {
		return memcachedClient;
	}

	/**
	 * 
	 * @param memcachedClient
	 */
	public void setMemcachedClient(MemcachedClient memcachedClient) {
		this.memcachedClient = memcachedClient;
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param name
	 * @throws Exception
	 */
	public void setName(String name) throws Exception {
		if (name.length() == 0) {
			throw new InvalidParameterException();
		}
		Map<K, V> all = getAll(keySet());
		clear();

		this.name = name;
		initialize();
		for (K key : all.keySet()) {
			put(key, all.get(key));
		}
	}

	/**
	 * 
	 * @return
	 */
	public int size() {
		return keySet().size();
	}

	/**
	 * 
	 * @return
	 */
	public Set<Entry<K, V>> entrySet() {
		return getAll(keySet()).entrySet();
	}

	/**
	 * 
	 */
	public void shutdown() {
	}
}
