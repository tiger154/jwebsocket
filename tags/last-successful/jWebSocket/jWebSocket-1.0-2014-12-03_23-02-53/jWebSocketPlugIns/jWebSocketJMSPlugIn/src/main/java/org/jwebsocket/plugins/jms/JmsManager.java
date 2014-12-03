//	---------------------------------------------------------------------------
//	jWebSocket - JmsManager (Community Edition, CE)
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
package org.jwebsocket.plugins.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javolution.util.FastSet;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jms.JMSPlugIn.ActionInput;
import org.jwebsocket.plugins.jms.infra.impl.DefaultMessageDelegate;
import org.jwebsocket.plugins.jms.infra.impl.JmsListenerContainer;
import org.jwebsocket.plugins.jms.util.ConfigurationJms;
import org.jwebsocket.plugins.jms.util.FieldJms;
import org.jwebsocket.token.Token;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;

/**
 *
 * @author Johannes Smutny
 */
public class JmsManager {

	/**
	 * logger
	 */
	private static final Logger mLog = Logging.getLogger();
	private final Map<String, ConnectionFactory> mConnectionFactories = new HashMap<String, ConnectionFactory>();
	private final Map<String, Queue> mQueues = new HashMap<String, Queue>();
	private final Map<String, Topic> mTopics = new HashMap<String, Topic>();
	private final ListenerStore mMessageListenerStore = new BaseListenerStore();
	private final SenderStore mSenderStore = new BaseSenderStore();
	private final Set<DestinationIdentifier> mDIs = new FastSet<DestinationIdentifier>();
	private final MessageConverter mMsgConverter = new SimpleMessageConverter();

	private JmsManager() {
	}

	/**
	 *
	 * @param aSettings
	 * @param aBeanFactory
	 * @return
	 */
	public static JmsManager getInstance(Map<String, Object> aSettings, BeanFactory aBeanFactory) {
		JmsManager lManager = new JmsManager();
		lManager.initJmsAssets(aSettings, aBeanFactory);
		return lManager;
	}

	private void initJmsAssets(Map<String, Object> aSettings, BeanFactory aBeanFactory) {
		for (String lOption : aSettings.keySet()) {
			if (lOption.startsWith(ConfigurationJms.CF_PREFIX.getValue())) {
				initConnectionFactory(aSettings, aBeanFactory, lOption);
			} else if (lOption.startsWith(ConfigurationJms.DESTINATION_PREFIX.getValue())) {
				initDestination(aSettings, aBeanFactory, lOption);
			}
		}
	}

	private void initConnectionFactory(Map<String, Object> aSettings, BeanFactory aBeanFactory, String aOption) {
		JSONObject lJSON = getJSONObject(aSettings, aOption);
		String lName = getName(lJSON);
		mConnectionFactories.put(lName, (ConnectionFactory) aBeanFactory.getBean(lName));
		if (mLog.isDebugEnabled()) {
			mLog.debug("Added JMS connectionFactory with bean name: '" + lName + "'");
		}
	}

	private void initDestination(Map<String, Object> aSettings, BeanFactory aBeanFactory, String aOption) {
		JSONObject lJSON = getJSONObject(aSettings, aOption);
		String name = getName(lJSON);
		String cfName = getConnectionFactoryName(lJSON);
		Boolean pubSubDomain = getPubSubDomain(lJSON);
		Boolean sessionTransacted = getSessionTransacted(lJSON);
		Integer sessionAckMode = getSessionAcknowledgeMode(lJSON);
		Boolean deliveryPersistent = getDeliveryPersistent(lJSON);
		DestinationIdentifier lDi = DestinationIdentifier.valueOf(cfName, name, pubSubDomain, sessionTransacted,
				sessionAckMode, deliveryPersistent);
		mDIs.add(lDi);

		if (pubSubDomain) {
			storeTopic(aBeanFactory, name);
		} else {
			storeQueue(aBeanFactory, name);
		}
	}

	private JSONObject getJSONObject(Map<String, Object> aSettings, String aOption) {
		Object lObj = aSettings.get(aOption);
		JSONObject lJSON;
		if (lObj instanceof JSONObject) {
			lJSON = (JSONObject) lObj;
		} else {
			lJSON = new JSONObject();
		}
		return lJSON;
	}

	private void storeQueue(BeanFactory aBeanFactory, String aName) {
		mQueues.put(aName, (Queue) aBeanFactory.getBean(aName));
		if (mLog.isDebugEnabled()) {
			mLog.debug("Added JMS queue with bean name: '" + aName + "'");
		}
	}

	private void storeTopic(BeanFactory aBeanFactory, String aName) {
		mTopics.put(aName, (Topic) aBeanFactory.getBean(aName));
		if (mLog.isDebugEnabled()) {
			mLog.debug("Added JMS topic with bean name: '" + aName + "'");
		}
	}

	private String getConnectionFactoryName(JSONObject aJson) {
		try {
			return aJson.getString(ConfigurationJms.CONNECTION_FACTORY_NAME.getValue());
		} catch (JSONException lEx) {
			throw new IllegalArgumentException(
					"JMSPlugIn configuration error: missing cfName Property", lEx);
		}
	}

	private String getName(JSONObject aJson) {
		try {
			return aJson.getString(ConfigurationJms.NAME.getValue());
		} catch (JSONException lEx) {
			throw new IllegalArgumentException(
					"JMSPlugIn configuration error: missing name Property", lEx);
		}
	}

	private Boolean getPubSubDomain(JSONObject aJson) {
		try {
			return aJson.getBoolean(ConfigurationJms.PUB_SUB_DOMAIN.getValue());
		} catch (JSONException lEx) {
			throw new IllegalArgumentException(
					"JMSPlugIn configuration error: missing pubSubDomain Property", lEx);
		}
	}

	private Boolean getSessionTransacted(JSONObject aJson) {
		try {
			return aJson.getBoolean(ConfigurationJms.SESSION_TRANSACTED.getValue());
		} catch (JSONException lEx) {
			return null;
		}
	}

	private Boolean getDeliveryPersistent(JSONObject aJson) {
		try {
			return aJson.getBoolean(ConfigurationJms.DELIVERY_PERSISTENT.getValue());
		} catch (JSONException lEx) {
			return null;
		}
	}

	private Integer getSessionAcknowledgeMode(JSONObject aJson) {
		try {
			return aJson.getInt(ConfigurationJms.SESSION_ACKNOWLEDGE_MODE.getValue());
		} catch (JSONException lEx) {
			return null;
		}
	}

	/**
	 *
	 * @param aInput
	 */
	public void deregisterConnectorFromMessageListener(ActionInput aInput) {
		JmsListenerContainer lListener = mMessageListenerStore.getListener(aInput.mDi);

		if (null != lListener) {
			lListener.getMessageConsumerRegistry().removeMessageConsumer(aInput.mConnector.getId());

			if (0 == lListener.getMessageConsumerRegistry().size()) {
				lListener.stop();
			}
		}
	}

	private JmsTemplate getSender(DestinationIdentifier aDestinationIdentifier) {
		JmsTemplate lSender = mSenderStore.getSender(aDestinationIdentifier);
		return null == lSender ? createSender(aDestinationIdentifier) : lSender;
	}

	private JmsTemplate createSender(DestinationIdentifier aDestinationIdentifier) {
		JmsTemplate lSender = new JmsTemplate();
		lSender.setConnectionFactory(
				mConnectionFactories.get(
						aDestinationIdentifier.getConnectionFactoryName()));
		lSender.setDefaultDestination(getDestination(aDestinationIdentifier));
		lSender.setPubSubDomain(aDestinationIdentifier.isPubSubDomain());
		setSessionAckModeAndSessionTransacted(lSender, aDestinationIdentifier);
		setDeliveryPersistent(lSender, aDestinationIdentifier);
		mSenderStore.storeSender(aDestinationIdentifier, lSender);
		return lSender;
	}

	/**
	 *
	 * @param aInput
	 */
	public void sendText(ActionInput aInput) {
		getSender(aInput.mDi).convertAndSend(aInput.mReqToken.getString(FieldJms.MESSSAGE_PAYLOAD.getValue()));
	}

	/**
	 *
	 * @param aInput
	 */
	public void sendTextMessage(final ActionInput aInput) {
		JmsTemplate lSender = getSender(aInput.mDi);
		lSender.send(lSender.getDefaultDestination(), new MessageCreator() {
			@Override
			public Message createMessage(Session aSession) throws JMSException {
				Message result = aSession.createTextMessage(aInput.mReqToken.getString(FieldJms.MESSSAGE_PAYLOAD
						.getValue()));
				try {
					enrichMessageWithHeaders(result, aInput.mReqToken.getMap(FieldJms.JMS_HEADER_PROPERTIES.getValue()));
				} catch (JSONException lEx) {
					mLog.error("Could not enrich message with headers: " + lEx.getMessage());
				}
				return result;
			}
		});
	}

	/**
	 *
	 * @param aInput
	 */
	public void sendMap(ActionInput aInput) {
		JmsTemplate lSender = getSender(aInput.mDi);
		if (null == lSender) {
			throw new IllegalArgumentException("Missing sender for destination: isPubSubdomain: "
					+ aInput.mDi.isPubSubDomain() + " name: " + aInput.mDi.getDestinationName());
		}

		lSender.convertAndSend(aInput.mReqToken.getMap(FieldJms.MESSSAGE_PAYLOAD.getValue()));
	}

	/**
	 *
	 * @param aInput
	 */
	public void sendMapMessage(final ActionInput aInput) {
		JmsTemplate lSender = getSender(aInput.mDi);
		lSender.send(lSender.getDefaultDestination(), new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				Message result = mMsgConverter.toMessage(aInput.mReqToken.getMap(FieldJms.MESSSAGE_PAYLOAD.getValue()),
						session);
				try {
					enrichMessageWithHeaders(result, aInput.mReqToken.getMap(FieldJms.JMS_HEADER_PROPERTIES.getValue()));
				} catch (JSONException e) {
					mLog.error("could not enrich message with headers: " + e.getMessage());
				}
				return result;
			}
		});
	}

	private void enrichMessageWithHeaders(Message msg, @SuppressWarnings("rawtypes") Map jmsHeaderProperties)
			throws JMSException, JSONException {
		if (null == jmsHeaderProperties || jmsHeaderProperties.isEmpty()) {
			return;
		}
		addHeader(FieldJms.JMS_HEADER_CORRELATION_ID, msg, jmsHeaderProperties);
		addHeader(FieldJms.JMS_HEADER_REPLY_TO, msg, jmsHeaderProperties);
		addHeader(FieldJms.JMS_HEADER_TYPE, msg, jmsHeaderProperties);
	}

	@SuppressWarnings("rawtypes")
	private void addHeader(FieldJms headerName, Message msg, Map jmsHeaderProperties) throws JMSException,
			JSONException {
		Object headerValue = jmsHeaderProperties.get(headerName.getValue());
		if (null == headerValue) {
			return;
		}
		switch (headerName) {
			case JMS_HEADER_CORRELATION_ID:
				if (!(headerValue instanceof String)) {
					return;
				}
				msg.setJMSCorrelationID((String) headerValue);
				break;
			case JMS_HEADER_REPLY_TO:
				if (!(headerValue instanceof Map)) {
					return;
				}
				msg.setJMSReplyTo(getDestination((Map) headerValue));
				break;
			case JMS_HEADER_TYPE:
				if (!(headerValue instanceof String)) {
					return;
				}
				msg.setJMSType((String) headerValue);
		}
	}

	private Destination getDestination(@SuppressWarnings("rawtypes") Map headerValue) throws JSONException {
		if (null == headerValue || headerValue.size() != 3) {
			return null;
		}

		String lConnectionFactoryName = (String) headerValue.get(FieldJms.CONNECTION_FACTORY_NAME.getValue());
		String lDestinationName = (String) headerValue.get(FieldJms.DESTINATION_NAME.getValue());
		Boolean lPubSubDomain = (Boolean) headerValue.get(FieldJms.PUB_SUB_DOMAIN.getValue());
		return getDestination(DestinationIdentifier.valueOf(lConnectionFactoryName, lDestinationName, lPubSubDomain));
	}

	/**
	 *
	 * @param aInput
	 * @param aTokenPlugIn
	 */
	public void registerConnectorWithListener(ActionInput aInput, TokenPlugIn aTokenPlugIn) {
		JmsListenerContainer lListener = mMessageListenerStore.getListener(aInput.mDi);
		if (null != lListener) {
			registerMessagePayloadConnector(lListener, aInput.mConnector.getId(), aInput.mReqToken);
		} else {
			createListener(aInput.mConnector.getId(), aInput.mReqToken, aInput.mDi, aTokenPlugIn);
		}
	}

	/**
	 *
	 * @param aInput
	 * @param aTokenPlugIn
	 */
	public void registerConnectorWithMessageListener(ActionInput aInput, TokenPlugIn aTokenPlugIn) {
		JmsListenerContainer lListener = mMessageListenerStore.getListener(aInput.mDi);
		if (null != lListener) {
			registerConnector(lListener, aInput.mConnector.getId(), aInput.mReqToken);
		} else {
			createMessageListener(aInput.mConnector.getId(), aInput.mReqToken, aInput.mDi, aTokenPlugIn);
		}

	}

	private void createListener(String aConnectionId, Token aToken, DestinationIdentifier aDestinationIdentifier,
			TokenPlugIn aTokenPlugIn) {
		JmsListenerContainer aCont = createJmsListenerContainer(aDestinationIdentifier, aTokenPlugIn);
		aCont.getMessageConsumerRegistry().addMessagePayloadConsumer(aConnectionId, aToken);
		initializeJmsListenerContainer(aCont, aDestinationIdentifier);
	}

	private void createMessageListener(String aConnectionId, Token aToken,
			DestinationIdentifier aDestinationIdentifier, TokenPlugIn aTokenPlugIn) {
		JmsListenerContainer aCont = createJmsListenerContainer(aDestinationIdentifier, aTokenPlugIn);
		aCont.getMessageConsumerRegistry().addMessageConsumer(aConnectionId, aToken);
		initializeJmsListenerContainer(aCont, aDestinationIdentifier);
	}

	private JmsListenerContainer createJmsListenerContainer(DestinationIdentifier aDestinationIdentifier,
			TokenPlugIn aTokenPlugIn) {
		return setSessionAckModeAndSessionTransacted(JmsListenerContainer.valueOf(
				createMessageDelegate(aDestinationIdentifier, aTokenPlugIn),
				mConnectionFactories.get(aDestinationIdentifier.getConnectionFactoryName()),
				getDestination(aDestinationIdentifier)), aDestinationIdentifier);
	}

	private void initializeJmsListenerContainer(JmsListenerContainer aJmsListenerContainer,
			DestinationIdentifier aDestinationIdentifier) {
		mMessageListenerStore.storeListener(aDestinationIdentifier, aJmsListenerContainer);
		aJmsListenerContainer.afterPropertiesSet();
		aJmsListenerContainer.start();
	}

	private DefaultMessageDelegate createMessageDelegate(DestinationIdentifier aDestinationIdentifier,
			TokenPlugIn aTokenPlugIn) {
		return new DefaultMessageDelegate(aTokenPlugIn, aDestinationIdentifier);
	}

	private Destination getDestination(DestinationIdentifier aDestinationIdentifier) {
		Destination lDestination = aDestinationIdentifier.isPubSubDomain() ? mTopics.get(aDestinationIdentifier
				.getDestinationName()) : mQueues.get(aDestinationIdentifier.getDestinationName());
		if (null == lDestination) {
			throw new IllegalArgumentException("Missing destination: isPubSubdomain: "
					+ aDestinationIdentifier.isPubSubDomain() + " name: " + aDestinationIdentifier.getDestinationName());
		}
		return lDestination;
	}

	private JmsListenerContainer setSessionAckModeAndSessionTransacted(JmsListenerContainer aJmsListenerContainer,
			DestinationIdentifier aDi) {
		DestinationIdentifier lDi = getFromConfig(aDi);
		aJmsListenerContainer.setSessionAcknowledgeMode(lDi.getSessionAcknowledgeMode());
		aJmsListenerContainer.setSessionTransacted(lDi.isSessionTransacted());
		return aJmsListenerContainer;
	}

	private JmsTemplate setSessionAckModeAndSessionTransacted(JmsTemplate aTemplate, DestinationIdentifier aDi) {
		DestinationIdentifier lDi = getFromConfig(aDi);
		aTemplate.setSessionAcknowledgeMode(lDi.getSessionAcknowledgeMode());
		aTemplate.setSessionTransacted(lDi.isSessionTransacted());
		return aTemplate;
	}

	private JmsTemplate setDeliveryPersistent(JmsTemplate aTemplate, DestinationIdentifier aDi) {
		aTemplate.setDeliveryPersistent(getFromConfig(aDi).getDeliveryPersistent());
		return aTemplate;
	}

	private DestinationIdentifier getFromConfig(DestinationIdentifier aDi) {
		if (null == aDi) {
			return aDi;
		}

		for (DestinationIdentifier lDi : mDIs) {
			if (lDi.getConnectionFactoryName().equals(aDi.getConnectionFactoryName())
					&& lDi.getDestinationName().equals(aDi.getDestinationName())
					&& lDi.isPubSubDomain().equals(aDi.isPubSubDomain())) {
				return lDi;
			}
		}

		return aDi;
	}

	private void registerConnector(JmsListenerContainer aListener, String aConnectionId, Token aToken) {
		aListener.getMessageConsumerRegistry().addMessageConsumer(aConnectionId, aToken);

		if (!aListener.isRunning()) {
			aListener.start();
		}
	}

	private void registerMessagePayloadConnector(JmsListenerContainer aListener, String aConnectionId, Token aToken) {
		aListener.getMessageConsumerRegistry().addMessagePayloadConsumer(aConnectionId, aToken);

		if (!aListener.isRunning()) {
			aListener.start();
		}
	}

	/**
	 *
	 * @param aConnectionId
	 */
	public void stopListener(String aConnectionId) {
		for (JmsListenerContainer next : mMessageListenerStore.getAll()) {
			next.getMessageConsumerRegistry().removeMessageConsumer(aConnectionId);

			if (0 == next.getMessageConsumerRegistry().size()) {
				next.stop();
			}
		}
	}

	/**
	 *
	 */
	public void shutDownListeners() {
		for (JmsListenerContainer next : mMessageListenerStore.getAll()) {
			next.shutdown();
		}
	}
}
