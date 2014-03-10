//	---------------------------------------------------------------------------
//	jWebSocket - RouterFilter (Community Edition, CE)
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
package org.jwebsocket.eventmodel.filter.router;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.event.filter.BeforeRouteResponseToken;
import org.jwebsocket.eventmodel.exception.InvalidConnectorIdentifier;
import org.jwebsocket.eventmodel.exception.ListenerNotFoundException;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class RouterFilter extends EventModelFilter {

	private static Logger mLog = Logging.getLogger(RouterFilter.class);

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void beforeCall(WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
		if (mLog.isInfoEnabled()) {
			mLog.info("Checking if the event: '" + aEvent.getId() + "' has listener(s) in the server side...");
		}

		//Stopping the connector if in "prod" environment
		if (getEm().getEnv().equals(EventModel.PROD_ENV)) {
			if (mLog.isInfoEnabled()) {
				mLog.info("Stopping the connector '" + aConnector.getId() + "'...");
			}
			//In production is not allowed invalid requests from the client
			aConnector.stopConnector(CloseReason.SERVER);
		}

		//If the incoming event has not listener, reject it!
		if (!getEm().hasListeners(aEvent.getClass())) {
			throw new ListenerNotFoundException("The incoming event '" + aEvent.getId() + "' has not listeners in the server side");
		}
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void afterCall(WebSocketConnector aConnector, C2SResponseEvent aEvent) throws Exception {
		TokenServer lServer = getEm().getParent().getServer();
		C2SEventDefinition lDef = getEm().getEventFactory().getEventDefinitions().getDefinition(aEvent.getId());
		if (!lDef.isResponseRequired()) {
			return;
		}

		//Send the token to the client(s)
		Token lToken = aEvent.getArgs();
		lToken.setInteger("code", aEvent.getCode());
		lToken.setDouble("_pt", aEvent.getProcessingTime());
		lToken.setString("msg", aEvent.getMessage());
		lToken.setNS(getEm().getParent().getNamespace());

		//BeforeSendResponseToken event notification
		BeforeRouteResponseToken lEvent = new BeforeRouteResponseToken(aEvent.getRequestId());
		lEvent.setId("before.route.response.token");
		lEvent.setArgs(lToken);
		lEvent.setEventDefinition(lDef);
		lEvent.setConnector(aConnector);
		getEm().notify(lEvent, null, true);

		//Sending the response
		if (mLog.isInfoEnabled()) {
			mLog.info("Sending the response for '" + aEvent.toString() + "' event to connectors...");
		}

		//Sending the sender connector
		if (aEvent.getTo().contains(aConnector.getId())) {
			aEvent.getTo().remove(aConnector.getId());
			lServer.sendTokenFragmented(aConnector, lToken, getEm().getFragmentSize());
		}

		//Sending to the rest of connectors
		if (!aEvent.getTo().isEmpty()) {
			Token lResponse = TokenFactory.createToken("external.response");
			lResponse.setNS(getEm().getParent().getNamespace());
			lResponse.setToken("response", lToken);
			lResponse.setString("owner", aConnector.getId());

			if (aEvent.getTo().size() > 0) {
				for (String lId : aEvent.getTo()) {
					//Getting the local WebSocketConnector instance if exists
					WebSocketConnector lConnector = lServer.getConnector(lId);
					if (null != lConnector) {
						//Sending locally on the server
						lServer.sendTokenFragmented(aConnector, lToken, getEm().getFragmentSize());
					} else if (getEm().isClusterNode()) {
						//Sending the token to the cluster network
						getEm().getClusterNode().sendToken(lId, lResponse);
					} else {
						throw new InvalidConnectorIdentifier("Not engine or cluster detected to send "
								+ "the token to the giving connector: '" + lId + "'!");
					}
				}
			}
		}
	}
}
