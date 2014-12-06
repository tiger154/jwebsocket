//	---------------------------------------------------------------------------
//	jWebSocket - JWebSocketJMSClient (Community Edition, CE)
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
package org.jwebsocket.client.java;

import java.net.URI;
import java.util.UUID;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.jwebsocket.api.WebSocketBaseClientEvent;
import org.jwebsocket.api.WebSocketClient;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketStatus;
import org.jwebsocket.jms.Attributes;
import org.jwebsocket.jms.MessageType;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.IsAlreadyConnectedException;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketEncoding;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketFrameType;
import org.jwebsocket.kit.WebSocketSubProtocol;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.MessagingControl;
import org.springframework.util.Assert;

/**
 * JMS WebSocket client implementation for the server-side JMS Engine
 *
 * @author Rolando Santamaria Maso
 */
public class JWebSocketJMSClient extends BaseClient {

	private ActiveMQConnection mConnection;
	private ActiveMQSession mSession;
	private String mUsername, mPassword, mClusterName;
	private MessageConsumer mConsumer;
	private MessageProducer mProducer;
	private final String mReplySelector = UUID.randomUUID().toString();
	private String mSessionId;

	/**
	 *
	 * @param aClusterName
	 * @param aSessionId
	 * @param aUsername
	 * @param aPassword
	 */
	public JWebSocketJMSClient(String aClusterName, String aSessionId, String aUsername, String aPassword) {
		Assert.notNull(aClusterName, "The 'cluster name' argument cannot be null!'");

		mUsername = aUsername;
		mPassword = aPassword;
		mClusterName = aClusterName;
		mSessionId = aSessionId;
	}

	/**
	 *
	 * @param aClusterName
	 * @param aUsername
	 * @param aPassword
	 */
	public JWebSocketJMSClient(String aClusterName, String aUsername, String aPassword) {
		this(aClusterName, UUID.randomUUID().toString(), aUsername, aPassword);
	}

	/**
	 *
	 * @param aClusterName
	 */
	public JWebSocketJMSClient(String aClusterName) {
		this(aClusterName, null, null);
	}

	@Override
	public void open(String aURL) throws WebSocketException {
		if (isConnected()) {
			throw new IsAlreadyConnectedException("JMS connection already started!");
		}

		try {
			mURI = new URI(aURL);
			setStatus(WebSocketStatus.CONNECTING);
			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(aURL);
			if (null != mUsername) {
				mConnection = (ActiveMQConnection) factory.createConnection(mUsername, mPassword);
			} else {
				mConnection = (ActiveMQConnection) factory.createConnection();
			}

			mConnection.setExceptionListener(new ExceptionListener() {
				@Override
				public void onException(JMSException lEx) {
					// capturing connection exceptions
					try {
						close(CloseReason.BROKEN);
					} catch (Exception lEx2) {
						throw new RuntimeException(lEx2);
					}
				}
			});
			mSession = (ActiveMQSession) mConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Topic lClusterTopic = mSession.createTopic(mClusterName);

			mConsumer = mSession.createConsumer(lClusterTopic, "replySelector='" + mReplySelector
					+ "' OR isBroadcast=true");
			mProducer = mSession.createProducer(lClusterTopic);
			final WebSocketClient lClient = this;

			mConsumer.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message aMessage) {
					try {
						ActiveMQTextMessage lMessage = (ActiveMQTextMessage) aMessage;
						String lMessageType = lMessage.getStringProperty(Attributes.MESSAGE_TYPE);
						String lData = lMessage.getText();
						if (MessageType.DISCONNECTION.name().equals(lMessageType)) {
							close(CloseReason.SERVER);
						} else {
							notifyPacket(new WebSocketBaseClientEvent(lClient, lMessageType, lData),
									new RawPacket(lData));
						}
					} catch (Exception lEx) {
						throw new RuntimeException(lEx);
					}
				}
			});

			// starting the connection
			mConnection.start();

			// sending CONNECTION message
			Message lMessage = createMessage(MessageType.CONNECTION, null);
			lMessage.setStringProperty(Attributes.REPLY_SELECTOR, mReplySelector);
			lMessage.setStringProperty(Attributes.SESSION_ID, mSessionId);
			mProducer.send(lMessage);

			// notifying logic "opening" listeners notification
			// we consider that a client has finally openned when 
			// the "max frame size" handshake has completed 
			final WebSocketClientEvent lEvent = new WebSocketBaseClientEvent(this, EVENT_OPENING, null);
			for (final WebSocketClientListener lListener : getListeners()) {
				getListenersExecutor().submit(new Runnable() {
					@Override
					public void run() {
						try {
							lListener.processOpening(lEvent);
						} catch (Exception lEx) {
							// nothing, soppose to be catched internally
						}
					}
				});
			}
		} catch (Exception lEx) {
			throw new WebSocketException(lEx);
		}
	}

	Message createMessage(MessageType aMessageType, String aData) throws JMSException {
		Message lMessage = mSession.createMessage();
		lMessage.setStringProperty(Attributes.MESSAGE_TYPE, aMessageType.name());
		lMessage.setStringProperty(Attributes.DATA, aData);
		lMessage.setStringProperty(Attributes.MESSAGE_ID, UUID.randomUUID().toString());
		lMessage.setStringProperty(Attributes.REPLY_SELECTOR, mReplySelector);

		return lMessage;
	}

	@Override
	public boolean isConnected() {
		return mConnection != null && mConnection.isStarted();
	}

	@Override
	public void open(int aVersion, String aURI) throws WebSocketException {
		open(aURI);
	}

	@Override
	public void open(int aVersion, String aURI, String aSubProtocols) throws WebSocketException {
		open(aURI);
	}

	@Override
	public void send(WebSocketPacket aPacket) throws WebSocketException {
		send(aPacket.getByteArray());
	}

	@Override
	public void send(byte[] aData) throws WebSocketException {
		String lData = new String(aData);
		Token lWrappedMsg = JSONProcessor.JSONStringToToken(lData);

		try {
			if (null != lWrappedMsg && lWrappedMsg.getBoolean(MessagingControl.PROPERTY_IS_WRAPPED_MESSAGE, false)) {
				if (MessagingControl.TYPE_INFO.equals(lWrappedMsg.getString(MessagingControl.PROPERTY_TYPE))
						&& MessagingControl.NAME_MESSAGE_DELIVERY_ACKNOWLEDGE.equals(lWrappedMsg.getString(MessagingControl.PROPERTY_NAME))) {
					Message lMessage = createMessage(MessageType.ACK, lData);
					lMessage.setStringProperty(Attributes.NODE_ID,
							lWrappedMsg.getString(MessagingControl.PROPERTY_DATA).split("-")[0]);

					mProducer.send(lMessage);
					return;
				}
			}

			mProducer.send(createMessage(MessageType.MESSAGE, lData));
		} catch (Exception lEx) {
			throw new WebSocketException(lEx);
		}
	}

	@Override
	public void send(byte[] aData, WebSocketFrameType aFrameType) throws WebSocketException {
		send(aData);
	}

	@Override
	public void send(String aData, String aEncoding) throws WebSocketException {
		send(aData.getBytes());
	}

	void close(CloseReason aCloseReason) throws WebSocketException {
		try {
			setStatus(WebSocketStatus.CLOSING);
			try {
				mConsumer.close();
				mProducer.close();
				mSession.close();
				mConnection.close();
			} catch (Exception lEx) {
				// don't catch
			}
			setStatus(WebSocketStatus.CLOSED);

			// notifying close event
			WebSocketBaseClientEvent lEvent = new WebSocketBaseClientEvent(this, EVENT_CLOSE, aCloseReason.name());
			notifyClosed(lEvent);
			if (CloseReason.BROKEN.equals(aCloseReason)) {
				checkReconnect(lEvent);
			}
		} catch (Exception lEx) {
			throw new WebSocketException(lEx);
		}
	}

	@Override
	public void close() throws WebSocketException {
		close(CloseReason.CLIENT);
	}

	@Override
	public void addSubProtocol(WebSocketSubProtocol aSubProt) {
	}

	@Override
	public String getNegotiatedSubProtocol() {
		return null;
	}

	@Override
	public WebSocketEncoding getNegotiatedEncoding() {
		return null;
	}
}
