//	---------------------------------------------------------------------------
//	jWebSocket - Event (Community Edition, CE)
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
package org.jwebsocket.eventmodel.observable;

import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Rolando Santamaria Maso
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
	 *
	 * @param aId
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
	 *
	 * @param aObj
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
