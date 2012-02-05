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
package org.jwebsocket.eventmodel.filter;

import java.util.Set;
import javolution.util.FastSet;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.eventmodel.observable.ObservableObject;
import org.jwebsocket.eventmodel.api.IEventModelFilter;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ResponseEvent;

/**
 *
 * @author kyberneees
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

	public void setEmEventClasses(Set<String> aEmEvents) throws Exception {
		Set lClasses = new FastSet();
		for (String lClass : aEmEvents) {
			lClasses.add(Class.forName(lClass));
		}
		getEm().addEvents(lClasses);
		getEm().on(lClasses, this);
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
