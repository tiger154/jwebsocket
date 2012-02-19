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
import org.jwebsocket.eventmodel.exception.ListenerNotFoundException;
import org.jwebsocket.eventmodel.exception.NotAuthorizedException;
import org.jwebsocket.eventmodel.exception.ValidatorException;
import org.jwebsocket.plugins.events.EventsPlugIn;
import org.jwebsocket.session.SessionManager;

/**
 *
 * @author kyberneees
 */
public class EventModel extends ObservableObject implements IInitializable, IListener {

	private String mEnv = EventModel.DEV_ENV;
	public final static String DEV_ENV = "dev";
	public final static String PROD_ENV = "prod";
	private Set<IEventModelFilter> mFilterChain = new FastSet<IEventModelFilter>();
	private Set<IEventModelPlugIn> mPlugIns = new FastSet<IEventModelPlugIn>();
	private EventsPlugIn mParent;
	private EventFactory mEventFactory;
	private static Logger mLog = Logging.getLogger(EventModel.class);
	private IExceptionHandler mExceptionHandler;
	private IWebSocketClusterNode mClusterNode;
	private String mNamespace;

	public String getNamespace() {
		return mNamespace;
	}

	public void setNamespace(String aNamespace) {
		this.mNamespace = aNamespace;
	}

	public EventModel(String aNamespace) {
		super();
		this.mNamespace = aNamespace;

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
		return (null != mClusterNode);
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
				mLog.info("Starting the 'event' workflow...");
			}

			executeFiltersBeforeCall(aEvent.getConnector(), aEvent);

			//"before.process.event" notification
			if (mLog.isDebugEnabled()) {
				mLog.debug("'before.process.event' notification...");
			}
			BeforeProcessEvent lEvent = (BeforeProcessEvent) getEventFactory().stringToEvent("before.process.event");
			lEvent.setEvent(aEvent);
			notify(lEvent, null, true);

			//++++++++++++++ Listeners notification
			if (mLog.isDebugEnabled()) {
				mLog.debug("Executing EM listeners notifications...");
			}
			C2SEventDefinition lDef = getEventFactory().getEventDefinitions().getDefinition(aEvent.getId());
			if (lDef.isNotificationConcurrent()) {
				notify(aEvent, aResponseEvent, true);
			} else {
				notify(aEvent, aResponseEvent, false);
			}

			//"after.process.event" notification
			if (mLog.isDebugEnabled()) {
				mLog.debug("'after.process.event' notification...");
			}
			AfterProcessEvent lEvent2 = (AfterProcessEvent) getEventFactory().stringToEvent("after.process.event");
			lEvent2.setEvent(aEvent);
			notify(lEvent2, aResponseEvent, true);

			executeFiltersAfterCall(aEvent.getConnector(), aResponseEvent);

			if (mLog.isInfoEnabled()) {
				mLog.info("The 'event' workflow has finished successfully!");
			}
		} catch (CachedResponseException ex) {
			if (mLog.isInfoEnabled()) {
				mLog.info("The response was recovery from cache!");
				mLog.info("The 'event' workflow has finished successfully!");
			}
		} catch (Exception ex) {

			//Creating error response for connector notification
			Token lToken = getParent().getServer().createResponse(aEvent.getArgs());

			//Parsing server the response code
			if (ex instanceof NotAuthorizedException) {
				lToken.setInteger("code", C2SResponseEvent.NOT_AUTHORIZED);
			} else if (ex instanceof ValidatorException) {
				lToken.setInteger("code", C2SResponseEvent.VALIDATION_FAILED);
			} else if (ex instanceof ListenerNotFoundException) {
				lToken.setInteger("code", C2SResponseEvent.C2SEVENT_WITHOUT_LISTENERS);
			} else {
				lToken.setInteger("code", C2SResponseEvent.UNDEFINED_SERVER_ERROR);
			}

			lToken.setNS(getParent().getNamespace());
			lToken.setString("msg", ex.getMessage());
			lToken.setString("exception", ex.getClass().getSimpleName());

			//Sending the error token...
			getParent().getServer().sendToken(aEvent.getConnector(), lToken);

			if (mLog.isInfoEnabled()) {
				mLog.info("The 'event' workflow has finished with errors: " + ex.toString());
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
		for (IEventModelFilter lFilter : getFilterChain()) {
			lFilter.beforeCall(aConnector, aEvent);
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
		int lIndex = getFilterChain().size() - 1;
		while (lIndex >= 0) {
			((IEventModelFilter) getFilterChain().
					toArray()[lIndex]).afterCall(aConnector, aResponseEvent);
			lIndex--;
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
		for (IEventModelPlugIn lPlugIn : getPlugIns()) {
			if (lPlugIn.getId().equals(aPlugInId)) {
				return lPlugIn;
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
		for (IEventModelFilter lFilter : getFilterChain()) {
			if (lFilter.getId().equals(aFilterId)) {
				return lFilter;
			}
		}
		throw new IndexOutOfBoundsException("The filter with id: " + aFilterId + ", does not exists!");
	}

	/**
	 * @return The EventModelFilter chain
	 */
	public Set<IEventModelFilter> getFilterChain() {
		return mFilterChain;
	}

	/**
	 * @param filterChain The EventModelFilter chain to set
	 */
	public void setFilterChain(Set<IEventModelFilter> aFilterChain) {
		this.mFilterChain.addAll(aFilterChain);
	}

	/**
	 * @return The EventModelPlugIn collection
	 */
	public Set<IEventModelPlugIn> getPlugIns() {
		return mPlugIns;
	}

	/**
	 * @param aPlugIns The EventModelPlugIn collection to set
	 */
	public void setPlugIns(Set<IEventModelPlugIn> aPlugIns) {
		this.mPlugIns.addAll(aPlugIns);
	}

	/**
	 * @return The EventsPlugIn (TokenPlugIn) 
	 */
	public EventsPlugIn getParent() {
		return mParent;
	}

	/**
	 * @param aParent The EventsPlugIn to set
	 */
	public void setParent(EventsPlugIn aParent) {
		this.mParent = aParent;
	}

	/**
	 * @return The EventFactory
	 */
	public EventFactory getEventFactory() {
		return mEventFactory;
	}

	/**
	 * @param aEventFactory The EventFactory to set
	 */
	public void setEventFactory(EventFactory aEventFactory) {
		this.mEventFactory = aEventFactory;
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
		return mExceptionHandler;
	}

	/**
	 * @param aExceptionHandler The IExceptionHandler to set
	 */
	public void setExceptionHandler(IExceptionHandler aExceptionHandler) {
		this.mExceptionHandler = aExceptionHandler;
	}

	/**
	 * 
	 * @return The user session factory
	 */
	public SessionManager getSessionFactory() {
		return (SessionManager) getParent().getBeanFactory().getBean(SessionManager.class);
	}

	/**
	 * 
	 * @return The event model runtime environment "dev" or "prod". Default value is "dev"
	 */
	public String getEnv() {
		return mEnv;
	}

	public void setEnv(String aEnv) {
		this.mEnv = aEnv;
	}

	public IWebSocketClusterNode getClusterNode() {
		return mClusterNode;
	}

	public void setClusterNode(IWebSocketClusterNode aClusterNode) {
		this.mClusterNode = aClusterNode;
	}
}
