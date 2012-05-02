//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.plugins.api;

import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.token.Token;

/**
 * The token argument definition class
 *
 * @author kyberneees
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
	 * @param The argument name to set
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
