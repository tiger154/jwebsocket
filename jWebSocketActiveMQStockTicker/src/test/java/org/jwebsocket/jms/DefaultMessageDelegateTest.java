package org.jwebsocket.jms;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.jms.infra.impl.DefaultMessageDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = { "classpath:JMSPlugIn.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class DefaultMessageDelegateTest {

	private Logger mLog = Logging.getLogger(getClass());

	@Autowired
	ConnectionFactory mConnectionFactory;

	@Autowired
	Queue mQueue;

	// @Autowired
	// DefaultMessageListenerContainer cont;
	//

	@Test
	public void testReceptionOfJmsMessages() {
		Assert.assertNotNull(mConnectionFactory);
		Assert.assertNotNull(mQueue);
		DefaultMessageDelegate del = new DefaultMessageDelegate(null, null);
		MessageListenerAdapter ad = new MessageListenerAdapter(del);
		ad.setMessageConverter(null);
		DefaultMessageListenerContainer cont = new DefaultMessageListenerContainer();
		cont.setConnectionFactory(mConnectionFactory);
		cont.setDestination(mQueue);
		cont.setMessageListener(ad);
		cont.afterPropertiesSet();
		cont.start();

		JmsTemplate lSender = new JmsTemplate();
		lSender.setConnectionFactory(mConnectionFactory);
		lSender.setDefaultDestination(mQueue);
		lSender.setPubSubDomain(false);

		for (int i = 0; i < 10; i++)
			lSender.convertAndSend("test string message");

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			Assert.fail("thread problem");
		}

	}
}
