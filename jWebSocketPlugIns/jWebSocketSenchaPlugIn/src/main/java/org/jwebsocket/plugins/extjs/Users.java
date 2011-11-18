//	---------------------------------------------------------------------------
//	jWebSocket - ExtJS Plugin
//	Copyright (c) 2011 Innotrade GmbH, jWebSocket.org, Alexander Schulze,
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.extjs;

import java.util.LinkedList;

/**
 *
 * @author Osvaldo Aguilar Lauzurique, Alexander Rojas Hernandez
 */
public class Users {

	private LinkedList<UserDef> users;
	private Integer count;

	public Users() {
		users = new LinkedList<UserDef>();
		count = 0;
	}

	public void add(UserDef user) throws Exception {
		for (UserDef u : users) {
			if (u.getEmail().equals(user.getEmail())) {
				throw new Exception("User duplicated");
			}
		}
		users.add(user);
		count++;
	}

	public LinkedList<UserDef> getUsers() {
		return users;
	}

	public UserDef getUser(Integer id) {
		UserDef us = null;
		for (UserDef u : users) {
			if (u.getId().equals(id)) {
				return u;
			}
		}
		return us;

	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getCount() {
		return count;
	}

	public boolean findUser(Integer id) {
		for (UserDef u : users) {
			if (u.getId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	public boolean deleteUser(Integer id) {
		for (UserDef u : users) {
			if (u.getId().equals(id)) {
				users.remove(u);
				return true;
			}
		}

		return false;
	}
}
