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

	private int code = 0;
	/**
	 * Response is OK
	 */
	public final static int OK = 0;
	/**
	 * Response with problems, not OK
	 */
	public final static int NOT_OK = -1;
	private Set<String> to = new FastSet<String>();
	private String message;
	private String requestId;

	/**
	 * @param requestId The WebSocketEvent arguments MD5
	 */
	public C2SResponseEvent(String requestId) {
		this.requestId = requestId;
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
		return code;
	}

	/**
	 * @param The response status code
	 * <p>
	 * C2SResponseEvent.OK
	 * <br>
	 * C2SResponseEvent.NOT_OK
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * @return The response message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message The response message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return The collection of client connectors to send the response
	 */
	public Set<String> getTo() {
		return to;
	}

	/**
	 * @param to The collection of client connectors to send the response
	 */
	public void setTo(Set<String> to) {
		this.to = to;
	}

	/**
	 * @return The WebSocketEvent arguments MD5
	 */
	public String getRequestId() {
		return requestId;
	}

}
