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

import javax.management.modelmbean.*;

/**
 * The main class that contains all the elements necessary to conform the class 
 * or plugin object to export and its metadata.
 * 
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class JMXDefinition {

	private AttributeDefinition[] mAttributes = new AttributeDefinition[0];
	private OperationDefinition[] mOperations = new OperationDefinition[0];
	private ConstructorDefinition[] mConstructors = new ConstructorDefinition[0];
	private NotificationDefinition[] mNotifications = new NotificationDefinition[0];
	private String mDescription = "";
	private String mClassName;
	private String mJarName;
	
	public JMXDefinition() {
	}

	public JMXDefinition(String aClassName, String aJarName, AttributeDefinition[] aAttributes,
			OperationDefinition[] aOperations, ConstructorDefinition[] aConstructors, 
			NotificationDefinition[] aNotifications) {

		this.mAttributes = aAttributes;
		this.mOperations = aOperations;
		this.mConstructors = aConstructors;
		this.mNotifications = aNotifications;

		if (!aClassName.equals("")) {
			this.mClassName = aClassName;
		} else {
			throw new IllegalArgumentException("The class name must not be empty.");
		}

		if (!aJarName.equals("")) {
			if (!aJarName.toLowerCase().endsWith(".jar")) {
				this.mJarName = aJarName + ".jar";
			} else {
				this.mJarName = aJarName;
			}
		} else {
			throw new IllegalArgumentException("The jar name must not be empty.");
		}
	}

	public AttributeDefinition[] getAttributes() {
		return mAttributes;
	}

	public void setAttributes(AttributeDefinition[] aAttributes) {
		this.mAttributes = aAttributes;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String aDescription) {
		this.mDescription = aDescription;
	}

	public OperationDefinition[] getOperations() {
		return mOperations;
	}

	public void setOperations(OperationDefinition[] aOperations) {
		this.mOperations = aOperations;
	}

	public ConstructorDefinition[] getConstructors() {
		return mConstructors;
	}

	public void setConstructors(ConstructorDefinition[] aConstructors) {
		this.mConstructors = aConstructors;
	}

	public String getClassName() {
		if (this.mClassName != null) {
			return mClassName;
		} else {
			throw new IllegalArgumentException("The class name must not be null.");
		}
	}

	public void setClassName(String aClassName) {
		if (!aClassName.equals("")) {
			this.mClassName = aClassName;
		} else {
			throw new IllegalArgumentException("The class name must not be empty.");
		}
	}

	public NotificationDefinition[] getNotifications() {
		return mNotifications;
	}

	public void setNotifications(NotificationDefinition[] aNotifications) {
		this.mNotifications = aNotifications;
	}

	public String getJarName() {
		if (this.mJarName != null) {
			return mJarName;
		} else {
			throw new IllegalArgumentException("The jar name must not be null.");
		}
	}

	public void setJarName(String aJarName) {
		if (!aJarName.equals("")) {
			if (!aJarName.toLowerCase().endsWith(".jar")) {
				this.mJarName = aJarName + ".jar";
			} else {
				this.mJarName = aJarName;
			}
		} else {
			throw new IllegalArgumentException("The jar name must not be empty.");
		}
	}
	
	public ModelMBeanInfo createMBeanInfo() {
		if (this.mClassName != null) {
			return new ModelMBeanInfoSupport(mClassName, mDescription,
					createMBeanAttributeInfoArray(),
					createMBeanConstructorInfoArray(),
					createMBeanOperationInfoArray(),
					createMBeanNotificationInfoArray());
		} else {
			throw new IllegalArgumentException("The class name must not be null.");
		}
	}

	protected ModelMBeanAttributeInfo[] createMBeanAttributeInfoArray() {
		ModelMBeanAttributeInfo[] lInfoArray = 
				new ModelMBeanAttributeInfo[mAttributes.length];
		for (int i = 0; i < mAttributes.length; i++) {
			lInfoArray[i] = mAttributes[i].createMBeanAttributeInfo();
		}
		return lInfoArray;
	}

	protected ModelMBeanConstructorInfo[] createMBeanConstructorInfoArray() {
		int lCount = mConstructors.length;
		ModelMBeanConstructorInfo[] lInfoArray = 
				new ModelMBeanConstructorInfo[lCount];
		for (int i = 0; i < lCount; i++) {
			lInfoArray[i] = mConstructors[i].createMBeanConstructorInfo();
		}
		return lInfoArray;
	}

	protected ModelMBeanOperationInfo[] createMBeanOperationInfoArray() {
		ModelMBeanOperationInfo[] lInfoArray = 
				new ModelMBeanOperationInfo[mOperations.length];
		for (int i = 0; i < mOperations.length; i++) {
			lInfoArray[i] = mOperations[i].createMBeanOperationInfo();
		}
		return lInfoArray;
	}

	protected ModelMBeanNotificationInfo[] createMBeanNotificationInfoArray() {
		ModelMBeanNotificationInfo[] lInfoArray = 
				new ModelMBeanNotificationInfo[mNotifications.length];
		for (int i = 0; i < mNotifications.length; i++) {
			lInfoArray[i] = mNotifications[i].createMBeanNotificationInfo();
		}
		return lInfoArray;
	}
}
