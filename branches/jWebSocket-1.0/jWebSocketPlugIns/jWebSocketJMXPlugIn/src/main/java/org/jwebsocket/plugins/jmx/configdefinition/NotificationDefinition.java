// ---------------------------------------------------------------------------
// jWebSocket - NotificationDefinition (Community Edition, CE)
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

import javax.management.modelmbean.ModelMBeanNotificationInfo;

/**
 * Class that allows to define an event notifications for the plugins and
 * classes to export and their metadata.
 *
 * @author Lisdey Perez Hernandez
 */
public class NotificationDefinition extends FeatureDefinition {

	private String[] mTypes;

	/**
	 * The class default constructor.
	 */
	public NotificationDefinition() {
	}

	/**
	 *
	 * @param aTypes
	 * @param aName
	 * @param aDescription
	 */
	public NotificationDefinition(String[] aTypes, String aName, String aDescription) {
		super(aName, aDescription);
		if (aTypes.length > 0) {
			if (!aTypes[0].equals("")) {
				this.mTypes = aTypes;
			} else {
				throw new IllegalArgumentException("The notification types must "
						+ "have at least one value.");
			}
		} else {
			throw new IllegalArgumentException("The notification types must have"
					+ " at least one value.");
		}
	}

	/**
	 *
	 * @return String[]
	 */
	public String[] getTypes() {
		if (mTypes != null) {
			return mTypes;
		} else {
			throw new IllegalArgumentException("The notification types must have"
					+ " at least one value.");
		}
	}

	/**
	 *
	 * @param aTypes
	 */
	public void setTypes(String[] aTypes) {
		if (aTypes.length > 0) {
			if (!aTypes[0].equals("")) {
				this.mTypes = aTypes;
			} else {
				throw new IllegalArgumentException("The notification types must "
						+ "have at least one value.");
			}
		} else {
			throw new IllegalArgumentException("The notification types must have"
					+ " at least one value.");
		}
	}

	/**
	 * Create the metadata of the notification definition.
	 *
	 * @return ModelMBeanNotificationInfo
	 */
	public ModelMBeanNotificationInfo createMBeanNotificationInfo() {
		if (mTypes != null) {
			return new ModelMBeanNotificationInfo(mTypes, super.getName(),
					super.getDescription());
		} else {
			throw new IllegalArgumentException("The notification types must have"
					+ " at least one value.");
		}
	}
}
