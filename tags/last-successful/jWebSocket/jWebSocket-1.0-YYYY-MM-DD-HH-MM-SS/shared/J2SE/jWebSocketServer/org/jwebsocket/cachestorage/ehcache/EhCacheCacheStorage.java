//	---------------------------------------------------------------------------
//	jWebSocket - EhCacheCacheStorage (Community Edition, CE)
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
package org.jwebsocket.cachestorage.ehcache;

import net.sf.ehcache.Element;
import org.jwebsocket.api.IBasicCacheStorage;
import org.jwebsocket.storage.ehcache.EhCacheStorage;

/**
 * a named storage (a map of key/value pairs) in EhCache. Please consider that
 * each storage is maintained in its own file on the hard disk.
 *
 * @param <K>
 * @param <V>
 * @author Alexander Schulze
 */
public class EhCacheCacheStorage<K, V> extends EhCacheStorage<K, V> implements IBasicCacheStorage<K, V> {

	/**
	 *
	 * @param aName
	 */
	public EhCacheCacheStorage(String aName) {
		super(aName);
	}

	@Override
	public V put(K aKey, V aData, int expTime) {
		Element lElement = new Element(aKey, aData);
		lElement.setTimeToLive(expTime);
		getCache().put(lElement);

		return (V) aData;
	}
}
