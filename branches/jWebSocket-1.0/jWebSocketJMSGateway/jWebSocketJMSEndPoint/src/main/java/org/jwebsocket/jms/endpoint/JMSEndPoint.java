//	---------------------------------------------------------------------------
//	jWebSocket - JMS Gateway Client (Community Edition, CE)
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
package org.jwebsocket.jms.endpoint;

/**
 * Java Client for jWebSocket JMS Gateway
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
import org.apache.log4j.Logger;

/**
 *
 * @author Alexander Schulze
 */
public class JMSEndPoint {

	// TODO: Introduce timeout management and support correlations
	/**
	 *
	 *
	 */
	public static boolean TEMPORARY = false;
	/**
	 *
	 */
	public static boolean DURABLE = true;
	static final Logger mLog = Logger.getLogger(JMSEndPoint.class);
	// the JMS connection factory
	private ActiveMQConnectionFactory mConnectionFactory;
	// the JMS connection instance/object
	private Connection mConnection;
	// session to create topics, producers and consumers
	private Session mSession;
	//
	private MessageProducer mProducer;
	// id to address the jWebSocket JMS Gateway
	private String mGatewayId;
	// id to address a certain node on the JMS topic
	private String mEndPointId;
	// the publishing component
	private JMSEndPointSender mSender = null;
	// the subscriber component
	private JMSEndPointListener mListener;
	// flag to shutdown a non-self-termintaing console client
	private boolean mShutDown = false;

	private void init(String aBrokerURI, String aGatewayTopic,
			String aGatewayId, String aEndPointId, int aThreadPoolSize,
			boolean aDurable) throws JMSException {
		// instantiate connection factory for ActiveMQ broker
		mConnectionFactory = new ActiveMQConnectionFactory(aBrokerURI);
		// save endpoint id 
		mEndPointId = aEndPointId;
		// save gateway id 
		mGatewayId = aGatewayId;
		// create the connection object
		mConnection = mConnectionFactory.createConnection();
		mConnection.setClientID(aEndPointId);
		// create a session for this connection
		mSession = mConnection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);

		// create a producer for the given gateway topic (JMS destination)
		Topic lGatewayTopic = mSession.createTopic(aGatewayTopic);
		mProducer = mSession.createProducer(lGatewayTopic);

		// create a consumer for the given gateway topic (JMS destination)
		// use endPointId to listen on a certain target address only
		String lSelector = "targetId='" + mEndPointId + "' or (targetId='*' and sourceId<>'" + mEndPointId + "')";
		MessageConsumer lConsumer;
		if (aDurable) {
			lConsumer = mSession.createDurableSubscriber(lGatewayTopic, lSelector);
		} else {
			lConsumer = mSession.createConsumer(lGatewayTopic, lSelector);
		}
		// create a listener and pass the sender to easily answer requests
		mListener = new JMSEndPointListener(aThreadPoolSize);
		// pass the listener to the JMS consumer object
		lConsumer.setMessageListener(mListener);

		// creating sender
		mSender = new JMSEndPointSender(this);
	}

	/**
	 *
	 */
	public JMSEndPoint() {
	}

	/**
	 *
	 * @param aBrokerURI
	 * @param aGatewayTopic
	 * @param aGatewayId
	 * @param aEndPointId
	 * @param aThreadPoolSize
	 * @param aDurable
	 */
	public JMSEndPoint(String aBrokerURI, String aGatewayTopic,
			String aGatewayId, String aEndPointId, int aThreadPoolSize,
			boolean aDurable) {
		try {
			init(aBrokerURI, aGatewayTopic,
					aGatewayId, aEndPointId, aThreadPoolSize,
					aDurable);
		} catch (JMSException lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " on connecting JMS client: "
					+ lEx.getMessage());
		}
	}

	/**
	 *
	 * @param aBrokerURI
	 * @param aGatewayTopic
	 * @param aGatewayId
	 * @param aEndPointId
	 * @param aThreadPoolSize
	 * @param aDurable
	 * @return
	 * @throws JMSException
	 */
	public static JMSEndPoint getInstance(String aBrokerURI, String aGatewayTopic,
			String aGatewayId, String aEndPointId, int aThreadPoolSize,
			boolean aDurable) throws JMSException {
		JMSEndPoint lEP = new JMSEndPoint();
		lEP.init(aBrokerURI, aGatewayTopic,
				aGatewayId, aEndPointId, aThreadPoolSize,
				aDurable);
		return lEP;
	}

	/**
	 *
	 */
	public void start() {
		try {
			// establish the connection
			mConnection.start();
		} catch (JMSException lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " on connecting JMS client: "
					+ lEx.getMessage());
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
		}
		if (null != mSession) {
			try {
				mSession.close();
			} catch (JMSException lEx) {
				// TODO: process exceptions properly
			}
		}
		if (null != mConnection) {
			try {
				mConnection.stop();
				mConnection.close();
			} catch (JMSException lEx) {
				// TODO: process exceptions properly
			}
		}
		// to end potential console loops
		mShutDown = true;
	}

	/**
	 * @return the mEndPointId
	 */
	public String getEndPointId() {
		return mEndPointId;
	}

	/**
	 * @return the GatewayId
	 */
	public String getGatewayId() {
		return mGatewayId;
	}

	/**
	 * @return the Session
	 */
	public Session getSession() {
		return mSession;
	}

	/**
	 * @return the Producer
	 */
	public MessageProducer getProducer() {
		return mProducer;
	}

}
