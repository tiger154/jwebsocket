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

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

/**
 *
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class OperationDefinition extends FeatureDefinition {

	private Class mReturnValueType;
	private String mImpact;
	private ParameterDefinition[] mParameters = new ParameterDefinition[0];

	/**
	 *
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
	public OperationDefinition(Class aReturnValueType, String aImpact, String aName, String aDescription) {
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
	 * @return
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
	 * @return
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
	 * @return
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
	 *
	 * @return
	 */
	public ModelMBeanOperationInfo createMBeanOperationInfo() {
		MBeanParameterInfo[] lParametersInfo = createMBeanParameterInfoArray();
		return new ModelMBeanOperationInfo(super.getName(), super.getDescription(), lParametersInfo, getReturnValueType().getName(), getImpactInt());
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
