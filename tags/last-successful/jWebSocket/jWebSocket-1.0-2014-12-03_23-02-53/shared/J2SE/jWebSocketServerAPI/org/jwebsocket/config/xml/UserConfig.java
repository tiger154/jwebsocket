// ---------------------------------------------------------------------------
// jWebSocket - UserConfig (Community Edition, CE)
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
package org.jwebsocket.config.xml;

import java.util.Collections;
import java.util.List;

import org.jwebsocket.config.Config;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * @author Alexander Schulze, Puran Singh
 */
public final class UserConfig implements Config {

	private final String mUUID;
	private final String mLoginname;
	private final String mFirstname;
	private final String mLastname;
	private final String mPassword;
	private final String mDescription;
	private final int mStatus;
	private final List<String> mRoles;

	/**
	 * Default user config constructor
	 *
	 * @param aUUID
	 * @param aLoginname the login name
	 * @param aFirstname the first name
	 * @param aLastname the last name
	 * @param aPassword the password
	 * @param aDescription the descritpion
	 * @param aStatus the user status
	 * @param aRoles the user roles
	 */
	public UserConfig(String aUUID, String aLoginname, String aFirstname, String aLastname,
			String aPassword, String aDescription, int aStatus, List<String> aRoles) {
		mUUID = aUUID;
		mLoginname = aLoginname;
		mFirstname = aFirstname;
		mLastname = aLastname;
		mPassword = aPassword;
		mDescription = aDescription;
		mStatus = aStatus;
		mRoles = aRoles;
		//validate user config
		validate();
	}

	/**
	 * @return the mUUID
	 */
	public String getUUID() {
		return mUUID;
	}

	/**
	 * @return the loginname
	 */
	public String getLoginname() {
		return mLoginname;
	}

	/**
	 * @return the firstname
	 */
	public String getFirstname() {
		return mFirstname;
	}

	/**
	 * @return the lastname
	 */
	public String getLastname() {
		return mLastname;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return mPassword;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return mDescription;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return mStatus;
	}

	/**
	 * @return the list of roles
	 */
	public List<String> getRoles() {
		return Collections.unmodifiableList(mRoles);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() {
		if ((mLoginname != null && mLoginname.length() > 0)
				&& (mFirstname != null && mFirstname.length() > 0)
				&& (mLastname != null && mLastname.length() > 0)
				&& (mDescription != null && mDescription.length() > 0)
				&& (mStatus > 0)) {
			return;
		}
		throw new WebSocketRuntimeException(
				"Missing one of the user configuration, please check your configuration file");
	}
}
