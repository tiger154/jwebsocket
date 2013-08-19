//	---------------------------------------------------------------------------
//	jWebSocket - JMS Engine (Community Edition, CE)
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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.jms.api.IConnectorsManager;
import org.jwebsocket.jms.api.INodesManager;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author kyberneees
 */
public class JMSEngine extends BaseEngine {

	private static Logger mLog = Logging.getLogger();
	private String mDestination;
	private final String NS = "org.jwebsocket.jms.engine";
	private ApplicationContext mBeanFactory;
	private Connection mConnection;
	private Session mSession;
	private JMSMessageListener mMessageListener;
	private IConnectorsManager mConnectorsManager;
	private String mNodeId = JWebSocketConfig.getConfig().getNodeId();
	private JMSLoadBalancer mLB;

	public JMSEngine(EngineConfiguration aConfiguration) {
		super(aConfiguration);

		// getting engine bean factory
		JWebSocketBeanFactory.load(NS, JWebSocketConfig.getConfigFolder("JMSEngine/jms.xml"),
				getClass().getClassLoader());
		mBeanFactory = JWebSocketBeanFactory.getInstance(NS);

		// getting the destination (node communication channel)
		mDestination = (String) getConfiguration().getSettings()
				.get("destination");
	}

	public String getNodeId() {
		return mNodeId;
	}

	public String getDestination() {
		return mDestination;
	}

	public Session getSession() {
		return mSession;
	}

	public IConnectorsManager getConnectorsManager() {
		return mConnectorsManager;
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
			// creating the session
			mSession = mConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// initializing connectors manager
			mConnectorsManager = (IConnectorsManager) mBeanFactory.getBean("connectorsManager");
			mConnectorsManager.setEngine(this);
			// producer will take the destination on send operation
			// @see: http://activemq.apache.org/how-should-i-implement-request-response-with-jms.html
			mConnectorsManager.setReplyProducer(mSession.createProducer(null));
			mConnectorsManager.initialize();

			// creating message listener
			mMessageListener = new JMSMessageListener(this);
			mMessageListener.initialize();

			// creating the load balancer
			mLB = new JMSLoadBalancer(mNodeId, mDestination, mSession,
					(INodesManager) mBeanFactory.getBean("nodesManager"));
			mLB.initialize();
		} catch (Exception lEx) {
			throw new WebSocketException(lEx);
		}

		if (mLog.isDebugEnabled()) {
			mLog.info("JmsEngine successfully started! Listenning on topic: '"
					+ mDestination + "'...");
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
		mSession.close();
		// closing the JMS connection
		mConnection.close();

		if (mLog.isDebugEnabled()) {
			mLog.info("Engine successfully stopped!");
		}
	}
}
