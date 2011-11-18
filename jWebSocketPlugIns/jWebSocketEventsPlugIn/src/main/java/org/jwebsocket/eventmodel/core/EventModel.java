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
package org.jwebsocket.eventmodel.core;

import org.jwebsocket.eventmodel.api.IEventModelFilter;
import org.jwebsocket.eventmodel.api.IEventModelPlugIn;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.observable.ObservableObject;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.eventmodel.factory.EventFactory;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.token.Token;
import org.jwebsocket.eventmodel.event.em.BeforeProcessEvent;
import org.jwebsocket.eventmodel.event.em.AfterProcessEvent;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import java.util.Set;
import javolution.util.FastSet;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.session.SessionFactory;
import org.jwebsocket.eventmodel.api.IExceptionHandler;
import org.jwebsocket.eventmodel.cluster.api.IWebSocketClusterNode;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.event.em.ConnectorStarted;
import org.jwebsocket.eventmodel.event.em.ConnectorStopped;
import org.jwebsocket.eventmodel.event.em.EngineStarted;
import org.jwebsocket.eventmodel.event.em.EngineStopped;
import org.jwebsocket.eventmodel.event.filter.BeforeRouteResponseToken;
import org.jwebsocket.eventmodel.event.em.S2CEventNotSupportedOnClient;
import org.jwebsocket.eventmodel.event.em.S2CResponse;
import org.jwebsocket.eventmodel.exception.CachedResponseException;
import org.jwebsocket.eventmodel.exception.ExceptionHandler;
import org.jwebsocket.plugins.events.EventsPlugIn;

/**
 *
 * @author kyberneees
 */
public class EventModel extends ObservableObject implements IInitializable, IListener {

	private String env = EventModel.DEV_ENV;
	public final static String DEV_ENV = "dev";
	public final static String PROD_ENV = "prod";
	private Set<IEventModelFilter> filterChain = new FastSet<IEventModelFilter>();
	private Set<IEventModelPlugIn> plugIns = new FastSet<IEventModelPlugIn>();
	private EventsPlugIn parent;
	private EventFactory eventFactory;
	private static Logger mLog = Logging.getLogger(EventModel.class);
	private IExceptionHandler exceptionHandler;
	private SessionFactory sessionFactory;
	private IWebSocketClusterNode clusterNode;

	public EventModel() {
		super();

		//Core Events Registration
		addEvents(ConnectorStarted.class);
		addEvents(ConnectorStopped.class);
		addEvents(BeforeProcessEvent.class);
		addEvents(AfterProcessEvent.class);
		addEvents(EngineStarted.class);
		addEvents(EngineStopped.class);
		addEvents(BeforeRouteResponseToken.class);
		addEvents(S2CResponse.class);
		addEvents(S2CEventNotSupportedOnClient.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() throws Exception {
	}

	/**
	 * 
	 * @return TRUE if the server is a cluster node, FALSE otherwise
	 */
	public boolean isClusterNode() {
		return (null != clusterNode);
	}

	/**
	 * Process all the client incoming events 
	 * 
	 * @param aEvent The event to process
	 * @param aResponseEvent The response event to populate
	 */
	public void processEvent(C2SEvent aEvent, C2SResponseEvent aResponseEvent) {
		try {
			if (null == aResponseEvent) {
				aResponseEvent = getEventFactory().createResponseEvent(aEvent);
			}
			if (mLog.isInfoEnabled()) {
				mLog.info(">> Starting the 'event' workflow...");
			}

			executeFiltersBeforeCall(aEvent.getConnector(), aEvent);

			//"before.process.event" notification
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> 'before.process.event' notification...");
			}
			BeforeProcessEvent e = (BeforeProcessEvent) getEventFactory().stringToEvent("before.process.event");
			e.setEvent(aEvent);
			notify(e, null, true);

			//++++++++++++++ Listeners notification
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Executing EM listeners notifications...");
			}
			C2SEventDefinition def = getEventFactory().getEventDefinitions().getDefinition(aEvent.getId());
			if (def.isNotificationConcurrent()) {
				notify(aEvent, aResponseEvent, true);
			} else {
				notify(aEvent, aResponseEvent, false);
			}

			//"after.process.event" notification
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> 'after.process.event' notification...");
			}
			AfterProcessEvent e2 = (AfterProcessEvent) getEventFactory().stringToEvent("after.process.event");
			e2.setEvent(aEvent);
			notify(e2, aResponseEvent, true);

			executeFiltersAfterCall(aEvent.getConnector(), aResponseEvent);

			if (mLog.isInfoEnabled()) {
				mLog.info(">> The 'event' workflow has finished successfully!");
			}
		} catch (CachedResponseException ex) {
			if (mLog.isInfoEnabled()) {
				mLog.info(">> The response was recovery from cache!");
				mLog.info(">> The 'event' workflow has finished successfully!");
			}
		} catch (Exception ex) {

			//Creating error response for connector notification
			Token aToken = getParent().getServer().createResponse(aEvent.getArgs());
			aToken.setInteger("code", C2SResponseEvent.NOT_OK);
			aToken.setString("msg", ex.toString());

			//Sending the error token...
			getParent().getServer().sendToken(aEvent.getConnector(), aToken);

			if (mLog.isInfoEnabled()) {
				mLog.info(">> The 'event' workflow has finished with errors: " + ex.toString());
			}

			//Calling the exception handler 'process' method
			ExceptionHandler.callProcessException(getExceptionHandler(), ex);

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown() throws Exception {
	}

	/**
	 * Filter chain iteration. Executing before call
	 *
	 * @param aConnector The client WebSocketConnector
	 * @param aEvent The client event to filter
	 * @throws Exception
	 */
	public void executeFiltersBeforeCall(WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
		for (IEventModelFilter f : getFilterChain()) {
			f.beforeCall(aConnector, aEvent);
		}
	}

	/**
	 * Filter chain iteration. Executing after call
	 * 
	 * @param aConnector The client WebSocketConnector
	 * @param aResponseEvent The response event to filter
	 * @throws Exception
	 */
	public void executeFiltersAfterCall(WebSocketConnector aConnector, C2SResponseEvent aResponseEvent) throws Exception {
		int index = getFilterChain().size() - 1;
		while (index >= 0) {
			((IEventModelFilter) getFilterChain().
					toArray()[index]).afterCall(aConnector, aResponseEvent);
			index--;
		}
	}

	/**
	 * Get a EventModelPlugIn using it identifier
	 * 
	 * @param aPlugInId The plug-in identifier
	 * @return The EventModelPlugIn 
	 * @throws IndexOutOfBoundsException
	 */
	public IEventModelPlugIn getPlugIn(String aPlugInId) throws IndexOutOfBoundsException {
		for (IEventModelPlugIn plugIn : getPlugIns()) {
			if (plugIn.getId().equals(aPlugInId)) {
				return plugIn;
			}
		}
		throw new IndexOutOfBoundsException("The plugIn with id: '" + aPlugInId + "', does not exists!");
	}

	/**
	 * Get a EventModelFilter using it identifier
	 *
	 * @param aFilterId The filter identifier
	 * @return The EventModelFilter
	 * @throws IndexOutOfBoundsException
	 */
	public IEventModelFilter getFilter(String aFilterId) throws IndexOutOfBoundsException {
		for (IEventModelFilter filter : getFilterChain()) {
			if (filter.getId().equals(aFilterId)) {
				return filter;
			}
		}
		throw new IndexOutOfBoundsException("The filter with id: " + aFilterId + ", does not exists!");
	}

	/**
	 * @return The EventModelFilter chain
	 */
	public Set<IEventModelFilter> getFilterChain() {
		return filterChain;
	}

	/**
	 * @param filterChain The EventModelFilter chain to set
	 */
	public void setFilterChain(Set<IEventModelFilter> filterChain) {
		this.filterChain.addAll(filterChain);
	}

	/**
	 * @return The EventModelPlugIn collection
	 */
	public Set<IEventModelPlugIn> getPlugIns() {
		return plugIns;
	}

	/**
	 * @param plugIns The EventModelPlugIn collection to set
	 */
	public void setPlugIns(Set<IEventModelPlugIn> plugIns) {
		this.plugIns.addAll(plugIns);
	}

	/**
	 * @return The EventsPlugIn (TokenPlugIn) 
	 */
	public EventsPlugIn getParent() {
		return parent;
	}

	/**
	 * @param parent The EventsPlugIn to set
	 */
	public void setParent(EventsPlugIn parent) {
		this.parent = parent;
	}

	/**
	 * @return The EventFactory
	 */
	public EventFactory getEventFactory() {
		return eventFactory;
	}

	/**
	 * @param eventFactory The EventFactory to set
	 */
	public void setEventFactory(EventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processEvent(Event aEvent, ResponseEvent aResponseEvent) {
		// IListener interface compatibility. Do not delete!
	}

	/**
	 * @return The IExceptionHandler
	 */
	public IExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 * @param exceptionHandler The IExceptionHandler to set
	 */
	public void setExceptionHandler(IExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * 
	 * @return The user session factory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * 
	 * @param The user session factory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * 
	 * @return The event model runtime environment "dev" or "prod". Default value is "dev"
	 */
	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public IWebSocketClusterNode getClusterNode() {
		return clusterNode;
	}

	public void setClusterNode(IWebSocketClusterNode clusterNode) {
		this.clusterNode = clusterNode;
	}
}
