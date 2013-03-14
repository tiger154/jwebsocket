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
 * @author Johannes Smutny
 */
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
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

	private Logger mLog = Logging.getLogger();
	private static final String NS_JMS =
			JWebSocketServerConstants.NS_BASE + ".plugins.jms";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket JMSPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket JMSPlugIn - Community Edition";
	private JmsManager mJmsManager = null;

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
			ApplicationContext lBeanFactory = getConfigBeanFactory();
			mJmsManager = JmsManager.getInstance(aConfiguration.getSettings(),
					lBeanFactory);
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " instantiation: " + lEx.getMessage());
		}
		// give a success message to the administrator
		if (mLog.isInfoEnabled()) {
			mLog.info("JMS plug-in successfully loaded.");
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
	/*
	 * @Override public void connectorStarted(WebSocketConnector aConnector) {
	 * super.connectorStarted(aConnector); }
	 */
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
		String lNS = aToken.getNS();

		if (lType == null || !getNamespace().equals(lNS)) {
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
		mLog.debug("Processing 'unlisten'...");
		executeAction(createActionInput(aConnector, aToken, "Successfully unlisten jms listener"), new ActionCommand() {
			@Override
			void execute(ActionInput aInput) throws Exception {
				mJmsManager.deregisterConnectorFromMessageListener(aInput);
			}
		});
	}

	private void listen(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'listen'...");
		executeAction(
				createActionInput(aConnector, aToken, "Successfully got jms listener", RightJms.LISTEN,
				RightJms.SEND_AND_LISTEN), new ActionCommand() {
			@Override
			void execute(ActionInput aInput) throws Exception {
				mJmsManager.registerConnectorWithListener(aInput, JMSPlugIn.this);
			}
		});
	}

	private void listenMessage(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'listenMessage'...");
		executeAction(
				createActionInput(aConnector, aToken, "Successfully got jms message listener", RightJms.LISTEN,
				RightJms.SEND_AND_LISTEN), new ActionCommand() {
			@Override
			void execute(ActionInput aInput) throws Exception {
				mJmsManager.registerConnectorWithMessageListener(aInput, JMSPlugIn.this);
			}
		});
	}

	private void sendText(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'sendText'...");
		executeAction(
				createActionInput(aConnector, aToken, "Text successfully sent", RightJms.SEND, RightJms.SEND_AND_LISTEN),
				new ActionCommand() {
			@Override
			void execute(ActionInput aInput) throws Exception {
				mJmsManager.sendText(aInput);
			}
		});
	}

	private void sendTextMessage(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'sendTextMessage'...");
		executeAction(
				createActionInput(aConnector, aToken, "Jms text message successfully sent", RightJms.SEND,
				RightJms.SEND_AND_LISTEN), new ActionCommand() {
			@Override
			void execute(ActionInput aInput) throws Exception {
				mJmsManager.sendTextMessage(aInput);
			}
		});
	}

	private void sendMap(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'sendMap'...");
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
		mLog.debug("Processing 'sendMapMessage'...");
		executeAction(
				createActionInput(aConnector, aToken, "Jms map message successfully sent", RightJms.SEND,
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
		setCodeAndMsg(lResponseToken, -1, "missing jms manager: correct your config");
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void sendMissingBeanFactoryResponseToken(WebSocketConnector aConnector, Token aToken) {
		Token lResponseToken = createResponse(aToken);
		setCodeAndMsg(lResponseToken, -1, "missing jms spring beanfactory: correct your config");
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void sendNegativeToken(ActionInput aInput, Exception aEx) {
		setCodeAndMsg(aInput.mResToken, -1, aEx.getMessage());
		sendToken(aInput);
	}

	private boolean isDestinationIdentifierValid(ActionInput aInput) {
		if (aInput.mDi.isMissingData()) {
			setCodeAndMsg(aInput.mResToken, -1, "Missing destination identifier input  data");
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
					NS_JMS + "." + next + "." + (aInput.mDi.isPubSubDomain() ? "topic" : "queue") + "."
					+ aInput.mDi.getDestinationName())) {
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

		private ActionInput(WebSocketConnector aConnector, Token aToken, String aPositiveMsg, RightJms... aRights) {
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
