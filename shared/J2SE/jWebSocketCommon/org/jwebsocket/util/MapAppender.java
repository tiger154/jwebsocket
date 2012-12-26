//	---------------------------------------------------------------------------
//	jWebSocket - MapAppender
//	Copyright (c) 2012 Rolando Santamaria Maso, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to append entries in chain on a Map instance
 *
 * @author kyberneees
 */
public class MapAppender {

	private Map mMap = new HashMap();

	public MapAppender(Map aMap) {
		mMap = aMap;
	}

	public MapAppender() {
	}

	public MapAppender append(Object aKey, Object aValue) {
		mMap.put(aKey, aValue);

		return this;
	}

	public MapAppender append(Map aMap) {
		mMap.putAll(aMap);

		return this;
	}

	public Map getMap() {
		return mMap;
	}
}
