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
package org.jwebsocket.eventmodel.filter.validator;

import org.springframework.validation.Validator;

/**
 *
 * @author kyberneees
 */
public class Argument {

	private String mName;
	private String mType;
	private boolean mOptional;
	private Object mValue;
	private Validator mValidator;

	/**
	 * @return The argument name
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @param aName The argument name to set
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
	 * @return <tt>TRUE</tt> if the argument is optional, <tt>FALSE</tt> otherwise
	 */
	public boolean isOptional() {
		return mOptional;
	}

	/**
	 * @param aOptional Indicate if the argument is optional
	 */
	public void setOptional(boolean aOptional) {
		this.mOptional = aOptional;
	}

	/**
	 * @return The argument value
	 */
	public Object getValue() {
		return mValue;
	}

	/**
	 * @param aValue The argument value to set
	 */
	public void setValue(Object aValue) {
		this.mValue = aValue;
	}

	/**
	 * @return The Spring validator for the argument
	 */
	public Validator getValidator() {
		return mValidator;
	}

	/**
	 * @param aValidator The Spring validator to set
	 */
	public void setValidator(Validator aValidator) {
		this.mValidator = aValidator;
	}
}
