//	---------------------------------------------------------------------------
//	jWebSocket - DefaultMessageDelegate (Community Edition, CE)
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
package org.jwebsocket.plugins.jms.infra.impl;

/**
 *
 * @author Johannes Smutny
 */
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jms.DestinationIdentifier;
import org.jwebsocket.plugins.jms.infra.MessageConsumerRegistry;
import org.jwebsocket.plugins.jms.infra.MessageDelegate;
import org.jwebsocket.plugins.jms.util.EventJms;
import org.jwebsocket.plugins.jms.util.FieldJms;
import org.jwebsocket.token.Token;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;

/**
 *
 * @author Alexander Schulze
 */
public class DefaultMessageDelegate implements MessageDelegate, MessageConsumerRegistry {

	private final Logger mLog = Logging.getLogger();
	private final Map<String, MessageListenerToken> mTokens = new ConcurrentHashMap<String, MessageListenerToken>();
	private final TokenPlugIn mTokenPlugin;
	private final MessageConverter mMsgConverter = new SimpleMessageConverter();
	private final DestinationIdentifier mDestIdentifier;

	/**
	 *
	 * @param aTokenPlugin
	 * @param aDestIdentifier
	 */
	public DefaultMessageDelegate(TokenPlugIn aTokenPlugin, DestinationIdentifier aDestIdentifier) {
		mTokenPlugin = aTokenPlugin;
		mDestIdentifier = aDestIdentifier;
	}

	/**
	 *
	 * @param aMessage
	 */
	@Override
	public void handleMessage(TextMessage aMessage) {
		for (String lConnectionId : mTokens.keySet()) {
			MessageListenerToken lToken = mTokens.get(lConnectionId);
			if (null == lToken || null == lToken.mToken) {
				continue;
			}

			sendMessage(aMessage, lToken, lConnectionId);

			if (!mDestIdentifier.isPubSubDomain()) {
				break;
			}
		}
	}

	/**
	 *
	 * @param aMessage
	 */
	@Override
	public void handleMessage(MapMessage aMessage) {
		for (String lConnectionId : mTokens.keySet()) {
			MessageListenerToken lToken = mTokens.get(lConnectionId);
			if (null == lToken || null == lToken.mToken) {
				continue;
			}

			sendMessage(aMessage, lToken, lConnectionId);

			if (!mDestIdentifier.isPubSubDomain()) {
				break;
			}
		}
	}

	private void sendMessage(MapMessage aMessage, MessageListenerToken aToken, String aConnectionId) {
		Token lResponseToken = mTokenPlugin.createResponse(aToken.mToken);
		if (aToken.getsMessagePayloadOnly) {
			preparePayloadToken(aMessage, lResponseToken);
		} else {
			prepareMessageToken(aMessage, lResponseToken);
		}

		sendResponseToken(aConnectionId, lResponseToken);
	}

	private void sendMessage(TextMessage aMessage, MessageListenerToken aToken, String aConnectionId) {
		Token lResponseToken = mTokenPlugin.createResponse(aToken.mToken);
		if (aToken.getsMessagePayloadOnly) {
			preparePayloadToken(aMessage, lResponseToken);
		} else {
			prepareMessageToken(aMessage, lResponseToken);
		}

		sendResponseToken(aConnectionId, lResponseToken);
	}

	private void prepareMessageToken(MapMessage aMessage, Token aToken) {
		MapMessageDto dto = new MapMessageDto(aMessage);
		if (!dto.mOk) {
			fillToken(aToken, -1, "could not get payload of MapMessage: " + dto.mLog);
		} else {
			fillEventToken(aToken, EventJms.HANDLE_MAP_MESSAGE.getValue(), 1, dto);
		}
	}

	private void preparePayloadToken(MapMessage aMessage, Token aToken) {
		Map<?, ?> lPayLoad = getMapMessagePayload(aMessage);
		if (null == lPayLoad) {
			fillToken(aToken, -1, "Could not get payload of MapMessage");
		} else {
			fillEventToken(aToken, EventJms.HANDLE_MAP.getValue(), 1, lPayLoad);
		}
	}

	private void fillEventToken(Token aToken, String aName, int aCode, MapMessageDto dto) {
		aToken.setType(FieldJms.EVENT.getValue());
		aToken.setString(FieldJms.NAME.getValue(), aName);
		aToken.setInteger(FieldJms.CODE.getValue(), aCode);
		aToken.setMap(FieldJms.MESSSAGE_PAYLOAD.getValue(), dto.mMsgPayLoad);
		aToken.setMap(FieldJms.JMS_HEADER_PROPERTIES.getValue(), getMessageHeaders(dto));
	}

	private void fillEventToken(Token aToken, String aName, int aCode, Map<?, ?> aPayLoad) {
		aToken.setType(FieldJms.EVENT.getValue());
		aToken.setString(FieldJms.NAME.getValue(), aName);
		aToken.setInteger(FieldJms.CODE.getValue(), aCode);
		aToken.setMap(FieldJms.MESSSAGE_PAYLOAD.getValue(), aPayLoad);
	}

	private void prepareMessageToken(TextMessage aMessage, Token aToken) {
		TextMessageDto dto = new TextMessageDto(aMessage);
		if (!dto.mOk) {
			fillToken(aToken, -1, "could not get payload of TextMessage: " + dto.mLog);
		} else {
			fillEventToken(aToken, EventJms.HANDLE_TEXT_MESSAGE.getValue(), 1, dto);
		}
	}

	private void preparePayloadToken(TextMessage aMessage, Token aToken) {
		String lPayLoad = getTextMessagePayload(aMessage);
		if (null == lPayLoad) {
			fillToken(aToken, -1, "Could not get payload of TextMessage");
		} else {
			fillEventToken(aToken, EventJms.HANDLE_TEXT.getValue(), 1, lPayLoad);
		}
	}

	private void fillEventToken(Token aToken, String aName, int aCode, TextMessageDto dto) {
		aToken.setType(FieldJms.EVENT.getValue());
		aToken.setString(FieldJms.NAME.getValue(), aName);
		aToken.setInteger(FieldJms.CODE.getValue(), aCode);
		aToken.setString(FieldJms.MESSSAGE_PAYLOAD.getValue(), dto.mMsgPayLoad);
		aToken.setMap(FieldJms.JMS_HEADER_PROPERTIES.getValue(), getMessageHeaders(dto));
	}

	private void fillEventToken(Token aToken, String aName, int aCode, String aPayLoad) {
		aToken.setType(FieldJms.EVENT.getValue());
		aToken.setString(FieldJms.NAME.getValue(), aName);
		aToken.setInteger(FieldJms.CODE.getValue(), aCode);
		aToken.setString(FieldJms.MESSSAGE_PAYLOAD.getValue(), aPayLoad);
	}

	private void fillToken(Token aToken, int aCode, String aMsg) {
		aToken.setInteger(FieldJms.CODE.getValue(), aCode);
		aToken.setString(FieldJms.MSG.getValue(), aMsg);
	}

	private void sendResponseToken(String aConnectionId, Token aResponseToken) {
		WebSocketConnector lConnector = getConnector(aConnectionId);
		mTokenPlugin.sendToken(lConnector, lConnector, mDestIdentifier.setDestinationIdentifier(aResponseToken));
	}

	private Map<String, Object> getMessageHeaders(MessageDto dto) {
		Map<String, Object> lHeaders = new HashMap<String, Object>();
		lHeaders.put(FieldJms.JMS_HEADER_CORRELATION_ID.getValue(), dto.mJmsCorrelationId);
		lHeaders.put(FieldJms.JMS_HEADER_REPLY_TO.getValue(), dto.mJmsReplyTo);
		lHeaders.put(FieldJms.JMS_HEADER_TYPE.getValue(), dto.mJmsType);
		lHeaders.put(FieldJms.JMS_HEADER_DESTINATION.getValue(), dto.mJmsDestination);
		lHeaders.put(FieldJms.JMS_HEADER_DELIVERY_MODE.getValue(), dto.mJmsDeliveryMode);
		lHeaders.put(FieldJms.JMS_HEADER_EXPIRATION.getValue(), dto.mJmsExpiration);
		lHeaders.put(FieldJms.JMS_HEADER_MESSAGE_ID.getValue(), dto.mJmsMessageId);
		lHeaders.put(FieldJms.JMS_HEADER_PRIORITY.getValue(), dto.mJmsPriority);
		lHeaders.put(FieldJms.JMS_HEADER_REDELIVERED.getValue(), dto.mJmsRedelivered);
		lHeaders.put(FieldJms.JMS_HEADER_TIMESTAMP.getValue(), dto.mJmsTimestamp);
		return lHeaders;
	}

	private String getTextMessagePayload(TextMessage aTextMessage) {
		try {
			String ret = aTextMessage.getText();
			return (null == ret) ? "" : ret;
		} catch (JMSException e) {
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	private Map getMapMessagePayload(MapMessage aMapMessage) {
		try {
			Map ret = (Map) mMsgConverter.fromMessage(aMapMessage);
			return (null == ret) ? new HashMap() : ret;
		} catch (JMSException e) {
			mLog.error("could not get payload of MapMessage", e);
			return null;
		}
	}

	/**
	 *
	 * @param aConnectionId
	 * @return
	 */
	public WebSocketConnector getConnector(String aConnectionId) {
		return JWebSocketFactory.getTokenServer().getConnector(aConnectionId);
	}

	private static String getDestinationName(Destination aDestination) throws JMSException {
		return (null == aDestination) ? null : (aDestination instanceof Queue) ? ((Queue) aDestination).getQueueName()
				: (aDestination instanceof Topic) ? ((Topic) aDestination).getTopicName() : null;
	}

	/**
	 *
	 * @param aConnectionId
	 * @param aToken
	 */
	@Override
	public void addMessageConsumer(String aConnectionId, Token aToken) {
		mTokens.put(aConnectionId, new MessageListenerToken(aToken, false));
	}

	/**
	 *
	 * @param aConnectionId
	 * @param aToken
	 */
	@Override
	public void addMessagePayloadConsumer(String aConnectionId, Token aToken) {
		mTokens.put(aConnectionId, new MessageListenerToken(aToken, true));
	}

	/**
	 *
	 * @param aConectionId
	 */
	@Override
	public void removeMessageConsumer(String aConectionId) {
		mTokens.remove(aConectionId);
	}

	/**
	 *
	 * @return
	 */
	@Override
	public int size() {
		return mTokens.size();
	}

	private static class MessageListenerToken {

		private Token mToken;
		private boolean getsMessagePayloadOnly;

		private MessageListenerToken(Token aToken, boolean getsMessagePayloadOnly) {
			super();
			mToken = aToken;
			this.getsMessagePayloadOnly = getsMessagePayloadOnly;
		}
	}

	private static class MessageDto {

		protected String mJmsCorrelationId;
		protected String mJmsReplyTo;
		protected String mJmsType;
		protected Boolean mOk = true;
		protected String mLog;
		protected String mJmsDestination;
		protected Integer mJmsDeliveryMode;
		protected String mJmsMessageId;
		protected Long mJmsTimestamp;
		protected Boolean mJmsRedelivered;
		protected Long mJmsExpiration;
		protected Integer mJmsPriority;

		private MessageDto(Message aMessage) {
			try {
				mJmsCorrelationId = aMessage.getJMSCorrelationID();
				mJmsReplyTo = getDestinationName(aMessage.getJMSReplyTo());
				mJmsType = aMessage.getJMSType();
				mJmsDestination = getDestinationName(aMessage.getJMSDestination());
				mJmsDeliveryMode = aMessage.getJMSDeliveryMode();
				mJmsMessageId = aMessage.getJMSMessageID();
				mJmsTimestamp = aMessage.getJMSTimestamp();
				mJmsRedelivered = aMessage.getJMSRedelivered();
				mJmsExpiration = aMessage.getJMSExpiration();
				mJmsPriority = aMessage.getJMSPriority();
			} catch (JMSException e) {
				mOk = false;
				mLog = e.getMessage();
			}
		}
	}

	private static class TextMessageDto extends MessageDto {

		private String mMsgPayLoad;

		private TextMessageDto(TextMessage aMessage) {
			super(aMessage);
			try {
				mMsgPayLoad = aMessage.getText();
			} catch (JMSException lEx) {
				mOk = false;
				mLog = lEx.getMessage();
			}
		}
	}

	@SuppressWarnings({"rawtypes"})
	private class MapMessageDto extends MessageDto {

		private Map mMsgPayLoad;

		private MapMessageDto(MapMessage aMessage) {
			super(aMessage);
			try {
				mMsgPayLoad = (Map) mMsgConverter.fromMessage(aMessage);
			} catch (JMSException lEx) {
				mOk = false;
				mLog = lEx.getMessage();
			}
		}
	}
}
