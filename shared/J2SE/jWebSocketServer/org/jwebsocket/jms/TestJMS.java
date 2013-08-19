package org.jwebsocket.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author kyberneees
 */
public class TestJMS {

	public static void main(String[] args) {
		try {
			ConnectionFactory mConnectionFactory = new ActiveMQConnectionFactory("tcp://0.0.0.0:61616");
			Connection mConnection = mConnectionFactory.createConnection();
			// setting the clientID is required for durable subscribers
			// mConnection.setClientID(mEndPointId);
			mConnection.start();

			Session mSession = mConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Session mSession2 = mConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			
			Topic lNvidiaTopic = mSession.createTopic("org.jwebsocket.jms.gateway");
			
			// create the listener to the advisory topic
			Topic lAdvisoryTopic = mSession.createTopic("ActiveMQ.Advisory.Consumer.Topic.org.jwebsocket.jms.gateway");
			MessageConsumer mAdvisoryConsumer = mSession.createConsumer(lAdvisoryTopic);
			
			mAdvisoryConsumer.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message msg) {
					System.out.println(msg);
				}
			});
		} catch (JMSException ex) {
			Logger.getLogger(TestJMS.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
