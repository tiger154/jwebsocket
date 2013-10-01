//  ---------------------------------------------------------------------------
//  jWebSocket - AMQClusterFilter (Community Edition, CE)
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
package org.jwebsocket.plugins.jms;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ConsumerInfo;

/**
 *
 * @author kyberneees
 */
public class AMQClusterFilter extends BrokerFilter {

	private List<String> mTargetDestinations;
	private Connection mConnection;
	private Session mSession;
	private MessageProducer mProducer;

	public AMQClusterFilter(Broker aBroker, List<String> aTargetDestinations, String aUsername, String aPassword) {
		super(aBroker);
		mTargetDestinations = aTargetDestinations;
	}

	@Override
	public void brokerServiceStarted() {
		super.brokerServiceStarted();

		try {
			ActiveMQConnectionFactory lFactory = new ActiveMQConnectionFactory(getVmConnectorURI());
			mConnection = lFactory.createConnection();
			mConnection.start();
			mSession = mConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			mProducer = mSession.createProducer(null);
		} catch (JMSException ex) {
			Logger.getLogger(AMQClusterFilter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public Subscription addConsumer(ConnectionContext aContext, ConsumerInfo aInfo) throws Exception {
		String lDest = aInfo.getDestination().getQualifiedName();
		String lSelector = aInfo.getSelector();
		if (null != lDest && null != lSelector) {
			for (String lClusterDest : mTargetDestinations) {
				if (lClusterDest.matches(lDest)) {
					Message lConsumerInfoMsg = mSession.createMessage();

					// sending consumer info advice directly to the nodes topic
					lConsumerInfoMsg.setStringProperty("msgType", "CONSUMER_INFO");
					lConsumerInfoMsg.setStringProperty("selector", lSelector);
					lConsumerInfoMsg.setStringProperty("connectionId", aInfo.getConsumerId().getConnectionId());
					lConsumerInfoMsg.setStringProperty("consumerId", aInfo.getConsumerId().toString());
					
					mProducer.send(mSession.createTopic(aInfo.getDestination().getPhysicalName() + "_nodes"), lConsumerInfoMsg);
				}
			}
		}

		return super.addConsumer(aContext, aInfo);
	}
}
