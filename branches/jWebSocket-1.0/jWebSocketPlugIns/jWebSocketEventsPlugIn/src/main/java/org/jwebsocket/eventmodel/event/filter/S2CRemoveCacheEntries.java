//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
//  ---------------------------------------------------------------------------F
package org.jwebsocket.eventmodel.event.filter;

import java.util.Collections;
import java.util.List;
import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.token.Token;

/**
 * S2C event to remove expired cache entries in the client
 * 
 * @author kyberneees
 */
public class S2CRemoveCacheEntries extends S2CEvent {

	private List<String> mEntries;
	private String mEventId;

	/**
	 * @param aEventId The event identifier
	 * @param aEntries The cache entries to remove
	 */
	public S2CRemoveCacheEntries(String aEventId, List<String> aEntries) {
		super();
		setId("cleanEntries");

		this.mEntries = aEntries;
		this.mEventId = aEventId;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void writeToToken(Token aToken) {
		aToken.setString("suffix", mEventId);
		aToken.setList("entries", getEntries());
	}

	/**
	 * @return The cache entries to remove
	 */
	public List<String> getEntries() {
		return Collections.unmodifiableList(mEntries);
	}

	/**
	 * @param aEntries The cache entries to remove
	 */
	public void setEntries(List<String> aEntries) {
		this.mEntries = aEntries;
	}

	/**
	 * @return The event identifier
	 */
	public String getEventId() {
		return mEventId;
	}

	/**
	 * @param aEventId The event identifier
	 */
	public void setEventId(String aEventId) {
		this.mEventId = aEventId;
	}
}
