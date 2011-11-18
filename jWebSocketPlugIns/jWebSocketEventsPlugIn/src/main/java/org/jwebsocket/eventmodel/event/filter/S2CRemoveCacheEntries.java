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

	private List<String> entries;
	private String eventId;

	/**
	 * @param eventId The event identifier
	 * @param entries The cache entries to remove
	 */
	public S2CRemoveCacheEntries(String eventId, List<String> entries) {
		super();
		setId("cleanEntries");

		this.entries = entries;
		this.eventId = eventId;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void writeToToken(Token token) {
		token.setString("suffix", getEventId());
		token.setList("entries", getEntries());
	}

	/**
	 * @return The cache entries to remove
	 */
	public List<String> getEntries() {
		return Collections.unmodifiableList(entries);
	}

	/**
	 * @param entries The cache entries to remove
	 */
	public void setEntries(List<String> entries) {
		this.entries = entries;
	}

	/**
	 * @return The event identifier
	 */
	public String getEventId() {
		return eventId;
	}

	/**
	 * @param eventId The event identifier
	 */
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
}
