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
import java.util.UUID;
import org.jwebsocket.api.IEventBus;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.JWSTimerTask;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 * JMS based IEventBus implementation
 *
 * @author Rolando Santamaria Maso
 */
public class JMSEventBus extends BaseEventBus {

	protected static final String EVENT_BUS_MSG_UUID = "message_uuid";
	protected static final String EVENT_BUS_MSG_REPLYTO = "jms_replyto";
	private final Connection mConnection;
	private Session mSession;
	private MessageConsumer mTopicConsumer;
	private MessageConsumer mQueueConsumer;
	private MessageProducer mProducer;
	private final String mDestinationId;
	private final Timer mTimer = Tools.getTimer();
	private MessageConsumer mReplyConsumer;
	private TemporaryQueue mReplyQueue;
	private Topic mTopic;
	private Queue mQueue;

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
	public IEventBus publish(Token aToken) {
		setUTID(aToken);
		sendGeneric(false, aToken, new Long(0));

		return this;
	}

	String setUTID(Token aToken) {
		String lUUID;
		if (!aToken.getMap().containsKey(EVENT_BUS_MSG_UUID)) {
			lUUID = UUID.randomUUID().toString();
			aToken.setString(EVENT_BUS_MSG_UUID, lUUID);
		} else {
			lUUID = aToken.getString(EVENT_BUS_MSG_UUID);
		}

		return lUUID;
	}

	@Override
	public IEventBus send(final Token aToken, IHandler aHandler) {
		final String lUUID = setUTID(aToken);
		Long lExpiration = new Long(0);
		if (null != aHandler) {
			aHandler.setEventBus(this);
			storeResponseHandler(lUUID, aHandler);
			if (aHandler.getTimeout() > 0) {
				lExpiration = aHandler.getTimeout();
				aToken.setLong(BaseToken.EXPIRES, System.currentTimeMillis() + aHandler.getTimeout());

				mTimer.schedule(new JWSTimerTask() {

					@Override
					public void runTask() {
						final IHandler lH = removeResponseHandler(lUUID);
						if (null != lH) {
							Tools.getThreadPool().submit(new Runnable() {

								@Override
								public void run() {
									try {
										lH.OnTimeout(aToken);
									} catch (Exception lEx) {
										getExceptionHandler().handle(lEx);
									}
								}
							});
						}
					}
				}, aHandler.getTimeout());
			}
		}
		sendGeneric(true, aToken, lExpiration);

		return this;
	}

	void sendGeneric(boolean aSendOp, Token aToken, Long aExpiration) {
		try {
			TextMessage lMsg = mSession.createTextMessage(JSONProcessor.objectToJSONString(aToken));
			if (aExpiration > 0) {
				lMsg.setJMSExpiration(aExpiration);
			}
			// setting higher priority for EventBus messages
			lMsg.setJMSPriority(9);
			lMsg.setJMSReplyTo(mReplyQueue);
			
			if (aToken.getMap().containsKey(EVENT_BUS_MSG_REPLYTO)) {
				// sending reply message
				mProducer.send((Destination) aToken.getMap().get(EVENT_BUS_MSG_REPLYTO), lMsg);
			} else if (aSendOp) {
				// sending message
				mProducer.send(mQueue, lMsg);
			} else {
				// publishing message
				mProducer.send(mTopic, lMsg);
			}
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}
	}

	@Override
	public void initialize() throws Exception {
		mSession = mConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		mTopic = mSession.createTopic(mDestinationId);
		mQueue = mSession.createQueue(mDestinationId);
		mReplyQueue = mSession.createTemporaryQueue();

		mTopicConsumer = mSession.createConsumer(mTopic);
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

		mQueueConsumer = mSession.createConsumer(mQueue);
		mQueueConsumer.setMessageListener(new MessageListener() {

			@Override
			public void onMessage(Message aMessage) {
				try {
					TextMessage lTextMsg = (TextMessage) aMessage;
					final Token lToken = JSONProcessor.JSONStringToToken(lTextMsg.getText());
					lToken.getMap().put(EVENT_BUS_MSG_REPLYTO, aMessage.getJMSReplyTo());

					Tools.getThreadPool().submit(new Runnable() {

						@Override
						public void run() {
							// discard if token has expired
							if (!lToken.hasExpired()) {
								invokeHandler(lToken.getNS(), lToken);
							}
						}
					});

				} catch (Exception lEx) {
					// do nothing, invalid message
				}
			}
		});

		mReplyConsumer = mSession.createConsumer(mReplyQueue);
		mReplyConsumer.setMessageListener(new MessageListener() {

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
								String lUTID = lToken.getString(EVENT_BUS_MSG_UUID);
								invokeResponseHandler(lUTID, lToken);
							}
						}
					});

				} catch (Exception lEx) {
					// do nothing, invalid message
				}
			}
		});

		mProducer = mSession.createProducer(null);
	}

	@Override
	public void shutdown() throws Exception {
		mProducer.close();
		mTopicConsumer.close();
		mQueueConsumer.close();
		mReplyConsumer.close();
		mSession.close();
	}

	@Override
	public Token createResponse(Token aInToken) {
		Token lResponse = super.createResponse(aInToken);
		lResponse.getMap().put(JMSEventBus.EVENT_BUS_MSG_REPLYTO, aInToken.getMap().get(JMSEventBus.EVENT_BUS_MSG_REPLYTO));

		return lResponse;
	}

}
