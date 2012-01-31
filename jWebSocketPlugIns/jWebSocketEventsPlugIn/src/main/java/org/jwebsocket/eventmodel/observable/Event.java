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

	private String mId;
	private Token mArgs = TokenFactory.createToken();
	private Object mSubject;
	private boolean mProcessed = false;

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
		mId = aId;
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
		return (null == mId) ? 0 : mId.hashCode()
				+ ((mProcessed) ? 1 : 0)
				+ mArgs.hashCode()
				+ ((null == mSubject) ? 0 : mSubject.hashCode());
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public boolean equals(Object aObj) {
		if (aObj == null) {
			return false;
		}
		if (getClass() != aObj.getClass()) {
			return false;
		}
		final Event other = (Event) aObj;
		if ((this.mId == null) ? (other.getId() != null) : !this.mId.equals(other.getId())) {
			return false;
		}
		if (this.mArgs != other.getArgs() && (this.getArgs() == null || !this.getArgs().equals(other.getArgs()))) {
			return false;
		}
		if (this.mSubject != other.getSubject() && (this.mSubject == null || !this.mSubject.equals(other.getSubject()))) {
			return false;
		}
		if (this.mProcessed != other.isProcessed()) {
			return false;
		}
		return true;
	}

	/**
	 * @return The Event identifier
	 */
	public String getId() {
		return mId;
	}

	/**
	 * @param aId The Event identifier
	 */
	public void setId(String aId) {
		this.mId = aId;
	}

	/**
	 * @return The Event arguments
	 */
	public Token getArgs() {
		return mArgs;
	}

	/**
	 * @param aArgs The Event arguments to set
	 */
	public void setArgs(Token aArgs) {
		this.mArgs = aArgs;
	}

	/**
	 * @return The subject that fired the Event
	 */
	public Object getSubject() {
		return mSubject;
	}

	/**
	 * @param aSubject The subject that fired the Event
	 */
	public void setSubject(Object aSubject) {
		this.mSubject = aSubject;
	}

	/**
	 * Indicate if the Event was processed by a listener
	 * 
	 * @return the processed
	 */
	public boolean isProcessed() {
		return mProcessed;
	}

	/**
	 * @param aProcessed Indicate if the Event was processed by a listener
	 */
	public void setProcessed(boolean aProcessed) {
		this.mProcessed = aProcessed;
	}
}
