//	---------------------------------------------------------------------------
//	jWebSocket - ActiveDirectory User (Community Edition, CE)
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
package org.jwebsocket.ldap;

/**
 * User Object in the Microsoft Active Directory.
 *
 * @author Predrag Stojadinovic, Alexander Schulze
 */
public class ADUser {

	private final String mGUID;
	private final String mLoginName;
	private final String mEmail;
	private final String mFirstName;
	private final String mLastName;
	private final String mName;
	private final String mDistinguishedName;
	private final String mEmployeeNumber;
	private boolean mIsDisabled = false;

	/**
	 *
	 * @param aGUID
	 * @param aLoginName
	 * @param aMail
	 * @param aFirstName
	 * @param aLastName
	 * @param aName
	 * @param aDistinguishedName
	 * @param aEmployeeNumber
	 */
	public ADUser(String aGUID, String aLoginName, String aMail, String aFirstName, String aLastName, String aName, String aDistinguishedName, String aEmployeeNumber) {
		mGUID = aGUID != null ? aGUID.toUpperCase().trim() : aGUID;
		mLoginName = aLoginName != null ? aLoginName.toUpperCase().trim() : aLoginName;
		mEmail = aMail.trim();
		mFirstName = aFirstName.trim();
		mLastName = aLastName.trim();
		mName = aName.trim();
		mDistinguishedName = aDistinguishedName.trim();
		mEmployeeNumber = aEmployeeNumber;
		if (mDistinguishedName.toLowerCase().contains("disabled accounts")) {
			mIsDisabled = true;
		}
	}

	/**
	 * String representation of the MS Active Directory User Object
	 *
	 * @return the String representation of the MS Active Directory User Object
	 */
	@Override
	public String toString() {
		return "[" + mGUID + ", " + mEmployeeNumber + ", " + mLoginName
				+ ", " + mEmail + ", " + mFirstName + ", " + mLastName
				+ ", " + mName + ", " + mIsDisabled + ", " + mDistinguishedName + "]";
	}

	/**
	 * @return the loginName
	 */
	public String getLoginName() {
		return mLoginName;
	}

	/**
	 * @return the mail
	 */
	public String getMail() {
		return mEmail;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return mFirstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return mLastName;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @return the name
	 */
	public String getGUID() {
		return mGUID;
	}

	/**
	 * @return disabled flag
	 */
	public boolean isDisabled() {
		return mIsDisabled;
	}

	/**
	 *
	 * @return
	 */
	public String getDistinguishedName() {
		return mDistinguishedName;
	}

	/**
	 *
	 * @return
	 */
	public String getEmployeeNumber() {
		return mEmployeeNumber;
	}
}
