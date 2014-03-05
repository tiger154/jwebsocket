//	---------------------------------------------------------------------------
//	jWebSocket - WebSocketSession (Community Edition, CE)
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
package org.jwebsocket.kit;

import java.util.Map;

/**
 *
 * @author aschulze
 * @author kyberneees (Persistent storage support for each connector session)
 */
public class WebSocketSession {

	private String mSessionId = null;
	private Map<String, Object> mStorage;
	/**
	 *
	 */
	public static final String CREATED_AT = "$created_at";

	/**
	 *
	 */
	public WebSocketSession() {
	}

	/**
	 *
	 * @return
	 */
	public Long getCreatedAt() {
		if (null != mStorage) {
			return (Long) mStorage.get(CREATED_AT);
		}

		return null;
	}

	/**
	 *
	 */
	public void setCreatedAt() {
		if (null == getCreatedAt()) {
			mStorage.put(CREATED_AT, System.currentTimeMillis());
		} else {
			throw new UnsupportedOperationException("The session 'createdAt' property is in read-only state!");
		}
	}

	/**
	 *
	 * @param aSessionId a session identifier
	 */
	public WebSocketSession(String aSessionId) {
		mSessionId = aSessionId;
	}

	/**
	 * @return the session identifier
	 */
	public String getSessionId() {
		return mSessionId;
	}

	/**
	 * @param aSessionId the session identifier to set
	 */
	public void setSessionId(String aSessionId) {
		if (null == mSessionId) {
			this.mSessionId = aSessionId;
		} else {
			throw new UnsupportedOperationException("The session identifier property is in read-only state!");
		}
	}

	/**
	 *
	 * @return the session persistent storage instance
	 */
	public Map<String, Object> getStorage() {
		return mStorage;
	}

	/**
	 *
	 * @param aStorage the session persistent storage instance to set
	 */
	public void setStorage(Map<String, Object> aStorage) {
		if (null == mStorage) {
			mStorage = aStorage;
		} else {
			throw new UnsupportedOperationException("The storage property is in read-only state!!");
		}
	}
}
