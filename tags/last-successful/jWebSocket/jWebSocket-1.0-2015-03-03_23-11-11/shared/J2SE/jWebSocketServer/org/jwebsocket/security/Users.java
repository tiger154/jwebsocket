//	---------------------------------------------------------------------------
//	jWebSocket - Users (Community Edition, CE)
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

import java.util.Map;
import javolution.util.FastMap;

/**
 * maintains the internal jWebSocket user FastMap. The users are loaded during
 * the startup process from the jWebSocket.xml file.
 *
 * @author Alexander Schulze
 */
public class Users {

	private Map<String, User> mUsers = new FastMap<String, User>();

	/**
	 * returns the user identified by its login name or <tt>null</tt> if no user
	 * with the given login name could be found.
	 *
	 * @param aLoginName
	 * @return
	 */
	public User getUserByLoginName(String aLoginName) {
		if (aLoginName != null) {
			return mUsers.get(aLoginName);
		}
		return null;
	}

	/**
	 * Adds a new user to the FastMap of users. If null is passed no operation
	 * is performed.
	 *
	 * @param aUser
	 */
	public void addUser(User aUser) {
		if (aUser != null) {
			mUsers.put(aUser.getLoginname(), aUser);
		}
	}

	/**
	 * Removes a certain user identified by its login name from the FastMap of
	 * users. If no user with the given login name could be found or the given
	 * login name is null no operation is performed.
	 *
	 * @param aLoginName
	 */
	public void removeUser(String aLoginName) {
		if (aLoginName != null) {
			mUsers.remove(aLoginName);
		}
	}

	/**
	 * Removes a certain user from the FastMap of users. If the user could be
	 * found or the given user object is null no operation is performed.
	 *
	 * @param aUser
	 */
	public void removeUser(User aUser) {
		if (aUser != null) {
			mUsers.remove(aUser.getLoginname());
		}
	}
}
