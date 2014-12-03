// ---------------------------------------------------------------------------
// jWebSocket - SharedObjects (Community Edition, CE)
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

import java.util.Set;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author Alexander Schulze
 */
public class SharedObjects {

	private static final Logger log = Logging.getLogger(SharedObjects.class);
	private final FastMap<String, Object> objects = new FastMap<String, Object>();

	/**
	 *
	 * @param aKey
	 * @param aObject
	 * @return
	 */
	public Object put(String aKey, Object aObject) {
		return objects.put(aKey, aObject);
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	public Object remove(String aKey) {
		return objects.remove(aKey);
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	public Object get(String aKey) {
		return objects.get(aKey);
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	public boolean contains(String aKey) {
		return objects.containsKey(aKey);
	}

	/**
	 *
	 * @return
	 */
	public Set<String> getKeys() {
		return objects.keySet();
	}
}
