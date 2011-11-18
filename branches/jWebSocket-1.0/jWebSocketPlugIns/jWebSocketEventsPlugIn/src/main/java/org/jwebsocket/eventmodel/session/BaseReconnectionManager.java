//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2011 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.session;

import org.jwebsocket.api.IBasicCacheStorage;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.eventmodel.api.ISessionReconnectionManager;

/**
 *
 * @author kyberneees
 */
public abstract class BaseReconnectionManager implements ISessionReconnectionManager, IInitializable {
	private IBasicCacheStorage<String, Object> reconnectionIndex;
	private String cacheStorageName;
	private Integer sessionExpirationTime = 60; //One minute by default
	private IBasicStorage<String, Object> sessionIdsTrash;
	private String trashStorageName;

	public String getCacheStorageName() {
		return cacheStorageName;
	}

	public void setCacheStorageName(String cacheStorageName) {
		this.cacheStorageName = cacheStorageName;
	}

	@Override
	public IBasicCacheStorage<String, Object> getReconnectionIndex() {
		return reconnectionIndex;
	}

	public void setReconnectionIndex(IBasicCacheStorage<String, Object> reconnectionIndex) {
		this.reconnectionIndex = reconnectionIndex;
	}

	@Override
	public Integer getSessionExpirationTime() {
		return sessionExpirationTime;
	}

	@Override
	public void setSessionExpirationTime(Integer sessionExpirationTime) {
		this.sessionExpirationTime = sessionExpirationTime;
	}

	@Override
	public IBasicStorage<String, Object> getSessionIdsTrash() {
		return sessionIdsTrash;
	}

	public void setSessionIdsTrash(IBasicStorage<String, Object> sessionIdsTrash) {
		this.sessionIdsTrash = sessionIdsTrash;
	}

	public String getTrashStorageName() {
		return trashStorageName;
	}

	public void setTrashStorageName(String trashStorageName) {
		this.trashStorageName = trashStorageName;
	}
	
	@Override
	public void putInReconnectionMode(String aSessionId) {
		getReconnectionIndex().put(aSessionId, true, getSessionExpirationTime());
		
		//Used by a deamon to release expired sessions database space
		getSessionIdsTrash().put(aSessionId, System.currentTimeMillis() + (getSessionExpirationTime() * 1000));
	}
}
