//	---------------------------------------------------------------------------
//	jWebSocket - MemcachedCacheStorage (Community Edition, CE)
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
package org.jwebsocket.cachestorage.memcached;

import net.spy.memcached.MemcachedClient;
import org.jwebsocket.api.IBasicCacheStorage;
import org.jwebsocket.storage.memcached.MemcachedStorage;

/**
 *
 * @param <K>
 * @param <V>
 * @author Rolando Santamaria Maso
 */
public class MemcachedCacheStorage<K extends String, V> extends MemcachedStorage<K, V> implements IBasicCacheStorage<K, V> {

	/**
	 *
	 * @param aName
	 * @param aMemcachedClient
	 */
	public MemcachedCacheStorage(String aName, MemcachedClient aMemcachedClient) {
		super(aName, aMemcachedClient);
	}

	/**
	 * {@inheritDoc }
	 *
	 * @param aKey
	 * @param aValue
	 * @param aExpTime
	 */
	@Override
	public V put(K aKey, V aValue, int aExpTime) {
		getMemcachedClient().add(aKey, aExpTime, aValue);

		return aValue;
	}
}
