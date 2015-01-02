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
import org.jwebsocket.util.Tools;

/**
 *
 * @author Alexander Schulze
 */
public class JMSEndPoint {

	// TODO: Introduce timeout management and support correlations
	/**
	 * Constant to establish temporary connections (vs. durable connections)
	 *
	 */
	public static boolean TEMPORARY = false;
	/**
	 * Constant to establish durable connections (vs. temporary connections)
	 */
	public static boolean DURABLE = true;
	private static final Logger mLog = Logger.getLogger(JMSEndPoint.class);
	// the JMS connection factory
	private ActiveMQConnectionFactory mConnectionFactory;
	// the JMS connection instance/object
	private Connection mConnection;
	// session to create topics, producers and consumers
	private Session mSession;
	//
	private MessageProducer mProducer;
	//
	private MessageConsumer mConsumer;
	// id to address the jWebSocket JMS Gateway
	private String mGatewayId;
	// id to address a certain node on the JMS topic
	private String mEndPointId;
	// the subscriber component
	private JMSEndPointListener mListener;
	// flag to shutdown a non-self-termintaing console client
	private boolean mShutDown = false;
	// the selector equation
	private String mSelector = null;
	// reference to the gateway topic
	private Topic mGatewayTopic = null;
	// indicates if the JWS shared ThreadPool and Timer should be released on endpoint shutdown
	private boolean mReleaseJWSResources = true;

	/**
	 *
	 * @param aBrokerURI
	 * @param aGatewayTopic
	 * @param aGatewayId
	 * @param aEndPointId
	 * @param aThreadPoolSize
	 * @param aDurable
	 * @throws JMSException
	 */
	protected void init(String aBrokerURI, String aGatewayTopic,
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
		mGatewayTopic = mSession.createTopic(aGatewayTopic);
		mProducer = mSession.createProducer(mGatewayTopic);

		// create a consumer for the given gateway topic (JMS destination)
		// use endPointId to listen on a certain target address only
		mSelector = "targetId='" + mEndPointId
				+ "' or (targetId='*' and sourceId<>'" + mEndPointId + "')";
		if (aDurable) {
			mConsumer = mSession.createDurableSubscriber(mGatewayTopic, mSelector);
		} else {
			mConsumer = mSession.createConsumer(mGatewayTopic, mSelector);
		}
		// create a listener and pass the sender to easily answer requests
		mListener = new JMSEndPointListener(aThreadPoolSize);
		// pass the listener to the JMS consumer object
		mConsumer.setMessageListener(mListener);

	}

	public void setReleaseJWSResources(boolean aRelease) {
		this.mReleaseJWSResources = aRelease;
	}

	/**
	 * Indicates if the JWebSocket shared ThreadPool and Timer resources should be released on
	 * endpoint shutdown. Default value: TRUE
	 *
	 * @return
	 */
	public boolean isReleaseJWSResources() {
		return mReleaseJWSResources;
	}

	protected void init(Connection aConnection, Session aSession,
			Topic aGatewayTopic,
			MessageProducer aProducer, MessageConsumer aConsumer,
			int aThreadPoolSize, boolean aDurable) throws JMSException {
		// take over the connection
		mConnection = aConnection;
		// get endpoint id of JMS gateway
		mEndPointId = mConnection.getClientID();
		// take over the session
		mSession = aSession;
		// take over the topic
		mGatewayTopic = aGatewayTopic;
		// gateway id, the endpoint id of the gateway.
		mGatewayId = mEndPointId;
		// create the connection object

		// take over producer from JMS gateway
		mProducer = aProducer;

		if (aDurable) {
			mConsumer = mSession.createDurableSubscriber(mGatewayTopic, mSelector);
		} else {
			mConsumer = mSession.createConsumer(mGatewayTopic, mSelector);
		}
		// create a listener and pass the sender to easily answer requests
		mListener = new JMSEndPointListener(aThreadPoolSize);
		// pass the listener to the JMS consumer object
		// mConsumer.setMessageListener(mListener);
	}

	// private constructor, public API only allows contructors 
	// with various arguments.
	/**
	 *
	 */
	protected JMSEndPoint() {
	}

	/**
	 * Constructor to create a new JMS endpoint. You need to set the Broker URI, the Gateway Topic,
	 * the Endpoint ID, the Thread Pool size and specify whether to establish a durable or a
	 * temporary connection. Usually you will establish temporary connections for clients. Durable
	 * connections usually are used only for server side services. Please be aware that this
	 * constructor does <b>not</b> check for duplicate endpoint IDs in a messaging infrastructure.
	 *
	 * @param aBrokerURI URI of the Message Broker (e.g.
	 * <tt>tcp://[host]:61616</tt>)
	 * @param aGatewayTopic Name of the topic for the JMS gateway on the JMS broker
	 * @param aGatewayId ID of the jWebSocket JMS Gateway to use for jWebSocket services
	 * @param aEndPointId ID of the endpoint used for this JMS connection
	 * @param aThreadPoolSize Maximum number of threads used to process requests concurrently
	 * @param aDurable <tt>JMSEndPoint.TEMPORARY</tt> (for clients and servers) or
	 * <tt>JMSEndPoint.DURABLE</tt> for durable connections (for durable services only)
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
	 * Static method to create a new JMS endpoint. You need to set the Broker URI, the Gateway
	 * Topic, the Endpoint ID, the Thread Pool size and specify whether to establish a durable or a
	 * temporary connection. Usually you will establish temporary connections for clients. Durable
	 * connections usually are used only for server side services. Please be aware that this
	 * constructor does check for duplicate endpoint IDs in a messaging infrastructure. An exception
	 * is raised in case another instance with the same endpoint ID is already connected to the
	 * selected JMS topic.
	 *
	 * @param aBrokerURI URI of the Message Broker (e.g.
	 * <tt>tcp://[host]:61616</tt>)
	 * @param aGatewayTopic Name of the topic for the JMS gateway on the JMS broker
	 * @param aGatewayId ID of the jWebSocket JMS Gateway to use for jWebSocket services
	 * @param aEndPointId ID of the endpoint used for this JMS connection
	 * @param aThreadPoolSize Maximum number of threads used to process requests concurrently
	 * @param aDurable <tt>JMSEndPoint.TEMPORARY</tt> (for clients and servers) or
	 * <tt>JMSEndPoint.DURABLE</tt> for durable connections (for durable services only)
	 * @return A new JMSEndPoint instance (in case of success), otherwise an exception will be
	 * raised.
	 * @throws JMSException
	 */
	public static JMSEndPoint getInstance(String aBrokerURI, String aGatewayTopic,
			String aGatewayId, String aEndPointId, int aThreadPoolSize,
			boolean aDurable) throws JMSException {
		// create an "empty" endpoint instance
		JMSEndPoint lEP = new JMSEndPoint();
		// and initialize it
		// checking for duplicate endpoints and raising exception if such
		lEP.init(aBrokerURI, aGatewayTopic,
				aGatewayId, aEndPointId, aThreadPoolSize,
				aDurable);
		// return JMS Endpoint instance in case of success
		return lEP;
	}

	public static JMSEndPoint getInstance(Connection aConnection,
			Session aSession, Topic aGatewayTopic, MessageProducer aProducer,
			MessageConsumer aConsumer, int aThreadPoolSize,
			boolean aDurable) throws JMSException {
		// create an "empty" endpoint instance
		JMSEndPoint lEP = new JMSEndPoint();
		// and take over settings from JMS gateway
		lEP.init(aConnection, aSession, aGatewayTopic, aProducer, aConsumer,
				aThreadPoolSize, aDurable);
		// return JMS Endpoint instance in case of success
		return lEP;
	}

	/**
	 * Starts the JMS connection to send or broadcast messages and to listen on incoming messages.
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
	 * Adds a new listener to the JMS Gateway Client. The listener must implement the
	 * IJMSMessageListener interface and can listen to unqualified messages, to text messages,
	 * binary messages, map messages and object messages.
	 *
	 * @param aListener
	 */
	public void addListener(IJMSMessageListener aListener) {
		// add the listener
		mListener.addMessageListener(aListener);
	}

	/**
	 * Removes a listener from the JMS Gateway Client. The listener will not be destroyed but not be
	 * called anymore after this call.
	 *
	 * @param aListener
	 */
	public void removeListener(IJMSMessageListener aListener) {
		mListener.removeMessageListener(aListener);
	}

	/**
	 * Returns if the JMS Endpoint is already shut down.
	 *
	 * @return <tt>true</tt> if the JMS Endpoint shutdown, otherwise
	 * <tt>false></tt>.
	 */
	public boolean isShutdown() {
		return mShutDown;
	}

	/**
	 * Returns if the JMS Endpoint connection is already open.
	 *
	 * @return <tt>true</tt> if the JMS Endpoint connection is open, otherwise
	 * <tt>false></tt>.
	 */
	public boolean isOpen() {
		return !isShutdown();
	}

	/**
	 * Shuts down the current instance of the JMS Endpoint. It closes the JMS session and the JMS
	 * connection and sets the shutDown flag for the application.
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

		if (mReleaseJWSResources) {
			Tools.stopUtilityTimer();
			Tools.stopUtilityThreadPool();
		}
		
		// to end potential console loops
		mShutDown = true;
	}

	/**
	 * @return the EndPoint-Id of this JMS Endpoint. This endpoint id is ensured to be unique within
	 * one JMS topic.
	 */
	public String getEndPointId() {
		return mEndPointId;
	}

	/**
	 * @return the Id of the jWebSocket JMS Gateway Endpoint. This ID is used to utilize services
	 * from a jWebSocket server connected to the JMS broker.
	 */
	public String getGatewayId() {
		return mGatewayId;
	}

	/**
	 * @return the JMS Session used for this JMS Endpoint. The session object may be used to create
	 * new messages, new consumers or even to create new queues or topics.
	 */
	public Session getSession() {
		return mSession;
	}

	/**
	 * @return the Message Producer of this JMS Endpoint. The produced may be used to send messages.
	 * Usually an application will not make use of this low level method.
	 */
	public MessageProducer getProducer() {
		return mProducer;
	}

}
