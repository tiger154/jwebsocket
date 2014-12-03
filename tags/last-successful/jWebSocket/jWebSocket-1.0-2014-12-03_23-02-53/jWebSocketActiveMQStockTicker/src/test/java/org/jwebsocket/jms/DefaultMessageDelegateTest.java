//	---------------------------------------------------------------------------
//	jWebSocket - DefaultMessageDelegateTest (Community Edition, CE)
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

/**
 *
 * @author Alexander Schulze
 */
@ContextConfiguration(locations = {"classpath:JMSPlugIn.xml"})
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
	/**
	 *
	 */
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

		for (int i = 0; i < 10; i++) {
			lSender.convertAndSend("test string message");
		}

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			Assert.fail("thread problem");
		}

	}
}
