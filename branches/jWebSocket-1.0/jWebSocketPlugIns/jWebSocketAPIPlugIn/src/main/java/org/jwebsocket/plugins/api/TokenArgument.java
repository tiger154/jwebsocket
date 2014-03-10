//	---------------------------------------------------------------------------
//	jWebSocket - TokenArgument (Community Edition, CE)
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
package org.jwebsocket.plugins.api;

import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.token.Token;

/**
 * The token argument definition class
 *
 * @author Rolando Santamaria Maso
 */
public class TokenArgument implements ITokenizable {

	private String mName;
	private String mType;
	private boolean mOptional = false;
	private String mTestValue;
	private String mComment;

	/**
	 * @return The argument name
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @param aName 
	 */
	public void setName(String aName) {
		this.mName = aName;
	}

	/**
	 * @return The argument type
	 */
	public String getType() {
		return mType;
	}

	/**
	 * @param aType The argument type to set
	 */
	public void setType(String aType) {
		this.mType = aType;
	}

	/**
	 * @return <tt>TRUE</tt> if the argument is optional <tt>FALSE</tt> otherwise
	 */
	public boolean isOptional() {
		return mOptional;
	}

	/**
	 * @param aOptional Indicates if the argument is optional
	 */
	public void setOptional(boolean aOptional) {
		this.mOptional = aOptional;
	}

	/**
	 * @return The argument comment
	 */
	public String getComment() {
		return mComment;
	}

	/**
	 * @param aComment The argument comment to set
	 */
	public void setComment(String aComment) {
		this.mComment = aComment;
	}

	/**
	 * @return The test value for functional tests (JSON format)
	 */
	public String getTestValue() {
		return mTestValue;
	}

	/**
	 * @param aTestValue The test value for functional tests (JSON format)
	 */
	public void setTestValue(String aTestValue) {
		this.mTestValue = aTestValue;
	}

	/**
	 * {@inheritDoc }
	 * 
	 * @param aToken 
	 */
	@Override
	public void writeToToken(Token aToken) {
		aToken.setString("name", getName());
		aToken.setString("comment", getComment());
		aToken.setString("type", getType());
		aToken.setBoolean("optional", isOptional());

		if (getTestValue() != null) {
			String lType = getType();
			String lVal = getTestValue();
			if ("integer".equals(lType)) {
				aToken.setInteger("testValue", Integer.parseInt(lVal, 10));
			} else if ("double".equals(lType)) {
				aToken.setDouble("testValue", Double.parseDouble(lVal));
			} else if ("boolean".equals(lType)) {
				aToken.setBoolean("testValue", Boolean.parseBoolean(lVal));
			} else if ("number".equals(lType)) {
				aToken.setDouble("testValue", Double.parseDouble(lVal));
			} else {
				aToken.setString("testValue", lVal);
			}
		}
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void readFromToken(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
