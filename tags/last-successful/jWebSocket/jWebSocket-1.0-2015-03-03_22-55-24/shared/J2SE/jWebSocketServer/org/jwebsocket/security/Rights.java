//	---------------------------------------------------------------------------
//	jWebSocket - Rights (Community Edition, CE)
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
package org.jwebsocket.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javolution.util.FastMap;

/**
 * implements a FastMap of rights to act as a role.
 *
 * @author Alexander Schulze
 */
public class Rights {

	private final Map<String, Right> mRights = new FastMap<String, Right>();

	/**
	 * adds a new right to the FastMap of rights. If there is already a right
	 * with the given stored in the FastMap it will be overwritten. If null is
	 * passed or if the right has no valid key no operation is performed.
	 *
	 * @param aRight
	 */
	public void addRight(Right aRight) {
		if (aRight != null && aRight.getId() != null) {
			mRights.put(aRight.getId(), aRight);
		}
	}

	/**
	 * returns a right identified by its key or <tt>null</tt> if the right
	 * cannot be found in the FastMap or the key passed is <tt>null</tt>.
	 *
	 * @param aKey
	 * @return
	 */
	public Right get(String aKey) {
		if (aKey != null) {
			return mRights.get(aKey);
		}
		return null;
	}

	/**
	 * removes a certain right identified by its key from the FastMap of rights.
	 * If the key is <tt>null</tt> or right could not be found in the FastMap no
	 * operation is performed.
	 *
	 * @param aKey
	 */
	public void removeRight(String aKey) {
		if (aKey != null) {
			mRights.remove(aKey);
		}
	}

	/**
	 * removes a certain right from the FastMap of rights. If the right could
	 * not be found in the FastMap no operation is performed.
	 *
	 * @param aRight
	 */
	public void removeRight(Right aRight) {
		if (aRight != null) {
			mRights.remove(aRight.getId());
		}
	}

	/**
	 * checks if the FastMap of rights contains a certain right. The key of the
	 * right passed must not be null.
	 *
	 * @param aRight
	 * @return
	 */
	public boolean hasRight(Right aRight) {
		if (aRight != null && aRight.getId() != null) {
			return mRights.containsKey(aRight.getId());
		} else {
			return false;
		}
	}

	/**
	 * checks if the FastMap of rights contains a certain right identified by
	 * its key. The key must not be null.
	 *
	 * @param aKey
	 * @return
	 */
	public boolean hasRight(String aKey) {
		if (aKey != null) {
			return mRights.containsKey(aKey);
		} else {
			return false;
		}
	}

	/**
	 * returns an unmodifiable collection of rights.
	 *
	 * @return
	 */
	public Collection<Right> getRights() {
		return Collections.unmodifiableCollection(mRights.values());
	}

	/**
	 * returns an unmodifiable set of the ids of all rights.
	 *
	 * @return
	 */
	public Set<String> getRightIdSet() {
		return Collections.unmodifiableSet(mRights.keySet());
	}
}
