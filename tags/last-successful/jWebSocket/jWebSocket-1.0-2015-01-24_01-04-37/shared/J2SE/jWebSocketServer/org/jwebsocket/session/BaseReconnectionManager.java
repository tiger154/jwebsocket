//	---------------------------------------------------------------------------
//	jWebSocket - BaseReconnectionManager (Community Edition, CE)
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
package org.jwebsocket.session;

import org.jwebsocket.api.*;
import org.jwebsocket.kit.WebSocketSession;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 *
 * @author Rolando Santamaria Maso, Alexander Schulze
 */
public abstract class BaseReconnectionManager implements ISessionReconnectionManager, IInitializable {

	private Integer mSessionExpirationTime = 60; // value is given in seconds
	private IBasicStorage<String, Object> mSessionIdsTrash;
	private String mTrashStorageName = "__session_ids_trash_storage_ns__";
	private IStorageProvider mStorageProvider;
	private ICacheStorageProvider mCacheStorageProvider;

	/**
	 *
	 * @return
	 */
	public ICacheStorageProvider getCacheStorageProvider() {
		return mCacheStorageProvider;
	}

	/**
	 *
	 * @param aCacheStorageProvider
	 */
	public void setCacheStorageProvider(ICacheStorageProvider aCacheStorageProvider) {
		this.mCacheStorageProvider = aCacheStorageProvider;
	}

	@Override
	public Integer getSessionExpirationTime() {
		return mSessionExpirationTime;
	}

	/**
	 *
	 * @param aSessionExpirationTime
	 */
	public void setSessionExpirationTime(Integer aSessionExpirationTime) {
		Assert.isTrue(aSessionExpirationTime > 0, "Expecting 'sessionExpirationTime' argument value > 0!");
		this.mSessionExpirationTime = aSessionExpirationTime;
	}

	@Override
	public IBasicStorage<String, Object> getSessionIdsTrash() {
		return mSessionIdsTrash;
	}

	/**
	 *
	 * @param aSessionIdsTrash
	 */
	public void setSessionIdsTrash(IBasicStorage<String, Object> aSessionIdsTrash) {
		this.mSessionIdsTrash = aSessionIdsTrash;
	}

	/**
	 *
	 * @return
	 */
	public String getTrashStorageName() {
		return mTrashStorageName;
	}

	/**
	 *
	 * @param aTrashStorageName
	 */
	public void setTrashStorageName(String aTrashStorageName) {
		this.mTrashStorageName = aTrashStorageName;
	}

	@Override
	public void putInReconnectionMode(WebSocketSession aSession) {
		// used by a deamon to release expired sessions resources
		getSessionIdsTrash().put(aSession.getSessionId(), System.currentTimeMillis() + (getSessionExpirationTime() * 1000));
	}

	@Override
	public IStorageProvider getStorageProvider() {
		return mStorageProvider;
	}

	/**
	 *
	 * @param mStorageProvider
	 */
	public void setStorageProvider(IStorageProvider mStorageProvider) {
		this.mStorageProvider = mStorageProvider;
	}

	@Override
	public void initialize() throws Exception {
		// the session cleaner will run every 6 seconds to balance performance
		// impactss
		Tools.getTimer().scheduleAtFixedRate(
				new CleanExpiredSessionsTask(
				getSessionIdsTrash(), getStorageProvider()), 0, 6000);
	}

	@Override
	public void shutdown() throws Exception {
	}

	@Override
	public boolean isExpired(String aSessionId) {
		try {
			return (mSessionIdsTrash.containsKey(aSessionId)
					&& (Long) mSessionIdsTrash.get(aSessionId) < System.currentTimeMillis());
		} catch (Exception lEx) {
			return true;
		}
	}
}
