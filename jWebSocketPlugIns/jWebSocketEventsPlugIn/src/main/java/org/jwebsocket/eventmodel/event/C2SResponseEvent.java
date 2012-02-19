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

import org.jwebsocket.eventmodel.observable.ResponseEvent;
import javolution.util.FastSet;
import java.util.Set;

/**
 *
 * @author kyberneees
 */
public class C2SResponseEvent extends ResponseEvent {

	private int mCode = 0;
	/**
	 * Response is OK
	 */
	public final static int OK = 0;
	/**
	 * Response with errors
	 */
	public final static int UNDEFINED_SERVER_ERROR = -1;
	/**
	 * Not authorized to notify the event
	 */
	public final static int NOT_AUTHORIZED = -2;
	/**
	 * The event has not listeners in the server
	 */
	public final static int C2SEVENT_WITHOUT_LISTENERS = -3;
	/**
	 * The validation process has failed
	 */
	public final static int VALIDATION_FAILED = -4;
	
	private Set<String> mTo = new FastSet();
	private String mMessage;
	private String mRequestId;

	/**
	 * @param aRequestId The WebSocketEvent arguments MD5
	 */
	public C2SResponseEvent(String aRequestId) {
		this.mRequestId = aRequestId;
	}

	/**
	 * 
	 */
	public C2SResponseEvent() {
	}

	/**
	 * The response code indicates the response status. 
	 * <p>
	 * C2SResponseEvent.OK
	 * <br>
	 * C2SResponseEvent.NOT_OK
	 * 
	 * @return The response status code
	 */
	public int getCode() {
		return mCode;
	}

	/**
	 * @param The response status code
	 * <p>
	 * C2SResponseEvent.OK
	 * <br>
	 * C2SResponseEvent.NOT_OK
	 */
	public void setCode(int aCode) {
		this.mCode = aCode;
	}

	/**
	 * @return The response message
	 */
	public String getMessage() {
		return mMessage;
	}

	/**
	 * @param aMessage The response message
	 */
	public void setMessage(String aMessage) {
		this.mMessage = aMessage;
	}

	/**
	 * @return The collection of client connectors to send the response
	 */
	public Set<String> getTo() {
		return mTo;
	}

	/**
	 * @param aTo The collection of client connectors to send the response
	 */
	public void setTo(Set<String> aTo) {
		this.mTo = aTo;
	}

	/**
	 * @return The WebSocketEvent arguments MD5
	 */
	public String getRequestId() {
		return mRequestId;
	}
}
