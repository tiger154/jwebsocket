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
package org.jwebsocket.eventsplugin.alarm.model;

/**
 *
 * @author kyberneees
 */
public class Alarm {

	private long mTime;
	private String mMessage;
	private String mUsername;

	public Alarm(long aTime, String aUsername, String aMessage) {
		mTime = aTime;
		mUsername = aUsername;
		mMessage = aMessage;
	}

	public String getMessage() {
		return mMessage;
	}

	public long getTime() {
		return mTime;
	}

	public String getUsername() {
		return mUsername;
	}

	@Override
	public String toString() {
		return "Alarm{" + "time=" + mTime + ", message=" + mMessage + ", username=" + mUsername + '}';
	}
	
	
}
