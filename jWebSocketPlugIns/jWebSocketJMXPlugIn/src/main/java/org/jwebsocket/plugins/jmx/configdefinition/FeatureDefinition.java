// ---------------------------------------------------------------------------
// jWebSocket - JMXPlugIn v1.0
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
// THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
// THIS CODE IS NOT YET SECURE AND MAY NOT BE USED FOR PRODUCTION ENVIRONMENTS!
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.plugins.jmx.configdefinition;

/**
 * Generic class that allows to define the name and description of all elements 
 * of the plugins or classes to export.
 * 
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
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
