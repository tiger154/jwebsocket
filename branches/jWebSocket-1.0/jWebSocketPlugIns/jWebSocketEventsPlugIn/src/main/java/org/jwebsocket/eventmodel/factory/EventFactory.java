//  ---------------------------------------------------------------------------
//  jWebSocket - EventFactory
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
package org.jwebsocket.eventmodel.factory;

import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.token.Token;
import org.jwebsocket.eventmodel.event.C2SEventDefinitionManager;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.observable.Event;

/**
 *
 * @author kyberneees
 */
public class EventFactory {

	private EventModel mEm;
	private C2SEventDefinitionManager mEventDefinitions;
	private static Logger mLog = Logging.getLogger(EventFactory.class);

	/**
	 * Convert a Event into a Token
	 * 
	 * @param aEvent The Event to convert
	 * @return The Token
	 */
	public Token eventToToken(Event aEvent) {
		aEvent.getArgs().setType(aEvent.getId());

		return aEvent.getArgs();
	}

	/**
	 * Convert a Token into a Event
	 * 
	 * @param aToken The Token to convert
	 * @return The Event
	 * @throws Exception
	 */
	public C2SEvent tokenToEvent(Token aToken) throws Exception {
		String lType = aToken.getType();
		C2SEvent lEvent = stringToEvent(lType);
		lEvent.setArgs(aToken);

		return lEvent;
	}

	/**
	 * Get a C2SEvent using it identifier
	 * 
	 * @param aEventId The C2SEvent identifier
	 * @return The C2SEvent
	 * @throws Exception
	 */
	public C2SEvent stringToEvent(String aEventId) throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Creating instance for event: '" + aEventId + "'...");
		}
		//Use the BeansFactory to load the instances
		C2SEvent lEvent = (C2SEvent) getEventDefinitions().getDefinition(aEventId).getEventClass().newInstance();
		lEvent.setId(aEventId);

		return lEvent;
	}

	/**
	 * @param aEvent The C2SEvent
	 * @return The string representation of a C2SEvent
	 */
	public String eventToString(C2SEvent aEvent) {
		return aEvent.getId();
	}

	/**
	 * @param aEventClass A Event class
	 * @return The Event string representation
	 * @throws Exception
	 */
	public String eventToString(Class<? extends Event> aEventClass) throws Exception {
		return getEventDefinitions().getIdByClass(aEventClass);
	}

	/**
	 * Create a response event for an incoming C2SEvent
	 * 
	 * @param aEvent The C2SEvent to get the response
	 * @return
	 */
	public C2SResponseEvent createResponseEvent(C2SEvent aEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Creating instance for response event: '" + aEvent.getId() + "'...");
		}
		C2SResponseEvent lResponse = new C2SResponseEvent(aEvent.getRequestId());
		lResponse.setId(aEvent.getId());
		lResponse.setArgs(getEm().getParent().getServer().createResponse(aEvent.getArgs()));

		return lResponse;
	}

	/**
	 * Indicate if exists and event definition using the event identifier
	 *
	 * @param aEventId The event identifier
	 * @return <tt>TRUE</tt> if the event definition exists, <tt>FAlSE</tt> otherwise
	 */
	public boolean hasEventDefinition(String aEventId) {
		return getEventDefinitions().hasDefinition(aEventId);
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
	 * @return The C2SEventDefinitionManager instance 
	 */
	public C2SEventDefinitionManager getEventDefinitions() {
		return mEventDefinitions;
	}

	/**
	 * @param aEventDefinitions The C2SEventDefinitionManager to set
	 */
	public void setEventDefinitions(C2SEventDefinitionManager aEventDefinitions) {
		this.mEventDefinitions = aEventDefinitions;
	}
}
