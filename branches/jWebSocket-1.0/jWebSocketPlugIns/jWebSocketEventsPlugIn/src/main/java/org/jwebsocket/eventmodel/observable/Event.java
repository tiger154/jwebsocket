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
package org.jwebsocket.eventmodel.observable;

import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author kyberneees
 */
public class Event {

	private String id;
	private Token args = TokenFactory.createToken();
	private Object subject;
	private boolean processed = false;

	/**
	 *
	 */
	public Event() {
	}

	/**
	 *
	 * @param The Event identifier
	 */
	public Event(String aId) {
		id = aId;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public String toString() {
		return getId();
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public int hashCode() {
		return (null == id) ? 0 : id.hashCode()
				+ ((processed) ? 1 : 0)
				+ args.hashCode()
				+ ((null == subject) ? 0 : subject.hashCode());
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Event other = (Event) obj;
		if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId())) {
			return false;
		}
		if (this.args != other.getArgs() && (this.getArgs() == null || !this.getArgs().equals(other.getArgs()))) {
			return false;
		}
		if (this.subject != other.getSubject() && (this.subject == null || !this.subject.equals(other.getSubject()))) {
			return false;
		}
		if (this.processed != other.isProcessed()) {
			return false;
		}
		return true;
	}

	/**
	 * @return The Event identifier
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id The Event identifier
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return The Event arguments
	 */
	public Token getArgs() {
		return args;
	}

	/**
	 * @param args The Event arguments to set
	 */
	public void setArgs(Token args) {
		this.args = args;
	}

	/**
	 * @return The subject that fired the Event
	 */
	public Object getSubject() {
		return subject;
	}

	/**
	 * @param subject The subject that fired the Event
	 */
	public void setSubject(Object subject) {
		this.subject = subject;
	}

	/**
	 * Indicate if the Event was processed by a listener
	 * 
	 * @return the processed
	 */
	public boolean isProcessed() {
		return processed;
	}

	/**
	 * @param processed Indicate if the Event was processed by a listener
	 */
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}
}
