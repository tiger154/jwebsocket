//	---------------------------------------------------------------------------
//	jWebSocket - AMQMessageAuthorizationFilter (Community Edition, CE)
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
package org.jwebsocket.plugins.jms;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.Message;
import org.apache.activemq.security.MessageAuthorizationPolicy;

/**
 * Message authorization policy implementation to provide security on replies
 * delivery. Only request owners can receive replies.
 *
 * @author kyberneees
 */
public class AMQMessageAuthorizationFilter implements MessageAuthorizationPolicy {

	private final List<String> mTargetDestinations = new LinkedList<String>();

	/**
	 *
	 * @return
	 */
	public List<String> getTargetDestinations() {
		return mTargetDestinations;
	}

	/**
	 *
	 * @param aTargetDestination
	 */
	public void setTargetDestinations(List<String> aTargetDestination) {
		mTargetDestinations.addAll(aTargetDestination);
	}

	/**
	 *
	 * @param aContext
	 * @param aMessage
	 * @return
	 */
	@Override
	public boolean isAllowedToConsume(ConnectionContext aContext, final Message aMessage) {
		try {
			String lMessageDest = aMessage.getDestination().getQualifiedName();
			for (String lSecureDest : mTargetDestinations) {
				if (lSecureDest.matches(lMessageDest)) {
					if (aContext.isNetworkConnection()) {
						return true;
					} else if (aContext.getConnectionId().getValue().equals((String) aMessage.getProperty("connectionId"))) {
						aMessage.removeProperty("connectionId");
						aMessage.removeProperty("replySelector");
						// allow if the consumer connection id matches the message target connection id
						return true;
					} else if (Boolean.TRUE.equals(aMessage.getProperty("isBroadcast"))) {
						// allow multiple subscribers to process broadcasted messages
						return true;
					} else if (aMessage.getDestination().getPhysicalName().equals(aContext.getUserName())) {
						// allow if the username matches the message destination 
						// (required for server side processing nodes)
						return true;
					}

					return false;
				}
			}
		} catch (IOException lEx) {
		}

		return true;
	}
}
