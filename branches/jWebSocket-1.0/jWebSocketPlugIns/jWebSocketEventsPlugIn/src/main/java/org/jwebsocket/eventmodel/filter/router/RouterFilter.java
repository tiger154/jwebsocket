//  ---------------------------------------------------------------------------
//  jWebSocket - RouterFilter
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
package org.jwebsocket.eventmodel.filter.router;

import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.exception.ListenerNotFoundException;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.token.Token;
import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.C2SEventDefinition;
import org.jwebsocket.eventmodel.event.filter.BeforeRouteResponseToken;
import org.jwebsocket.eventmodel.exception.MissingTokenSenderException;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author kyberneees
 */
public class RouterFilter extends EventModelFilter {

	private static Logger mLog = Logging.getLogger(RouterFilter.class);

	/**
	 *{@inheritDoc }
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

			if (lDef.isResponseAsync()) {
				getEm().getParent().getServer().sendTokenAsync(aConnector, lToken);
			} else {
				getEm().getParent().getServer().sendToken(aConnector, lToken);
			}
		}

		//Sending to the rest of connectors
		if (!aEvent.getTo().isEmpty()) {
			Token lResponseNotification = TokenFactory.createToken("external.response");
			lResponseNotification.setNS(getEm().getParent().getNamespace());
			lResponseNotification.setToken("response", lToken);
			lResponseNotification.setString("owner", aConnector.getId());

			if (aEvent.getTo().size() > 0) {
				for (String lId : aEvent.getTo()) {
					//Getting the local WebSocketConnector instance if exists
					WebSocketConnector lConnector = getEm().getParent().getServer().getConnector(lId);
					if (null != lConnector) {
						//Sending locally on the server
						getEm().getParent().getServer().sendToken(lConnector, lResponseNotification);
					} else if (getEm().isClusterNode()) {
						//Sending the token to the cluster network
						getEm().getClusterNode().sendToken(lId, lResponseNotification);
					} else {
						throw new MissingTokenSenderException("Not engine or cluster detected to send "
								+ "the token to the giving connector: '" + lId + "'!");
					}
				}
			}
		}
	}
}
