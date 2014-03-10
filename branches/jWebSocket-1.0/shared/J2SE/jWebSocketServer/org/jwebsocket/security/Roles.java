//	---------------------------------------------------------------------------
//	jWebSocket - Roles (Community Edition, CE)
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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javolution.util.FastMap;
import javolution.util.FastSet;

/**
 *
 * @author Alexander Schulze
 */
public class Roles {

	private final Map<String, Role> mRoles = new FastMap<String, Role>();

	/**
	 *
	 */
	public Roles() {
	}

	/**
	 *
	 * @param aRoles
	 */
	public Roles(Role... aRoles) {
		if (aRoles != null) {
			for (Role aRole : aRoles) {
				addRole(aRole);
			}
		}
	}

	/**
	 * Adds a new role to the FastMap of roles.
	 *
	 * @param aRole
	 */
	public void addRole(Role aRole) {
		if (aRole != null) {
			mRoles.put(aRole.getId(), aRole);
		}
	}

	/**
	 * Returns a certain role from the FastMap of roles identified by its key or
	 * <tt>null</tt> if no role with the given exists in the FastMap of roles.
	 *
	 * @param aKey
	 * @return
	 */
	public Role getRole(String aKey) {
		return mRoles.get(aKey);
	}

	/**
	 * Removes a certain role from the FastMap of roles.
	 *
	 * @param aKey
	 */
	public void removeRole(String aKey) {
		mRoles.remove(aKey);
	}

	/**
	 * Removes a certain role from the FastMap of roles.
	 *
	 * @param aRole
	 */
	public void removeRole(Role aRole) {
		if (aRole != null) {
			mRoles.remove(aRole.getId());
		}
	}

	/**
	 *
	 * @param aRight
	 * @return
	 */
	public boolean hasRight(String aRight) {
		for (Role lRole : mRoles.values()) {
			if (lRole.hasRight(aRight)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * returns an unmodifiable set of all rights of this role instance.
	 *
	 * @return
	 */
	public Set<Right> getRights() {
		Set<Right> lSet = new FastSet<Right>();
		for (Role lRole : mRoles.values()) {
			for (Right lRight : lRole.getRights()) {
				lSet.add(lRight);
			}
		}
		return Collections.unmodifiableSet(lSet);
	}

	/**
	 * returns an unmodifiable set of all rights of this role instance.
	 *
	 * @return
	 */
	public Set<String> getRightIdSet() {
		Set<String> lSet = new FastSet<String>();
		for (Role lRole : mRoles.values()) {
			for (Right lRight : lRole.getRights()) {
				lSet.add(lRight.getId());
			}
		}
		return Collections.unmodifiableSet(lSet);
	}

	/**
	 * checks if the roles contain a certain role . The role is passed as a
	 * string which associates the key of the role.
	 *
	 * @param aRole
	 * @return
	 */
	public boolean hasRole(String aRole) {
		return mRoles.containsKey(aRole);
	}

	/**
	 * returns an unmodifiable set of the ids of all roles in the role map.
	 *
	 * @return
	 */
	public Set<String> getRoleIdSet() {
		return Collections.unmodifiableSet(mRoles.keySet());
	}
}
