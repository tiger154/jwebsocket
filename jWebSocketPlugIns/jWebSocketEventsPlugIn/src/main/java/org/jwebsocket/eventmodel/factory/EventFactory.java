//	---------------------------------------------------------------------------
//	jWebSocket - EventFactory (Community Edition, CE)
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
package org.jwebsocket.eventmodel.factory;

import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.api.IC2SEventDefinitionManager;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class EventFactory {

	private EventModel mEm;
	private IC2SEventDefinitionManager mEventDefinitions;
	private static Logger mLog = Logging.getLogger(EventFactory.class);

	/**
	 * Convert a Event into a Token
	 *
	 * @param aEvent The Event to convert
	 * @return Returns the Token representation of a given Event instance.
	 */
	public Token eventToToken(Event aEvent) {
		aEvent.getArgs().setType(aEvent.getId());

		return aEvent.getArgs();
	}

	/**
	 * Convert a Token into a Event
	 *
	 * @param aToken The Token to convert
	 * @return Returns the C2SEvent representation of a given Token instance.
	 * @throws Exception
	 */
	public C2SEvent tokenToEvent(Token aToken) throws Exception {
		C2SEvent lEvent = idToEvent(aToken.getType());
		lEvent.setArgs(aToken);

		return lEvent;
	}

	/**
	 * Get a C2SEvent using it identifier
	 *
	 * @param aEventId The C2SEvent identifier
	 * @return Returns a new instance of the C2SEvent that matches the given
	 * event identifier.
	 * @throws Exception
	 */
	public C2SEvent idToEvent(String aEventId) throws Exception {
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
	 * @return Returns the event identifier of the given C2SEvent instance.
	 */
	public String eventToId(C2SEvent aEvent) {
		return aEvent.getId();
	}

	/**
	 * @param aEventClass A Event class
	 * @return Returns the identifier of the event that matches the given event
	 * class.
	 * @throws Exception
	 */
	public String eventClassToEventId(Class<? extends Event> aEventClass) throws Exception {
		return getEventDefinitions().getIdByClass(aEventClass);
	}

	/**
	 * Create a response event for an incoming C2SEvent
	 *
	 * @param aEvent The C2SEvent to get the response
	 * @return Create a response event for a given C2SEvent instance.
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
	 * @return Returns TRUE if exists a C2SEventDefinition that matches the
	 * given identifier, FALSE otherwise.
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
	public IC2SEventDefinitionManager getEventDefinitions() {
		return mEventDefinitions;
	}

	/**
	 * @param aEventDefinitions The C2SEventDefinitionManager to set
	 */
	public void setEventDefinitions(IC2SEventDefinitionManager aEventDefinitions) {
		this.mEventDefinitions = aEventDefinitions;
	}
}
