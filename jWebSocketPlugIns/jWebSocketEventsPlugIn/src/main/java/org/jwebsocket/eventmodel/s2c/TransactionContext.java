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

	private EventModel em;
	private C2SEvent event;
	private Map<String, Object> objects;
	private double processingTime;
	private double elapsedTime;

	/**
	 * Create a Transaction context
	 *
	 * The Transaction context is a collection of resources used to success
	 * back to the target client the response on a S2C call.
	 *
	 * @param em The EventModel instance
	 * @param event The event from the client
	 * @param objects Other objects references
	 */
	public TransactionContext(EventModel em, C2SEvent event, Map<String, Object> objects) {
		this.em = em;
		this.event = event;
		this.objects = objects;
	}

	/**
	 * @return The EventModel instance
	 */
	public EventModel getEm() {
		return em;
	}

	/**
	 * @param em The EventModel instance to set
	 */
	public void setEm(EventModel em) {
		this.em = em;
	}

	/**
	 * @return Objects collection to use by the callbacks
	 */
	public Map<String, Object> getObjects() {
		return objects;
	}

	/**
	 * @param objects Useful objects collection to use by the callbacks
	 */
	public void setObjects(Map<String, Object> objects) {
		this.objects = objects;
	}

	/**
	 * @return The event from the client
	 */
	public C2SEvent getEvent() {
		return event;
	}

	/**
	 * @param event The event from the client to set
	 */
	public void setEvent(C2SEvent event) {
		this.event = event;
	}

	/**
	 * Notify the sender client about the success transaction
	 * 
	 * @param response The response from the target client
	 */
	@SuppressWarnings("unchecked")
	public void success(Object response) {
		C2SResponseEvent r = getEm().getEventFactory().createResponseEvent(event);

		//Send the token to the client(s)
		Token aToken = r.getArgs();
		aToken.setInteger("code", C2SResponseEvent.OK);
		if (null != response){
			aToken.getMap().put("response", response);
		}
		aToken.setDouble("processingTime", getProcessingTime());
		aToken.setDouble("elapsedTime", getElapsedTime());

		getEm().getParent().getServer().sendToken(event.getConnector(), aToken);
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
		C2SResponseEvent r = getEm().getEventFactory().createResponseEvent(event);

		//Send the token to the client(s)
		Token aToken = r.getArgs();
		aToken.setInteger("code", C2SResponseEvent.NOT_OK);
		aToken.setString("msg", message);
		aToken.setString("reason", reason.name());
		aToken.setDouble("elapsedTime", getElapsedTime());

		getEm().getParent().getServer().sendToken(event.getConnector(), aToken);
	}

	/**
	 * @return Time required by the client to process the event
	 * <p>
	 * Time unit in nanoseconds or milliseconds depending of the client
	 */
	public double getProcessingTime() {
		return processingTime;
	}

	/**
	 * @param processingTime Time required by the client to process the event
	 */
	public void setProcessingTime(double processingTime) {
		this.processingTime = processingTime;
	}

	/**
	 * @return The complete time in nanoseconds passed from the "sent" time mark to 
	 * the "response received" time mark
	 */
	public double getElapsedTime() {
		return elapsedTime;
	}

	/**
	 * @param elapsedTime The complete time in nanoseconds passed from the "sent" 
	 * time mark to the "response received" time mark
	 */
	public void setElapsedTime(double elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
}
