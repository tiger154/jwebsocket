//  ---------------------------------------------------------------------------
//  jWebSocket 
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventsplugin.alarm.filter;

import java.util.Date;
import java.util.Map;
import javolution.util.FastMap;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.filter.EventModelFilter;

/**
 *
 * @author kyberneees
 */
public class MinimumNotificationTimeIntervalFilter extends EventModelFilter {

	private Map<String, Long> mNotifications = new FastMap();

	@Override
	public void beforeCall(WebSocketConnector aConnector, C2SEvent aEvent)
			throws Exception {
		C2SEventDefinition lEventDef = getEm().getEventFactory().
				getEventDefinitions().getDefinition(aEvent.getId());
		if (lEventDef instanceof C2SEventDefinitionExt) {
			String lKey = aConnector.getUsername() + aEvent.getId();

			Long lLastAccess = null;
			Boolean lUpdate = false;
			if (mNotifications.containsKey(lKey)) {
				lLastAccess = mNotifications.get(lKey);
			} else {
				lUpdate = true;
			}

			if (null != lLastAccess) {
				//Checking if the notification interval is less than allowed
				if (lLastAccess + ((C2SEventDefinitionExt) lEventDef).getMinimumNotificationTimeInterval() * 60000 > new Date().getTime()) {
					throw new Exception("The C2S event '" + aEvent.getClass().getSimpleName()
							+ "' can't be notified due to the minimum notification"
							+ " time interval restrictions!");
				} else {
					lUpdate = true;
				}
			}

			if (lUpdate) {
				//Updating the last event notification time
				mNotifications.put(lKey, new Date().getTime());
			}
		}
	}
}
