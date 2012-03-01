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

import java.util.HashMap;
import javax.management.MBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanInfo;

/**
 *
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class NotificationInfoMap extends HashMap<String, MBeanNotificationInfo> {

	/**
	 *
	 * @param aModelMBeanInfo
	 */
	public NotificationInfoMap(ModelMBeanInfo aModelMBeanInfo) {
		initializeMap(aModelMBeanInfo);
	}

	private void initializeMap(ModelMBeanInfo aModelMBeanInfo) {
		MBeanNotificationInfo[] lNotifications = aModelMBeanInfo.getNotifications();
		String[] lMatchTypes = {".set.", ".before.", ".after."};
		for (MBeanNotificationInfo lInfo : lNotifications) {
			String[] lNotificationTypes = lInfo.getNotifTypes();
			for (String lNotificationType : lNotificationTypes) {
				for (String lMatchType : lMatchTypes) {
					int lIndex = lNotificationType.indexOf(lMatchType);
					if (lIndex > -1) {
						String lKey = lNotificationType.substring(lIndex + 1);
						put(lKey, lInfo);
					}
				}
			}
		}
	}

	/**
	 *
	 * @param aPrefix
	 * @param aName
	 * @return
	 */
	public MBeanNotificationInfo findNotificationInfo(String aPrefix, String aName) {
		return get(aPrefix + "." + aName);
	}
}
