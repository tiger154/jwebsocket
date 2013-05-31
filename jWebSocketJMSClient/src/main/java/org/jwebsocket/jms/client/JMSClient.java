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
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class JMSClient implements Runnable {

	private JmsTemplate mJMSTemplate;
	private DefaultMessageListenerContainer mBridgeListenerCont = null;

	public static void main(String[] aArgs) {
		new JMSClient().run();
	}

	public JMSClient() {
		mJMSTemplate = new JmsTemplate();
		ActiveMQConnectionFactory lConnectionFactory = new ActiveMQConnectionFactory(
				"failover:(tcp://0.0.0.0:61616,tcp://127.0.0.1:61616)?initialReconnectDelay=100&randomize=false");
		mJMSTemplate.setConnectionFactory(lConnectionFactory);
		mJMSTemplate.setDefaultDestinationName("org.jwebsocket.jms2jws");
		mJMSTemplate.setDeliveryPersistent(false);
		mJMSTemplate.setPubSubDomain(true);
		mJMSTemplate.setSessionTransacted(false);

		FileSystemXmlApplicationContext lFileCtx =
				new FileSystemXmlApplicationContext("jms_client.xml");
		mBridgeListenerCont =
				(DefaultMessageListenerContainer) lFileCtx.getBean("jws2jmsListenerContainer");
		JMSClientListener lListener = (JMSClientListener) mBridgeListenerCont.getMessageListener();
		lListener.setJmsTemplate(mJMSTemplate);
		mBridgeListenerCont.start();
	}

	@Override
	public void run() {
		System.out.println("Connecting JMS client...");
		mSendConnect();

		System.out.println("Sending JMS broadcasts...");
		try {
			while (true) {
				Thread.sleep(2000L);
				mBroadcast();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (null != mBridgeListenerCont) {
			mBridgeListenerCont.stop();
		}

	}

	private void mSendConnect() {
		String lJSON = "{\"ns\":\"org.jwebsocket.jms.bridge\",\"type\":\"connect\"}";
		System.out.println("Sending connect " + lJSON + "...");
		mJMSTemplate.convertAndSend(lJSON);
	}

	private void mBroadcast() {
		String lPacket = "{\"ns\":\"org.jwebsocket.plugins.system\",\"type\":\"broadcast\",\"timestamp\":" + System.currentTimeMillis() + ",\"data\":\"test\"}";
		System.out.println("Sending broadcast " + lPacket);
		mJMSTemplate.convertAndSend(lPacket);
	}
}
