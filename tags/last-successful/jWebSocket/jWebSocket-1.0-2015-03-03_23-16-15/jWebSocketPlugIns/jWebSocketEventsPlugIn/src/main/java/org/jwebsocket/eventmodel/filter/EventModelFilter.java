//	---------------------------------------------------------------------------
//	jWebSocket - EventModelFilter (Community Edition, CE)
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
package org.jwebsocket.eventmodel.filter;

import java.util.Set;
import javolution.util.FastSet;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IEventModelFilter;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ObservableObject;
import org.jwebsocket.eventmodel.observable.ResponseEvent;

/**
 *
 * @author Rolando Santamaria Maso
 */
public abstract class EventModelFilter extends ObservableObject implements IEventModelFilter, IInitializable, IListener {

	private String mId;
	private EventModel mEm;

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void beforeCall(WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void afterCall(WebSocketConnector aConnector, C2SResponseEvent aEvent) throws Exception {
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void initialize() throws Exception {
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void shutdown() throws Exception {
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void processEvent(Event aEvent, ResponseEvent aResponseEvent) {
		System.out.println("Response from '" + this.getClass().getName() + "', please override this method!");
	}

	/**
	 * The collection of events to register in the EventModel component.
	 *
	 * @param aEmEvents The collection of events to register
	 * @throws Exception
	 */
	public void setEmEvents(Set<Class<? extends Event>> aEmEvents) throws Exception {
		getEm().addEvents(aEmEvents);
		getEm().on(aEmEvents, this);
	}

	/**
	 *
	 * @param aEmEvents
	 * @throws Exception
	 */
	public void setEmEventClasses(Set<String> aEmEvents) throws Exception {
		Set lClasses = new FastSet();
		for (String lClass : aEmEvents) {
			lClasses.add(Class.forName(lClass));
		}
		setEmEvents(lClasses);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public String getId() {
		return mId;
	}

	/**
	 * {@inheritDoc }
	 *
	 * @param aId
	 */
	@Override
	public void setId(String aId) {
		this.mId = aId;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public EventModel getEm() {
		return mEm;
	}

	/**
	 * {@inheritDoc }
	 *
	 * @param aEm
	 */
	@Override
	public void setEm(EventModel aEm) {
		this.mEm = aEm;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public String toString() {
		return getId();
	}
}
