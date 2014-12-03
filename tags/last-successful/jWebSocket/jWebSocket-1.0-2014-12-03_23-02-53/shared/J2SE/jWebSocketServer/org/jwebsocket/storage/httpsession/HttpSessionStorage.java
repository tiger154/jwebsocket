//	---------------------------------------------------------------------------
//	jWebSocket - HttpSessionStorage (Community Edition, CE)
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
package org.jwebsocket.storage.httpsession;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.http.HttpSession;
import javolution.util.FastSet;
import org.jwebsocket.storage.BaseStorage;

/**
 * A named storage (a map of key/value pairs) for HttpSession wrappers. Consider
 * to use this storage when running jWebSocket in embedded mode inside a Servlet
 * container.
 *
 * @author Rolando Santamaria Maso
 */
public class HttpSessionStorage extends BaseStorage<String, Object> {

	private String mName = null;
	private final HttpSession mSession;

	/**
	 *
	 * @return The HttpSession instance
	 */
	public HttpSession getSession() {
		return mSession;
	}

	/**
	 *
	 * @param aSession
	 */
	public HttpSessionStorage(HttpSession aSession) {
		mName = aSession.getId();
		mSession = aSession;

		initialize();
	}

	/**
	 *
	 * {@inheritDoc }
	 *
	 * @return
	 */
	@Override
	public String getName() {
		return mName;
	}

	/**
	 *
	 * {@inheritDoc }
	 *
	 * @param aName
	 * @throws java.lang.Exception
	 */
	@Override
	public void setName(String aName) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return
	 */
	@Override
	public Set<String> keySet() {
		Set<String> lKeys = new FastSet<String>();
		Enumeration<String> lSessionKeys = mSession.getAttributeNames();
		while (lSessionKeys.hasMoreElements()) {
			lKeys.add(lSessionKeys.nextElement());
		}
		return lKeys;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public Object get(Object aKey) {
		return mSession.getAttribute(aKey.toString());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public Object remove(Object aKey) {
		Object lRes = get(aKey);
		mSession.removeAttribute(aKey.toString());
		return lRes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		Iterator lIterator = keySet().iterator();
		while (lIterator.hasNext()) {
			Object lKey = lIterator.next();
			remove(lKey);
		}
	}

	/**
	 *
	 * {@inheritDoc }
	 *
	 * @param aKey
	 * @param aData
	 * @return
	 */
	@Override
	public Object put(String aKey, Object aData) {
		mSession.setAttribute(aKey.toString(), aData);

		return aData;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void initialize() {
	}
}
