//	---------------------------------------------------------------------------
//	jWebSocket - JMSManager abstraction for Scripting Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.scripting.app;

import java.util.Map;
import java.util.UUID;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javolution.util.FastMap;
import org.jwebsocket.spring.JWebSocketBeanFactory;

/**
 * JMS based messaging abstraction for Script applications
 *
 * @author kyberneees
 */
public class JMSManager {

	private Connection mConn;
	private Session mSession;
	private Map<String, MessageConsumer> mListeners = new FastMap<String, MessageConsumer>().shared();
	private Map<String, MessageProducer> mProducers = new FastMap<String, MessageProducer>().shared();
	private BaseScriptApp mScriptApp;

	public JMSManager(BaseScriptApp aScriptApp) {
		this(aScriptApp, false);
	}

	public JMSManager(BaseScriptApp aScriptApp, boolean aUseTransaction) {
		this(aScriptApp, aUseTransaction, (Connection) JWebSocketBeanFactory
				.getInstance().getBean("jmsConnection"));
	}

	public JMSManager(BaseScriptApp aScriptApp, boolean aUseTransaction, Connection aConn) {
		try {
			mScriptApp = aScriptApp;
			mConn = aConn;
			mSession = mConn.createSession(aUseTransaction, Session.AUTO_ACKNOWLEDGE);
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}
	}

	/**
	 * Gets the JMS Session instance
	 *
	 * @return
	 */
	public Session getSession() {
		return mSession;
	}

	/**
	 * Gets the JMS Connection instance
	 *
	 * @return
	 */
	public Connection getConnection() {
		return mConn;
	}

	/**
	 * Close the JMS session
	 *
	 * @throws JMSException
	 */
	public void close() throws JMSException {
		mSession.close();
	}

	/**
	 * Commit changes if using transactions
	 *
	 * @throws JMSException
	 */
	public void commit() throws JMSException {
		mSession.commit();
	}

	/**
	 * Gets a destination object giving a destination string
	 *
	 * @param aDestination
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public Destination getDestination(String aDestination) throws JMSException, Exception {
		String lPrefix = aDestination.substring(0, 8);
		Destination lDest;
		if ("queue://".equals(lPrefix)) {
			lDest = mSession.createQueue(aDestination.substring(8));
		} else if ("topic://".equals(lPrefix)) {
			lDest = mSession.createTopic(aDestination.substring(8));
		} else {
			throw new Exception("Expecting a valid destination schema. "
					+ "Please use 'queue://' or 'topic://' as "
					+ "destination prefix!");
		}

		return lDest;
	}

	/**
	 * Subscribe to a target destination
	 *
	 * @param aDestination
	 * @param aCallback
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribe(String aDestination, Object aCallback) throws JMSException, Exception {
		return subscribe(aDestination, aCallback, null);
	}

	/**
	 * Subscribe to a target destination
	 *
	 * @param aDestination
	 * @param aCallback
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribe(String aDestination, Object aCallback,
			boolean aDurableSubscription) throws JMSException, Exception {
		return subscribe(aDestination, aCallback, null, aDurableSubscription);
	}

	/**
	 * Subscribe to a target destination using a message selector
	 *
	 * @param aDestination
	 * @param aCallback
	 * @param aSelector
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribe(String aDestination, Object aCallback,
			String aSelector) throws JMSException, Exception {
		return subscribe(aDestination, aCallback, aSelector, false);
	}

	/**
	 * Subscribe to a target destination using a message selector
	 *
	 * @param aDestination
	 * @param aCallback
	 * @param aSelector
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribe(String aDestination, Object aCallback, String aSelector,
			boolean aDurableSubscription) throws JMSException, Exception {
		MessageConsumer lListener;
		Destination lDest = getDestination(aDestination);

		String lSubscriptionId = UUID.randomUUID().toString();

		if (aDurableSubscription) {
			if (lDest instanceof Topic) {
				if (null != aSelector) {
					lListener = mSession.createDurableSubscriber((Topic) lDest,
							lSubscriptionId, aSelector, false);
				} else {
					lListener = mSession.createDurableSubscriber((Topic) lDest,
							lSubscriptionId);
				}
			} else {
				throw new Exception("Cannot create durable subscriptions on queues!");
			}
		} else {
			if (null != aSelector) {
				lListener = mSession.createConsumer(lDest, aSelector);
			} else {
				lListener = mSession.createConsumer(lDest);
			}
		}

		// registrating consumer callback
		lListener.setMessageListener((MessageListener) mScriptApp.cast(aCallback,
				MessageListener.class));

		// storing consumer
		mListeners.put(lSubscriptionId, lListener);

		// returning subscription id
		return lSubscriptionId;
	}

	/**
	 * Sends a message to a target destination
	 *
	 * @param aDestination
	 * @param aMessage
	 * @throws JMSException
	 * @throws Exception
	 */
	public void send(String aDestination, Object aMessage) throws JMSException, Exception {
		Message lMsg;
		if (aMessage instanceof String) {
			lMsg = mSession.createTextMessage((String) aMessage);
		} else {
			lMsg = (Message) aMessage;
		}

		// checking producer
		if (!mProducers.containsKey(aDestination)) {
			mProducers.put(aDestination, mSession.createProducer(this.getDestination(aDestination)));
		}

		// sending message
		mProducers.get(aDestination).send(lMsg);
	}

	/**
	 * Terminates a subscription using the subscrition unique identifier
	 *
	 * @param aSubscriptionId
	 * @throws JMSException
	 */
	public void unsubscribe(String aSubscriptionId) throws JMSException {
		if (mListeners.containsKey(aSubscriptionId)) {
			// closing consumer
			mListeners.get(aSubscriptionId).close();
			// if consumer is a topic subscriber, unsubscribe
			if (mListeners.get(aSubscriptionId) instanceof TopicSubscriber) {
				mSession.unsubscribe(aSubscriptionId);
			}
			// removing listener
			mListeners.remove(aSubscriptionId);
		}
	}

	/**
	 * Shutdown the manager instance. Close JMS message producers, consumers and
	 * session.
	 *
	 * @throws Exception
	 */
	public void shutdown() throws Exception {
		for (String lId : mListeners.keySet()) {
			unsubscribe(lId);
		}
		for (String lDest : mProducers.keySet()) {
			mProducers.remove(lDest).close();
		}
		mSession.close();
	}
}
