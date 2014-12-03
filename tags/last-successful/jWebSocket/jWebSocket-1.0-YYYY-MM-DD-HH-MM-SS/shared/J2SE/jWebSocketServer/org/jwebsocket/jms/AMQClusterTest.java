//	---------------------------------------------------------------------------
//	jWebSocket - AMQClusterTest (Community Edition, CE)
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
package org.jwebsocket.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class AMQClusterTest {

	private static final String SERVER1 = "tcp://192.168.56.2:61616";
	private static final String SERVER2 = "tcp://192.168.56.3:61616";
	private static final String SERVER3 = "tcp://192.168.56.4:61616";
	
	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			
			ActiveMQConnectionFactory lFactory = new ActiveMQConnectionFactory(SERVER1);
			Connection lConnection1 = lFactory.createConnection();
			lConnection1.setClientID("01");
			lConnection1.start();
			Session lSession1 = lConnection1.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageConsumer lConsumer = lSession1.createConsumer(lSession1.createTopic("jws_cloud1"));
			lConsumer.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message msg) {
					System.out.println(msg);
				}
			});

			ActiveMQConnectionFactory lFactory2 = new ActiveMQConnectionFactory(SERVER3);
			Connection lConnection2 = lFactory2.createConnection();
			lConnection2.setClientID("02");
			lConnection2.start();

			Session lSession2 = lConnection2.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer lProducer = lSession2.createProducer(lSession2.createTopic("jws_cloud1"));
			lProducer.send(lSession2.createTextMessage("Hello cluster P1"));

			ActiveMQConnectionFactory lFactory3 = new ActiveMQConnectionFactory(SERVER2);
			Connection lConnection3 = lFactory3.createConnection();
			lConnection3.setClientID("03");
			lConnection3.start();

			Session lSession3 = lConnection3.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer lProducer2 = lSession2.createProducer(lSession3.createTopic("jws_cloud1"));
			lProducer2.send(lSession3.createTextMessage("Hello cluster P2"));

		} catch (JMSException ex) {
			Logger.getLogger(AMQClusterTest.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
