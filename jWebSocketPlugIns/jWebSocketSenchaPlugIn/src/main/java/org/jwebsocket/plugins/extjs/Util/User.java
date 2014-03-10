//	---------------------------------------------------------------------------
//	jWebSocket - CustomerDef for ExtJS plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.extjs.Util;

import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.token.Token;

/**
 *
 * @author Osvaldo Aguilar Lauzurique Lauzurique, Alexander Rojas Hernandez
 */
public class User implements ITokenizable {

	private Integer mId;
	private String mName;
	private String mEmail;
	private Integer mAge;

	/**
	 *
	 * @param aId
	 * @param aName
	 * @param aEmail
	 * @param aAge
	 */
	public User(Integer aId, String aName, String aEmail, Integer aAge) {
		this.mName = aName;
		this.mId = aId;
		this.mEmail = aEmail;
		this.mAge = aAge;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return mId;
	}

	/**
	 * @param aId the id to set
	 */
	public void setId(Integer aId) {
		this.mId = aId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @param aName the name to set
	 */
	public void setName(String aName) {
		this.mName = aName;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return mEmail;
	}

	/**
	 *
	 * @param aEmail
	 */
	public void setEmail(String aEmail) {
		this.mEmail = aEmail;
	}

	/**
	 * @return the age
	 */
	public Integer getAge() {
		return mAge;
	}

	/**
	 *
	 * @param aAge
	 */
	public void setAge(Integer aAge) {
		this.mAge = aAge;
	}

	@Override
	public void writeToToken(Token aToken) {
		aToken.setInteger("id", mId);
		aToken.setString("name", mName);
		aToken.setString("email", mEmail);
		aToken.setInteger("age", mAge);
	}

	@Override
	public void readFromToken(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String toString() {
		return "Name: " + mName + ", Email: " + mEmail + ", Age: " + mAge;
	}
}
