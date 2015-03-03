//	---------------------------------------------------------------------------
//	jWebSocket - MemoryCacheStorage (Community Edition, CE)
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
 * @param <K>
 * @param <V>
 * @author Rolando Santamaria Maso
 */
public class MemoryCacheStorage<K, V> implements IBasicCacheStorage<K, V> {

	class Element<V> {

		private int mExpTime;
		private Long mInsertionTime;
		private V mValue;

		public Element(V value, Long insertionTime, int expTime) {
			this.mExpTime = expTime;
			this.mValue = value;
			this.mInsertionTime = insertionTime;
		}

		public int getExpTime() {
			return mExpTime;
		}

		public void setExpTime(int expTime) {
			this.mExpTime = expTime;
		}

		public V getValue() {
			return mValue;
		}

		public void setValue(V value) {
			this.mValue = value;
		}

		public Long getInsertionTime() {
			return mInsertionTime;
		}

		public void setInsertionTime(Long insertionTime) {
			this.mInsertionTime = insertionTime;
		}
	}
	private static FastMap<String, FastMap> mContainer;
	private String mName;
	private FastMap<K, Element<V>> mMap;

	/**
	 * Create a new MemoryStorage instance
	 *
	 * @param aName The name of the storage container
	 *
	 */
	public MemoryCacheStorage(String aName) {
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
	public synchronized void setName(String aNewName) throws Exception {
		if (getContainer().containsKey(mName)) {
			FastMap value = getContainer().remove(mName);
			if (mMap != null) {
				getContainer().put(aNewName, mMap);
			} else {
				getContainer().put(aNewName, value);
			}
		}

		this.mName = aNewName;
	}

	/**
	 * {@inheritDoc
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
	 * @throws java.lang.Exception
	 */
	@Override
	public void initialize() throws Exception {
		if (!getContainer().containsKey(mName) || null == getContainer().get(mName)) {
			getContainer().put(mName, new FastMap<K, Element<V>>());
		}

		mMap = getContainer().get(mName);
	}

	/**
	 * {@inheritDoc
	 *
	 * @throws java.lang.Exception
	 */
	@Override
	public void shutdown() throws Exception {
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public int size() {
		return mMap.size();
	}

	/**
	 * {@inheritDoc
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
	 */
	@Override
	public boolean containsKey(Object aKey) {
		if (mMap.containsKey((String) aKey)) {
			if (isValid((K) aKey, mMap.get(aKey))) {
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
	private boolean isValid(K aKey, Element<V> aElement) {
		Integer lExpTime = aElement.getExpTime();
		if (lExpTime < 1) {
			return true;
		}

		if (aElement.getInsertionTime() + lExpTime >= System.currentTimeMillis() / 1000) {
			return true;
		}
		//Useful to keep the collection up to date with only non-expired values
		mMap.remove(aKey);

		return false;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aValue
	 */
	@Override
	public boolean containsValue(Object aValue) {
		Iterator<K> lKeys = mMap.keySet().iterator();
		while (lKeys.hasNext()) {
			K lKey = lKeys.next();
			Element<V> lElement = mMap.get(lKey);
			if (lElement.getValue().equals(aValue) && isValid((K) lKey, lElement)) {
				return true;
			}
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
		Element<V> lElement = mMap.get(aKey);
		if (lElement != null && isValid((K) aKey, lElement)) {
			return lElement.getValue();
		}

		return null;
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKey
	 * @param aValue
	 */
	@Override
	public V put(K aKey, V aValue) {
		return put(aKey, aValue, 0);
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKey
	 * @param aValue
	 * @param aExpTime
	 */
	@Override
	public V put(K aKey, V aValue, int aExpTime) {
		Element<V> lElement = new Element<V>(aValue, (Long) (System.currentTimeMillis() / 1000), aExpTime);
		mMap.put(aKey, lElement);

		return lElement.getValue();
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public V remove(Object aKey) {
		return (V) mMap.remove(aKey).getValue();
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aMap
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> aMap) {
		Iterator keys = aMap.keySet().iterator();
		while (keys.hasNext()) {
			K key = (K) keys.next();
			put(key, aMap.get(key));
		}
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
	 */
	@Override
	public Set<K> keySet() {
		Set<K> lKeyset = new FastSet<K>();
		Iterator<K> lKeys = mMap.keySet().iterator();
		while (lKeys.hasNext()) {
			K lKey = lKeys.next();
			Element<V> lElement = mMap.get(lKey);
			if (isValid(lKey, lElement)) {
				lKeyset.add(lKey);
			}
		}

		return lKeyset;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public Collection<V> values() {
		Set<V> lValues = new FastSet<V>();
		Set<K> lKeys = keySet();

		if (!lKeys.isEmpty()) {
			for (K k : lKeys) {
				lValues.add(mMap.get(k).getValue());
			}
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
