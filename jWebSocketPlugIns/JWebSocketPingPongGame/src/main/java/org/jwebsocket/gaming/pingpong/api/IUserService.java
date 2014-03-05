//	---------------------------------------------------------------------------
//	jWebSocket - IUserService (Community Edition, CE)
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
package org.jwebsocket.gaming.pingpong.api;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 *
 * @author armando
 */
public interface IUserService {

	/**
	 * Create a new user account
	 *
	 * @param aUser
	 * @return
	 */
	boolean create(DBObject aUser);

	/**
	 *
	 * @param aUserName
	 * @return TRUE if the username is used by other user, FALSE otherwise
	 */
	boolean containsUn(String aUserName);

	/**
	 * @param aUserName
	 * @param aPwd
	 *
	 * @return TRUE if the password match the active user password
	 */
	boolean isPwdCorrect(String aUserName, String aPwd);

	/**
	 *
	 * @return The user profile
	 */
	DBCursor getProfileList();

	/**
	 *
	 * @param aUserName
	 * @param aWins
	 * @param aLost
	 */
	void updateValue(String aUserName, int aWins, int aLost);

	/**
	 *
	 * @param aUserName
	 * @return The user profile
	 */
	DBObject getProfile(String aUserName);

	/**
	 *
	 *
	 * @param aDay
	 */
	void removeUser(int aDay);
}
