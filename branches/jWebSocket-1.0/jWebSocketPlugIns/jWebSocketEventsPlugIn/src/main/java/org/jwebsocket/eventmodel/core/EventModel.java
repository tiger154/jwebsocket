//	---------------------------------------------------------------------------
//	jWebSocket - EventModel (Community Edition, CE)
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
package org.jwebsocket.eventmodel.core;

import java.util.Set;
import javolution.util.FastSet;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IEventModelFilter;
import org.jwebsocket.eventmodel.api.IEventModelPlugIn;
import org.jwebsocket.eventmodel.api.IExceptionHandler;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.cluster.api.IWebSocketClusterNode;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.event.em.*;
import org.jwebsocket.eventmodel.event.filter.BeforeRouteResponseToken;
import org.jwebsocket.eventmodel.exception.*;
import org.jwebsocket.eventmodel.factory.EventFactory;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ObservableObject;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.eventmodel.s2c.S2CEventNotificationHandler;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.events.EventsPlugIn;
import org.jwebsocket.token.Token;

/**
 * The EventsPlugIn core component
 *
 * @author Rolando Santamaria Maso
 */
public class EventModel extends ObservableObject implements IInitializable, IListener {

	private String mEnv = EventModel.DEV_ENV;
	/**
	 *
	 */
	public final static String DEV_ENV = "dev";
	/**
	 *
	 */
	public final static String PROD_ENV = "prod";
	private Set<IEventModelFilter> mFilterChain = new FastSet<IEventModelFilter>();
	private Set<IEventModelPlugIn> mPlugIns = new FastSet<IEventModelPlugIn>();
	private EventsPlugIn mParent;
	private EventFactory mEventFactory;
	private static Logger mLog = Logging.getLogger(EventModel.class);
	private IExceptionHandler mExceptionHandler;
	private S2CEventNotificationHandler mS2CEventNotificationHandler;
	private IWebSocketClusterNode mClusterNode;
	private String mNamespace;
	private int mFragmentSize = 1024 * 5;

	/**
	 *
	 * @return
	 */
	public int getFragmentSize() {
		return mFragmentSize;
	}

	/**
	 *
	 * @param aFragmentSize
	 */
	public void setFragmentSize(int aFragmentSize) {
		this.mFragmentSize = aFragmentSize;
	}

	/**
	 *
	 * @return
	 */
	public String getNamespace() {
		return mNamespace;
	}

	/**
	 *
	 * @param aNamespace
	 */
	public void setNamespace(String aNamespace) {
		this.mNamespace = aNamespace;
	}

	/**
	 *
	 * @param aNamespace
	 * @param aEventFactory
	 * @param aS2CEventNH
	 * @param aExceptionHandler
	 */
	public EventModel(String aNamespace, EventFactory aEventFactory,
			S2CEventNotificationHandler aS2CEventNH, IExceptionHandler aExceptionHandler) {
		super();
		mNamespace = aNamespace;
		mEventFactory = aEventFactory;
		mS2CEventNotificationHandler = aS2CEventNH;
		mExceptionHandler = aExceptionHandler;

		//Setting the EventModel instance on received dependencies
		mEventFactory.setEm(this);
		mS2CEventNotificationHandler.setEm(this);

		//Core events registration
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
		mS2CEventNotificationHandler.initialize();
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
			BeforeProcessEvent lEvent = (BeforeProcessEvent) getEventFactory().idToEvent("before.process.event");
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
			AfterProcessEvent lEvent2 = (AfterProcessEvent) getEventFactory().idToEvent("after.process.event");
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
	 *
	 * @param aFilterChain
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
		mEventFactory = aEventFactory;
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
	 * @return The event model runtime environment "dev" or "prod". Default
	 * value is "dev"
	 */
	public String getEnv() {
		return mEnv;
	}

	/**
	 *
	 * @param aEnv
	 */
	public void setEnv(String aEnv) {
		this.mEnv = aEnv;
	}

	/**
	 *
	 * @return
	 */
	public IWebSocketClusterNode getClusterNode() {
		return mClusterNode;
	}

	/**
	 *
	 * @param aClusterNode
	 */
	public void setClusterNode(IWebSocketClusterNode aClusterNode) {
		this.mClusterNode = aClusterNode;
	}

	/**
	 *
	 * @return
	 */
	public S2CEventNotificationHandler getS2CEventNotificationHandler() {
		return mS2CEventNotificationHandler;
	}

	/**
	 *
	 * @param aS2CEventNotificationHandler
	 */
	public void setS2CEventNotificationHandler(S2CEventNotificationHandler aS2CEventNotificationHandler) {
		this.mS2CEventNotificationHandler = aS2CEventNotificationHandler;
	}
}
