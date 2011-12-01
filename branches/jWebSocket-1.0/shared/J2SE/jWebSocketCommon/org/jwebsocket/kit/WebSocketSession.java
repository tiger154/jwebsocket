//	---------------------------------------------------------------------------
//	jWebSocket - Connector API
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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

	public WebSocketSession() {
	}

	public WebSocketSession(String aSessionId) {
		mSessionId = aSessionId;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return mSessionId;
	}

	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.mSessionId = sessionId;
	}

	public Map<String, Object> getStorage() {
		return mStorage;
	}

	public void setStorage(Map<String, Object> aStorage) {
		if (null == mStorage) {
			mStorage = aStorage;
		} else {
			throw new UnsupportedOperationException("This operation is dedicated to the system level only!");
		}
	}
}
