//  ---------------------------------------------------------------------------
//  jWebSocket - JMS Gateway Client (Community Edition, CE)
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
package org.jwebsocket.jms.client;

/**
 * Java client for jWebSocket JMS Gateway
 *
 * @author Alexander Schulze
 */
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author aschulze
 */
public class JMSClient {

	// the JMS connection factory
	private ActiveMQConnectionFactory mConnectionFactory;
	// the JMS connection instance/object
	private Connection mConnection;
	// session to create topics, producers and consumers
	private Session mSession;
	// id to address a certain node on the JMS topic
	private String mNodeId;
	// the publishing component
	private JMSClientSender mSender = null;
	// the subscriber component
	private JMSClientListener mListener;
	// flag to shutdown a non-self-termintaing console client
	private boolean mShutDown = false;

	/**
	 *
	 * @param aBrokerURI
	 * @param aConsumerTopic
	 * @param aProducerTopic
	 * @param aNodeId
	 */
	public JMSClient(String aBrokerURI, String aConsumerTopic,
			String aProducerTopic, String aNodeId, int aThreadPoolSize) {
		// instantiate connection factory for ActiveMQ broker
		mConnectionFactory = new ActiveMQConnectionFactory(aBrokerURI);
		try {
			// save node id 
			mNodeId = aNodeId;
			// create the connection object
			mConnection = mConnectionFactory.createConnection();
			// create a session for this connection
			Session mSession = mConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			// establush the connection
			mConnection.start();

			// create a producer for the given output topic (JMS destination)
			Topic lProducerTopic = mSession.createTopic(aProducerTopic);
			MessageProducer lProducer = mSession.createProducer(lProducerTopic);
			mSender = new JMSClientSender(mSession, lProducer, mNodeId);

			// create a consumer for the given input topic (JMS destination)
			Topic lConsumerTopic = mSession.createTopic(aConsumerTopic);
			// use JMSCorrelation ID to listen on a certain target address only
			MessageConsumer lConsumer = mSession.createConsumer(
					lConsumerTopic,
					"JMSCorrelationID = '" + mNodeId + "'");
			// create a listener and pass the sender to easily answer requests
			mListener = new JMSClientListener(aThreadPoolSize);
			// pass the listener to the JMS consumer object
			lConsumer.setMessageListener(mListener);

			/* 
			 // send in initial register message if required
			 mSender.sendText("{\"ns\":\"org.jwebsocket.jms.bridge\""
			 + ",\"type\":\"register\""
			 + ",\"sourceId\":\"" + mNodeId + "\""
			 + "}");
			 */
		} catch (JMSException lEx) {
			System.out.println(lEx.getClass().getSimpleName()
					+ "on connecting JMS client.");
		}
	}

	/**
	 * Adds a new listener to the JMS Gateway Client.
	 *
	 * @param aListener
	 */
	public void addListener(IJMSMessageListener aListener) {
		// assign the sender to the listener to easily allow to answer messages.
		aListener.setSender(mSender);
		// add the listener
		mListener.addMessageListener(aListener);
	}

	/**
	 * Removes a listener from the JMS Gateway Client.
	 *
	 * @param aListener
	 */
	public void removeListener(IJMSMessageListener aListener) {
		mListener.removeMessageListener(aListener);
	}

	/**
	 * Returns if the the JMS Gateway client is already shut down.
	 *
	 * @return
	 */
	public boolean isShutdown() {
		return mShutDown;
	}

	/**
	 * Shuts down the current instance of the JMS Gateway client.
	 */
	public void shutdown() {
		// clean the garbage
		if (null != mListener) {
			try {
				mListener.shutdown();
			} catch (Exception lEx) {
				// TODO: process exceptions properly
			}
			try {
				mSession.close();
			} catch (Exception lEx) {
				// TODO: process exceptions properly
			}
			try {
				mConnection.close();
				mConnection.stop();
			} catch (Exception lEx) {
				// TODO: process exceptions properly
			}
		}
		// to end potential console loops
		mShutDown = true;
	}
}
