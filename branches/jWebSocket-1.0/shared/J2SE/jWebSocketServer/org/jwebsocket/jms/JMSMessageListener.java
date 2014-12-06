//	---------------------------------------------------------------------------
//	jWebSocket - JMSMessageListener (Community Edition, CE)
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
package org.jwebsocket.jms;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.instance.JWebSocketInstance;
import org.jwebsocket.jms.api.IConnectorsManager;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.util.Tools;

/**
 * JMS Engine message consumer (listener)
 *
 * @author Rolando Santamaria Maso
 */
public class JMSMessageListener implements MessageListener, IInitializable {

	private final JMSEngine mEngine;
	private MessageConsumer mConsumer;

	/**
	 *
	 * @param aEngine
	 */
	public JMSMessageListener(JMSEngine aEngine) {
		mEngine = aEngine;
	}

	/**
	 *
	 * @param aMessage
	 */
	@Override
	public void onMessage(Message aMessage) {
		try {
			MessageType lType = MessageType.valueOf(aMessage.getStringProperty(Attributes.MESSAGE_TYPE));
			IConnectorsManager lConnManager = mEngine.getConnectorsManager();

			switch (lType) {
				case CONNECTION: {
					String lReplySelector = aMessage.getStringProperty(Attributes.REPLY_SELECTOR);
					// storing the connector
					if (!lConnManager.exists(lReplySelector)) {
						lConnManager.add(
								aMessage.getStringProperty(Attributes.CONNECTION_ID),
								aMessage.getStringProperty(Attributes.CONSUMER_ID),
								aMessage.getStringProperty(Attributes.REPLY_SELECTOR),
								aMessage.getStringProperty(Attributes.SESSION_ID));

						// getting the connector instance
						JMSConnector lConnector = lConnManager.getConnectorById(lReplySelector);
						lConnector.startConnector();
					}
					break;
				}
				case SHUTDOWN_NODE: {
					JWebSocketInstance.setStatus(JWebSocketInstance.SHUTTING_DOWN);
					break;
				}
				case MESSAGE: {
					String lReplySelector = aMessage.getStringProperty(Attributes.REPLY_SELECTOR);
					if (lConnManager.exists(lReplySelector)) {
						// getting the connector
						JMSConnector lConnector = lConnManager.getConnectorById(lReplySelector);
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
					String lConsumerId = aMessage.getStringProperty(Attributes.CONSUMER_ID);
					String lReplySelector;
					if (null != lConsumerId) {
						// supporting LB clients disconnection advisory support
						lReplySelector = lConnManager.getReplySelectorByConsumerId(lConsumerId);
					} else {
						// client sent DISCONNECTION command
						lReplySelector = aMessage.getStringProperty(Attributes.REPLY_SELECTOR);
					}

					if (null != lReplySelector && lConnManager.exists(lReplySelector)) {
						// getting the connector
						JMSConnector lConnector = lConnManager.getConnectorById(lReplySelector);
						// stopping the connector
						lConnector.stopConnector(CloseReason.CLIENT);
					}

					break;
				}
			}
		} catch (Exception lEx) {
		}
	}

	@Override
	public void initialize() throws Exception {
		Topic lEngineTopic = mEngine.getSession().createTopic(mEngine.getDestination() + "_nodes");

		// creating message consumer
		mConsumer = mEngine.getSession().createConsumer(lEngineTopic,
				Attributes.NODE_ID + " = '" + mEngine.getNodeId() + "'");

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