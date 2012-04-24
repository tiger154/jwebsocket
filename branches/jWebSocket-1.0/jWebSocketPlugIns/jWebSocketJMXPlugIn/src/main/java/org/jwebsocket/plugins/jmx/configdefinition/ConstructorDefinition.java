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
import javax.management.modelmbean.ModelMBeanConstructorInfo;

/**
 * Class that allows to define the constructor of the classes to export and 
 * their metadata.
 * 
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class ConstructorDefinition extends FeatureDefinition {

	private ConstuctorParameterDefinition[] mParameters = 
			new ConstuctorParameterDefinition[0];

	/**
	 * The class default constructor
	 */
	public ConstructorDefinition() {
	}

	/**
	 *
	 * @return ConstuctorParameterDefinition[]
	 */
	public ConstuctorParameterDefinition[] getParameters() {
		return mParameters;
	}

	/**
	 *
	 * @param aParameters
	 */
	public void setParameters(ConstuctorParameterDefinition[] aParameters) {
		this.mParameters = aParameters;
	}

	/**
	 * Create the metadata of the constructor definition.
	 * 
	 * @return ModelMBeanConstructorInfo
	 */
	public ModelMBeanConstructorInfo createMBeanConstructorInfo() {
		MBeanParameterInfo[] lParametersInfo = createMBeanParameterInfoArray();
		return new ModelMBeanConstructorInfo(super.getName(), 
				super.getDescription(), lParametersInfo);
	}

	private MBeanParameterInfo[] createMBeanParameterInfoArray() {
		MBeanParameterInfo[] lInfoArray = new MBeanParameterInfo[0];
		if (mParameters != null) {
			lInfoArray = new MBeanParameterInfo[mParameters.length];
			for (int i = 0; i < mParameters.length; i++) {
				lInfoArray[i] = mParameters[i].createMBeanParameterInfo();
			}
		}
		return lInfoArray;
	}
}
