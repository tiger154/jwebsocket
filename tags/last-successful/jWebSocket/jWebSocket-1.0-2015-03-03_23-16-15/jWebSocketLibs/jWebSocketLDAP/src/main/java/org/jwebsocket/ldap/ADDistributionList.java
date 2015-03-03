//	---------------------------------------------------------------------------
//	jWebSocket - ActiveDirectory Distribution List (DL) (Community Edition, CE)
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
 * Distribution List (DL) Object in the the Active Directory.
 *
 * @author Predrag Stojadinovic, Alexander Schulze
 */
public class ADDistributionList {

	private final String mGUID;
	private final String mName;
	private final String mEmail;

	/**
	 * Constructor to create a new DL object.
	 *
	 * @param aGUID
	 * @param aName
	 * @param aEmail
	 */
	public ADDistributionList(String aGUID, String aName, String aEmail) {
		mGUID = aGUID != null ? aGUID.toUpperCase().trim() : aGUID;
		mName = aName;
		mEmail = aEmail;
	}

	/**
	 * Returns the string representation of the DL.
	 *
	 * @return the string representation of the DL
	 */
	@Override
	public String toString() {
		return "[" + mGUID + ", " + mName + ", " + mEmail + "]";
	}

	/**
	 * @return the Name of the DL
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @return the Email of the DL
	 */
	public String getEmail() {
		return mEmail;
	}

	/**
	 * @return the GUID of the DL
	 */
	public String getGUID() {
		return mGUID;
	}
}
