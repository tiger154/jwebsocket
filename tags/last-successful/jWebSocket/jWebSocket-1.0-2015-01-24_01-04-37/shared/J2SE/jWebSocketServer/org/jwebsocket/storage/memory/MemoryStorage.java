//	---------------------------------------------------------------------------
//	jWebSocket - MemoryStorage (Community Edition, CE)
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
package org.jwebsocket.storage.memory;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javolution.util.FastMap;
import org.jwebsocket.api.IBasicStorage;
import org.springframework.util.Assert;

/**
 *
 * @param <K>
 * @param <V>
 * @author Rolando Betancourt Toucet
 */
public class MemoryStorage<K, V> implements IBasicStorage<K, V> {

	private static FastMap<String, FastMap> mContainer;
	private String mName;
	private FastMap mMap;

	/**
	 * Create a new MemoryStorage instance
	 *
	 * @param aName The name of the storage container
	 *
	 */
	public MemoryStorage(String aName) {
		this.mName = aName;
	}

	/**
	 *
	 * @return
	 */
	public static FastMap<String, FastMap> getContainer() {
		if (null == mContainer) {
			mContainer = new FastMap<String, FastMap>().shared();
		}
		return mContainer;
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
	 * {@inheritDoc
	 *
	 * @param aNewName
	 * @throws Exception
	 */
	@Override
	public synchronized void setName(String aNewName) throws Exception {
		if (getContainer().containsKey(mName)) {
			FastMap lValue = getContainer().remove(mName);
			if (mMap != null) {
				getContainer().put(aNewName, mMap);
			} else {
				getContainer().put(aNewName, lValue);
			}
		}

		this.mName = aNewName;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKeys
	 * @return
	 */
	@Override
	public Map<K, V> getAll(Collection<K> aKeys) {
		FastMap<K, V> lMap = new FastMap<K, V>();
		for (K lKey : aKeys) {
			lMap.put((K) lKey, get((K) lKey));
		}

		return lMap;
	}

	/**
	 * {@inheritDoc
	 *
	 * @throws Exception
	 */
	@Override
	public void initialize() throws Exception {
		Assert.notNull(mName, "The 'name', argument cannot be null!");

		if (!getContainer().containsKey(mName) || null == getContainer().get(mName)) {
			getContainer().put(mName, new FastMap<K, V>());
		}

		mMap = getContainer().get(mName);
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
		return mMap.size();
	}

	/**
	 * {@inheritDoc
	 *
	 * @return
	 */
	@Override
	public boolean isEmpty() {
		if (mMap.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public boolean containsKey(Object aKey) {
		if (mMap.containsKey((String) aKey)) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aValue
	 * @return
	 */
	@Override
	public boolean containsValue(Object aValue) {
		if (mMap.containsValue(aValue)) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param lKey
	 * @return
	 */
	@Override
	public V get(Object lKey) {
		return (V) mMap.get(lKey);
	}

	/**
	 * {@inheritDoc
	 *
	 * @param lKey
	 * @param lValue
	 * @return
	 */
	@Override
	public V put(K lKey, V lValue) {
		return (V) mMap.put(lKey, lValue);
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public V remove(Object aKey) {
		return (V) mMap.remove(aKey);
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aMap
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> aMap) {
		mMap.putAll(aMap);
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void clear() {
		mMap.clear();
	}

	/**
	 * {@inheritDoc
	 *
	 * @return
	 */
	@Override
	public Set<K> keySet() {
		return mMap.keySet();
	}

	/**
	 * {@inheritDoc
	 *
	 * @return
	 */
	@Override
	public Collection<V> values() {
		return mMap.values();
	}

	/**
	 * {@inheritDoc
	 *
	 * @return
	 */
	@Override
	public Set<Entry<K, V>> entrySet() {
		return mMap.entrySet();
	}
}