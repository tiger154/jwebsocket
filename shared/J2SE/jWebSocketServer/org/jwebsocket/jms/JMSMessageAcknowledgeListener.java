//	---------------------------------------------------------------------------
//	jWebSocket - JMSMessageAcknowledgeListener (Community Edition, CE)
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
import javax.jms.Topic;
import org.apache.activemq.command.ActiveMQMessage;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.util.Tools;

/**
 * JMS Engine message consumer (listener)
 *
 * @author kyberneees
 */
public class JMSMessageAcknowledgeListener implements MessageListener, IInitializable {

	private JMSEngine mEngine;
	private MessageConsumer mMessageConsumedListener;
	private MessageConsumer mMessageExpiredListener;

	public JMSMessageAcknowledgeListener(JMSEngine aEngine) {
		mEngine = aEngine;
	}

	@Override
	public void onMessage(Message aMessage) {
		try {
			ActiveMQMessage lMessage = (ActiveMQMessage) aMessage;

		} catch (Exception lEx) {
		}
	}

	@Override
	public void initialize() throws Exception {
		Topic lMessageConsumedTopic = mEngine.getSession()
				.createTopic("ActiveMQ.Advisory.MessageConsumed.Topic." + mEngine.getDestination());
		Topic lMessageExpiredTopic = mEngine.getSession()
				.createTopic("ActiveMQ.Advisory.MessageDiscarded.Topic." + mEngine.getDestination());

		// creating message consumers
		mMessageConsumedListener = mEngine.getSession().createConsumer(lMessageConsumedTopic);
		mMessageExpiredListener = mEngine.getSession().createConsumer(lMessageExpiredTopic);

		// registering listener
		final MessageListener lListener = this;
		mMessageConsumedListener.setMessageListener(new MessageListener() {
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
		mMessageExpiredListener.setMessageListener(new MessageListener() {
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
		mMessageConsumedListener.close();
		mMessageExpiredListener.close();
	}
}