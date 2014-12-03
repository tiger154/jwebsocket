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
import java.util.UUID;

/**
 *
 * @author Alexander Schulze
 * @author Rolando Santamaria Maso
 */
public class WebSocketSession {

	private String mSessionId = null;
	private Map<String, Object> mStorage;
	public static final String CREATED_AT = "$created_at";
	public static final String USERNAME = "$username";
	public static final String IS_AUTHENTICATED = "$is_authenticated";
	private final String SESSION_UID = "$session_uid";

	/**
	 * Create a new WebSocketSession instance
	 */
	public WebSocketSession() {
	}

	/**
	 * Clear all session data. The 'createdAt' property gets a new value, the
	 * sessionId value is kept.
	 */
	public synchronized void reset() {
		getStorage().clear();
		setCreatedAt();
	}

	/**
	 * Get the session creation timestamp.
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
	 * Indicate if the client associated to the session is authenticated.
	 *
	 * @return
	 */
	public boolean isAuthenticated() {
		if (null != mStorage) {
			return mStorage.containsKey(IS_AUTHENTICATED) ? (Boolean) mStorage.get(IS_AUTHENTICATED) : false;
		}

		return false;
	}

	/**
	 * Get the session UUID value. Different value to the sessionId for security
	 * reasons.
	 *
	 * @return
	 */
	public String getUUID() {
		Map<String, Object> lStorage = getStorage();
		if (null != lStorage) {
			if (lStorage.containsKey(SESSION_UID)) {
				return lStorage.get(SESSION_UID).toString();
			} else {
				String lUUID = UUID.randomUUID().toString();
				lStorage.put(SESSION_UID, lUUID);
				return lUUID;
			}
		} else {
			return null;
		}
	}

	/**
	 * Set the session creation timestamp.
	 */
	public void setCreatedAt() {
		if (null == getCreatedAt()) {
			mStorage.put(CREATED_AT, System.currentTimeMillis());
		}
	}

	/**
	 * Create a new WebSocketSession instance
	 *
	 * @param aSessionId The session identifier value
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
	 * Get the username of the client associated to the session.
	 *
	 * @return the session username
	 */
	public String getUsername() {
		Map<String, Object> lStorage = getStorage();
		return (null != lStorage
				? (String) lStorage.get(USERNAME)
				: null);
	}

	/**
	 * Set the session identifier.
	 *
	 * @param aSessionId the session identifier to set
	 */
	public void setSessionId(String aSessionId) {
		if (null == mSessionId) {
			this.mSessionId = aSessionId;
		} else {
			throw new UnsupportedOperationException(
					"The 'sessionId' property is in read-only state!");
		}
	}

	/**
	 * Get the session data storage.
	 *
	 * @return the session persistent storage instance
	 */
	public Map<String, Object> getStorage() {
		return mStorage;
	}

	/**
	 * Set the session data storage
	 *
	 * @param aStorage the session persistent storage instance to set
	 */
	public void setStorage(Map<String, Object> aStorage) {
		if (null == mStorage) {
			mStorage = aStorage;
		} else {
			throw new UnsupportedOperationException(
					"The storage property is in read-only state!");
		}
	}
}
