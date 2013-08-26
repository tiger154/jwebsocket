package org.jwebsocket.jms;

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
public class JMSMessageAuthorizationFilter implements MessageAuthorizationPolicy {

	private List<String> mTargetDestinations = new LinkedList<String>();

	public List<String> getTargetDestinations() {
		return mTargetDestinations;
	}

	public void setTargetDestinations(List<String> aTargetDestination) {
		mTargetDestinations.addAll(aTargetDestination);
	}

	@Override
	public boolean isAllowedToConsume(ConnectionContext aContext, Message aMessage) {
		try {
			// excluding messages target to other destinations
			if (!mTargetDestinations.contains(aMessage.getDestination().getPhysicalName())) {
				return true;
			}

			// allow if the connection id matches the message connection id
			if (aContext.getConnectionId().getValue().equals((String) aMessage.getProperty(Attributes.CONNECTION_ID))) {
				aMessage.removeProperty(Attributes.CONNECTION_ID);
				return true;
			} else {
				// allow multiple subscribers to process broadcasted messages
				if (Boolean.TRUE.equals(aMessage.getProperty("isBroadcast"))) {
					return true;
				}
			}
		} catch (Exception lEx) {
		}

		return false;
	}
}
