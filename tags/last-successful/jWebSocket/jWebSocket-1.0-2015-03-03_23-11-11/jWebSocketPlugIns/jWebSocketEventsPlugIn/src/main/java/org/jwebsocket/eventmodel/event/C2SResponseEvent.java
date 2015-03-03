//	---------------------------------------------------------------------------
//	jWebSocket - C2SResponseEvent (Community Edition, CE)
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
package org.jwebsocket.eventmodel.event;

import java.util.Set;
import javolution.util.FastSet;
import org.jwebsocket.eventmodel.observable.ResponseEvent;

/**
 *
 * @author Rolando Santamaria Maso
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
	 * @param aCode
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
