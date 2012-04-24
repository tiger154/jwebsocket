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

import javax.management.MBeanParameterInfo;

/**
 * Class that allows to define the constructor parameters of the classes to 
 * export and their metadata.
 * 
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
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
