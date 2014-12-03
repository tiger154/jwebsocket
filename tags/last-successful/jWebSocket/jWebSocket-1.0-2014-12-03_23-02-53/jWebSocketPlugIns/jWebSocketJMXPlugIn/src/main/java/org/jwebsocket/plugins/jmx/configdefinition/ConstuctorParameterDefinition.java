// ---------------------------------------------------------------------------
// jWebSocket - ConstuctorParameterDefinition (Community Edition, CE)
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
package org.jwebsocket.plugins.jmx.configdefinition;

import javax.management.MBeanParameterInfo;

/**
 * Class that allows to define the constructor parameters of the classes to
 * export and their metadata.
 *
 * @author Lisdey Perez Hernandez
 */
public class ConstuctorParameterDefinition extends FeatureDefinition {

	private Object mValue;

	/**
	 * The class default constructor
	 */
	public ConstuctorParameterDefinition() {
	}

	/**
	 *
	 * @param aValue
	 * @param aName
	 * @param aDescription
	 */
	public ConstuctorParameterDefinition(Object aValue, String aName,
			String aDescription) {
		super(aName, aDescription);
		this.mValue = aValue;
	}

	/**
	 *
	 * @return Object
	 */
	public Object getValue() {
		if (this.mValue != null) {
			return this.mValue;
		} else {
			throw new IllegalArgumentException("The parameter value must not be "
					+ "null");
		}
	}

	/**
	 *
	 * @param aValue
	 */
	public void setValue(Object aValue) {
		this.mValue = aValue;
	}

	/**
	 * Create the metadata of the constructor parameter definition.
	 *
	 * @return MBeanParameterInfo
	 */
	public MBeanParameterInfo createMBeanParameterInfo() {
		return new MBeanParameterInfo(super.getName(),
				mValue.getClass().getName(), super.getDescription());
	}
}
