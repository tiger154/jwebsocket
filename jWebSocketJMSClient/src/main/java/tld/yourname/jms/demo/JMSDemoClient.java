//  ---------------------------------------------------------------------------
//  jWebSocket - JMS Demo Client (Community Edition, CE)
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
package tld.yourname.jms.demo;

import java.util.Properties;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jwebsocket.jms.client.JMSClient;

/**
 * JMS Gateway Demo Client
 *
 * @author Alexander Schulze
 */
public class JMSDemoClient {

	static final Logger mLog = Logger.getLogger(JMSDemoClient.class);

	/**
	 *
	 * @param aArgs
	 */
	public static void main(String[] aArgs) {

		// set up log4j logging
		// later this should be read from a shared log4j properties or xml file!
		Properties lProps = new Properties();
		lProps.setProperty("log4j.rootLogger", "INFO, console");
		lProps.setProperty("log4j.logger.org.apache.activemq.spring", "WARN");
		lProps.setProperty("log4j.logger.org.apache.activemq.web.handler", "WARN");
		lProps.setProperty("log4j.logger.org.springframework", "WARN");
		lProps.setProperty("log4j.logger.org.apache.xbean", "WARN");
		lProps.setProperty("log4j.logger.org.apache.camel", "INFO");
		lProps.setProperty("log4j.logger.org.eclipse.jetty", "WARN");
		lProps.setProperty("log4j.appender.console", "org.apache.log4j.ConsoleAppender");
		lProps.setProperty("log4j.appender.console.layout", "org.apache.log4j.PatternLayout");
		lProps.setProperty("log4j.appender.console.layout.ConversionPattern", "%p: %m%n");
		lProps.setProperty("log4j.appender.console.threshold", "INFO");
		PropertyConfigurator.configure(lProps);

		String lBrokerURL = "failover:(tcp://0.0.0.0:61616,tcp://127.0.0.1:61616)?initialReconnectDelay=100&randomize=false";
		String lConsumerTopic = "org.jwebsocket.jws2jms";
		String lProducerTopic = "org.jwebsocket.jms2jws";
		String lNodeId = UUID.randomUUID().toString();

		// tcp://172.20.116.68:61616 org.jwebsocket.jws2jms org.jwebsocket.jms2jws aschulze-dt
		mLog.info("JMS Gateway Demo Client");

		if (null != aArgs && aArgs.length >= 3) {
			lBrokerURL = aArgs[0];
			lConsumerTopic = aArgs[1];
			lProducerTopic = aArgs[2];
			if (aArgs.length >= 4) {
				lNodeId = aArgs[3];
			}
			mLog.info("Using: "
					+ lBrokerURL + ", "
					+ lConsumerTopic + ", "
					+ lProducerTopic + ", "
					+ lNodeId);
		} else {
			mLog.info("Usage: java -jar jWebSocketActiveMQClientBundle-<ver>.jar URL consumer-topic producer-topic [node-id]");
			mLog.info("Example: java -jar jWebSocketActiveMQClientBundle-1.0.jar tcp://172.20.116.68:61616 " + lConsumerTopic + " " + lProducerTopic + " [your node id]");
			System.exit(1);
		}

		// instantiate a new jWebSocket JMS Gateway Client
		JMSClient lJMSClient = new JMSClient(
				lBrokerURL,
				lConsumerTopic, // consumer topic
				lProducerTopic, // producer topic
				lNodeId, // unique node id
				5 // thread pool size, message being processed concurrently
				);

		// add a listener to listen in coming messages
		lJMSClient.addListener(new JMSDemoMessageListener(lJMSClient));

		// this is a console app demo
		// so wait in a thread loop until the client get shut down
		try {
			while (!lJMSClient.isShutdown()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException lEx) {
			// ignore a potential exception here
		}

		// check if JMS client has already been shutdown by logic
		if (!lJMSClient.isShutdown()) {
			// if not yet done...
			mLog.info("Shutting down JMS client...");
			// shut the client properly down
			lJMSClient.shutdown();
		}
		// and show final status message in the console
		mLog.info("JMS node properly shutdown.");
	}
}
