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

import javax.management.modelmbean.ModelMBeanNotificationInfo;

/**
 *
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class NotificationDefinition extends FeatureDefinition {

	private String[] mTypes;

	/**
	 * 
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
				throw new IllegalArgumentException("The notification types must have at least one value.");
			}
		} else {
			throw new IllegalArgumentException("The notification types must have at least one value.");
		}
	}

	/**
	 * 
	 * @return
	 */
	public String[] getTypes() {
		if (mTypes != null) {
			return mTypes;
		} else {
			throw new IllegalArgumentException("The notification types must have at least one value.");
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
				throw new IllegalArgumentException("The notification types must have at least one value.");
			}
		} else {
			throw new IllegalArgumentException("The notification types must have at least one value.");
		}
	}

	/**
	 * 
	 * @return
	 */
	public ModelMBeanNotificationInfo createMBeanNotificationInfo() {
		if (mTypes != null) {
			return new ModelMBeanNotificationInfo(mTypes, super.getName(), super.getDescription());
		} else {
			throw new IllegalArgumentException("The notification types must have at least one value.");
		}
	}
}
