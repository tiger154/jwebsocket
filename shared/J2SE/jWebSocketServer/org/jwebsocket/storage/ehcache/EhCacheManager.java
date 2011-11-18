/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
	 */
	public static CacheManager getInstance() {
		if (mInstance == null) {
			mInstance = new CacheManager(JWebSocketConfig.getConfigFolder("ehcache.xml"));
		}
		return mInstance;
	}

	/**
	 * Static method, returns the one and only instance
	 */
	public static Cache getCache(String aName) {
		return getInstance().getCache(aName);
	}
}