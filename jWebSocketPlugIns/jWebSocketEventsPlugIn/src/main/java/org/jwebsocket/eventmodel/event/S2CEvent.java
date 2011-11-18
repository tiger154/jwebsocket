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
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.event;

import org.jwebsocket.token.ITokenizable;
import org.jwebsocket.token.Token;

/**
 *
 * @author kyberneees
 */
public abstract class S2CEvent implements ITokenizable {

	private String id;
	private String responseType = "void"; //void by default
	private String plugInId;
	private Integer timeout = 1000; //One second by default

	/**
	 * Write the parent class fields values to the token
	 */
	public void writeParentToToken(Token token) {
		token.setString("event_name", getId());
		token.setString("plugin_id", getPlugInId());
		token.setString("response_type", getResponseType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readFromToken(Token token) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * @return The S2CEvent identifier
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id The S2CEvent identifier to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return The S2CEvent response type
	 */
	public String getResponseType() {
		return responseType;
	}

	/**
	 * @param responseType The S2CEvent response type to set
	 */
	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	/**
	 * @return The client target plug-in identifier
	 */
	public String getPlugInId() {
		return plugInId;
	}

	/**
	 * @param plugInId The client target plug-in identifier to set
	 */
	public void setPlugInId(String plugInId) {
		this.plugInId = plugInId;
	}

	/**
	 * 
	 * @return The timeout limitation for this s2c event notification, 0 means unlimited
	 */
	public Integer getTimeout() {
		return timeout;
	}

	/**
	 * 
	 * @param timeout The timeout limitation for this s2c event notification, 0 means unlimited
	 */
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
}
