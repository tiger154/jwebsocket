//  ---------------------------------------------------------------------------
//  jWebSocket - JMSClient (Community Edition, CE)
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
package org.jwebsocket.jms.client;

/**
 *
 * @author Alexander Schulze
 */
import java.util.UUID;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class JMSClient implements Runnable {

	private JmsTemplate mJMSTemplate;
	private DefaultMessageListenerContainer mBridgeListenerCont = null;
	private String mCorrelationID = UUID.randomUUID().toString();
	private JMSClientSender mSender = null;
	private ActiveMQConnectionFactory mConnectionFactory;

	public static void main(String[] aArgs) {
		new JMSClient().run();
	}

	public JMSClient() {
		mJMSTemplate = new JmsTemplate();
		mConnectionFactory = new ActiveMQConnectionFactory(
				"failover:(tcp://0.0.0.0:61616,tcp://127.0.0.1:61616)?initialReconnectDelay=100&randomize=false");
		mJMSTemplate.setConnectionFactory(mConnectionFactory);
		mJMSTemplate.setDefaultDestinationName("org.jwebsocket.jms2jws");
		mJMSTemplate.setDeliveryPersistent(false);
		mJMSTemplate.setPubSubDomain(true);
		mJMSTemplate.setSessionTransacted(false);

		mSender = new JMSClientSender(mJMSTemplate, mCorrelationID);


		/*
		 FileSystemXmlApplicationContext lFileCtx =
		 new FileSystemXmlApplicationContext("jms_client.xml");
		 mBridgeListenerCont =
		 (DefaultMessageListenerContainer) lFileCtx.getBean("jws2jmsListenerContainer");
		 JMSClientListener lListener = (JMSClientListener) mBridgeListenerCont.getMessageListener();
		 lListener.setSender(mSender);
		 mBridgeListenerCont.start();
		 */
		
		try {
			Connection lConnection = mConnectionFactory.createConnection();
			Session lSession = lConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Topic lTopic = lSession.createTopic("org.jwebsocket.jws2jms");
			MessageConsumer lConsumer = lSession.createConsumer(lTopic, "JMSCorrelationID = '" + mCorrelationID + "'");
			JMSClientListener lListener = new JMSClientListener();
			lListener.setSender(mSender);
			lConsumer.setMessageListener(lListener);
			lConnection.start();
		} catch (JMSException exp) {
		}

	}

	@Override
	public void run() {
		System.out.println("Connected, id: " + mCorrelationID);
		try {
			int lIdx = 0;
			while (lIdx < 2000) {
				Thread.sleep(2000L);
				lIdx++;
				mBroadcast();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Disconnecting JMS client...");
		if (null != mBridgeListenerCont) {
			mBridgeListenerCont.shutdown();
		}

		System.out.println("Disconnected JMS client.");
		System.out.println("Terminating app.");
	}

	private void mBroadcast() {
		final String lJSON = "{\"ns\":\"org.jwebsocket.plugins.system\",\"type\":\"broadcast\",\"timestamp\":" + System.currentTimeMillis() + ",\"data\":\"test\"}";
		System.out.println("Sending broadcast " + lJSON);
		mJMSTemplate.send(new MessageCreator() {
			@Override
			public Message createMessage(Session aSession) throws JMSException {
				Message lMsg = aSession.createTextMessage(lJSON);
				lMsg.setJMSCorrelationID(mCorrelationID);
				return lMsg;
			}
		});
	}
}
