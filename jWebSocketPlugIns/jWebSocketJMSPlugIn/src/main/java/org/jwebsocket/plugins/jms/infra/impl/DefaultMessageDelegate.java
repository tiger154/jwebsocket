//	---------------------------------------------------------------------------
//	jWebSocket - DefaultMessageDelegate
//	Copyright (c) 2011, Innotrade GmbH - jWebSocket.org, Alexander Schulze
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
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

public class DefaultMessageDelegate implements MessageDelegate, MessageConsumerRegistry {

	private Logger mLog = Logging.getLogger(getClass());
	private final Map<String, MessageListenerToken> mTokens = new ConcurrentHashMap<String, MessageListenerToken>();
	private final TokenPlugIn mTokenPlugin;
	private MessageConverter mMsgConverter = new SimpleMessageConverter();
	private DestinationIdentifier mDestIdentifier;

	public DefaultMessageDelegate(TokenPlugIn aTokenPlugin, DestinationIdentifier aDestIdentifier) {
		mTokenPlugin = aTokenPlugin;
		mDestIdentifier = aDestIdentifier;
	}

	@Override
	public void handleMessage(TextMessage aMessage) {
		for (String lConnectionId : mTokens.keySet()) {
			MessageListenerToken lToken = mTokens.get(lConnectionId);
			if (null == lToken || null == lToken.mToken)
				continue;

			sendMessage(aMessage, lToken, lConnectionId);

			if (!mDestIdentifier.isPubSubDomain())
				break;
		}
	}

	@Override
	public void handleMessage(MapMessage aMessage) {
		for (String lConnectionId : mTokens.keySet()) {
			MessageListenerToken lToken = mTokens.get(lConnectionId);
			if (null == lToken || null == lToken.mToken)
				continue;

			sendMessage(aMessage, lToken, lConnectionId);

			if (!mDestIdentifier.isPubSubDomain())
				break;
		}
	}

	private void sendMessage(MapMessage aMessage, MessageListenerToken aToken, String aConnectionId) {
		Token lResponseToken = mTokenPlugin.createResponse(aToken.mToken);
		if (aToken.getsMessagePayloadOnly)
			preparePayloadToken(aMessage, lResponseToken);
		else
			prepareMessageToken(aMessage, lResponseToken);

		sendResponseToken(aConnectionId, lResponseToken);
	}

	private void sendMessage(TextMessage aMessage, MessageListenerToken aToken, String aConnectionId) {
		Token lResponseToken = mTokenPlugin.createResponse(aToken.mToken);
		if (aToken.getsMessagePayloadOnly)
			preparePayloadToken(aMessage, lResponseToken);
		else
			prepareMessageToken(aMessage, lResponseToken);

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
		if (!dto.mOk)
			fillToken(aToken, -1, "could not get payload of TextMessage: " + dto.mLog);
		else
			fillEventToken(aToken, EventJms.HANDLE_TEXT_MESSAGE.getValue(), 1, dto);
	}

	private void preparePayloadToken(TextMessage aMessage, Token aToken) {
		String lPayLoad = getTextMessagePayload(aMessage);
		if (null == lPayLoad)
			fillToken(aToken, -1, "Could not get payload of TextMessage");
		else
			fillEventToken(aToken, EventJms.HANDLE_TEXT.getValue(), 1, lPayLoad);
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

	public WebSocketConnector getConnector(String aConnectionId) {
		return JWebSocketFactory.getTokenServer().getConnector(aConnectionId);
	}

	private static String getDestinationName(Destination aDestination) throws JMSException {
		return (null == aDestination) ? null : (aDestination instanceof Queue) ? ((Queue) aDestination).getQueueName()
				: (aDestination instanceof Topic) ? ((Topic) aDestination).getTopicName() : null;
	}

	@Override
	public void addMessageConsumer(String aConnectionId, Token aToken) {
		mTokens.put(aConnectionId, new MessageListenerToken(aToken, false));
	}

	@Override
	public void addMessagePayloadConsumer(String aConnectionId, Token aToken) {
		mTokens.put(aConnectionId, new MessageListenerToken(aToken, true));
	}

	@Override
	public void removeMessageConsumer(String aConectionId) {
		mTokens.remove(aConectionId);
	}

	@Override
	public int size() {
		return mTokens.size();
	}

	private static class MessageListenerToken {
		private Token mToken;
		private boolean getsMessagePayloadOnly;

		private MessageListenerToken(Token token, boolean getsMessagePayloadOnly) {
			super();
			mToken = token;
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
			} catch (JMSException e) {
				mOk = false;
				mLog = e.getMessage();
			}
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private class MapMessageDto extends MessageDto {
		private Map mMsgPayLoad;

		private MapMessageDto(MapMessage aMessage) {
			super(aMessage);
			try {
				mMsgPayLoad = (Map) mMsgConverter.fromMessage(aMessage);
			} catch (JMSException e) {
				mOk = false;
				mLog = e.getMessage();
			}
		}
	}

}
