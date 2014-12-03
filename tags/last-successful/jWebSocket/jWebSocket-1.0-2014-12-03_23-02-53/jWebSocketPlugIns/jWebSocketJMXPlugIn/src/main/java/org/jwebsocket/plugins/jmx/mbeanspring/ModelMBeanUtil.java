// ---------------------------------------------------------------------------
// jWebSocket - ModelMBeanUtil (Community Edition, CE)
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
package org.jwebsocket.plugins.jmx.mbeanspring;

import java.lang.reflect.Method;
import javax.management.Descriptor;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;

/**
 * Class containing helper methods for creating the ModelMBean.
 *
 * @author Lisdey Perez Hernandez
 */
public class ModelMBeanUtil {

	/**
	 * Method used to construct the name of a method given a prefix.
	 *
	 * @param aPrefix
	 * @param aName
	 * @return
	 */
	protected static String toMethodName(String aPrefix, String aName) {
		char lHead = Character.toUpperCase(aName.charAt(0));
		String lTail = aName.substring(1);
		return aPrefix + lHead + lTail;
	}

	/**
	 * Method used to determine the type of notification to be released.
	 *
	 * @param aInfo
	 * @param aMatch
	 * @return String
	 */
	protected static String matchType(MBeanNotificationInfo aInfo, String aMatch) {
		String[] lTypes = aInfo.getNotifTypes();
		for (String lType : lTypes) {
			if (lType.endsWith(aMatch)) {
				return lType;
			}
		}
		return lTypes[0];
	}

	/**
	 * Method for determining the get method belonging to a particular
	 * attribute.
	 *
	 * @param aModelMBeanInfo
	 * @param aManagedBean
	 * @param aAttribute
	 * @return Method
	 * @throws MBeanException
	 */
	public static Method findGetMethod(ModelMBeanInfo aModelMBeanInfo,
			Object aManagedBean, String aAttribute)
			throws MBeanException {
		try {
			ModelMBeanAttributeInfo lInfo =
					aModelMBeanInfo.getAttribute(aAttribute);
			Descriptor lDescriptor = lInfo.getDescriptor();
			String lMethodName = (String) (lDescriptor.getFieldValue("getMethod"));
			if (lMethodName == null) {
				lMethodName = toMethodName("get", aAttribute);
			}
			return aManagedBean.getClass().getMethod(lMethodName, new Class[]{});
		} catch (NoSuchMethodException e) {
			throw new MBeanException(e);
		}
	}

	/**
	 * Method for determining the set method belonging to a particular
	 * attribute.
	 *
	 * @param aModelMBeanInfo
	 * @param aManagedBean
	 * @param aAttribute
	 * @return Method
	 * @throws MBeanException
	 */
	public static Method findSetMethod(ModelMBeanInfo aModelMBeanInfo,
			Object aManagedBean, String aAttribute)
			throws MBeanException {
		try {
			ModelMBeanAttributeInfo lInfo = aModelMBeanInfo.getAttribute(aAttribute);
			Descriptor lDescriptor = lInfo.getDescriptor();
			String lMethodName = (String) (lDescriptor.getFieldValue("setMethod"));
			if (lMethodName == null) {
				lMethodName = toMethodName("set", aAttribute);
			}
			Class[] lType = {Class.forName(lInfo.getType())};
			return aManagedBean.getClass().getMethod(lMethodName, lType);
		} catch (ClassNotFoundException e) {
			throw new MBeanException(e);
		} catch (NoSuchMethodException e) {
			throw new MBeanException(e);
		}
	}
}
