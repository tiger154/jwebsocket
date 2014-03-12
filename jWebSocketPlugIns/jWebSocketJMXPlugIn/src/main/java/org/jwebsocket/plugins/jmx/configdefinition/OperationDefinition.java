// ---------------------------------------------------------------------------
// jWebSocket - OperationDefinition (Community Edition, CE)
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

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

/**
 * Class that allows to define the operations of the plugins or classes to
 * export and their metadata.
 *
 * @author Lisdey Perez Hernandez
 */
public class OperationDefinition extends FeatureDefinition {

	private Class mReturnValueType;
	private String mImpact;
	private ParameterDefinition[] mParameters = new ParameterDefinition[0];

	/**
	 * The class default constructor.
	 */
	public OperationDefinition() {
	}

	/**
	 *
	 * @param aReturnValueType
	 * @param aImpact
	 * @param aName
	 * @param aDescription
	 */
	public OperationDefinition(Class aReturnValueType, String aImpact,
			String aName, String aDescription) {
		super(aName, aDescription);
		if (aReturnValueType != null) {
			this.mReturnValueType = aReturnValueType;
		} else {
			this.mReturnValueType = java.lang.Void.class;
		}

		if ((aImpact != null) & (!aImpact.equals(""))) {
			this.mImpact = aImpact;
		} else {
			this.mImpact = "UNKNOWN";
		}
	}

	/**
	 *
	 * @return Class
	 */
	public Class getReturnValueType() {
		return (mReturnValueType != null) ? mReturnValueType : java.lang.Void.class;
	}

	/**
	 *
	 * @param aReturnValueType
	 */
	public void setReturnValueType(Class aReturnValueType) {
		this.mReturnValueType = aReturnValueType;
	}

	/**
	 *
	 * @return String
	 */
	public String getImpact() {
		return (mImpact != null) || (!mImpact.equals("")) ? mImpact : "UNKNOWN";
	}

	/**
	 *
	 * @param aImpact
	 */
	public void setImpact(String aImpact) {
		this.mImpact = aImpact;
	}

	/**
	 *
	 * @return ParameterDefinition[]
	 */
	public ParameterDefinition[] getParameters() {
		return mParameters;
	}

	/**
	 *
	 * @param mParameters
	 */
	public void setParameters(ParameterDefinition[] mParameters) {
		this.mParameters = mParameters;
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

	/**
	 * Create the metadata of the operation definition.
	 *
	 * @return ModelMBeanOperationInfo
	 */
	public ModelMBeanOperationInfo createMBeanOperationInfo() {
		MBeanParameterInfo[] lParametersInfo = createMBeanParameterInfoArray();
		return new ModelMBeanOperationInfo(super.getName(),
				super.getDescription(), lParametersInfo,
				getReturnValueType().getName(), getImpactInt());
	}

	private int getImpactInt() {
		if ("ACTION".equals(mImpact)) {
			return MBeanOperationInfo.ACTION;
		} else if ("INFO".equals(mImpact)) {
			return MBeanOperationInfo.INFO;
		} else if ("ACTION_INFO".equals(mImpact)) {
			return MBeanOperationInfo.ACTION_INFO;
		} else {
			return MBeanOperationInfo.UNKNOWN;
		}
	}
}
