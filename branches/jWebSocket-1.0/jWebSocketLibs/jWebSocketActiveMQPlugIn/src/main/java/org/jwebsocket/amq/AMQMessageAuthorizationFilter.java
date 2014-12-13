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
package org.jwebsocket.amq;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.Message;
import org.apache.activemq.security.MessageAuthorizationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message authorization policy implementation to provide security on replies
 * delivery. The filter ensures that only request owners can receive replies.
 *
 * @author Rolando Santamaria Maso
 */
public class AMQMessageAuthorizationFilter implements MessageAuthorizationPolicy {

	private final List<String> mTargetDestinations = new LinkedList<String>();
	private final String NODES_TOPIC_POSTFIX = "_nodes";
	private final String MESSAGEHUB_TOPIC_POSTFIX = "_messagehub";
	private boolean mSecureNodesTopic = true;
	private boolean mSecureMessageHubTopic = true;
	private static final Logger mLog = LoggerFactory.getLogger(AMQBasicSecurityPlugIn.class);

	public AMQMessageAuthorizationFilter() {
		mLog.info("Instantiating jWebSocket AMQMessageAuthorizationFilter...");
	}

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
	 * @param aSecureMessageHubTopic
	 */
	public void setSecureMessageHubTopic(boolean aSecureMessageHubTopic) {
		this.mSecureMessageHubTopic = aSecureMessageHubTopic;
	}

	/**
	 *
	 * @return
	 */
	public boolean isSecureMessageHubTopic() {
		return mSecureMessageHubTopic;
	}

	/**
	 *
	 * @param aSecureNodesTopic
	 */
	public void setSecureNodesTopic(boolean aSecureNodesTopic) {
		this.mSecureNodesTopic = aSecureNodesTopic;
	}

	/**
	 *
	 * @return
	 */
	public boolean isSecureNodesTopic() {
		return mSecureNodesTopic;
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
				// authorizing AMQ cluster nodes
				if (aContext.isNetworkConnection()) {
					return true;
				}
				// restricting message consumption in the cluster main topic
				if (lSecureDest.matches(lMessageDest)) {
					if (aContext.getConnectionId().getValue().equals((String) aMessage.getProperty("connectionId"))) {
						aMessage.removeProperty("connectionId");
						aMessage.removeProperty("replySelector");
						// allow if the consumer connection id matches the message target connection id
						return true;
					} else if (Boolean.TRUE.equals(aMessage.getProperty("isBroadcast"))) {
						// allow multiple subscribers to process broadcasted messages
						if (aMessage.getDestination().getPhysicalName().equals(aMessage.getUserID())) {
							// allow only server nodes to broadcast messages (security protection)
							return true;
						}
					} else if (aMessage.getDestination().getPhysicalName().equals(aContext.getUserName())) {
						// allow if the username matches the message destination 
						// guarantees message consumption across server nodes
						return true;
					}

					return false;
				}

				// restricting message consumption in the cluster server nodes topic  
				if (mSecureNodesTopic && (lSecureDest + NODES_TOPIC_POSTFIX).matches(lMessageDest)) {
					return aMessage.getDestination().getPhysicalName()
							.equals(aContext.getUserName() + NODES_TOPIC_POSTFIX);
				}

				// restricting message consumption in the cluster message hub topic
				if (mSecureMessageHubTopic && (lSecureDest + MESSAGEHUB_TOPIC_POSTFIX).matches(lMessageDest)) {
					return aMessage.getDestination().getPhysicalName()
							.equals(aContext.getUserName() + MESSAGEHUB_TOPIC_POSTFIX);
				}
			}
		} catch (IOException lEx) {
		}

		return true;
	}
}
