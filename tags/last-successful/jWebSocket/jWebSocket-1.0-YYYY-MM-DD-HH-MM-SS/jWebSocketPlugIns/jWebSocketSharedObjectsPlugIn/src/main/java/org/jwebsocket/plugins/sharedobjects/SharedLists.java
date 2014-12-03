// ---------------------------------------------------------------------------
// jWebSocket - SharedLists (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
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
package org.jwebsocket.plugins.sharedobjects;

import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 */
public class SharedLists {

	private final Map<String, FastList> mLists = new FastMap<String, FastList>();

	/**
	 *
	 * @param aResponse
	 * @param aId
	 */
	public void create(Token aResponse, String aId) {
		FastList lList = mLists.get(aId);
		if (lList != null) {
			mLists.put(aId, new FastList());
		}
	}

	/**
	 *
	 * @param aResponse
	 * @param aId
	 */
	public void clear(Token aResponse, String aId) {
		List lList = mLists.get(aId);
		if (lList != null) {
			mLists.clear();
		}
	}

	/**
	 *
	 * @param aResponse
	 * @param aId
	 * @param aIndex
	 * @return
	 */
	public Object get(Token aResponse, String aId, int aIndex) {
		List lList = mLists.get(aId);
		if (lList != null) {
			return lList.get(aIndex);
		}
		return null;
	}

	/**
	 *
	 * @param aResponse
	 * @param aId
	 * @param aObject
	 */
	public void add(Token aResponse, String aId, Object aObject) {
		List lList = mLists.get(aId);
		if (lList != null) {
			lList.add(aObject);
		}
	}

	/**
	 *
	 * @param aResponse
	 * @param aId
	 * @param aIndex
	 */
	public void remove(Token aResponse, String aId, int aIndex) {
		List lList = mLists.get(aId);
		if (lList != null) {
			lList.remove(aIndex);
		}
	}

	/**
	 *
	 * @param aResponse
	 * @param aId
	 */
	public void destroy(Token aResponse, String aId) {
		mLists.remove(aId);
	}
}
