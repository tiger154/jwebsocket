//  ---------------------------------------------------------------------------
//  jWebSocket - JMS Client (Community Edition, CE)
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
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JMSClient {

	private String mCorrelationID = UUID.randomUUID().toString();
	private JMSClientSender mSender = null;
	private ActiveMQConnectionFactory mConnectionFactory;
	private boolean mShutDown = false;
	private Connection mConnection;

	public JMSClient(String aBrokerURI, String aConsumerTopic,
			String aProducerTopic) {
		mConnectionFactory = new ActiveMQConnectionFactory(aBrokerURI);
		try {
			mConnection = mConnectionFactory.createConnection();
			Session lSession = mConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			mConnection.start();

			Topic lProducerTopic = lSession.createTopic(aProducerTopic);
			MessageProducer lProducer = lSession.createProducer(lProducerTopic);
			mSender = new JMSClientSender(lSession, lProducer, mCorrelationID);

			Topic lConsumerTopic = lSession.createTopic(aConsumerTopic);
			MessageConsumer lConsumer = lSession.createConsumer(
					lConsumerTopic,
					"JMSCorrelationID = '" + mCorrelationID + "'");
			JMSClientListener lListener = new JMSClientListener(mSender);
			lConsumer.setMessageListener(lListener);
			mSender.send("{\"ns\":\"org.jwebsocket.jms.bridge\""
					+ ",\"type\":\"register\""
					+ ",\"sourceId\":\"" + mCorrelationID + "\""
					+ "}");
		} catch (JMSException lEx) {
			System.out.println(lEx.getClass().getSimpleName() 
					+ "on connecting JMS client.");
		}
	}

	public boolean isShutDown() {
		return mShutDown;
	}

	public void shutDown() {
		// clean the garbage
		if (null != mConnection) {
			try {
				mConnection.stop();
			} catch (JMSException lEx) {
			}
		}
		// to end potential console loops
		mShutDown = true;
	}

	public static void main(String[] aArgs) {
		// instantiate JMS client
		JMSClient lJMSClient = new JMSClient(
				"failover:(tcp://0.0.0.0:61616,tcp://127.0.0.1:61616)?initialReconnectDelay=100&randomize=false",
				"org.jwebsocket.jws2jms", // consumer topic
				"org.jwebsocket.jms2jws" // producer topic
				);

		// this is a console app 
		// so wait in a thread loop until the client get shut down
		try {
			while (!lJMSClient.isShutDown()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException lEx) {
		}

		System.out.println("Disconnecting JMS client...");
		lJMSClient.shutDown();
		System.out.println("Disconnected JMS client.");
	}
}
