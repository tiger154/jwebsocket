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
package org.jwebsocket.plugins.jmx.mbeanspring;

import java.lang.reflect.Method;
import javax.management.Descriptor;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;

/**
 *
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class ModelMBeanUtil {

	/**
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
	 *
	 * @param aInfo
	 * @param aMatch
	 * @return
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
	 *
	 * @param aModelMBeanInfo
	 * @param aManagedBean
	 * @param aAttribute
	 * @return
	 * @throws MBeanException
	 */
	public static Method findGetMethod(ModelMBeanInfo aModelMBeanInfo, Object aManagedBean, String aAttribute)
			throws MBeanException {
		try {
			ModelMBeanAttributeInfo lInfo = aModelMBeanInfo.getAttribute(aAttribute);
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
	 *
	 * @param aModelMBeanInfo
	 * @param aManagedBean
	 * @param aAttribute
	 * @return
	 * @throws MBeanException
	 */
	public static Method findSetMethod(ModelMBeanInfo aModelMBeanInfo, Object aManagedBean, String aAttribute)
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
