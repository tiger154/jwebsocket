//	---------------------------------------------------------------------------
//	jWebSocket - IBasicCacheStorage (Community Edition, CE)
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
package org.jwebsocket.api;

/**
 * Basic storages with simple expiration capability
 *
 * @param <K>
 * @param <V>
 * @author Rolando Santamaria Maso
 */
public interface IBasicCacheStorage<K, V> extends IBasicStorage<K, V> {

	/**
	 * put a value in the storage and indicate it expiration time
	 *
	 * @param key
	 * @param value
	 * @param expTime The value expiration time
	 * @return
	 */
	public V put(K key, V value, int expTime);
}
