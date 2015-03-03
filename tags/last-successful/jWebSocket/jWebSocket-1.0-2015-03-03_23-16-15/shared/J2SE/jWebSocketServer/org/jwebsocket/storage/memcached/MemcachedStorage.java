//	---------------------------------------------------------------------------
//	jWebSocket - MemcachedStorage (Community Edition, CE)
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
package org.jwebsocket.storage.memcached;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import javolution.util.FastSet;
import net.spy.memcached.MemcachedClient;
import org.jwebsocket.storage.BaseStorage;

/**
 *
 * @param <K>
 * @param <V>
 * @author Rolando Santamaria Maso
 */
public class MemcachedStorage<K, V> extends BaseStorage<K, V> {

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
	 * {@inheritDoc }
	 *
	 * @throws java.lang.Exception
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
	 * {@inheritDoc }
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void clear() {
		super.clear();

		//Removing the index
		mMemcachedClient.set(mName + KEYS_LOCATION, NOT_EXPIRE, "");
	}

	/**
	 *
	 * {@inheritDoc }
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
	 * {@inheritDoc }
	 *
	 * @param lKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public V get(Object lKey) {
		V lValue;
		lValue = (V) mMemcachedClient.get(lKey.toString());

		return lValue;
	}

	/**
	 *
	 * {@inheritDoc }
	 *
	 * @param lKey
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
	 * {@inheritDoc }
	 *
	 * @param aKey
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
	 * {@inheritDoc }
	 *
	 * @return
	 */
	@Override
	public String getName() {
		return mName;
	}

	/**
	 *
	 * {@inheritDoc }
	 *
	 * @param aName
	 * @throws java.lang.Exception
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
}
