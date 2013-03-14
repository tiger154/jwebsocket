//	---------------------------------------------------------------------------
//	jWebSocket - JmsListenerContainer (Community Edition, CE)
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
package org.jwebsocket.plugins.jms.infra.impl;

/**
 *
 * @author Johannes Smutny
 */
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.jwebsocket.plugins.jms.infra.MessageConsumerRegistry;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

/**
 *
 * @author aschulze
 */
public class JmsListenerContainer extends DefaultMessageListenerContainer {

	private MessageConsumerRegistry mMessageConsumerRegistry;

	private JmsListenerContainer() {
	}

	/**
	 *
	 * @param aMessageDelegate
	 * @param aConnectionFactory
	 * @param aDestination
	 * @return
	 */
	public static JmsListenerContainer valueOf(DefaultMessageDelegate aMessageDelegate,
			ConnectionFactory aConnectionFactory, Destination aDestination) {
		JmsListenerContainer result = initContainer(aMessageDelegate);
		init(result, aMessageDelegate, aConnectionFactory, aDestination);
		return result;
	}

	private static JmsListenerContainer initContainer(DefaultMessageDelegate aMessageDelegate) {
		JmsListenerContainer lResult = new JmsListenerContainer();
		MessageListenerAdapter lAdapter = new MessageListenerAdapter(aMessageDelegate);
		lAdapter.setMessageConverter(null);
		lResult.setMessageListener(lAdapter);
		return lResult;
	}

	private static void init(JmsListenerContainer result, MessageConsumerRegistry aMessageConsumerRegistry,
			ConnectionFactory aConnectionFactory, Destination aDestination) {
		result.setMessageConsumerRegistry(aMessageConsumerRegistry);
		result.setConnectionFactory(aConnectionFactory);
		result.setDestination(aDestination);
		result.setConcurrentConsumers(1);
	}

	private void setMessageConsumerRegistry(MessageConsumerRegistry aMessageConsumerRegistry) {
		mMessageConsumerRegistry = aMessageConsumerRegistry;

	}

	/**
	 *
	 * @return
	 */
	public MessageConsumerRegistry getMessageConsumerRegistry() {
		return mMessageConsumerRegistry;
	}
}
