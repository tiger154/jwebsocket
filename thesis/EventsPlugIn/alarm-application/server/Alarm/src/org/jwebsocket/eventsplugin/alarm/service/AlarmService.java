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
package org.jwebsocket.eventsplugin.alarm.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import javolution.util.FastList;
import org.jwebsocket.eventmodel.observable.ObservableObject;
import org.jwebsocket.eventsplugin.alarm.api.IAlarmService;
import org.jwebsocket.eventsplugin.alarm.event.AlarmActive;
import org.jwebsocket.eventsplugin.alarm.model.Alarm;
import org.jwebsocket.util.Tools;

/**
 *
 * @author kyberneees
 */
public class AlarmService extends ObservableObject implements IAlarmService {

	private Map<String, List<Alarm>> mAlarms = new HashMap();

	public AlarmService() {
		//Registering the service events
		addEvents(AlarmActive.class);
	}

	private List<Alarm> getUserAlarms(String aUsername) {
		if (!mAlarms.containsKey(aUsername)) {
			mAlarms.put(aUsername, new FastList());
		}

		return mAlarms.get(aUsername);
	}

	private class TTask extends TimerTask {

		private ObservableObject mSubject;
		private Alarm mAlarm;

		public TTask(ObservableObject aSubject, Alarm aAlarm) {
			mSubject = aSubject;
			mAlarm = aAlarm;
		}

		@Override
		public void run() {
			try {
				mSubject.notify(new AlarmActive(mAlarm));
			} catch (Exception ex) {
				//catch me...
			}
		}
	}

	@Override
	public void create(Alarm aAlarm) {
		getUserAlarms(aAlarm.getUsername()).add(aAlarm);

		long lDelay = aAlarm.getTime() - System.currentTimeMillis();
		//Using the jWebSocket utility Timer
		Tools.getTimer().schedule(new TTask(this, aAlarm), lDelay);
	}

	@Override
	public List<Alarm> list(String aUsername) {
		return getUserAlarms(aUsername);
	}
}
