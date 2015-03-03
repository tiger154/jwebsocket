//	---------------------------------------------------------------------------
//	jWebSocket - Right (Community Edition, CE)
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

/**
 * implements a right as part of a FastMap of rights for a certain role.
 *
 * @author Alexander Schulze
 */
public class Right {

	private String mId = null;
	private String mDescription = null;

	/**
	 * creates a new default right with a name space, an id and a description.
	 *
	 * @param aNS
	 * @param aId
	 * @param aDescription
	 */
	public Right(String aNS, String aId, String aDescription) {
		mId = aNS + "." + aId;
		mDescription = aDescription;
	}

	/**
	 * creates a new default right with a id and a description.
	 *
	 * @param aId
	 * @param aDescription
	 */
	public Right(String aId, String aDescription) {
		mId = aId;
		mDescription = aDescription;
	}

	/**
	 * returns the id of the right. The key is the unique identifier of the
	 * right and should contain the entire name space e.g.
	 * <tt>org.jwebsocket.plugins.chat.broadcast</tt>. The key is case-sensitve.
	 *
	 * @return
	 */
	public String getId() {
		return mId;
	}

	/**
	 * specifies the id of the right. The key is the unique identifier of the
	 * right and should contain the entire name space e.g.
	 * <tt>org.jwebsocket.plugins.chat.broadcast</tt>. The key is case-sensitve.
	 *
	 * @param aId
	 */
	public void setId(String aId) {
		this.mId = aId;
	}

	/**
	 * returns the description of the right.
	 *
	 * @return
	 */
	public String getDescription() {
		return mDescription;
	}

	/**
	 * specifies the description of the right.
	 *
	 * @param description
	 */
	public void setDescription(String description) {
		this.mDescription = description;
	}
}
