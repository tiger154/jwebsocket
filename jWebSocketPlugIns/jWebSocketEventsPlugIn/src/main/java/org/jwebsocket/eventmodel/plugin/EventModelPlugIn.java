//  ---------------------------------------------------------------------------
//  jWebSocket - EventModelPlugIn
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
package org.jwebsocket.eventmodel.plugin;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IEventModelPlugIn;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ObservableObject;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.eventmodel.s2c.S2CEventNotification;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author kyberneees
 */
public abstract class EventModelPlugIn extends ObservableObject implements IEventModelPlugIn {

	private String mId;
	private EventModel mEm;
	private Map<String, Class<? extends Event>> mClientAPI;
	private static Logger mLog = Logging.getLogger(EventModelPlugIn.class);

	/**
	 * {@inheritDoc }
	 *
	 * @throws Exception
	 */
	@Override
	public void initialize() throws Exception {
	}

	/**
	 * Short-cut to set the plug-in events definitions
	 *
	 * @param aDefs The plug-in events definitions
	 */
	public void setEventsDefinitions(Set<C2SEventDefinition> aDefs) {
		getEm().getEventFactory().getEventDefinitions().getDefinitions().addAll(aDefs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processEvent(Event aEvent, ResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Response from '" + this.getClass().getName() + "', please override this method!");
		}
	}

	@Override
	public Map<String, WebSocketConnector> getServerAllConnectors() {
		return this.getEm().getParent().getServer().getAllConnectors();
	}

	/**
	 *
	 * @throws Exception
	 */
	@Override
	public void shutdown() throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public S2CEventNotification notifyS2CEvent(S2CEvent aEvent) {
		return new S2CEventNotification(this.getId(), aEvent, getEm().getS2CEventNotificationHandler());
	}

	/**
	 * Register the events in the EventModel subject and the plug-in as a
	 * listener for them
	 *
	 * @param aEmEvents The events to register
	 * @throws Exception
	 */
	public void setEmEvents(Collection<Class<? extends Event>> aEmEvents) throws Exception {
		getEm().addEvents(aEmEvents);
		getEm().on(aEmEvents, this);
	}

	public void setEmEventClasses(Set<String> aEmEvents) throws Exception {
		Set lClasses = new FastSet();
		for (String lClass : aEmEvents) {
			lClasses.add(Class.forName(lClass));
		}
		setEmEvents(lClasses);
	}

	/**
	 * Event Model events registration and client API definition
	 *
	 * @param aEmEvents
	 * @throws Exception
	 */
	public void setEmEventsAndClientAPI(Map<String, Class<? extends Event>> aEmEvents) throws Exception {
		setClientAPI(aEmEvents);
		getEm().addEvents(aEmEvents.values());
		getEm().on(aEmEvents.values(), this);
	}

	public void setEmEventClassesAndClientAPI(Map<String, String> aEmEvents) throws Exception {
		Map lClasses = new FastMap<String, Class>();
		for (String lKey : aEmEvents.keySet()) {
			try {
				lClasses.put(lKey, Class.forName(aEmEvents.get(lKey)));
			} catch (ClassNotFoundException ex) {
				mLog.error("Exception setting the client API for plug-in '"
						+ getId() + "'. Class not found: '" + aEmEvents.get(lKey) + "'!");
				throw ex;
			}
		}
		setEmEventsAndClientAPI(lClasses);
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
	public Map<String, Class<? extends Event>> getClientAPI() {
		return mClientAPI;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void setClientAPI(Map<String, Class<? extends Event>> aClientAPI) {
		this.mClientAPI = aClientAPI;
	}

	/**
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
	public void readFromToken(Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void writeToToken(Token aToken) {
		Map lApi = new FastMap();
		Token lTokenEventDef;
		C2SEventDefinition lEventDef;
		for (String lKey : getClientAPI().keySet()) {
			try {
				String aEventId = getEm().getEventFactory().eventToString(getClientAPI().get(lKey));
				lEventDef = getEm().getEventFactory().getEventDefinitions().getDefinition(aEventId);

				lTokenEventDef = TokenFactory.createToken();
				lEventDef.writeToToken(lTokenEventDef);

				lApi.put(lKey, lTokenEventDef.getMap());
			} catch (Exception ex) {
				mLog.debug(ex.getMessage(), ex);
			}
		}

		aToken.setString("id", getId());
		aToken.setMap("api", lApi);
	}
}
