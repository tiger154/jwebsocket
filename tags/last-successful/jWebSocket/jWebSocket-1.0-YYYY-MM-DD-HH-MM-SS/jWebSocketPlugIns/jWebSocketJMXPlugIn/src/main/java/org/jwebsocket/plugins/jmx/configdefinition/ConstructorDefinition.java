// ---------------------------------------------------------------------------
// jWebSocket - ConstructorDefinition (Community Edition, CE)
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
import javax.management.modelmbean.ModelMBeanConstructorInfo;

/**
 * Class that allows to define the constructor of the classes to export and
 * their metadata.
 *
 * @author Lisdey Perez Hernandez
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
