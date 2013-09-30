/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @author kyberneees
 */
public class AMQClusterTest {

	public static void main(String[] args) {
		try {
			ActiveMQConnectionFactory lFactory = new ActiveMQConnectionFactory("tcp://192.168.56.2:61616");
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
			
			ActiveMQConnectionFactory lFactory2 = new ActiveMQConnectionFactory("tcp://192.168.56.4:61616");
			Connection lConnection2 = lFactory2.createConnection();
			lConnection2.setClientID("02");
			lConnection2.start();
			
			Session lSession2 = lConnection2.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer lProducer = lSession2.createProducer(lSession2.createTopic("jws_cloud1"));
			lProducer.send(lSession2.createTextMessage("Hello cluster P1"));
			
			ActiveMQConnectionFactory lFactory3 = new ActiveMQConnectionFactory("tcp://192.168.56.3:61616");
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
