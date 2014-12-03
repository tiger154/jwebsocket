//	---------------------------------------------------------------------------
//	jWebSocket UsageExample (Community Edition, CE)
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
package org.jwebsocket.eventbus;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 *
 * @author kyberneees
 */
public class UsageExample {

	public static void main(String[] aArgs) {
		try {
			// creating AMQ test broker
			final BrokerService lBroker = new BrokerService();
			lBroker.setBrokerName("testBroker");
			lBroker.start();

			// creating JMS connection
			final Connection lConnection = new ActiveMQConnectionFactory("vm://testBroker").createConnection();
			// starting JMS connection
			lConnection.start();
			// creating IEventBus instances
			final JMSEventBus lEB1 = new JMSEventBus(lConnection, "event_bus");
			final JMSEventBus lEB2 = new JMSEventBus(lConnection, "event_bus");
			// initializing EB 1
			lEB1.initialize();
			// initializing EB 2
			lEB2.initialize();
			
			// registering handler to "test.topic" name-space
			lEB1.register("test.topic", new Handler() {

				@Override
				public void OnMessage(Token aToken) {
					System.out.println("Topic Listener 1: " + aToken);
				}
			});
			// registering other handler to "test.topic" name-space
			lEB1.register("test.t*", new Handler() {

				@Override
				public void OnMessage(Token aToken) {
					System.out.println("Topic Listener 2: " + aToken);
				}
			});
			// registering other handler to "test.topic" name-space
			lEB2.register("test.t*", new Handler() {

				@Override
				public void OnMessage(Token aToken) {
					System.out.println("Topic Listener 3: " + aToken);
				}
			});
			// registering handler to "test.queue" name-space
			lEB1.register("test.q*", new Handler() {

				@Override
				public void OnMessage(Token aToken) {
					System.out.println("Queue Listener 1:" + aToken);
					reply(createResponse(aToken));
				}
			});
			// registering other handler to "test.topic" name-space. 
			lEB1.register("test.queue", new Handler() {

				@Override
				public void OnMessage(Token aToken) {
					System.out.println("Queue Listener 2:" + aToken);
				}
			});
			// registering other handler to "test.topic" name-space.
			lEB2.register("test.queue", new Handler() {

				@Override
				public void OnMessage(Token aToken) {
					System.out.println("Queue Listener 3:" + aToken);
				}
			});

			// publishing a message to "test.topic" name-space
			// should invoke Topic Listener 1 and Topic Listener 2
			lEB1.publish(TokenFactory.createToken("test.topic", "action"));
			// sending a message to "test.queue" name-space
			// should invoke Queue Listener 1
			lEB1.send(TokenFactory.createToken("test.queue", "action"), new Handler() {

				@Override
				public void OnSuccess(Token aToken) {
					try {
						System.out.println("Response listener 1: " + aToken);
					} catch (Exception ex) {
						Logger.getLogger(UsageExample.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			});
			// sending message to "to_no_handlers_ns" name-space
			// should invoke Timeout callback because no response will be received
			lEB1.send(TokenFactory.createToken("to_no_handlers_ns", "action"), new Handler(new Long(3000)) {

				@Override
				public void OnTimeout(Token aToken) {
					try {
						System.out.println("Timeout callback: " + aToken);

						// finally release resources
						lEB1.shutdown();
						lEB2.shutdown();
						lConnection.close();
						lBroker.stop();
						Tools.stopUtilityTimer();
						Tools.stopUtilityThreadPool();
					} catch (Exception ex) {
						Logger.getLogger(UsageExample.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			});

		} catch (Exception ex) {
			Logger.getLogger(UsageExample.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
