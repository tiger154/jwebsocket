//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
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
import java.util.Timer;
import org.apache.log4j.Logger;
import javolution.util.FastMap;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.event.em.ConnectorStopped;
import org.jwebsocket.eventmodel.event.em.S2CEventNotSupportedOnClient;
import org.jwebsocket.eventmodel.event.em.S2CResponse;
import org.jwebsocket.eventmodel.exception.MissingTokenSender;
import org.jwebsocket.eventmodel.filter.validator.TypesMap;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author kyberneees
 */
public class S2CEventNotificationHandler implements IInitializable, IListener {

	private static Logger mLog = Logging.getLogger(S2CEventNotificationHandler.class);
	private Integer uid = 0;
	private EventModel em;
	private TypesMap typesMap;
	private FastMap<String, FastMap<String, OnResponse>> callsMap = new FastMap<String, FastMap<String, OnResponse>>();
	private Timer timeoutHandler;

	/**
	 * Send an event to the client
	 *
	 * @param aEvent The S2CEvent to send
	 * @param to The destiny client connector
	 * @param aOnResponse The server on-response callbacks
	 */
	public void send(S2CEvent aEvent, String to, OnResponse aOnResponse) throws MissingTokenSender {
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Preparing S2C event notification...");
		}

		if (!getCallsMap().containsKey(to)) {
			getCallsMap().put(to, new FastMap<String, OnResponse>());
		}

		//Creating the token
		Token token = TokenFactory.createToken(getEm().getParent().getNamespace(), "s2c.event_notification");
		aEvent.writeToToken(token);
		aEvent.writeParentToToken(token);
		token.setString("uid", getNextUID());

		//Saving the callback
		if (null != aOnResponse) {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Saving the OnResponse callback for the event '" + aEvent.getId() + "'...");
			}
			//Saving the callback
			aOnResponse.setRequiredType(aEvent.getResponseType());
			getCallsMap().get(to).put(token.getString("uid"), aOnResponse);
			//Setting the send time
			aOnResponse.setSentTime(System.nanoTime());

			//2CEvent have a callback
			token.setBoolean("has_callback", true);

			//Registering timeout callbacks
			if (aEvent.getTimeout() > 0) {
				timeoutHandler.schedule(new TimeoutCallbackTask(to, token.getString("uid"), this), aEvent.getTimeout());
			}
		} else {
			//S2CEvent don't have a callback
			token.setBoolean("has_callback", false);
		}

		//Sending the token
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Sending S2C event notification to '" + to + "' connector...");
		}

		//Getting the local WebSocketConnector instance if exists
		WebSocketConnector c = getEm().getParent().getServer().getConnector(to);

		if (null != c) {
			//Sending locally on the server
			getEm().getParent().getServer().sendToken(c, token);
		} else if (getEm().isClusterNode()) {
			//Sending the token to the cluster network
			getEm().getClusterNode().sendToken(to, token);
		} else {
			throw new MissingTokenSender("Not engine or cluster detected to send "
					+ "the token to the giving connector: '" + to + "'!");
		}
	}

	/**
	 * Send an event to the client
	 *
	 * @param aEvent The S2CEvent to send
	 * @param to The destiny client connector
	 * @param aOnResponse The server on-response callbacks
	 */
	public void send(S2CEvent aEvent, WebSocketConnector to, OnResponse aOnResponse) throws MissingTokenSender {
		send(aEvent, to.getId(), aOnResponse);
	}

	/**
	 * Executes the OnResponse callback appropriate method when a Response is gotten from the client
	 *
	 * @param aEvent The response event from the client
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(S2CResponse aEvent, C2SResponseEvent aResponseEvent) throws Exception {

		String connector_id = aEvent.getConnector().getId();
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Processing S2CResponse(" + aEvent.getReqId()
					+ ") from '" + connector_id + "' connector...");
		}

		//Getting the response
		if (aEvent.getArgs().getMap().containsKey("response")) {
			aEvent.setResponse(aEvent.getArgs().getObject("response"));
		}

		//If a callback is pending for this response
		if (getCallsMap().containsKey(connector_id) && getCallsMap().get(connector_id).containsKey(aEvent.getReqId())) {
			//Getting the OnResponse callback
			OnResponse aOnResponse = getCallsMap().get(connector_id).remove(aEvent.getReqId());

			//Setting the processing time
			aOnResponse.setProcessingTime(aEvent.getProcessingTime());

			//Cleaning if empty
			if (getCallsMap().get(connector_id).isEmpty()) {
				getCallsMap().remove(connector_id);
			}

			//Executing the validation process...
			if (!aOnResponse.getRequiredType().equals("void")) {
				//Validating the response
				if (getTypesMap().swapType(aOnResponse.getRequiredType()).isInstance(aEvent.getResponse())
						&& aOnResponse.isValid(aEvent.getResponse(), connector_id)) {
					aOnResponse.setElapsedTime(System.nanoTime() - aOnResponse.getSentTime());
					aOnResponse.success(aEvent.getResponse(), connector_id);
				} else {
					aOnResponse.setElapsedTime(System.nanoTime() - aOnResponse.getSentTime());
					aOnResponse.failure(FailureReason.INVALID_RESPONSE, connector_id);
				}
			} else {
				aOnResponse.setElapsedTime(System.nanoTime() - aOnResponse.getSentTime());
				aOnResponse.success(null, connector_id);
			}
		} else {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> The S2CResponse(" + aEvent.getReqId()
						+ ") from '" + aEvent.getConnector().getId() + "' has not pending callbacks!");
			}
		}
	}

	/**
	 * Removing pending callbacks when a client gets disconnected
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(ConnectorStopped aEvent, ResponseEvent aResponseEvent) {
		String connector_id = aEvent.getConnector().getId();
		if (getCallsMap().containsKey(aEvent.getConnector().getId())) {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Removing pending callbacks for '" + connector_id + "' connector...");
			}

			//Getting pending callbacks and removing
			FastMap<String, OnResponse> pending_calls = getCallsMap().remove(connector_id);

			double currentTime = System.nanoTime();

			for (Map.Entry<String, OnResponse> e : pending_calls.entrySet()) {
				//Updating the  elapsed time
				e.getValue().setElapsedTime(currentTime - e.getValue().getSentTime());
				//Calling the failure method
				e.getValue().failure(FailureReason.CONNECTOR_STOPPED, connector_id);
			}
		}
	}

	/**
	 * Event fired when the client does not support the S2C event from the server
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(S2CEventNotSupportedOnClient aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Processing the 'S2CEventNotSupportedOnClient' event...");
		}

		//Caching the connector connector_id for performance
		String connector_id = aEvent.getConnector().getId();

		//Removing only if a callback is pending
		if (getCallsMap().containsKey(connector_id)
				&& getCallsMap().get(connector_id).containsKey(aEvent.getReqId())) {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Removing pending callback for '" + aEvent.getId() + "' event. Client does not support it!...");
			}

			//Getting the callback and removing
			OnResponse aOnResponse = getCallsMap().get(connector_id).remove(aEvent.getReqId());

			//Updating the elapsed time
			aOnResponse.setElapsedTime(System.nanoTime() - aOnResponse.getSentTime());

			//Calling the failure method
			aOnResponse.failure(FailureReason.EVENT_NOT_SUPPORTED_BY_CLIENT, connector_id);
		}
	}

	/**
	 * @return The stored callbacks 
	 */
	public FastMap<String, FastMap<String, OnResponse>> getCallsMap() {
		return callsMap;
	}

	/**
	 * @param callsMap The stored callbacks  to set
	 */
	public void setCallsMap(FastMap<String, FastMap<String, OnResponse>> callsMap) {
		this.callsMap = callsMap;
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
	 * @return The cross types map
	 */
	public TypesMap getTypesMap() {
		return typesMap;
	}

	/**
	 * @param typesMap The cross types map to set
	 */
	public void setTypesMap(TypesMap typesMap) {
		this.typesMap = typesMap;
	}

	/**
	 *
	 * @return The unique identifier to identify the token
	 */
	synchronized public String getNextUID() {
		if (uid.equals(Integer.MAX_VALUE)) {
			uid = 0;
			return "0";
		}
		uid += 1;

		//Adding the node identifier if in a cluster
		String clusterNodeId = "";
		if (getEm().isClusterNode()) {
			clusterNodeId = getEm().getClusterNode().getId();
		}

		return clusterNodeId + Integer.toString(uid);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void initialize() throws Exception {
		//Listening this events
		getEm().on(ConnectorStopped.class, this);
		getEm().on(S2CResponse.class, this);
		getEm().on(S2CEventNotSupportedOnClient.class, this);

		timeoutHandler = new Timer();
		//Purge cancelled on timeout callbacks every 5 minutes
		timeoutHandler.scheduleAtFixedRate(new PurgeCancelledTimeoutsTask(timeoutHandler), 0, 300000);
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
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
