//	---------------------------------------------------------------------------
//	jWebSocket - jQuery User management Demo Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.jquery;

/**
 * @author Victor Antonio Barzana Crespo
 */
public class User {

	private String username;
	private String name;
	private String lastname;
	private String mail;

	/**
	 *
	 * @param username
	 * @param mail
	 * @param name
	 * @param lastname
	 */
	public User(String username, String mail, String name, String lastname) {
		this.username = username;
		this.name = name;
		this.lastname = lastname;
		this.mail = mail;
	}

	/**
	 *
	 * @return
	 */
	public String getLastname() {
		return lastname;
	}

	/**
	 *
	 * @param lastname
	 */
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	/**
	 *
	 * @return
	 */
	public String getMail() {
		return mail;
	}

	/**
	 *
	 * @param mail
	 */
	public void setMail(String mail) {
		this.mail = mail;
	}

	/**
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *
	 * @return
	 */
	public String getUsername() {
		return username;
	}

	/**
	 *
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.username != null ? this.username.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final User other = (User) obj;
		if ((this.username == null) ? (other.username != null) : !this.username.equals(other.username)) {
			return false;
		}
		return true;
	}
}
