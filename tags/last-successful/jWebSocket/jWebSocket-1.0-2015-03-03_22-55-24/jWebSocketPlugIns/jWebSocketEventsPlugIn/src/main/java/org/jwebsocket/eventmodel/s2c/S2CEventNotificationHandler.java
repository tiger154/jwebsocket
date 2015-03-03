//	---------------------------------------------------------------------------
//	jWebSocket - S2CEventNotificationHandler (Community Edition, CE)
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
package org.jwebsocket.eventmodel.s2c;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.eventmodel.event.em.ConnectorStopped;
import org.jwebsocket.eventmodel.event.em.S2CEventNotSupportedOnClient;
import org.jwebsocket.eventmodel.event.em.S2CResponse;
import org.jwebsocket.eventmodel.exception.InvalidConnectorIdentifier;
import org.jwebsocket.eventmodel.filter.validator.TypesMap;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class S2CEventNotificationHandler implements IInitializable, IListener {

	private static Logger mLog = Logging.getLogger(S2CEventNotificationHandler.class);
	private Integer mUID = 0;
	private EventModel mEm;
	private TypesMap mTypesMap;
	private FastMap<String, FastMap<String, OnResponse>> mCallbacks = new FastMap<String, FastMap<String, OnResponse>>().shared();

	/**
	 * Send an event to the client
	 *
	 * @param aEvent The S2CEvent to send
	 * @param aConnectorId The destiny client
	 * @param aOnResponse The server on-response callbacks
	 * @throws InvalidConnectorIdentifier
	 */
	public void send(S2CEvent aEvent, String aConnectorId, OnResponse aOnResponse) throws InvalidConnectorIdentifier {
		TokenServer lServer = getEm().getParent().getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Preparing S2C event notification...");
		}

		if (!mCallbacks.containsKey(aConnectorId)) {
			mCallbacks.put(aConnectorId, new FastMap<String, OnResponse>());
		}

		//Creating the token
		Token lToken = TokenFactory.createToken(getEm().getParent().getNamespace(), "s2c.en");
		aEvent.writeToToken(lToken);
		aEvent.writeParentToToken(lToken);
		lToken.setString("uid", getNextUID());

		//Saving the callback
		if (null != aOnResponse) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Saving the OnResponse callback for the event '" + aEvent.getId() + "'...");
			}
			//Saving the callback
			aOnResponse.setRequiredType(aEvent.getResponseType());
			mCallbacks.get(aConnectorId).put(lToken.getString("uid"), aOnResponse);
			//Setting the send time
			aOnResponse.setSentTime(System.nanoTime());

			//2CEvent have a callback
			lToken.setBoolean("hc", true);

			//Registering timeout callbacks
			if (aEvent.getTimeout() > 0) {
				Tools.getTimer().schedule(new TimeoutCallbackTask(aConnectorId, lToken.getString("uid"), this), aEvent.getTimeout());
			}
		} else {
			//S2CEvent don't have a callback
			lToken.setBoolean("hc", false);
		}

		//Sending the token
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending S2C event notification to '" + aConnectorId + "' connector...");
		}

		//Getting the local WebSocketConnector instance if exists
		WebSocketConnector lConnector = lServer.getConnector(aConnectorId);

		if (null != lConnector) {
			//Sending locally on the server
			lServer.sendTokenFragmented(lConnector, lToken, getEm().getFragmentSize());
		} else if (getEm().isClusterNode() && getEm().getClusterNode().
				getAllConnectors().contains(aConnectorId)) {
			//Sending the token to the cluster network
			getEm().getClusterNode().sendToken(aConnectorId, lToken);
		} else {
			throw new InvalidConnectorIdentifier("The connector identifier: "
					+ "'" + aConnectorId + "' is not valid!");
		}
	}

	/**
	 * Send an event to the client
	 *
	 * @param aEvent The S2CEvent to send
	 * @param aTo The destiny client connector
	 * @param aOnResponse The server on-response callbacks
	 * @throws InvalidConnectorIdentifier
	 */
	public void send(S2CEvent aEvent, WebSocketConnector aTo, OnResponse aOnResponse) throws InvalidConnectorIdentifier {
		send(aEvent, aTo.getId(), aOnResponse);
	}

	/**
	 * Executes the OnResponse callback appropriate method when a Response is
	 * gotten from the client
	 *
	 * @param aEvent The response event from the client
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void processEvent(S2CResponse aEvent, C2SResponseEvent aResponseEvent) throws Exception {

		String lConnectorId = aEvent.getConnector().getId();
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing S2CResponse(" + aEvent.getReqId()
					+ ") from '" + lConnectorId + "' connector...");
		}

		//If a callback is pending for this response
		if (mCallbacks.containsKey(lConnectorId) && mCallbacks.get(lConnectorId).containsKey(aEvent.getReqId())) {
			//Getting the OnResponse callback
			OnResponse lCallback = mCallbacks.get(lConnectorId).remove(aEvent.getReqId());

			//Setting the processing time
			lCallback.setProcessingTime(aEvent.getProcessingTime());

			//Cleaning if empty
			if (mCallbacks.get(lConnectorId).isEmpty()) {
				mCallbacks.remove(lConnectorId);
			}

			//Executing the validation process...
			if (!lCallback.getRequiredType().equals("void")) {
				//Validating the response
				if (getTypesMap().swapType(lCallback.getRequiredType()).isInstance(aEvent.getResponse())
						&& lCallback.isValid(aEvent.getResponse(), lConnectorId)) {
					lCallback.setElapsedTime(System.nanoTime() - lCallback.getSentTime());
					lCallback.success(aEvent.getResponse(), lConnectorId);
				} else {
					lCallback.setElapsedTime(System.nanoTime() - lCallback.getSentTime());
					lCallback.failure(FailureReason.INVALID_RESPONSE, lConnectorId);
				}
			} else {
				lCallback.setElapsedTime(System.nanoTime() - lCallback.getSentTime());
				lCallback.success(null, lConnectorId);
			}
		} else {
			if (mLog.isDebugEnabled()) {
				mLog.debug("The S2CResponse(" + aEvent.getReqId()
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
		String lConnectorId = aEvent.getConnector().getId();
		if (mCallbacks.containsKey(aEvent.getConnector().getId())) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Removing pending callbacks for '" + lConnectorId + "' connector...");
			}

			//Getting pending callbacks and removing
			FastMap<String, OnResponse> lPendingCallbacks = mCallbacks.remove(lConnectorId);

			double lCurrentTime = System.nanoTime();
			for (Iterator<Entry<String, OnResponse>> lIt = lPendingCallbacks.entrySet().iterator(); lIt.hasNext();) {
				Map.Entry<String, OnResponse> lCalls = lIt.next();
				lCalls.getValue().setElapsedTime(lCurrentTime - lCalls.getValue().getSentTime());
				lCalls.getValue().failure(FailureReason.CONNECTOR_STOPPED, lConnectorId);
			}
		}
	}

	/**
	 * Event fired when the client does not support the S2C event from the
	 * server
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(S2CEventNotSupportedOnClient aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing the 'S2CEventNotSupportedOnClient' event...");
		}

		//Caching the connector connector_id for performance
		String lConnectorId = aEvent.getConnector().getId();

		//Removing only if a callback is pending
		if (mCallbacks.containsKey(lConnectorId)
				&& mCallbacks.get(lConnectorId).containsKey(aEvent.getReqId())) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Removing pending callback for '" + aEvent.getId() + "' event. Client does not support it!...");
			}

			//Getting the callback and removing
			OnResponse lCallback = mCallbacks.get(lConnectorId).remove(aEvent.getReqId());

			//Updating the elapsed time
			lCallback.setElapsedTime(System.nanoTime() - lCallback.getSentTime());

			//Calling the failure method
			lCallback.failure(FailureReason.EVENT_NOT_SUPPORTED_BY_CLIENT, lConnectorId);
		}
	}

	/**
	 *
	 * @return
	 */
	public FastMap<String, FastMap<String, OnResponse>> getCallbacks() {
		return mCallbacks;
	}

	/**
	 * @return The EventModel instance
	 */
	public EventModel getEm() {
		return mEm;
	}

	/**
	 *
	 * @param aEm
	 */
	public void setEm(EventModel aEm) {
		this.mEm = aEm;
	}

	/**
	 * @return The cross types map
	 */
	public TypesMap getTypesMap() {
		return mTypesMap;
	}

	/**
	 * @param aTypesMap The cross types map to set
	 */
	public void setTypesMap(TypesMap aTypesMap) {
		this.mTypesMap = aTypesMap;
	}

	/**
	 *
	 * @return The unique identifier to identify the token
	 */
	synchronized public String getNextUID() {
		if (mUID.equals(Integer.MAX_VALUE)) {
			mUID = 0;
			return "0";
		}
		mUID += 1;

		//Adding the node identifier if in a cluster
		String lClusterNodeId = "";
		if (getEm().isClusterNode()) {
			lClusterNodeId = getEm().getClusterNode().getId();
		}

		return lClusterNodeId + Integer.toString(mUID);
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

		//Purge cancelled on timeout callbacks every 5 minutes
		Tools.getTimer().scheduleAtFixedRate(new PurgeCancelledTimeoutsTask(Tools.getTimer()), 0, 300000);
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
