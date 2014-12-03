// ---------------------------------------------------------------------------
// jWebSocket - ParameterDefinition (Community Edition, CE)
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
 * Class that allows to define the operations parameters of the plugins or
 * classes to export and their metadata.
 *
 * @author Lisdey Perez Hernandez
 */
public class ParameterDefinition extends FeatureDefinition {

	private Class mType;

	/**
	 * The class default constructor.
	 */
	public ParameterDefinition() {
	}

	/**
	 *
	 * @param aType
	 * @param aName
	 * @param aDescription
	 */
	public ParameterDefinition(Class aType, String aName, String aDescription) {
		super(aName, aDescription);
		this.mType = aType;
	}

	/**
	 *
	 * @return Class
	 */
	public Class getType() {
		if (this.mType != null) {
			return this.mType;
		} else {
			throw new IllegalArgumentException("The parameter type must not be "
					+ "null");
		}
	}

	/**
	 *
	 * @param aType
	 */
	public void setType(Class aType) {
		if (aType != null) {
			this.mType = aType;
		} else {
			throw new IllegalArgumentException("The parameter type must not be "
					+ "null");
		}
	}

	/**
	 * Create the metadata of the operation parameters definition.
	 *
	 * @return MBeanParameterInfo
	 */
	public MBeanParameterInfo createMBeanParameterInfo() {
		if (this.mType != null) {
			return new MBeanParameterInfo(super.getName(), mType.getName(),
					super.getDescription());
		} else {
			throw new IllegalArgumentException("The parameter type must not be null");
		}
	}
}
