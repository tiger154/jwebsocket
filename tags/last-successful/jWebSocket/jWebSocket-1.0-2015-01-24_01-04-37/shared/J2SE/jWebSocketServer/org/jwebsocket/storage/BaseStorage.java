//	---------------------------------------------------------------------------
//	jWebSocket - BaseStorage (Community Edition, CE)
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
package org.jwebsocket.storage;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javolution.util.FastMap;
import org.jwebsocket.api.IBasicStorage;

/**
 * Abstract base storage implementation.
 *
 * @param <K>
 * @param <V>
 * @author Rolando Santamaria Maso
 */
public abstract class BaseStorage<K, V> implements IBasicStorage<K, V> {

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
	 * @param o
	 */
	@Override
	public boolean containsKey(Object o) {
		return keySet().contains(o);
	}

	/**
	 * {@inheritDoc
	 *
	 * @param o
	 */
	@Override
	public boolean containsValue(Object o) {
		return values().contains(o);
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
		return keySet().size();
	}

	/**
	 * {@inheritDoc
	 *
	 * @return
	 */
	@Override
	public boolean isEmpty() {
		return keySet().isEmpty();
	}

	/**
	 * {@inheritDoc
	 *
	 * @param aMap
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> aMap) {
		for (K lkey : aMap.keySet()) {
			put(lkey, aMap.get(lkey));
		}
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void clear() {
		for (K lKey : keySet()) {
			remove(lKey);
		}
	}

	/**
	 * {@inheritDoc
	 *
	 * @return
	 */
	@Override
	public Set<Entry<K, V>> entrySet() {
		return getAll(keySet()).entrySet();
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public Collection<V> values() {
		return getAll(keySet()).values();
	}
}