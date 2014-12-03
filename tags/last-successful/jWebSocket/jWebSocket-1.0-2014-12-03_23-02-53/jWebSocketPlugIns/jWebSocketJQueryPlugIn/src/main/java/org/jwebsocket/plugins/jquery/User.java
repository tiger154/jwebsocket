//	---------------------------------------------------------------------------
//	jWebSocket - jQuery User management Demo Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.jquery;

/**
 * @author Victor Antonio Barzana Crespo
 */
public class User {

	private String mUsername;
	private String mName;
	private String mLastname;
	private String mMail;

	/**
	 *
	 * @param aUsername
	 * @param aMail
	 * @param aName
	 * @param aLastname
	 */
	public User(String aUsername, String aMail, String aName, String aLastname) {
		this.mUsername = aUsername;
		this.mName = aName;
		this.mLastname = aLastname;
		this.mMail = aMail;
	}

	/**
	 *
	 * @return
	 */
	public String getLastname() {
		return mLastname;
	}

	/**
	 *
	 * @param aLastname
	 */
	public void setLastname(String aLastname) {
		this.mLastname = aLastname;
	}

	/**
	 *
	 * @return
	 */
	public String getMail() {
		return mMail;
	}

	/**
	 *
	 * @param aMail
	 */
	public void setMail(String aMail) {
		this.mMail = aMail;
	}

	/**
	 *
	 * @return
	 */
	public String getName() {
		return mName;
	}

	/**
	 *
	 * @param aName
	 */
	public void setName(String aName) {
		this.mName = aName;
	}

	/**
	 *
	 * @return
	 */
	public String getUsername() {
		return mUsername;
	}

	/**
	 *
	 * @param aUsername
	 */
	public void setUsername(String aUsername) {
		this.mUsername = aUsername;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.mUsername != null ? this.mUsername.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object aObj) {
		if (aObj == null) {
			return false;
		}
		if (getClass() != aObj.getClass()) {
			return false;
		}
		final User other = (User) aObj;
		if ((this.mUsername == null) ? (other.mUsername != null) : !this.mUsername.equals(other.mUsername)) {
			return false;
		}
		return true;
	}
}
