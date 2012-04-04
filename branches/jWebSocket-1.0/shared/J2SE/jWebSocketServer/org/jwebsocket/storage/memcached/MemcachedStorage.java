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

	private MemcachedClient mMemcachedClient;
	private String mName;
	private final static String KEYS_LOCATION = ".KEYS::1234567890";
	private final static String KEY_SEPARATOR = "::-::";
	private final static int NOT_EXPIRE = 0;

	/**
	 * 
	 * @param aName
	 * @param aMemcachedClient
	 */
	public MemcachedStorage(String aName, MemcachedClient aMemcachedClient) {
		this.mName = aName;
		this.mMemcachedClient = aMemcachedClient;
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Override
	public void initialize() throws Exception {
		//Key index support
		if (null == get(mName + KEYS_LOCATION)) {
			mMemcachedClient.set(mName + KEYS_LOCATION, NOT_EXPIRE, "");
		}
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void clear() {
		for (Object key : keySet()) {
			remove((K) key);
		}
		//Removing the index
		mMemcachedClient.set(mName + KEYS_LOCATION, NOT_EXPIRE, "");
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<K> keySet() {
		String lIndex = (String) get(mName + KEYS_LOCATION);
		if (lIndex.length() == 0) {
			return new FastSet<K>();
		} else {
			String[] lKeys = lIndex.split(KEY_SEPARATOR);
			FastSet lKeySet = new FastSet();
			lKeySet.addAll(Arrays.asList(lKeys));

			return lKeySet;
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Collection<V> values() {
		return getAll(keySet()).values();
	}

	/**
	 * 
	 * @param aKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object aKey) {
		return keySet().contains((K) aKey);
	}

	/**
	 * 
	 * @param aValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean containsValue(Object aValue) {
		return values().contains((V) aValue);
	}

	/**
	 * 
	 * @param aKeys
	 * @return
	 */
	@Override
	public Map<K, V> getAll(Collection<K> aKeys) {
		FastMap<K, V> lMap = new FastMap<K, V>();
		for (K lKey : aKeys) {
			lMap.put(lKey, get(lKey));
		}

		return lMap;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public V get(Object lKey) {
		V lValue = null;
		lValue = (V) mMemcachedClient.get(lKey.toString());

		return lValue;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public V remove(Object lKey) {
		V lValue = get(lKey);
		mMemcachedClient.delete(lKey.toString());

		//Key index update
		String lIndex = (String) get(mName + KEYS_LOCATION);
		lIndex = lIndex.replace(lKey.toString() + KEY_SEPARATOR, "");
		mMemcachedClient.set(mName + KEYS_LOCATION, NOT_EXPIRE, lIndex);

		return lValue;
	}

	/**
	 * 
	 * @param key
	 * @param aValue
	 * @return
	 */
	@Override
	public V put(K aKey, V aValue) {
		mMemcachedClient.set(aKey.toString(), NOT_EXPIRE, aValue);

		//Key index update
		if (!keySet().contains(aKey)) {
			String lIndex = (String) get(mName + KEYS_LOCATION);
			lIndex = lIndex + aKey.toString() + KEY_SEPARATOR;
			mMemcachedClient.set(mName + KEYS_LOCATION, NOT_EXPIRE, lIndex);
		}

		return aValue;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean isEmpty() {
		return keySet().isEmpty();
	}

	/**
	 * 
	 * @param aMap
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> aMap) {
		for (K lKey : aMap.keySet()) {
			put(lKey, aMap.get(lKey));
		}
	}

	/**
	 * 
	 * @return
	 */
	public MemcachedClient getMemcachedClient() {
		return mMemcachedClient;
	}

	/**
	 * 
	 * @param aMemcachedClient
	 */
	public void setMemcachedClient(MemcachedClient aMemcachedClient) {
		this.mMemcachedClient = aMemcachedClient;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public String getName() {
		return mName;
	}

	/**
	 * 
	 * @param aName
	 * @throws Exception
	 */
	@Override
	public void setName(String aName) throws Exception {
		if (aName.length() == 0) {
			throw new InvalidParameterException();
		}
		Map<K, V> lMap = getAll(keySet());
		clear();

		this.mName = aName;
		initialize();
		for (K key : lMap.keySet()) {
			put(key, lMap.get(key));
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public int size() {
		return keySet().size();
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Set<Entry<K, V>> entrySet() {
		return getAll(keySet()).entrySet();
	}

	/**
	 * 
	 */
	@Override
	public void shutdown() {
	}
}
