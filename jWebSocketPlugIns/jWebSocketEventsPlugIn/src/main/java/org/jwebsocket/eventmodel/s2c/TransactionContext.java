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
package org.jwebsocket.eventmodel.s2c;

import java.util.Map;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.token.Token;

/**
 * The Transaction context is a collection of resources used to success
 * back to the target client the response on a S2C call
 *
 * @author kyberneees
 */
public class TransactionContext {

	private EventModel mEm;
	private C2SEvent mEvent;
	private Map<String, Object> mObjects;
	private double mProcessingTime;
	private double mElapsedTime;

	/**
	 * Create a Transaction context
	 *
	 * The Transaction context is a collection of resources used to success
	 * back to the target client the response on a S2C call.
	 *
	 * @param aEm The EventModel instance
	 * @param aEvent The event from the client
	 * @param aObjects Other objects references
	 */
	public TransactionContext(EventModel aEm, C2SEvent aEvent, Map<String, Object> aObjects) {
		this.mEm = aEm;
		this.mEvent = aEvent;
		this.mObjects = aObjects;
	}

	/**
	 * @return The EventModel instance
	 */
	public EventModel getEm() {
		return mEm;
	}

	/**
	 * @param aEm The EventModel instance to set
	 */
	public void setEm(EventModel aEm) {
		this.mEm = aEm;
	}

	/**
	 * @return Objects collection to use by the callbacks
	 */
	public Map<String, Object> getObjects() {
		return mObjects;
	}

	/**
	 * @param aObjects Useful objects collection to use by the callbacks
	 */
	public void setObjects(Map<String, Object> aObjects) {
		this.mObjects = aObjects;
	}

	/**
	 * @return The event from the client
	 */
	public C2SEvent getEvent() {
		return mEvent;
	}

	/**
	 * @param aEvent The event from the client to set
	 */
	public void setEvent(C2SEvent aEvent) {
		this.mEvent = aEvent;
	}

	/**
	 * Notify the sender client about the success transaction
	 * 
	 * @param aResponse The response from the target client
	 */
	@SuppressWarnings("unchecked")
	public void success(Object aResponse) {
		C2SResponseEvent lResponseEvent = getEm().getEventFactory().createResponseEvent(mEvent);

		//Send the token to the client(s)
		Token lToken = lResponseEvent.getArgs();
		lToken.setInteger("code", C2SResponseEvent.OK);
		if (null != aResponse){
			lToken.getMap().put("response", aResponse);
		}
		lToken.setDouble("_pt", getProcessingTime());

		getEm().getParent().getServer().sendToken(mEvent.getConnector(), lToken);
	}
	
	/**
	 * Notify the sender client about the success transaction
	 */
	public void success() {
		success(null);
	}
	
	/**
	 * Notify the sender client about the failure transaction
	 * 
	 * @param reason Failure reason
	 * @param message Custom failure message
	 */
	public void failure(FailureReason reason, String message){
		C2SResponseEvent lResponseEvent = getEm().getEventFactory().createResponseEvent(mEvent);

		//Send the token to the client(s)
		Token lToken = lResponseEvent.getArgs();
		lToken.setInteger("code", C2SResponseEvent.UNDEFINED_SERVER_ERROR);
		lToken.setString("msg", message);
		lToken.setString("reason", reason.name());

		getEm().getParent().getServer().sendToken(mEvent.getConnector(), lToken);
	}

	/**
	 * @return Time required by the client to process the event
	 * <p>
	 * Time unit in nanoseconds or milliseconds depending of the client
	 */
	public double getProcessingTime() {
		return mProcessingTime;
	}

	/**
	 * @param aProcessingTime Time required by the client to process the event
	 */
	public void setProcessingTime(double aProcessingTime) {
		this.mProcessingTime = aProcessingTime;
	}

	/**
	 * @return The complete time in nanoseconds passed from the "sent" time mark to 
	 * the "response received" time mark
	 */
	public double getElapsedTime() {
		return mElapsedTime;
	}

	/**
	 * @param aElapsedTime The complete time in nanoseconds passed from the "sent" 
	 * time mark to the "response received" time mark
	 */
	public void setElapsedTime(double aElapsedTime) {
		this.mElapsedTime = aElapsedTime;
	}
}
