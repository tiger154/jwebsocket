//  ---------------------------------------------------------------------------
//  jWebSocket - EhCacheStorage  (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.storage.ehcache;

import java.util.Set;
import javolution.util.FastSet;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.jwebsocket.storage.BaseStorage;

/**
 * a named storage (a map of key/value pairs) in EhCache. Please consider that
 * each storage is maintained in its own file on the hard disk.
 *
 * @param <K>
 * @param <V>
 * @author aschulze
 */
public class EhCacheStorage<K, V> extends BaseStorage<K, V> {

	private String mName = null;
	private static CacheManager mCacheManager = null;
	private Cache mCache = null;

	/**
	 *
	 * @return
	 */
	public Cache getCache() {
		return mCache;
	}

	/**
	 *
	 * @param aCache
	 */
	public void setCache(Cache aCache) {
		this.mCache = aCache;
	}

	/**
	 *
	 * @param aName
	 */
	public EhCacheStorage(String aName) {
		mName = aName;
		initialize();
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public String getName() {
		return mName;
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public void setName(String aName) throws Exception {
		mName = aName;
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public Set keySet() {
		Set lKeys = new FastSet();
		lKeys.addAll(mCache.getKeys());
		return lKeys;
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public int size() {
		return mCache.getSize();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aKey
	 */
	@Override
	public V get(Object aKey) {
		Element lElement = mCache.get(aKey);
		return (lElement != null ? (V) lElement.getObjectValue() : null);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aKey
	 */
	@Override
	public V remove(Object aKey) {
		// TODO: The interface specs that a previous object is supposed to be returned
		// this may not be desired and reduce performance, provide second message
		V lRes = (V) mCache.get(aKey);
		mCache.remove(aKey);
		return lRes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		mCache.removeAll();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aKey
	 * @param aData
	 */
	@Override
	public Object put(Object aKey, Object aData) {
		Element lElement = new Element(aKey, aData);
		mCache.put(lElement);

		return aData;
	}

	/**
	 *
	 * {@inheritDoc }
	 *
	 * @param aKey
	 */
	@Override
	public boolean containsKey(Object aKey) {
		return mCache.get(aKey) != null;
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public void initialize() {
		mCacheManager = EhCacheManager.getInstance();
		if (mCacheManager != null) {
			// TODO: think about how to configure or pass settings to this cache.
			if (!mCacheManager.cacheExists(mName)) {
				mCacheManager.addCache(mName);
			}
			mCache = mCacheManager.getCache(mName);
		}
	}
}
