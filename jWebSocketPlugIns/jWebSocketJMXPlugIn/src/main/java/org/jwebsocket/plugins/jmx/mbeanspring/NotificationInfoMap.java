// ---------------------------------------------------------------------------
// jWebSocket - NotificationInfoMap (Community Edition, CE)
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

import java.util.HashMap;
import javax.management.MBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanInfo;

/**
 * Class that defines a map of events notifications to be created for a specific
 * ModelMBean.
 *
 * @author Lisdey Perez Hernandez
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
	 * Search a notification on the map given the prefix assigned to it and the
	 * name of the attribute or operation as appropriate.
	 *
	 * @param aPrefix
	 * @param aName
	 * @return MBeanNotificationInfo
	 */
	public MBeanNotificationInfo findNotificationInfo(String aPrefix, String aName) {
		return get(aPrefix + "." + aName);
	}
}
