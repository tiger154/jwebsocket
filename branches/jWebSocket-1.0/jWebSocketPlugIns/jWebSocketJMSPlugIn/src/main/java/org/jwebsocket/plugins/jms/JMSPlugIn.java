//	---------------------------------------------------------------------------
//	jWebSocket - JMSPlugIn (Community Edition, CE)
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
package org.jwebsocket.plugins.jms;

/**
 *
 * @author Johannes Smutny, Alexander Schulze
 */
import java.util.List;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javolution.util.FastList;
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
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jms.bridge.JMSAdvisoryListener;
import org.jwebsocket.plugins.jms.bridge.JMSEngine;
import org.jwebsocket.plugins.jms.bridge.JMSListener;
import org.jwebsocket.plugins.jms.bridge.JMSSender;
import org.jwebsocket.plugins.jms.util.ActionJms;
import org.jwebsocket.plugins.jms.util.FieldJms;
import org.jwebsocket.plugins.jms.util.RightJms;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.token.Token;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author aschulze
 */
public class JMSPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	private static final String NS_JMS =
			JWebSocketServerConstants.NS_BASE + ".plugins.jms";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket JMSPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket JMSPlugIn - Community Edition";
	private JmsManager mJmsManager = null;
	private JMSEngine mJMSEngine = null;
	private ActiveMQConnectionFactory mConnectionFactory;
	private Connection mConnection;
	private Session mSession;
	private MessageConsumer mConsumer;
	private JMSSender mSender;
	private JMSListener mListener;
	private MessageConsumer mAdvisoryConsumer;
	private Settings mSettings;

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

			List<String> lDomains = new FastList<String>();
			lDomains.add("*");
			EngineConfig lEngineCfg = new EngineConfig(
					"jwsjmsgw0", // id
					"jWebSocket JMSGateway", // name 
					"-", // jar
					0, // port
					0, // ssl port
					"-", // keystore
					"-", // keystore pw
					"-", // context
					"-", // servlet
					0, // timeout
					65536, // max frame size
					lDomains, // domains
					1000, // max connections
					"-", // max connection stretegy
					null // settings
					);
			mJMSEngine = new JMSEngine(lEngineCfg);

			JWebSocketFactory.getEngines()
					.put(lEngineCfg.getId(), mJMSEngine);
			List<WebSocketServer> lServers = JWebSocketFactory.getServers();
			for (WebSocketServer lServer : lServers) {
				lServer.addEngine(mJMSEngine);
			}

			// Advisory listener
			// setting up the JMS Gateway
			String aBrokerURI =	mSettings.getBrokerURI();
			String mEndPointId = mSettings.getEndPointId();
			String lRequestTopicId = mSettings.getGatewayTopic();
			String lResponseTopicId = mSettings.getGatewayTopic();
			String lAdvisoryTopicId = mSettings.getAdvisoryTopic();

			mConnectionFactory = new ActiveMQConnectionFactory(aBrokerURI);
			try {
				mConnection = mConnectionFactory.createConnection();
				mConnection.start();

				mSession = mConnection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);

				Topic lRequestTopic = mSession.createTopic(lRequestTopicId);
				MessageProducer lProducer = mSession.createProducer(lRequestTopic);
				mSender = new JMSSender(mSession, lProducer, mEndPointId);

				Topic lResponseTopic = mSession.createTopic(lResponseTopicId);
				mConsumer = mSession.createConsumer(
						lResponseTopic,
						"targetId='" + mEndPointId + "'");
				mListener = new JMSListener(mJMSEngine, mSender);
				mConsumer.setMessageListener(mListener);

				//mSender.sendText("{\"ns\":\"org.jwebsocket.jms.bridge\""
				//		+ ",\"type\":\"register\""
				//		+ ",\"sourceId\":\"" + mNodeId + "\""
				//		+ "}");

				// create the listener to the advisory topic
				Topic lAdvisoryTopic = mSession.createTopic(lAdvisoryTopicId);
				mAdvisoryConsumer = mSession.createConsumer(lAdvisoryTopic);
				JMSAdvisoryListener lAdvisoryListener = new JMSAdvisoryListener();
				mAdvisoryConsumer.setMessageListener(lAdvisoryListener);
				lAdvisoryListener.setSender(mSender);
				lAdvisoryListener.setEngine(mJMSEngine);

			} catch (JMSException lEx) {
				System.out.println(lEx.getClass().getSimpleName()
						+ " on connecting JMS client.");
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " instantiation: " + lEx.getMessage());
		}
		// give a success message to the administrator
		if (mLog.isInfoEnabled()) {
			mLog.info("JMS plug-in successfully instantiated.");
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
			} catch (JMSException lEX) {
			}
		}
		// shutdown advisory listener
		if (null != mAdvisoryConsumer) {
			try {
				mAdvisoryConsumer.close();
			} catch (JMSException lEX) {
			}
		}
		if (null != mConnection) {
			try {
				mConnection.stop();
			} catch (JMSException lEX) {
			}
			try {
				mConnection.close();
			} catch (JMSException lEX) {
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
				mJmsManager.deregisterConnectorFromMessageListener(aInput);
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
				mJmsManager.registerConnectorWithListener(aInput, JMSPlugIn.this);
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
				mJmsManager.registerConnectorWithMessageListener(aInput, JMSPlugIn.this);
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
				mJmsManager.sendText(aInput);
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
				mJmsManager.sendTextMessage(aInput);
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
				mJmsManager.sendMap(aInput);
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
				mJmsManager.sendMapMessage(aInput);
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

	private void sendMissingJmsManagerResponseToken(WebSocketConnector aConnector, Token aToken) {
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
}
