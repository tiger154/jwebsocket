//	---------------------------------------------------------------------------
//	jWebSocket - JMSEngine (Community Edition, CE)
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.jms.api.IConnectorsManager;
import org.jwebsocket.jms.api.INodesManager;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * TODO: Support origin based authorization check.
 *
 * @author Rolando Santamaria Maso
 */
public class JMSEngine extends BaseEngine {

	private final static Logger mLog = Logging.getLogger();
	private String mDestination;
	public static final String NS = "org.jwebsocket.jms.engine";
	private final ApplicationContext mBeanFactory;
	private Connection mConnection;
	private Session mSessionForClients, mSessionForServer;
	private JMSMessageListener mMessageListener;
	private IConnectorsManager mConnectorsManager;
	private MessageProducer mReplyProducer;
	private final String mNodeId = JWebSocketConfig.getConfig().getNodeId();
	private JMSLoadBalancer mLB;
	private INodesManager mNodesManager;

	/**
	 *
	 * @param aConfiguration
	 */
	public JMSEngine(EngineConfiguration aConfiguration) {
		super(aConfiguration);

		// getting engine bean factory
		String lSpringConfig = (String) getConfiguration().getSettings().get("spring_config");
		Assert.notNull(lSpringConfig, "Missing 'spring_config' configuration setting!");

		JWebSocketBeanFactory.load(NS, JWebSocketConfig.expandEnvVarsAndProps(lSpringConfig),
				getClass().getClassLoader());
		mBeanFactory = JWebSocketBeanFactory.getInstance(NS);

		// getting the destination (node communication channel)
		mDestination = (String) getConfiguration().getSettings()
				.get("destination");
	}

	/**
	 *
	 * @return
	 */
	public String getNodeId() {
		return mNodeId;
	}

	/**
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
	public String getDestination() {
		return mDestination;
	}

	/**
	 *
	 * @return
	 */
	public Session getSession() {
		return mSessionForClients;
	}

	/**
	 *
	 * @return
	 */
	public INodesManager getNodesManager() {
		return mNodesManager;
	}

	@Override
	public Map<String, WebSocketConnector> getConnectors() {
		// temporal patch to support InternalClient instances
		Map<String, WebSocketConnector> lConnectors = new HashMap<String, WebSocketConnector>();
		lConnectors.putAll(super.getConnectors());

		try {
			lConnectors.putAll(mConnectorsManager.getAll());
			return lConnectors;
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		try {
			// removing connector from database
			mConnectorsManager.remove(aConnector.getId());
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "removing connector data from database"));
		}

		// notify servers that a connector has stopped
		for (final WebSocketServer lServer : getServers().values()) {
			lServer.connectorStopped(aConnector, aCloseReason);
		}
	}

	@Override
	public WebSocketConnector getConnectorById(String aConnectorId) {
		try {
			return mConnectorsManager.getConnectorById(aConnectorId);
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}
	}

	/**
	 *
	 * @return
	 */
	public IConnectorsManager getConnectorsManager() {
		return mConnectorsManager;
	}

	public void shutdownNode(String aNodeId) throws Exception {
		Assert.isTrue(mNodesManager.exists(aNodeId), "The node '" + aNodeId + "' does not exists!");

		MapMessage lMessage = mSessionForServer.createMapMessage();
		lMessage.setStringProperty(Attributes.MESSAGE_TYPE, MessageType.SHUTDOWN_NODE.toString());
		lMessage.setStringProperty(Attributes.NODE_ID, aNodeId);

		mLB.getNodesMessagesProducer().send(lMessage);
	}

	@Override
	public void startEngine() throws WebSocketException {
		try {
			// getting destination
			mDestination = (String) mBeanFactory.getBean("destination");

			ConnectionFactory lFactory = (ConnectionFactory) mBeanFactory.getBean("connectionFactory");
			// creating the connection
			mConnection = lFactory.createConnection();

			// starting the connection
			mConnection.start();
			// creating the sessions
			mSessionForClients = mConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			mSessionForServer = mConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			// setting the global reply producer
			mReplyProducer = mSessionForClients.createProducer(mSessionForClients.createTopic(mDestination));

			// initializing connectors manager
			mConnectorsManager = (IConnectorsManager) mBeanFactory.getBean("connectorsManager");
			mConnectorsManager.setEngine(this);
			// creating message listener
			mMessageListener = new JMSMessageListener(this);
			mMessageListener.initialize();

			// creating the load balancer
			mNodesManager = (INodesManager) mBeanFactory.getBean("nodesManager");

			final INodesManager lNodesManager = (INodesManager) mBeanFactory.getBean("nodesManager");
			// getting the hostname
			String lHostname = getConfiguration().getHostname();
			if (null == lHostname) {
				lHostname = JWebSocketServerConstants.DEFAULT_HOSTNAME;
			}
			mLB = new JMSLoadBalancer(mNodeId, mDestination, mSessionForClients,
					mSessionForServer, mNodesManager, lHostname) {
						@Override
						public void shutdown() throws Exception {
							// close clients if all nodes are down
							if (lNodesManager.count() == 1) {
								Iterator<WebSocketConnector> lIt = JWebSocketFactory
								.getTokenServer().getAllConnectorsIterator();

								while (lIt.hasNext()) {
									lIt.next().stopConnector(CloseReason.SHUTDOWN);
								}
							}

							// shutdown load balancer
							super.shutdown();
						}
					};
		} catch (Exception lEx) {
			throw new WebSocketException(lEx);
		}

		// notifying servers
		super.engineStarted();

		if (mLog.isDebugEnabled()) {
			mLog.info("Engine successfully started! Listenning on topic: '"
					+ mDestination + "'...");
		}
	}

	@Override
	public void stopEngine(CloseReason aCloseReason) throws WebSocketException {
		super.engineStopped();
	}

	/**
	 *
	 * @return
	 */
	public MessageProducer getReplyProducer() {
		return mReplyProducer;
	}

	@Override
	public void systemStarted() throws Exception {
		super.systemStarted();

		// initializing connectors manager
		mConnectorsManager.initialize();
		// initializing load balancer once the plugins and filters are started
		mLB.initialize();
	}

	@Override
	public void broadcastPacket(WebSocketConnector aSource, WebSocketPacket aDataPacket) {
		try {
			ActiveMQTextMessage lMessage = new ActiveMQTextMessage();
			lMessage.setBooleanProperty(Attributes.IS_BROADCAST, true);
			lMessage.setText(aDataPacket.getString());

			mReplyProducer.send(lMessage);
		} catch (JMSException ex) {
			mLog.error(Logging.getSimpleExceptionMessage(ex, "broadcasting packet"));
		}
	}

	@Override
	public Map<String, WebSocketConnector> getSharedSessionConnectors(String aSessionId) {
		try {
			return mConnectorsManager.getSharedSession(aSessionId);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "getting connectors that share the session"));
			return new HashMap<String, WebSocketConnector>();
		}
	}

	@Override
	public void systemStopping() throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.info("Stopping engine...");
		}

		// stopping node load balancer
		mLB.shutdown();
		// stopping node message listener
		mMessageListener.shutdown();
		// stopping connectors manager
		mConnectorsManager.shutdown();
		// closing the JMS session
		mSessionForClients.close();
		mSessionForServer.close();
		// closing the JMS connection
		mConnection.close();

		if (mLog.isDebugEnabled()) {
			mLog.info("Engine successfully stopped!");
		}
	}

	@Override
	public Long getConnectorsCount() {
		try {
			return mConnectorsManager.count();
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}
	}

	@Override
	public Iterator<WebSocketConnector> getConnectorsIterator() {
		return mConnectorsManager.getIterator();
	}
}
