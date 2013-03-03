//	---------------------------------------------------------------------------
//	jWebSocket - Users for ExtJS plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.plugins.extjs;

import java.util.LinkedList;

/**
 *
 * @author Osvaldo Aguilar Lauzurique, Alexander Rojas Hernandez
 */
public class Users {

	private LinkedList<UserDef> mUsers;
	private Integer mCount;

	/**
	 *
	 */
	public Users() {
		mUsers = new LinkedList<UserDef>();
		mCount = 0;
	}

	/**
	 *
	 * @param aUuser
	 * @throws Exception
	 */
	public void add(UserDef aUuser) throws Exception {
		for (UserDef lUserDef : mUsers) {
			if (lUserDef.getEmail().equals(aUuser.getEmail())) {
				throw new Exception("User duplicated");
			}
		}
		mUsers.add(aUuser);
		mCount++;
	}

	/**
	 *
	 * @return
	 */
	public LinkedList<UserDef> getUsers() {
		return mUsers;
	}

	/**
	 *
	 * @param aId
	 * @return
	 */
	public UserDef getUser(Integer aId) {
		UserDef lUser = null;
		for (UserDef lUserDef : mUsers) {
			if (lUserDef.getId().equals(aId)) {
				return lUserDef;
			}
		}
		return lUser;

	}

	/**
	 *
	 *
	 * @param aCount
	 */
	public void setCount(Integer aCount) {
		this.mCount = aCount;
	}

	/**
	 *
	 * @return
	 */
	public Integer getCount() {
		return mCount;
	}

	/**
	 *
	 * @param aId
	 * @return
	 */
	public boolean findUser(Integer aId) {
		for (UserDef lUserDef : mUsers) {
			if (lUserDef.getId().equals(aId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * @param aId
	 * @return
	 */
	public boolean deleteUser(Integer aId) {
		for (UserDef lUserDef : mUsers) {
			if (lUserDef.getId().equals(aId)) {
				mUsers.remove(lUserDef);
				return true;
			}
		}

		return false;
	}
}
