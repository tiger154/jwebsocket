// ---------------------------------------------------------------------------
// jWebSocket - FeatureDefinition (Community Edition, CE)
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

/**
 * Generic class that allows to define the name and description of all elements
 * of the plugins or classes to export.
 *
 * @author Lisdey Perez Hernandez
 */
public abstract class FeatureDefinition {

	private String mName;
	private String mDescription;

	/**
	 * The class default constructor.
	 */
	public FeatureDefinition() {
	}

	/**
	 *
	 * @param aName
	 * @param aDescription
	 */
	public FeatureDefinition(String aName, String aDescription) {
		this.mDescription = aDescription;
		if (!aName.equals("")) {
			this.mName = aName;
		} else {
			throw new IllegalArgumentException("Name must not be empty");
		}
	}

	/**
	 *
	 * @return String
	 */
	public String getName() {
		if (this.mName != null) {
			return this.mName;
		} else {
			throw new IllegalArgumentException("Name must not be null");
		}
	}

	/**
	 *
	 * @param aName
	 */
	public void setName(String aName) {
		if (!aName.equals("")) {
			this.mName = aName;
		} else {
			throw new IllegalArgumentException("Name must not be empty");
		}
	}

	/**
	 *
	 * @return String
	 */
	public String getDescription() {
		return mDescription == null ? "" : mDescription;
	}

	/**
	 *
	 * @param aDescription
	 */
	public void setDescription(String aDescription) {
		this.mDescription = aDescription;
	}
}
