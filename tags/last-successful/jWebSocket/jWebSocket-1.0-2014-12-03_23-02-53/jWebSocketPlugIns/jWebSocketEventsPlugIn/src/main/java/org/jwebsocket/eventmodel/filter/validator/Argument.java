//	---------------------------------------------------------------------------
//	jWebSocket - Argument (Community Edition, CE)
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
package org.jwebsocket.eventmodel.filter.validator;

import org.springframework.validation.Validator;

/**
 *
 * @author Rolando Santamaria Maso
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
	 * @return <tt>TRUE</tt> if the argument is optional, <tt>FALSE</tt>
	 * otherwise
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
