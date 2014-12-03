//	---------------------------------------------------------------------------
//	jWebSocket - Role (Community Edition, CE)
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

import java.util.Collection;

/**
 * implements a role which contains a set of rights.
 *
 * @author Alexander Schulze
 */
public class Role {

	private String mId = null;
	private String mDescription = null;
	private Rights mRights = new Rights();

	/**
	 * creates a new empty Role object.
	 */
	public Role() {
	}

	/**
	 * creates a new Role object and initializes its key and description.
	 *
	 * @param aId
	 * @param aDescription
	 */
	public Role(String aId, String aDescription) {
		mId = aId;
		mDescription = aDescription;
	}

	/**
	 * creates a new Role object and initializes its key, description and
	 * rights.
	 *
	 * @param aId
	 * @param aDescription
	 * @param aRights
	 */
	public Role(String aId, String aDescription, Right... aRights) {
		mId = aId;
		mDescription = aDescription;
		if (aRights != null) {
			for (int i = 0; i < aRights.length; i++) {
				addRight(aRights[i]);
			}
		}
	}

	/**
	 * creates a new Role object and initializes its key, description and
	 * rights.
	 *
	 * @param aId
	 * @param aDescription
	 * @param aRights
	 */
	public Role(String aId, String aDescription, Rights aRights) {
		mId = aId;
		mDescription = aDescription;
		mRights = aRights;
	}

	/**
	 *
	 * @return
	 */
	public String getId() {
		return mId;
	}

	/**
	 *
	 * @param aId
	 */
	public void setKey(String aId) {
		this.mId = aId;
	}

	/**
	 *
	 * @return
	 */
	public String getDescription() {
		return mDescription;
	}

	/**
	 *
	 * @param description
	 */
	public void setDescription(String description) {
		this.mDescription = description;
	}

	/**
	 *
	 * @param aRight
	 */
	public void addRight(Right aRight) {
		mRights.addRight(aRight);
	}

	/**
	 *
	 * @param aRight
	 * @return
	 */
	public boolean hasRight(Right aRight) {
		return mRights.hasRight(aRight);
	}

	/**
	 *
	 * @param aRight
	 * @return
	 */
	public boolean hasRight(String aRight) {
		return mRights.hasRight(aRight);
	}

	/**
	 * returns all rights of this role instance
	 *
	 * @return
	 */
	public Collection<Right> getRights() {
		// getRights already returns an unmodifiable collection
		return mRights.getRights();
	}
}
