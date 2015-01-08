//	---------------------------------------------------------------------------
//	jWebSocket - JMSPlugIn (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License att
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.jms;

/**
 *
 * @author Johannes Smutny, Alexander Schulze
 */
import java.util.List;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javolution.util.FastList;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.config.xml.EngineConfig;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.jms.endpoint.JWSEndPoint;
import org.jwebsocket.jms.endpoint.JWSResponseTokenListener;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.kit.WebSocketSession;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jms.gateway.JMSAdvisoryListener;
import org.jwebsocket.plugins.jms.gateway.JMSConnector;
import org.jwebsocket.plugins.jms.gateway.JMSEngine;
import org.jwebsocket.plugins.jms.gateway.JMSListener;
import org.jwebsocket.plugins.jms.gateway.JMSLogger;
import org.jwebsocket.plugins.jms.gateway.JMSSender;
import org.jwebsocket.plugins.jms.gateway.JMSTransportListener;
import org.jwebsocket.plugins.jms.util.ActionJms;
import org.jwebsocket.plugins.jms.util.FieldJms;
import org.jwebsocket.plugins.jms.util.RightJms;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Alexander Schulze
 */
public class JMSPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	private static final String NS_JMS = JWebSocketServerConstants.NS_BASE + ".plugins.jms";
	private final static String VERSION = "1.0.5";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket JMSPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket JMSPlugIn - Community Edition";
	private JmsManager mJmsManager = null;
	private JMSEngine mJMSEngine = null;
	private ActiveMQConnectionFactory mConnectionFactory;
	private Connection mConnection;
	private Session mConsumerSession, mProducerSession;
	private MessageConsumer mConsumer;
	private JMSLogger mJMSLogger;
	private MessageProducer mProducer;
	private JMSSender mSender;
	private JMSListener mListener;
	private MessageConsumer mAdvisoryConsumer;
	private Settings mSettings;
	private String mBrokerURI;
	private String mEndPointId;
	private String mGatewayTopicId;
	private String mAdvisoryTopicId;
	private JMSTransportListener mTransportListener;

	private List<String> mDomains = new FastList<String>();
	private boolean mInitialized = false;
	private JWSEndPoint mJWSEndPoint = null;
	private Topic mGatewayTopic = null;

	/**
	 *
	 * @param aConfiguration
	 */
	public JMSPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating JMS plug-in...");
		}

		this.setNamespace(NS_JMS);
		try {
			ApplicationContext lBeanFactory = getConfigBeanFactory(NS_JMS);
			mJmsManager = JmsManager.getInstance(aConfiguration.getSettings(),
					lBeanFactory);
			mSettings = (Settings) lBeanFactory.getBean("org.jwebsocket.plugins.jms.settings");

			mDomains = new FastList<String>();
			mDomains.add("*");
		} catch (BeansException lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx,
					"instantiating JMS client."));
		}
	}

	private void init() {
		// check if already initialized
		if (mInitialized) {
			return;
		} else {
			mInitialized = true;
		}
		EngineConfig lEngineCfg = new EngineConfig(
				"jmsgw0", // id
				"jWebSocket JMSGateway", // name 
				"-", // jar
				0, // port
				0, // ssl port
				JWebSocketServerConstants.DEFAULT_HOSTNAME,
				"-", // keystore
				"-", // keystore pw
				"-", // context
				"-", // servlet
				0, // timeout
				65536, // max frame size
				mDomains, // domains
				1000, // max connections
				"-", // max connection stretegy
				false, // notify on system stopping
				null, // settings
				JWebSocketServerConstants.KEEP_ALIVE_CONNECTORS, // defaults to false
				JWebSocketServerConstants.KEEP_ALIVE_CONNECTORS_INTERVAL,
				JWebSocketServerConstants.KEEP_ALIVE_CONNECTORS_TIMEOUT
		);
		mJMSEngine = new JMSEngine(lEngineCfg);

		// Advisory listener
		// setting up the JMS Gateway
		mBrokerURI = mSettings.getBrokerURI();
		mEndPointId = mSettings.getEndPointId();
		mGatewayTopicId = mSettings.getGatewayTopic();
		mAdvisoryTopicId = mSettings.getAdvisoryTopic();

		mConnectionFactory = new ActiveMQConnectionFactory(mBrokerURI);
		mConnectionFactory.setConnectionIDPrefix(mEndPointId);

		try {
			// registering JMSEngine once JMS connection is already started
			Map<String, WebSocketEngine> lEngines = JWebSocketFactory.getEngines();
			lEngines.put(lEngineCfg.getId(), mJMSEngine);
			List<WebSocketServer> lServers = JWebSocketFactory.getServers();
			for (WebSocketServer lServer : lServers) {
				// automatically registers the server at the engine (cross connection)
				lServer.addEngine(mJMSEngine);
			}

			mConnection = mConnectionFactory.createConnection();

			// add the transport listener to listen to connect and disconnect from broker events
			mTransportListener = new JMSTransportListener(this);
			((ActiveMQConnection) mConnection).addTransportListener(mTransportListener);

			// setting the clientID is required for durable subscribers as well as to avoid/identify multiple endpoints with same endpoint id
			mConnection.setClientID(mEndPointId);

			// if we detect a duplicate endpoint id this start operation
			// will fail and cause an exception, such that we cannot
			// connect to the queue or topic.
			mConnection.start();

			mConsumerSession = mConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			mProducerSession = mConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			mGatewayTopic = mConsumerSession.createTopic(mGatewayTopicId);
			mProducer = mProducerSession.createProducer(mGatewayTopic);
			mSender = new JMSSender(mConsumerSession, mProducer, mEndPointId);

			// creating message consumers
			createConsumers();

			// give a success message to the administrator
			if (mLog.isInfoEnabled()) {
				mLog.info("JMS plug-in successfully instantiated, JMS Gateway endpoint id: '" + mEndPointId + "'.");
			}
			/*
			 mJWSEndPoint = JWSEndPoint.getInstance(
			 mConnection,
			 mSession,
			 mGatewayTopic,
			 mProducer,
			 mConsumer,
			 5,
			 JMSEndPoint.TEMPORARY // durable (for servers) or temporary (for clients)
			 );
			 lJWSEndPoint.addRequestListener(
			 "org.jwebsocket.jms.gateway", "echo", new JWSMessageListener(lJWSEndPoint) {
			 @Override
			 public void processToken(String aSourceId, Token aToken) {
			 mLog.info("Answering 'echo'...");
			 Token lToken = TokenFactory.createToken("org.jwebsocket.jms.gateway", "echo");
			 sendToken(aSourceId, lToken);
			 }
			 }
			 );
			 */
		} catch (JMSException lEx) {
			// cleanup engines and servers to allow at least other engines to run without side effects
			Map<String, WebSocketEngine> lEngines = JWebSocketFactory.getEngines();
			lEngines.remove(lEngineCfg.getId());
			List<WebSocketServer> lServers = JWebSocketFactory.getServers();
			for (WebSocketServer lServer : lServers) {
				// automatically removes the server at the engine (cross connection)
				lServer.removeEngine(mJMSEngine);
			}
			mLog.error(Logging.getSimpleExceptionMessage(lEx,
					"connecting JMS gateway, de-registered JMS engine from server environment."));
		}
	}

	private void createConsumers() throws JMSException {
		// we use a durable subscriber here to allow a restart 
		// of the jWebSocket server instance.
		mConsumer = // mSession.createDurableSubscriber(
				mConsumerSession.createConsumer(
						mGatewayTopic,
						"targetId='" + mEndPointId + "' or (targetId='*' and sourceId<>'" + mEndPointId + "')");
		mListener = new JMSListener(mJMSEngine, mSender);
		mConsumer.setMessageListener(mListener);

		// create the listener to the advisory topic
		Topic lAdvisoryTopic = mConsumerSession.createTopic(mAdvisoryTopicId);
		mAdvisoryConsumer = mConsumerSession.createConsumer(lAdvisoryTopic);
		JMSAdvisoryListener lAdvisoryListener = new JMSAdvisoryListener(
				this, mJMSEngine, mSender, mSettings.getBroadcastAdvisoryEvents());
		mAdvisoryConsumer.setMessageListener(lAdvisoryListener);

		/*
		 Topic lConnectionTopic = mSession.createTopic("ActiveMQ.Advisory.Connection");
		 MessageConsumer mConnectionConsumer = mSession.createConsumer(lConnectionTopic);
		 mConnectionConsumer.setMessageListener(lAdvisoryListener);
		 */
		if (mSettings.getLoggerActive()) {
			mJMSLogger = new JMSLogger(mConnection, new Destination[]{mGatewayTopic, lAdvisoryTopic});
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
		return NS_JMS;
	}

	@Override
	public void sessionStarted(WebSocketConnector aConnector, WebSocketSession aSession) {
		if (aConnector instanceof JMSConnector) {
			Token lToken = TokenFactory.createToken(
					"org.jwebsocket.jms.gateway",
					"welcome");
			aConnector.sendPacket(JSONProcessor.tokenToPacket(lToken));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if (null != mJmsManager) {
			mJmsManager.stopListener(aConnector.getId());
		}
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Engine '" + aEngine.getId() + "' started.");
		}
		init();
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		if (null != mJmsManager) {
			mJmsManager.shutDownListeners();
		}
		// shutdown JMS bridge listener
		/*
		 if (null != mJms2JwsListenerCont) {
		 mJms2JwsListenerCont.shutdown();
		 }
		 */
		// shutdown message listener
		if (null != mConsumer) {
			try {
				mConsumer.close();
				mConsumerSession.close();
			} catch (JMSException lEx) {
			}
		}
		if (null != mProducer) {
			try {
				mProducer.close();
				mProducerSession.close();
			} catch (JMSException lEx) {
			}
		}
		// shutdown advisory listener
		if (null != mAdvisoryConsumer) {
			try {
				mAdvisoryConsumer.close();
			} catch (JMSException lEx) {
			}
		}

		// this one processes potential exceptions itself
		mJMSLogger.close();

		if (null != mConnection) {
			try {
				mConnection.stop();
			} catch (JMSException lEx) {
			}
			try {
				mConnection.close();
			} catch (JMSException lEx) {
			}
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if (null == JWebSocketBeanFactory.getInstance()) {
			sendMissingBeanFactoryResponseToken(aConnector, aToken);
		} else if (null == mJmsManager) {
			sendMissingJmsManagerResponseToken(aConnector, aToken);
		} else {
			processToken(aConnector, aToken);
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void processToken(WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();

		if (lType == null) {
			return;
		}

		switch (ActionJms.get(lType)) {
			case LISTEN:
				listen(aConnector, aToken);
				break;
			case LISTEN_MESSAGE:
				listenMessage(aConnector, aToken);
				break;
			case SEND_TEXT:
				sendText(aConnector, aToken);
				break;
			case SEND_TEXT_MESSAGE:
				sendTextMessage(aConnector, aToken);
				break;
			case SEND_MAP:
				sendMap(aConnector, aToken);
				break;
			case SEND_MAP_MESSAGE:
				sendMapMessage(aConnector, aToken);
				break;
			case UNLISTEN:
				unlisten(aConnector, aToken);
				break;
			case IDENTIFY:
				identify(aConnector, aToken);
				break;
			case PING:
				ping(aConnector, aToken);
				break;
			case IS_BROKER_CONNECTED:
				isBrokerConnected(aConnector, aToken);
				break;
			case TEST:
				test(aConnector, aToken);
		}
	}

	private void unlisten(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'unlisten'...");
		}
		executeAction(createActionInput(aConnector, aToken,
				"Successfully unlisten JMS listener"), new ActionCommand() {
					@Override
					void execute(ActionInput aInput) throws Exception {
						if (null != mJmsManager) {
							mJmsManager.deregisterConnectorFromMessageListener(aInput);
						}
					}
				});
	}

	private void listen(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'listen'...");
		}
		executeAction(createActionInput(aConnector, aToken,
				"Successfully got JMS listener", RightJms.LISTEN,
				RightJms.SEND_AND_LISTEN), new ActionCommand() {
					@Override
					void execute(ActionInput aInput) throws Exception {
						if (null != mJmsManager) {
							mJmsManager.registerConnectorWithListener(aInput, JMSPlugIn.this);
						}
					}
				});
	}

	private void listenMessage(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'listenMessage'...");
		}
		executeAction(createActionInput(aConnector, aToken,
				"Successfully got JMS message listener", RightJms.LISTEN,
				RightJms.SEND_AND_LISTEN), new ActionCommand() {
					@Override
					void execute(ActionInput aInput) throws Exception {
						if (null != mJmsManager) {
							mJmsManager.registerConnectorWithMessageListener(aInput, JMSPlugIn.this);
						}
					}
				});
	}

	private void sendText(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'sendText'...");
		}
		executeAction(createActionInput(aConnector, aToken,
				"Text successfully sent", RightJms.SEND, RightJms.SEND_AND_LISTEN),
				new ActionCommand() {
					@Override
					void execute(ActionInput aInput) throws Exception {
						if (null != mJmsManager) {
							mJmsManager.sendText(aInput);
						}
					}
				});
	}

	private void sendTextMessage(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'sendTextMessage'...");
		}
		executeAction(createActionInput(aConnector, aToken,
				"JMS text message successfully sent", RightJms.SEND,
				RightJms.SEND_AND_LISTEN), new ActionCommand() {
					@Override
					void execute(ActionInput aInput) throws Exception {
						if (null != mJmsManager) {
							mJmsManager.sendTextMessage(aInput);
						}
					}
				});
	}

	private void sendMap(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'sendMap'...");
		}
		executeAction(
				createActionInput(aConnector, aToken, "Map message successfully sent", RightJms.SEND,
						RightJms.SEND_AND_LISTEN), new ActionCommand() {
					@Override
					void execute(ActionInput aInput) throws Exception {
						if (null != mJmsManager) {
							mJmsManager.sendMap(aInput);
						}
					}
				});
	}

	private void sendMapMessage(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'sendMapMessage'...");
		}
		executeAction(createActionInput(aConnector, aToken,
				"JMS map message successfully sent", RightJms.SEND,
				RightJms.SEND_AND_LISTEN), new ActionCommand() {
					@Override
					void execute(ActionInput aInput) throws Exception {
						if (null != mJmsManager) {
							mJmsManager.sendMapMessage(aInput);
						}
					}
				});
	}

	private void executeAction(ActionInput aInput, ActionCommand aCommand) {
		if (!actionIsExecutable(aInput)) {
			return;
		}

		if (!executeCommand(aInput, aCommand)) {
			return;
		}

		sendPositiveToken(aInput);
	}

	private boolean executeCommand(ActionInput aInput, ActionCommand aCommand) {
		try {
			aCommand.execute(aInput);
			return true;
		} catch (Exception e) {
			sendNegativeToken(aInput, e);
			return false;
		}
	}

	private boolean actionIsExecutable(ActionInput aInput) {
		return isDestinationIdentifierValid(aInput) && hasRight(aInput);
	}

	private void sendPositiveToken(ActionInput aInput) {
		setCodeAndMsg(aInput.mResToken, 0, aInput.mPositiveMsg);
		sendToken(aInput);
	}

	private void sendErrorToken(WebSocketConnector aConnector, Token aToken,
			String aMessage) {
		Token lResponseToken = createResponse(aToken);
		setCodeAndMsg(lResponseToken, -1, aMessage);
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void sendNotConnectedToken(WebSocketConnector aConnector, Token aToken) {
		sendErrorToken(aConnector, aToken,
				"not connected to message queue or topic");
	}

	private void sendTargetNotFound(WebSocketConnector aConnector, Token aToken) {
		sendErrorToken(aConnector, aToken, "No target connector with endpoint-id '"
				+ aToken.getString("targetId") + "' found.");
	}

	private void sendMissingJmsManagerResponseToken(WebSocketConnector aConnector,
			Token aToken) {
		Token lResponseToken = createResponse(aToken);
		setCodeAndMsg(lResponseToken, -1,
				"missing JMS manager: correct your config");
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void sendMissingBeanFactoryResponseToken(WebSocketConnector aConnector, Token aToken) {
		Token lResponseToken = createResponse(aToken);
		setCodeAndMsg(lResponseToken, -1,
				"missing JMS spring beanfactory: correct your config");
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void sendNegativeToken(ActionInput aInput, Exception aEx) {
		setCodeAndMsg(aInput.mResToken, -1, aEx.getMessage());
		sendToken(aInput);
	}

	private boolean isDestinationIdentifierValid(ActionInput aInput) {
		if (aInput.mDi.isMissingData()) {
			setCodeAndMsg(aInput.mResToken, -1,
					"Missing destination identifier input  data");
			sendToken(aInput);
			return false;
		}
		return true;
	}

	private void setCodeAndMsg(Token aToken, int aCode, String aMsg) {
		aToken.setInteger(FieldJms.CODE.getValue(), aCode);
		aToken.setString(FieldJms.MSG.getValue(), aMsg);
	}

	private void sendToken(ActionInput aInput) {
		sendToken(aInput.mConnector, aInput.mConnector, aInput.mResToken);
	}

	private void sendAccessDeniedToken(ActionInput aInput) {
		aInput.mResToken = createAccessDenied(aInput.mResToken);
		sendToken(aInput);
	}

	private boolean hasRight(ActionInput aInput) {
		if (null == aInput.mRights || aInput.mRights.length == 0) {
			return true;
		}

		for (RightJms next : aInput.mRights) {
			if (hasAuthority(aInput.mConnector,
					NS_JMS
					+ "." + next
					+ "." + (aInput.mDi.isPubSubDomain() ? "topic" : "queue")
					+ "." + aInput.mDi.getDestinationName())) {
				return true;
			}
		}

		sendAccessDeniedToken(aInput);
		return false;
	}

	private ActionInput createActionInput(WebSocketConnector aConnector, Token aToken, String aPositiveMsg,
			RightJms... aRights) {
		return new ActionInput(aConnector, aToken, aPositiveMsg, aRights);

	}

	/**
	 * @return the mSettings
	 */
	public Settings getSpringSettings() {
		return mSettings;
	}

	/**
	 * @return the mConnection
	 */
	public Connection getConnection() {
		return mConnection;
	}

	/**
	 * @return the mSession
	 */
	public Session getSession() {
		return mConsumerSession;
	}

	/**
	 * @return the mConsumer
	 */
	public MessageConsumer getConsumer() {
		return mConsumer;
	}

	/**
	 * @return the mProducer
	 */
	public MessageProducer getProducer() {
		return mProducer;
	}

	/**
	 * @return the mAdvisoryConsumer
	 */
	public MessageConsumer getAdvisoryConsumer() {
		return mAdvisoryConsumer;
	}

	class ActionInput {

		WebSocketConnector mConnector;
		Token mReqToken;
		Token mResToken;
		String mPositiveMsg;
		RightJms[] mRights;
		DestinationIdentifier mDi;

		private ActionInput(WebSocketConnector aConnector, Token aToken,
				String aPositiveMsg, RightJms... aRights) {
			mDi = DestinationIdentifier.valueOf(aToken);
			mConnector = aConnector;
			mReqToken = aToken;
			mResToken = createResponse(aToken);
			mPositiveMsg = aPositiveMsg;
			mRights = aRights;
			mDi.setDestinationIdentifier(mResToken);
			mDi.setDestinationIdentifier(mReqToken);
		}
	}

	private abstract class ActionCommand {

		abstract void execute(ActionInput aInput) throws Exception;
	}

	private void ping(WebSocketConnector aConnector, Token aToken) {
		if (null != mSender) {
			String lTargetId = aToken.getString("targetId");
			if (null == getServer().getConnector(lTargetId)) {
				sendTargetNotFound(aConnector, aToken);
				return;
			}
			Integer lUTID = aToken.getInteger("utid");
			Token lToken = TokenFactory.createToken("org.jwebsocket.jms.gateway", "ping");
			lToken.setString("sourceId", aConnector.getId());
			lToken.setString("gatewayId", mEndPointId);
			lToken.setInteger("utid", lUTID);
			try {
				mSender.sendText(lTargetId, JSONProcessor.tokenToPacket(lToken).getUTF8());
			} catch (JMSException lEx) {
				mLog.debug(Logging.getSimpleExceptionMessage(lEx, "ping"));
				sendErrorToken(aConnector, aToken, lEx.getMessage() + "on ping");
			}
		} else {
			sendNotConnectedToken(aConnector, aToken);
		}
	}

	private void identify(WebSocketConnector aConnector, Token aToken) {
		if (null != mSender) {
			String lTargetId = aToken.getString("targetId");
			Integer lUTID = aToken.getInteger("utid");
			Token lToken = TokenFactory.createToken("org.jwebsocket.jms.gateway", "identify");
			lToken.setString("sourceId", aConnector.getId());
			lToken.setString("gatewayId", mEndPointId);
			lToken.setInteger("utid", lUTID);
			try {
				mSender.sendText(lTargetId, JSONProcessor.tokenToPacket(lToken).getUTF8());
			} catch (JMSException lEx) {
				mLog.debug(Logging.getSimpleExceptionMessage(lEx, "identify"));
				sendErrorToken(aConnector, aToken, lEx.getMessage() + "on identify");
			}
		} else {
			sendNotConnectedToken(aConnector, aToken);
		}
	}

	private void isBrokerConnected(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);
		lResponse.setBoolean("isConnected", mTransportListener.isConnected());
		sendToken(aConnector, lResponse);
	}

	private void test(final WebSocketConnector aConnector, Token aToken) {
		if (null != mSender) {
			final Token lResponse = createResponse(aToken);
			lResponse.setInteger("code", 0);
			lResponse.setString("msg", "Ok");
			lResponse.setString("text", "JMS plug-in test ok.");
			mJWSEndPoint.sendPayload("JMSServer", "org.jwebsocket.jms.demo",
					"echo", aToken.getInteger("utid"), aToken.getString("originId"),
					null, "{}", new JWSResponseTokenListener() {

						@Override
						public void onTimeout() {
							lResponse.setInteger("code", -1);
							lResponse.setString("msg", "Timeout");
							lResponse.setString("text", "JMS plug-in test timed out.");
						}

						@Override
						public void onFailure(Token aReponse) {
							lResponse.setInteger("code", -1);
							lResponse.setString("msg", "Failure");
							lResponse.setString("text", "JMS plug-in test failed.");
							sendToken(aConnector, lResponse);
						}

						@Override
						public void onSuccess(Token aReponse) {
							lResponse.setInteger("code", 0);
							lResponse.setString("msg", "Ok");
							lResponse.setString("text", "JMS plug-in test succeeded.");
							sendToken(aConnector, lResponse);
						}

					}, 3000);
		} else {
			sendNotConnectedToken(aConnector, aToken);
		}
	}

}
