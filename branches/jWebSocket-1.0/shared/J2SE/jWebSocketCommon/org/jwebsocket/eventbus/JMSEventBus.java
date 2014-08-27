//	---------------------------------------------------------------------------
//	jWebSocket JMSEventBus (Community Edition, CE)
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
package org.jwebsocket.eventbus;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import org.jwebsocket.api.IEventBus;
import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 * JMS based IEventBus implementation
 *
 * @author Rolando Santamaria Maso
 */
public class JMSEventBus extends BaseEventBus {

	protected static final String ATTR_TOKEN_BUS_UTID = "tokenbus_utid";
	private final Connection mConnection;
	private Session mSession;
	private MessageConsumer mTopicConsumer;
	private MessageConsumer mQueueConsumer;
	private MessageProducer mQueueProducer;
	private MessageProducer mTopicProducer;
	private final String mDestinationId;
	private final Timer mTimer = Tools.getTimer();

	public Connection getConnection() {
		return mConnection;
	}

	public JMSEventBus(Connection aConnection, String aDestinationId) {
		Assert.notNull(aConnection, "The 'connection' argument cannot be null!");
		Assert.notNull(aDestinationId, "The 'destinationId' argument cannot be null!");

		mConnection = aConnection;
		mDestinationId = aDestinationId;
	}

	@Override
	public Registration register(final String aNS, final IHandler aHandler) {
		Assert.notNull(aNS, "The 'NS' argument cannot be null!");
		Assert.notNull(aHandler, "The 'handler' argument cannot be null!");

		storeHandler(aNS, aHandler);

		return new Registration() {

			@Override
			public String getNS() {
				return aNS;
			}

			@Override
			public void cancel() {
				removeHandler(aNS, aHandler);
			}

			@Override
			public IHandler getHandler() {
				return aHandler;
			}
		};
	}

	@Override
	public IEventBus publish(Token aToken) {
		setUTID(aToken);
		sendGeneric(mTopicProducer, aToken);

		return this;
	}

	String setUTID(Token aToken) {
		String lUUID;
		if (!aToken.getMap().containsKey(ATTR_TOKEN_BUS_UTID)) {
			lUUID = UUID.randomUUID().toString();
			aToken.setString(ATTR_TOKEN_BUS_UTID, lUUID);
		} else {
			lUUID = aToken.getString(ATTR_TOKEN_BUS_UTID);
		}

		return lUUID;
	}

	@Override
	public IEventBus send(final Token aToken, IHandler aHandler) {
		final String lUUID = setUTID(aToken);
		if (null != aHandler) {
			aHandler.setEventBus(this);
			storeResponseHandler(lUUID, aHandler);
			if (aHandler.getTimeout() > 0) {
				aToken.setLong(BaseToken.EXPIRES, System.currentTimeMillis() + aHandler.getTimeout());

				mTimer.schedule(new TimerTask() {

					@Override
					public void run() {
						final IHandler lH = removeResponseHandler(lUUID);
						if (null != lH) {
							Tools.getThreadPool().submit(new Runnable() {

								@Override
								public void run() {
									lH.OnTimeout(aToken);
								}
							});
						}
					}
				}, aHandler.getTimeout());
			}
		}
		sendGeneric(mQueueProducer, aToken);

		return this;
	}

	void sendGeneric(MessageProducer aProducer, Token aToken) {
		try {
			aProducer.send(mSession.createTextMessage(JSONProcessor.objectToJSONString(aToken)));
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}
	}

	@Override
	public void initialize() throws Exception {
		mSession = mConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		Topic lTopic = mSession.createTopic(mDestinationId);
		Queue lQueue = mSession.createQueue(mDestinationId);

		mTopicConsumer = mSession.createConsumer(lTopic);
		mTopicConsumer.setMessageListener(new MessageListener() {

			@Override
			public void onMessage(Message aMessage) {
				try {
					TextMessage lTextMsg = (TextMessage) aMessage;
					final Token lToken = JSONProcessor.JSONStringToToken(lTextMsg.getText());

					Tools.getThreadPool().submit(new Runnable() {

						@Override
						public void run() {
							// discard if token has expired
							if (!lToken.hasExpired()) {
								invokeHandlers(lToken.getNS(), lToken);
							}
						}
					});

				} catch (Exception lEx) {
					// do nothing, invalid message was sent to the JMS destination
				}
			}
		});
		mQueueConsumer = mSession.createConsumer(lQueue);
		mQueueConsumer.setMessageListener(new MessageListener() {

			@Override
			public void onMessage(Message aMessage) {
				try {
					TextMessage lTextMsg = (TextMessage) aMessage;
					final Token lToken = JSONProcessor.JSONStringToToken(lTextMsg.getText());

					Tools.getThreadPool().submit(new Runnable() {

						@Override
						public void run() {
							// discard if token has expired
							if (!lToken.hasExpired()) {
								if ("response".equals(lToken.getType())) {
									String lUTID = lToken.getString(ATTR_TOKEN_BUS_UTID);
									invokeResponseHandler(lUTID, lToken);
								} else {
									invokeHandler(lToken.getNS(), lToken);
								}
							}
						}
					});

				} catch (Exception lEx) {
					// do nothing, invalid message
				}
			}
		});

		mTopicProducer = mSession.createProducer(lTopic);
		mQueueProducer = mSession.createProducer(lQueue);
	}

	@Override
	public void shutdown() throws Exception {
		mQueueProducer.close();
		mTopicProducer.close();
		mTopicConsumer.close();
		mQueueConsumer.close();
		mSession.close();
	}
}
