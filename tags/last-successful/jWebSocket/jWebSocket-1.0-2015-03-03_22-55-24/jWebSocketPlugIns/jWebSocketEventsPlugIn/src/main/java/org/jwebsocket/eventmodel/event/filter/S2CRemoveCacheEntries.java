//	---------------------------------------------------------------------------
//	jWebSocket - S2CRemoveCacheEntries (Community Edition, CE)
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
package org.jwebsocket.eventmodel.event.filter;

import java.util.Collections;
import java.util.List;
import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.token.Token;

/**
 * S2C event to remove expired cache entries in the client
 *
 * @author Rolando Santamaria Maso
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
