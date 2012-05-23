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
package org.jwebsocket.eventsplugin.alarm.controller;

import java.util.Iterator;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.eventsplugin.alarm.event.AlarmActive;
import org.jwebsocket.eventsplugin.alarm.event.AlarmActiveNotification;
import org.jwebsocket.eventsplugin.alarm.event.CreateNewAlarm;
import org.jwebsocket.eventsplugin.alarm.event.ListAlarms;
import org.jwebsocket.eventsplugin.alarm.model.Alarm;
import org.jwebsocket.eventsplugin.alarm.service.AlarmService;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author kyberneees
 */
public class AlarmController extends EventModelPlugIn {

	private AlarmService mService;
	private static Logger mLog = Logging.getLogger();

	@Override
	public void initialize() throws Exception {
		//Listening the AlarmActive eventon the AlarmService subject...
		mService.on(AlarmActive.class, this);
	}

	public AlarmService getService() {
		return mService;
	}

	public void setService(AlarmService aService) {
		mService = aService;
	}

	public void processEvent(CreateNewAlarm aEvent, C2SResponseEvent aResponseEvent)
			throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'CreateNewAlarm' event: " + aEvent.getTime() + " - "
					+ aEvent.getMessage() + "...");
		}
		mService.create(new Alarm(
				aEvent.getTime(),
				aEvent.getConnector().getUsername(),
				aEvent.getMessage()));
	}

	public void processEvent(ListAlarms aEvent, C2SResponseEvent aResponseEvent)
			throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'ListAlarms' event: ...");
		}
		aResponseEvent.getArgs().setList("data",
				mService.list(aEvent.getConnector().getUsername()));
	}

	public void processEvent(AlarmActive aEvent, ResponseEvent aResponseEvent)
			throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'AlarmActive' event: " + aEvent.getAlarm().toString());
		}

		//Just online users are able to receive actives alarms notifications
		for (Iterator<WebSocketConnector> it = getServerAllConnectors().values().
				iterator(); it.hasNext();) {

			WebSocketConnector lConnector = it.next();
			if (lConnector.getUsername().equals(aEvent.getAlarm().getUsername())) {
				notifyS2CEvent(new AlarmActiveNotification(aEvent.getAlarm().getMessage())).
						to(lConnector, null);
			}
		}
	}
}
