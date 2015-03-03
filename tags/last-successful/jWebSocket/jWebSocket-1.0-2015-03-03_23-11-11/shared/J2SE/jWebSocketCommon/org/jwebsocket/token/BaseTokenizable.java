//	---------------------------------------------------------------------------
//	jWebSocket BaseTokenizable (Community Edition, CE)
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
package org.jwebsocket.token;

/**
 *
 * @author Alexander Schulze
 */
public class BaseTokenizable {

	/**
	 *
	 */
	public static final String ARG_CLASS_ID = "_jwsClassName";
	private String mClassName = null;

	/**
	 *
	 */
	public BaseTokenizable() {
		mClassName = this.getClass().getName();
	}

	/**
	 *
	 * @param aToken
	 */
	public void writeToToken(Token aToken) {
		if (aToken != null) {
			// add reserved field _jwsClassName to identify class at target
			aToken.setString(ARG_CLASS_ID, mClassName);
		}
	}

	/**
	 *
	 * @param aToken
	 */
	public void readFromToken(Token aToken) {
		if (aToken != null) {
			// add reserved field _jwsClassName to identify class at target
			mClassName = aToken.getString(ARG_CLASS_ID);
		}
	}

	/**
	 * @return the mClassName
	 */
	public String getClassName() {
		return mClassName;
	}

	// return the object as a token
	/**
	 *
	 * @return
	 */
	public Token toToken() {
		Token lToken = TokenFactory.createToken();
		writeToToken(lToken);
		return lToken;
	}
}
