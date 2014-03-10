//	---------------------------------------------------------------------------
//	jWebSocket MapAppender (Community Edition, CE)
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
package org.jwebsocket.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to append entries in chain on a Map instance
 *
 * @author Rolando Santamaria Maso
 */
public class MapAppender {

	private Map mMap = new HashMap();

	/**
	 *
	 * @param aMap
	 */
	public MapAppender(Map aMap) {
		mMap = aMap;
	}

	/**
	 *
	 */
	public MapAppender() {
	}

	/**
	 *
	 * @param aKey
	 * @param aValue
	 * @return
	 */
	public MapAppender append(Object aKey, Object aValue) {
		mMap.put(aKey, aValue);

		return this;
	}

	/**
	 *
	 * @param aMap
	 * @return
	 */
	public MapAppender append(Map aMap) {
		mMap.putAll(aMap);

		return this;
	}

	/**
	 *
	 * @return
	 */
	public Map getMap() {
		return mMap;
	}
}
