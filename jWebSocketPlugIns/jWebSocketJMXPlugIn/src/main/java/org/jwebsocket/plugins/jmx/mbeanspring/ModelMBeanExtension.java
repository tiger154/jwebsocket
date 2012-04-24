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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.management.*;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;

/**
 * Class that implements a specific ModelMBean for the module.
 * 
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class ModelMBeanExtension extends RequiredModelMBean {
	
	protected NotificationInfoMap mNotificationInfoMap;
	protected ModelMBeanInfo mModelMBeanInfo;
	protected Object mManagedMBean;

	/**
	 * The class default constructor.
	 * 
	 * @throws MBeanException
	 */
	public ModelMBeanExtension() throws MBeanException {
	}

	/**
	 *
	 * @param aModelMBeanInfo
	 * @throws MBeanException
	 */
	public ModelMBeanExtension(ModelMBeanInfo aModelMBeanInfo) throws MBeanException {
		super(aModelMBeanInfo);
		this.mModelMBeanInfo = aModelMBeanInfo;
		mNotificationInfoMap = new NotificationInfoMap(aModelMBeanInfo);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setModelMBeanInfo(ModelMBeanInfo aModelMBeanInfo) throws MBeanException {
		this.mModelMBeanInfo = aModelMBeanInfo;
		mNotificationInfoMap = new NotificationInfoMap(aModelMBeanInfo);
		super.setModelMBeanInfo(aModelMBeanInfo);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		return mModelMBeanInfo.getNotifications();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setManagedResource(Object aManagedBean, String aType) throws MBeanException,
			RuntimeOperationsException, InstanceNotFoundException, 
			InvalidTargetObjectTypeException {
		super.setManagedResource(aManagedBean, aType);
		this.mManagedMBean = aManagedBean;
	}

	/**
	 * Method that allows launch the notifications defined for a particular 
	 * operation.
	 * 
	 * @param aType
	 * @param aName
	 * @throws MBeanException
	 */
	protected void maybeSendMethodNotification(String aType, String aName)
			throws MBeanException {
		MBeanNotificationInfo lInfo = 
				mNotificationInfoMap.findNotificationInfo(aType, aName);
		if (lInfo != null) {
			long lTimeStamp = System.currentTimeMillis();
			String lNotificationType = 
					ModelMBeanUtil.matchType(lInfo, "." + aType + "." + aName);
			sendNotification(new Notification(
					lNotificationType, this, lTimeStamp,
					lInfo.getDescription()));
		}
	}

	/**
	 * Method that allows launch the notifications defined for a particular 
	 * attribute.
	 * 
	 * @param aAttribute
	 * @throws MBeanException
	 * @throws AttributeNotFoundException
	 * @throws InvalidAttributeValueException
	 * @throws ReflectionException
	 */
	protected void maybeSendAttributeNotification(Attribute aAttribute)
			throws MBeanException, AttributeNotFoundException,
			InvalidAttributeValueException, ReflectionException {
		String lName = aAttribute.getName();
		MBeanNotificationInfo lInfo = 
				mNotificationInfoMap.findNotificationInfo("set", aAttribute.getName());
		if (lInfo != null) {
			Object lOldValue = getAttribute(lName);
			Object lNewValue = aAttribute.getValue();
			long lTimeStamp = System.currentTimeMillis();
			String lNotificationType = 
					ModelMBeanUtil.matchType(lInfo, ".set." + lName);
			sendNotification(new AttributeChangeNotification(
					this, lTimeStamp, lTimeStamp,
					lInfo.getDescription(), lInfo.getName(),
					lNotificationType, lOldValue, lNewValue));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object invoke(String aName, Object[] aArgs, String[] aSignature)
			throws MBeanException, ReflectionException {
		maybeSendMethodNotification("before", aName);
		Object lReturnValue = super.invoke(aName, aArgs, aSignature);
		maybeSendMethodNotification("after", aName);
		return lReturnValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAttribute(String aName) throws MBeanException,
			AttributeNotFoundException, ReflectionException {
		try {
			ModelMBeanAttributeInfo lInfo = mModelMBeanInfo.getAttribute(aName);
			if (!lInfo.isReadable()) {
				throw new AttributeNotFoundException("getAttribute is failed: " 
						+ aName + " is not readable");
			}
			Method lMethod = ModelMBeanUtil.findGetMethod(
					mModelMBeanInfo, mManagedMBean, aName);
			return lMethod.invoke(mManagedMBean, new Object[]{});
		} catch (IllegalAccessException e) {
			throw new MBeanException(e);
		} catch (InvocationTargetException e) {
			throw new MBeanException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAttribute(Attribute aAttribute) throws MBeanException,
			AttributeNotFoundException, InvalidAttributeValueException, 
			ReflectionException {
		try {
			ModelMBeanAttributeInfo lInfo = 
					mModelMBeanInfo.getAttribute(aAttribute.getName());
			if (lInfo.isWritable()) {
				Method lMethod = ModelMBeanUtil.findSetMethod(
						mModelMBeanInfo, mManagedMBean, aAttribute.getName());
				lMethod.invoke(mManagedMBean, aAttribute.getValue());
				maybeSendAttributeNotification(aAttribute);
			} else {
				throw new AttributeNotFoundException("setAttribute failed: " 
						+ aAttribute.getName() + " is not writable");
			}
		} catch (IllegalAccessException e) {
			throw new MBeanException(e);
		} catch (InvocationTargetException e) {
			throw new MBeanException(e);
		}
	}
}
