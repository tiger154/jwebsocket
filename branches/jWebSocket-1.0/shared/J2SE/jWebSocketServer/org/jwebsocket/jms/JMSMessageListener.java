//	---------------------------------------------------------------------------
//	jWebSocket - JMS Connection Advisor (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.jms;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.apache.activemq.command.ActiveMQDestination;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.jms.api.IConnectorsManager;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.util.Tools;

/**
 * JMS Engine message consumer (listener)
 *
 * @author kyberneees
 */
public class JMSMessageListener implements MessageListener, IInitializable {

	private JMSEngine mEngine;
	private MessageConsumer mConsumer;

	public JMSMessageListener(JMSEngine aEngine) {
		mEngine = aEngine;
	}

	@Override
	public void onMessage(Message aMessage) {
		try {
			MessageType lType = MessageType.valueOf(aMessage.getStringProperty(Attributes.MESSAGE_TYPE));
			IConnectorsManager lConnManager = mEngine.getConnectorsManager();

			switch (lType) {
				case CONNECTION: {
					// session id is MD5 value for security
					String lSessionId = aMessage.getStringProperty(Attributes.SESSION_ID);
					// storing the connector
					lConnManager.addConnector(
							lSessionId,
							aMessage.getStringProperty(Attributes.IP_ADDRESS),
							((ActiveMQDestination) aMessage.getJMSReplyTo()).getPhysicalName());

					// getting the connector instance
					JMSConnector lConnector = lConnManager.getConnector(lSessionId);
					// start connector
					lConnector.startConnector();
					break;
				}
				case MESSAGE: {
					String lSessionId = aMessage.getStringProperty(Attributes.SESSION_ID);
					if (lConnManager.sessionExists(lSessionId)) {
						// getting the connector
						JMSConnector lConnector = lConnManager.getConnector(lSessionId);
						// getting the packet content
						TextMessage lMessage = (TextMessage) aMessage;
						// notifying process packet
						lConnector.processPacket(new RawPacket(lMessage.getText()));
					} else {
						// message is discarded, client does not exists
					}
					break;
				}
				case DISCONNECTION: {
					String lSessionId = aMessage.getStringProperty(Attributes.SESSION_ID);
					if (lConnManager.sessionExists(lSessionId)) {
						// getting the connector
						JMSConnector lConnector = lConnManager.getConnector(lSessionId);
						// stopping the connector
						lConnector.stopConnector(CloseReason.CLIENT);

						// setting status to offline
						lConnManager.setStatus(lSessionId, ConnectorStatus.OFFLINE);
					}
					break;
				}
				case SESSION_STOPPED: {
					String lSessionId = aMessage.getStringProperty(Attributes.SESSION_ID);
					if (lConnManager.sessionExists(lSessionId)) {
						SystemPlugIn.stopSession(mEngine.getConnectorsManager()
								.getConnector(lSessionId).getSession());

						// removing from index and destroying session
						lConnManager.removeConnector(lSessionId);
					}
					break;
				}
			}
		} catch (Exception lEx) {
		}
	}

	@Override
	public void initialize() throws Exception {
		Topic lEngineTopic = mEngine.getSession().createTopic(mEngine.getDestination());

		// creating message consumer
		mConsumer = mEngine.getSession().createConsumer(lEngineTopic,
				Attributes.NODE + " = '" + mEngine.getNodeId() + "'");

		// registering listener
		final MessageListener lListener = this;
		mConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(final Message aMessage) {
				// executes messages processing into a thread pool
				Tools.getThreadPool().submit(new Runnable() {
					@Override
					public void run() {
						lListener.onMessage(aMessage);
					}
				});
			}
		});
	}

	@Override
	public void shutdown() throws Exception {
		mConsumer.close();
	}
}