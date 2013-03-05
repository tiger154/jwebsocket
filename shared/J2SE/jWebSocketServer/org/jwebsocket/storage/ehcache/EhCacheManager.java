//  ---------------------------------------------------------------------------
//  jWebSocket - EhCacheManager (Community Edition, CE)
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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.jwebsocket.config.JWebSocketConfig;

/**
 *
 * @author aschulze
 */
public class EhCacheManager {

	private static CacheManager mInstance = null;

	/**
	 * Default constructor, cannot be called from outside this class.
	 */
	private EhCacheManager() {
	}

	/**
	 * Static method, returns the one and only instance
	 *
	 * @return
	 */
	public static CacheManager getInstance() {
		if (mInstance == null) {
			ClassLoader lClassLoader = Thread.currentThread().getContextClassLoader();
			mInstance = new CacheManager(JWebSocketConfig.getConfigFolder("ehcache.xml", lClassLoader));
		}
		return mInstance;
	}

	/**
	 * Static method, returns the one and only instance
	 *
	 * @param aName
	 * @return
	 */
	public static Cache getCache(String aName) {
		return getInstance().getCache(aName);
	}
}