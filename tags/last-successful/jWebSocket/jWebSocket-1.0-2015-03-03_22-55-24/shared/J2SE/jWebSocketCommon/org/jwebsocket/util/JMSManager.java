//	---------------------------------------------------------------------------
//	jWebSocket - JMSManager for messaging (Community Edition, CE)
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
package org.jwebsocket.util;

import java.util.Map;
import java.util.UUID;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javolution.util.FastMap;
import org.jwebsocket.jms.Attributes;

/**
 * JMS based message hub for components messaging
 *
 * @author Rolando Santamaria Maso
 */
public class JMSManager {

	private Session mSession;
	private final Map<String, MessageConsumer> mListeners
			= new FastMap<String, MessageConsumer>().shared();
	private final Map<String, MessageProducer> mProducers
			= new FastMap<String, MessageProducer>().shared();
	private String mDefaultDestination;
	private final Connection mConnection;

	/**
	 *
	 * @param aConn
	 */
	public JMSManager(Connection aConn) {
		this(false, aConn);
	}

	/**
	 * The JMSManager JMS connection
	 *
	 * @return
	 */
	public Connection getConnection() {
		return mConnection;
	}

	/**
	 *
	 * @return
	 */
	public String getDefaultDestination() {
		return mDefaultDestination;
	}

	/**
	 *
	 * @param aDefaultDestination
	 */
	public void setDefaultDestination(String aDefaultDestination) {
		mDefaultDestination = aDefaultDestination;
	}

	/**
	 * Create new JMSManager instance
	 *
	 * @param aUseTransaction Indicates if the internal JMS session will use
	 * transactions
	 * @param aConn The JMS connection to use
	 */
	public JMSManager(boolean aUseTransaction, Connection aConn) {
		this(aUseTransaction, aConn, null);
	}

	/**
	 * Create new JMSManager instance
	 *
	 * @param aUseTransaction Indicates if the internal JMS session will use
	 * transactions
	 * @param aConn The JMS connection to use
	 * @param aDefaultDestination The default destination to send messages
	 * (default: "topic://jwebsocket_messagehub")
	 */
	public JMSManager(boolean aUseTransaction, Connection aConn, String aDefaultDestination) {
		try {
			if (null == aDefaultDestination) {
				aDefaultDestination = "topic://jwebsocket_messagehub";
			}
			mDefaultDestination = aDefaultDestination;
			mSession = aConn.createSession(aUseTransaction, Session.AUTO_ACKNOWLEDGE);
			mConnection = aConn;
		} catch (JMSException lEx) {
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
	 * Alias for shutdown method
	 *
	 * @throws JMSException
	 */
	public void close() throws Exception {
		shutdown();
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
	private Destination getDestination(String aDestination) throws JMSException, Exception {
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
	 * Builds a new message instance.
	 *
	 * @param aNS
	 * @param aMsgType
	 * @return
	 * @throws Exception
	 */
	public MapMessage buildMessage(String aNS, String aMsgType) throws Exception {
		MapMessage lMessage = mSession.createMapMessage();
		lMessage.setStringProperty(Attributes.NAMESPACE, aNS);
		lMessage.setStringProperty(Attributes.MESSAGE_ID, UUID.randomUUID().toString());
		lMessage.setStringProperty(Attributes.MESSAGE_TYPE, aMsgType);

		return lMessage;
	}

	/**
	 * Subscribe to default destination
	 *
	 * @param aCallback
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribe(MessageListener aCallback) throws JMSException, Exception {
		return subscribe(mDefaultDestination, aCallback, null);
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
	public String subscribe(String aDestination, MessageListener aCallback) throws JMSException, Exception {
		return subscribe(aDestination, aCallback, null);
	}

	/**
	 * Subscribe to a the default destination
	 *
	 * @param aCallback
	 * @param aDurableSubscription
	 * @param aSubscriptionId
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribe(MessageListener aCallback,
			boolean aDurableSubscription, String aSubscriptionId) throws JMSException, Exception {
		return subscribe(mDefaultDestination, aCallback, aDurableSubscription, aSubscriptionId);
	}

	/**
	 * Subscribe to a target destination
	 *
	 * @param aDestination
	 * @param aCallback
	 * @param aDurableSubscription
	 * @param aSubscriptionId
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribe(String aDestination, MessageListener aCallback,
			boolean aDurableSubscription, String aSubscriptionId) throws JMSException, Exception {
		return subscribe(aDestination, aCallback, null, aDurableSubscription, null);
	}

	/**
	 * Subscribe to the default destination using a message selector
	 *
	 * @param aCallback
	 * @param aSelector
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribe(MessageListener aCallback,
			String aSelector) throws JMSException, Exception {
		return subscribe(mDefaultDestination, aCallback, aSelector);
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
	public String subscribe(String aDestination, MessageListener aCallback,
			String aSelector) throws JMSException, Exception {
		return subscribe(aDestination, aCallback, aSelector, false, null);
	}

	/**
	 * Subscribe to the default destination using a message selector
	 *
	 * @param aCallback
	 * @param aSelector
	 * @param aDurableSubscription
	 * @param aSubscriptionId
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribe(MessageListener aCallback, String aSelector,
			boolean aDurableSubscription, String aSubscriptionId) throws JMSException, Exception {
		return subscribe(mDefaultDestination, aCallback, aSelector, aDurableSubscription, aSubscriptionId);
	}

	/**
	 * Subscribe to a target destination using a message selector
	 *
	 * @param aDestination
	 * @param aCallback
	 * @param aSelector
	 * @param aDurableSubscription
	 * @param aSubcriptionId
	 * @return
	 * @throws JMSException
	 * @throws Exception
	 */
	public String subscribe(String aDestination, MessageListener aCallback, String aSelector,
			boolean aDurableSubscription, String aSubcriptionId) throws JMSException, Exception {
		MessageConsumer lListener;
		Destination lDest = getDestination(aDestination);

		String lSubscriptionId = UUID.randomUUID().toString();
		if (null != aSubcriptionId) {
			lSubscriptionId = aSubcriptionId;
		}

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
		lListener.setMessageListener(aCallback);

		// storing consumer
		mListeners.put(lSubscriptionId, lListener);

		// returning subscription id
		return lSubscriptionId;
	}

	/**
	 * Sends a message to the default destination
	 *
	 * @param aMessage
	 * @throws JMSException
	 * @throws Exception
	 */
	public void send(Object aMessage) throws JMSException, Exception {
		send(mDefaultDestination, aMessage);
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
		if (aMessage instanceof Message) {
			lMsg = (Message) aMessage;
		} else {
			lMsg = mSession.createTextMessage(aMessage.toString());
		}

		// checking producer
		if (!mProducers.containsKey(aDestination)) {
			mProducers.put(aDestination, mSession.createProducer(this.getDestination(aDestination)));
		}

		// sending message
		mProducers.get(aDestination).send(lMsg);
	}

	/**
	 * Terminates a subscription
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
		for (String lDest : mProducers.keySet()) {
			mProducers.remove(lDest).close();
		}
		mSession.close();
	}

	/**
	 * Cancel all active subscriptions
	 *
	 * @throws JMSException
	 */
	public void unsubscribeAll() throws JMSException {
		for (String lId : mListeners.keySet()) {
			unsubscribe(lId);
		}
	}
}
