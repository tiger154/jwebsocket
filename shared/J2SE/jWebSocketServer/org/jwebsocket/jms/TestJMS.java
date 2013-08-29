package org.jwebsocket.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnection;

/**
 *
 * @author kyberneees
 */
public class TestJMS {

	public static void main(String[] args) {
		try {
			ActiveMQConnection lConnection = ActiveMQConnection.makeConnection();
			lConnection.start();
			final Session lSession = lConnection.createSession(true, Session.SESSION_TRANSACTED);
			final Session lSession2 = lConnection.createSession(true, Session.SESSION_TRANSACTED);

			
			MessageConsumer lAdvisor = lSession.createConsumer(lSession.createTopic("ActiveMQ.Advisory.MessageConsumed.Queue.>"));
			lAdvisor.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message msg) {
					System.out.println(msg);
				}
			});
			MessageConsumer lConsumer = lSession.createConsumer(lSession.createQueue("test"));
			lConsumer.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message msg) {
					try {
						System.out.println(msg);
						lSession.commit();
					} catch (JMSException ex) {
						Logger.getLogger(TestJMS.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			});

			MessageProducer lProducer = lSession2.createProducer(lSession2.createQueue("test"));
			TextMessage lMessage = lSession2.createTextMessage("Hello fuck!");
			lProducer.send(lMessage);
			lSession2.commit();

		} catch (Exception ex) {
			Logger.getLogger(TestJMS.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
