//  ---------------------------------------------------------------------------
//  jWebSocket - ActionJms
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org, Alexander Schulze
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
package org.jwebsocket.plugins.jms.util;

/**
 * 
 * @author Johannes Smutny
 */
public enum ActionJms {

	LISTEN("listenJms"), LISTEN_MESSAGE("listenJmsMessage"), UNLISTEN("unlistenJms"), SEND_TEXT("sendJmsText"), SEND_TEXT_MESSAGE(
			"sendJmsTextMessage"), SEND_MAP("sendJmsMap"), SEND_MAP_MESSAGE("sendJmsMapMessage");
	private String mValue;

	private ActionJms(String value) {
		this.mValue = value;
	}

	public String getValue() {
		return mValue;
	}

	public boolean equals(String aAction) {
		return mValue.equals(aAction);
	}

	@Override
	public String toString() {
		return mValue;
	}

	public static ActionJms get(String value) {
		if (null == value)
			throw new IllegalArgumentException("missing value");

		for (ActionJms next : ActionJms.values())
			if (next.getValue().equals(value))
				return next;

		throw new IllegalArgumentException("missing ActionJms for value: '" + value + "'");
	}
}
