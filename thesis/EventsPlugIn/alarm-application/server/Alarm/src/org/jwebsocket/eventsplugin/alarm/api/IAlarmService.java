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
package org.jwebsocket.eventsplugin.alarm.api;

import java.util.List;
import org.jwebsocket.eventsplugin.alarm.model.Alarm;

/**
 *
 * @author kyberneees
 */
public interface IAlarmService {

	/**
	 * Create a new alarm
	 * 
	 * @param aAlarm The alarm to be created
	 */
	void create(Alarm aAlarm);

	/**
	 * Get all the created alarms of a user given it username
	 * 
	 * @param aUsername The username value
	 * @return
	 */
	List<Alarm> list(String aUsername);
}
