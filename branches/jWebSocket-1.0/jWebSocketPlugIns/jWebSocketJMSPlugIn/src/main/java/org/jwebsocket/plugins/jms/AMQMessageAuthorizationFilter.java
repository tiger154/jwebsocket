package org.jwebsocket.plugins.jms;

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

	private List<String> mTargetDestinations = new LinkedList<String>();

	public List<String> getTargetDestinations() {
		return mTargetDestinations;
	}

	public void setTargetDestinations(List<String> aTargetDestination) {
		mTargetDestinations.addAll(aTargetDestination);
	}

	@Override
	public boolean isAllowedToConsume(ConnectionContext aContext, final Message aMessage) {
		try {
			// excluding messages target to other destinations
			if (!mTargetDestinations.contains(aMessage.getDestination().getQualifiedName())) {
				return true;
			}
			
			if (aContext.getConnectionId().getValue().equals((String) aMessage.getProperty("connectionId"))) {
				aMessage.removeProperty("connectionId");
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
		} catch (Exception lEx) {
		}

		return false;
	}
}
