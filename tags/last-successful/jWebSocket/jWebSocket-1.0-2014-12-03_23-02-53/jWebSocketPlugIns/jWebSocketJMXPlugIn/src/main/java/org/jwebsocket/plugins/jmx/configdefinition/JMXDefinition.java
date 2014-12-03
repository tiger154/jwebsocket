// ---------------------------------------------------------------------------
// jWebSocket - JMXDefinition (Community Edition, CE)
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

import javax.management.modelmbean.*;

/**
 * The main class that contains all the elements necessary to conform the class
 * or plugin object to export and its metadata.
 *
 * @author Lisdey Perez Hernandez
 */
public class JMXDefinition {

	private AttributeDefinition[] mAttributes = new AttributeDefinition[0];
	private OperationDefinition[] mOperations = new OperationDefinition[0];
	private ConstructorDefinition[] mConstructors = new ConstructorDefinition[0];
	private NotificationDefinition[] mNotifications = new NotificationDefinition[0];
	private String mDescription = "";
	private String mClassName;
	private String mJarName;

	/**
	 *
	 */
	public JMXDefinition() {
	}

	/**
	 *
	 * @param aClassName
	 * @param aJarName
	 * @param aAttributes
	 * @param aOperations
	 * @param aConstructors
	 * @param aNotifications
	 */
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

	/**
	 *
	 * @return
	 */
	public AttributeDefinition[] getAttributes() {
		return mAttributes;
	}

	/**
	 *
	 * @param aAttributes
	 */
	public void setAttributes(AttributeDefinition[] aAttributes) {
		this.mAttributes = aAttributes;
	}

	/**
	 *
	 * @return
	 */
	public String getDescription() {
		return mDescription;
	}

	/**
	 *
	 * @param aDescription
	 */
	public void setDescription(String aDescription) {
		this.mDescription = aDescription;
	}

	/**
	 *
	 * @return
	 */
	public OperationDefinition[] getOperations() {
		return mOperations;
	}

	/**
	 *
	 * @param aOperations
	 */
	public void setOperations(OperationDefinition[] aOperations) {
		this.mOperations = aOperations;
	}

	/**
	 *
	 * @return
	 */
	public ConstructorDefinition[] getConstructors() {
		return mConstructors;
	}

	/**
	 *
	 * @param aConstructors
	 */
	public void setConstructors(ConstructorDefinition[] aConstructors) {
		this.mConstructors = aConstructors;
	}

	/**
	 *
	 * @return
	 */
	public String getClassName() {
		if (this.mClassName != null) {
			return mClassName;
		} else {
			throw new IllegalArgumentException("The class name must not be null.");
		}
	}

	/**
	 *
	 * @param aClassName
	 */
	public void setClassName(String aClassName) {
		if (!aClassName.equals("")) {
			this.mClassName = aClassName;
		} else {
			throw new IllegalArgumentException("The class name must not be empty.");
		}
	}

	/**
	 *
	 * @return
	 */
	public NotificationDefinition[] getNotifications() {
		return mNotifications;
	}

	/**
	 *
	 * @param aNotifications
	 */
	public void setNotifications(NotificationDefinition[] aNotifications) {
		this.mNotifications = aNotifications;
	}

	/**
	 *
	 * @return
	 */
	public String getJarName() {
		if (this.mJarName != null) {
			return mJarName;
		} else {
			throw new IllegalArgumentException("The jar name must not be null.");
		}
	}

	/**
	 *
	 * @param aJarName
	 */
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

	/**
	 *
	 * @return
	 */
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

	/**
	 *
	 * @return
	 */
	protected ModelMBeanAttributeInfo[] createMBeanAttributeInfoArray() {
		ModelMBeanAttributeInfo[] lInfoArray
				= new ModelMBeanAttributeInfo[mAttributes.length];
		for (int i = 0; i < mAttributes.length; i++) {
			lInfoArray[i] = mAttributes[i].createMBeanAttributeInfo();
		}
		return lInfoArray;
	}

	/**
	 *
	 * @return
	 */
	protected ModelMBeanConstructorInfo[] createMBeanConstructorInfoArray() {
		int lCount = mConstructors.length;
		ModelMBeanConstructorInfo[] lInfoArray
				= new ModelMBeanConstructorInfo[lCount];
		for (int i = 0; i < lCount; i++) {
			lInfoArray[i] = mConstructors[i].createMBeanConstructorInfo();
		}
		return lInfoArray;
	}

	/**
	 *
	 * @return
	 */
	protected ModelMBeanOperationInfo[] createMBeanOperationInfoArray() {
		ModelMBeanOperationInfo[] lInfoArray
				= new ModelMBeanOperationInfo[mOperations.length];
		for (int i = 0; i < mOperations.length; i++) {
			lInfoArray[i] = mOperations[i].createMBeanOperationInfo();
		}
		return lInfoArray;
	}

	/**
	 *
	 * @return
	 */
	protected ModelMBeanNotificationInfo[] createMBeanNotificationInfoArray() {
		ModelMBeanNotificationInfo[] lInfoArray
				= new ModelMBeanNotificationInfo[mNotifications.length];
		for (int i = 0; i < mNotifications.length; i++) {
			lInfoArray[i] = mNotifications[i].createMBeanNotificationInfo();
		}
		return lInfoArray;
	}
}
