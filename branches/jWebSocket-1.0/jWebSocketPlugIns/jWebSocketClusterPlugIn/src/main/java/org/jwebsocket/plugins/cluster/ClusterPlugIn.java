//	---------------------------------------------------------------------------
//	jWebSocket - Cluster Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.cluster;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author aschulze
 */
public class ClusterPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	// if namespace changed update client plug-in accordingly!
	/**
	 *
	 */
	public static final String NS_CLUSTER =
			JWebSocketServerConstants.NS_BASE + ".plugins.cluster";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ClusterPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket ClusterPlugIn - Community Edition";
	private ActiveMQConnectionFactory mConnectionFactory;
	private Topic mClusterTopic;
	private Topic mAdvisoryTopic;
	private ClusterSender mClusterSender;
	private static ApplicationContext mBeanFactory;
	private static Settings mSettings;

	/**
	 *
	 * @param aConfiguration
	 */
	public ClusterPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating cluster plug-in...");
		}

		// specify default name space for cluster plugin
		this.setNamespace(NS_CLUSTER);

		try {
			mBeanFactory = getConfigBeanFactory();
			if (null == mBeanFactory) {
				mLog.error("No or invalid spring configuration for cluster plug-in, some features may not be available.");
			} else {
				mSettings = (Settings) mBeanFactory.getBean("org.jwebsocket.plugins.cluster.settings");
				if (mLog.isInfoEnabled()) {
					mLog.info("Cluster plug-in successfully instantiated.");
				}
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating cluster plug-in"));
		}
		if (null != mSettings) {
			mConnectionFactory = new ActiveMQConnectionFactory(
					mSettings.getBrokerURI());
			try {
				Connection lConnection = mConnectionFactory.createConnection();
				Session lSession = lConnection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
				// connect to message broker
				lConnection.start();

				String lNodeId = mSettings.getNodeId();

				mClusterTopic = lSession.createTopic(mSettings.getClusterTopic());
				mAdvisoryTopic = lSession.createTopic(mSettings.getAdvisoryTopic());

				// create advisory consumer
				// used to listen to the advisory messages from ActiveMQ
				MessageConsumer lAdvisoryConsumer = lSession.createConsumer(mAdvisoryTopic);
				ClusterAdvisoryListener lAdvisoryListener = new ClusterAdvisoryListener();
				lAdvisoryConsumer.setMessageListener(lAdvisoryListener);
				if (mLog.isDebugEnabled()) {
					mLog.debug("Cluster advisory listener successfully allocated.");
				}

				// create producer
				// used to send the messages to the other nodes
				MessageProducer lProducer = lSession.createProducer(mClusterTopic);
				mClusterSender = new ClusterSender(lSession, lProducer, mClusterTopic, lNodeId);

				// create message consumer
				// used to listen to the messages from the other cluster nodes
				String lSelector = "JMSCorrelationID = '" + lNodeId + "'";
				MessageConsumer lConsumer = lSession.createConsumer(mClusterTopic, lSelector);
				ClusterListener lListener = new ClusterListener(mClusterSender);
				lConsumer.setMessageListener(lListener);
				if (mLog.isDebugEnabled()) {
					mLog.debug("Cluster listener successfully allocated.");
				}

				// broadcast status request from other nodes
				Token lToken = TokenFactory.createToken("ojc", "register");
				lToken.setString("sourceId", lNodeId);
				mClusterSender.sendToken(lToken);

			} catch (JMSException lEx) {
				mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating cluster plug-in"));
			}
		}
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getVendor() {
		return VENDOR;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getLicense() {
		return LICENSE;
	}

	@Override
	public String getNamespace() {
		return NS_CLUSTER;
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		// a new client connected to this node
		// so inform the other nodes about this event
		if (null != mClusterSender) {
			mClusterSender.send(
					"{\"ns\":\"ojc\""
					+ ",\"type\":\"connected\""
					+ ",\"sourceId\":\"" + mSettings.getNodeId() + "\""
					+ ",\"clientIds\":[]}");
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if (null != mClusterSender) {
			mClusterSender.send(
					"{\"ns\":\"ojc\""
					+ ",\"type\":\"disconnected\""
					+ ",\"sourceId\":\"" + mSettings.getNodeId() + "\""
					+ ",\"clientIds\":[]}");
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if ("".equals(lType)) {
				mthd(aConnector, aToken);
			}
		}
	}

	private void mthd(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// String lMessage = aToken.getString("message", "Default Text Message");
		// Integer lCount = aToken.getInteger("count", 50);

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);


		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
}
