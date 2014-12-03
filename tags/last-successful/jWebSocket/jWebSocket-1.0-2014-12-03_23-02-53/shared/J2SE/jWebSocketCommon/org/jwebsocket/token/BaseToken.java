//	---------------------------------------------------------------------------
//	jWebSocket BaseToken (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//      Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.token;

import java.util.List;
import java.util.Map;
import javolution.util.FastMap;
import org.jwebsocket.api.ITokenizable;

/**
 *
 * @author Alexander Schulze
 */
public abstract class BaseToken implements Token {

	/**
	 *
	 */
	public static final String TT_EVENT = "event";
	/**
	 *
	 */
	public static final String IS_CHUNK = "isChunk";
	/**
	 *
	 */
	public static final String EXPIRES = "expires";
	/**
	 *
	 */
	public static final String IS_LAST_CHUNK = "isLastChunk";
	/**
	 *
	 */
	public static final String CHUNK_TYPE = "chunkType";
	/**
	 *
	 */
	public static final String CODE = "code";
	/**
	 *
	 */
	private static final FastMap<String, String> mExclFromLogs;

	/**
	 *
	 */
	static {
		mExclFromLogs = new FastMap<String, String>();
		setExclLogField(null);
	}
	/**
	 *
	 */
	private static final String SUPRESSED_LOG_FIELDS = "supressed_log_fields";
	/**
	 *
	 */
	private static final String SUPRESSED_LOG_FIELDS_VALUE = "supressed_log_fields_value";

	@Override
	public Boolean hasExpired() {
		return getMap().containsKey(EXPIRES) && getLong(EXPIRES) < System.currentTimeMillis();
	}

	@Override
	public Integer getCode() {
		return getInteger(CODE);
	}

	@Override
	public void setCode(Integer aCode) {
		setInteger(CODE, aCode);
	}

	@Override
	public Boolean isChunk() {
		return getBoolean(IS_CHUNK, false);
	}

	@Override
	public Boolean isLastChunk() {
		return getBoolean(IS_LAST_CHUNK, false);
	}

	@Override
	public String getChunkType() {
		return getString(CHUNK_TYPE);
	}

	@Override
	public void setChunkType(String aChunkType) {
		setString(CHUNK_TYPE, aChunkType);
	}

	@Override
	public void setChunk(Boolean aIsChunk) {
		setBoolean(IS_CHUNK, aIsChunk);
	}

	@Override
	public void setLastChunk(Boolean aIsLastChunk) {
		setBoolean(IS_LAST_CHUNK, aIsLastChunk);
	}

	@Override
	public void setDouble(String aKey, Float aValue) {
		setDouble(aKey, Double.valueOf(aValue));
	}

	@Override
	public boolean setValidated(String aKey, Object aObj) {
		boolean lRes = true;
		if (aObj instanceof ITokenizable) {
			Token lToken = TokenFactory.createToken();
			((BaseTokenizable) aObj).writeToToken(lToken);
			setToken(aKey, lToken);
		} else if (aObj instanceof Boolean) {
			setBoolean(aKey, (Boolean) aObj);
		} else if (aObj instanceof Integer) {
			setInteger(aKey, (Integer) aObj);
		} else if (aObj instanceof Double) {
			setDouble(aKey, (Double) aObj);
		} else if (aObj instanceof String) {
			setString(aKey, (String) aObj);
		} else if (aObj instanceof List) {
			setList(aKey, (List) aObj);
		} else if (aObj instanceof Map) {
			setMap(aKey, (Map) aObj);
		} else {
			lRes = false;
		}
		return lRes;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public final String getType() {
		return getString("type");
	}

	/**
	 *
	 * @param aType
	 */
	@Override
	public final void setType(String aType) {
		setString("type", aType);
	}

	/**
	 * Returns the name space of the token. If you have the same token type
	 * interpreted by multiple different plug-ins the name space allows to
	 * uniquely address a certain plug-in. Each plug-in has its own name space.
	 *
	 * @return the name space.
	 */
	@Override
	public final String getNS() {
		return getString("ns");
	}

	/**
	 * Sets the name space of the token. If you have the same token type
	 * interpreted by multiple different plug-ins the namespace allows to
	 * uniquely address a certain plug-in. Each plug-in has its own namespace.
	 *
	 * @param aNS the namespace to be set for the token.
	 */
	@Override
	public final void setNS(String aNS) {
		setString("ns", aNS);
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	public String getExclLogField(String aKey) {
		return mExclFromLogs.get(aKey);
	}

	/**
	 *
	 * @param aSettings
	 */
	public static void setExclLogField(Map<String, Object> aSettings) {
		String[] lKeys = new String[]{"password", "secretPassword",
			"accessPassword", "newPassword", "secret"};
		String lValue = "*****";
		if (null != aSettings) {
			if (aSettings.containsKey(SUPRESSED_LOG_FIELDS)) {
				lKeys = aSettings.get(SUPRESSED_LOG_FIELDS).toString().split(",");
			}
			if (aSettings.containsKey(SUPRESSED_LOG_FIELDS_VALUE)) {
				lValue = aSettings.get(SUPRESSED_LOG_FIELDS_VALUE).toString();
			}
		}
		for (String lKey : lKeys) {
			try {
				mExclFromLogs.put(lKey.trim(), lValue);
			} catch (Exception lEx) {
			}
		}
	}
}
