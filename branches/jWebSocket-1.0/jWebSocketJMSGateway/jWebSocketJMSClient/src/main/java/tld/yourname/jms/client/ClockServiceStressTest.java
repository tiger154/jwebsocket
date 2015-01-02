//	---------------------------------------------------------------------------
//	jWebSocket - JMS Gateway Stress Test (Community Edition, CE)
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
package tld.yourname.jms.client;

import java.util.Properties;
import java.util.UUID;
import javax.jms.JMSException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jwebsocket.jms.endpoint.JMSEndPoint;
import org.jwebsocket.jms.endpoint.JWSMessageListener;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.jwebsocket.jms.endpoint.JMSLogging;
import org.jwebsocket.jms.endpoint.JWSEndPoint;
import org.jwebsocket.jms.endpoint.JWSResponseTokenListener;

/**
 * JMS Gateway stress test for Clock Demo service (developers oriented)
 *
 * @author Rolando Santamaria Maso
 */
public class ClockServiceStressTest {

	static final Logger mLog = Logger.getLogger(ClockServiceStressTest.class);

	private static JWSEndPoint lJWSEndPoint;

	private static long lStartTime, lEndTime;
	private static int lReqSent = 0;
	private static int lResponseReceived = 0;

	/**
	 *
	 * @param aArgs
	 */
	@SuppressWarnings("SleepWhileInLoop")
	public static void main(String[] aArgs) {

		// set up log4j logging
		// later this should be read from a shared log4j properties or xml file!
		Properties lProps = new Properties();
		lProps.setProperty("log4j.rootLogger", "ERROR, console");
		lProps.setProperty("log4j.logger.org.apache.activemq.spring", "WARN");
		lProps.setProperty("log4j.logger.org.apache.activemq.web.handler", "WARN");
		lProps.setProperty("log4j.logger.org.springframework", "WARN");
		lProps.setProperty("log4j.logger.org.apache.xbean", "WARN");
		lProps.setProperty("log4j.logger.org.apache.camel", "INFO");
		lProps.setProperty("log4j.logger.org.eclipse.jetty", "WARN");
		lProps.setProperty("log4j.appender.console", "org.apache.log4j.ConsoleAppender");
		lProps.setProperty("log4j.appender.console.layout", "org.apache.log4j.PatternLayout");
		lProps.setProperty("log4j.appender.console.layout.ConversionPattern",
				// "%p: %m%n"
				"%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p - %C{1}: %m%n"
		);
		// set here the jWebSocket log level:
		lProps.setProperty("log4j.logger.org.jwebsocket", "ERROR");
		lProps.setProperty("log4j.appender.console.threshold", "ERROR");
		PropertyConfigurator.configure(lProps);

		// only for debug purposes
		// JMSLogging.setFullTextLogging(true);
		mLog.info("jWebSocket JMS Gateway Demo Client");

		Configuration lConfig = null;
		boolean lConfigLoaded;
		try {
			// try to load properties files from local folder or jar
			String lPath = "JMSClient.properties";
			mLog.debug("Tring to read properties from: " + lPath);
			lConfig = new PropertiesConfiguration(lPath);
		} catch (ConfigurationException ex) {
		}
		if (null == lConfig) {
			try {
				// try to load properties files from JWEBSOCKET_HOME/conf/JMSPlugIn
				String lPath = Tools.expandEnvVarsAndProps("${JWEBSOCKET_HOME}conf/JMSPlugIn/JMSClient.properties");
				// String lPath = Tools.expandEnvVarsAndProps("${JWEBSOCKET_HOME}conf/JMSPlugIn/JMSClient.properties");
				mLog.debug("Tring to read properties from: " + lPath);
				lConfig = new PropertiesConfiguration(lPath);
			} catch (ConfigurationException ex) {
			}
		}
		if (null == lConfig) {
			mLog.error("Configuration file could not be opened.");
			return;
		}

		// the URL of the message broker
		String lBrokerURL = lConfig.getString("BrokerURL", "tcp://127.0.0.1:61616");
		// "failover:(tcp://0.0.0.0:61616,tcp://127.0.0.1:61616)?initialReconnectDelay=100&randomize=false";
		// the name of the JMS Gateway topic
		String lGatewayTopic = lConfig.getString("GatewayTopic", "org.jwebsocket.jms.gateway");
		// endpoint id of JMS Gateway
		String lGatewayId = lConfig.getString("GatewayId", "org.jwebsocket.jms.gateway");
		String lEndPointId = lConfig.getString("EndPointId", UUID.randomUUID().toString());

		// get authentication information against jWebSocket
		final String lJWSUsername = lConfig.getString("JWSUsername");
		final String lJWSPassword = lConfig.getString("JWSPassword");
		final boolean lFullTextLogging = lConfig.getBoolean("FullTextLogging", false);

		mLog.info("Using: "
				+ lBrokerURL + ", "
				+ "topic: " + lGatewayTopic + ", "
				+ "gateway-id: " + lGatewayId + ", "
				+ "endpoint-id: " + lEndPointId);

		// todo: Comment that for production purposes
		JMSLogging.setFullTextLogging(lFullTextLogging);

		try {
			lJWSEndPoint = JWSEndPoint.getInstance(
					lBrokerURL,
					lGatewayTopic, // gateway topic
					lGatewayId, // gateway endpoint id
					lEndPointId, // unique node id
					4, // thread pool size, messages being processed concurrently
					JMSEndPoint.TEMPORARY // durable (for servers) or temporary (for clients)
			);

			// in case you will require the server LoadBalancer features
			// set the CPU updater for the instance
			// lCpuUpdater = new JWSLoadBalancerCpuUpdater(lJWSEndPoint, lTargetEndPointId);
			// lCpuUpdater.autoStart();
		} catch (JMSException lEx) {
			mLog.fatal("JMSEndpoint could not be instantiated: " + lEx.getMessage());
			System.exit(0);
		}

		// on welcome message from jWebSocket, authenticate against jWebSocket
		lJWSEndPoint.addRequestListener("org.jwebsocket.jms.gateway", "welcome", new JWSMessageListener(lJWSEndPoint) {
			@Override
			public void processToken(String aSourceId, Token aToken) {
				if ("org.jwebsocket.jms.gateway".equals(aSourceId)) {
					// create a login token...
					mLog.info("Authenticating against jWebSocket...");
					Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.system", "login");
					if (null != lJWSUsername && null != lJWSPassword) {
						lToken.setString("username", lJWSUsername);
						lToken.setString("password", lJWSPassword);
					}
					// and send it to the gateway (which is was the source of the message)
					sendToken(aSourceId, lToken,
							new JWSResponseTokenListener(JWSResponseTokenListener.RESP_TIME_FIELD) {

								@Override
								public void onTimeout() {
									mLog.info("Login timed out!");
								}

								@Override
								public void onFailure(Token aReponse) {
									mLog.error("Login failure!");
								}

								@Override
								public void onSuccess(Token aReponse) {
									if (mLog.isInfoEnabled()) {
										mLog.info("Login success (response received in "
												+ aReponse.getLong(JWSResponseTokenListener.RESP_TIME_FIELD) + "ms).");
									}
								}

							}, 1000);
				}
			}
		});

		// process response of the JMS Gateway login...
		lJWSEndPoint.addResponseListener("org.jwebsocket.plugins.system", "login",
				new JWSMessageListener(lJWSEndPoint) {
					@Override
					public void processToken(String aSourceId, Token aToken) {
						mLog.info("Login successful, initiating stress test...");
						startStressTest();
					}

				});

		// start the endpoint all all listener have been assigned
		lJWSEndPoint.start();

		// add a listener to listen in coming messages
		// lJMSClient.addListener(new JMSClientMessageListener(lJMSClient));
		// this is a console app demo
		// so wait in a thread loop until the client get shut down
		try {
			while (lJWSEndPoint.isOpen()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException lEx) {
			// ignore a potential exception here
		}

		// check if JMS client has already been shutdown by logic
		if (!lJWSEndPoint.isShutdown()) {
			// if not yet done...
			mLog.info("Shutting down JMS Client Endpoint...");
			// shut the client properly down
			lJWSEndPoint.shutdown();
		}

		// and show final status message in the console
		mLog.info("JMS Client Endpoint properly shutdown.");
	}

	private static void startStressTest() {
		final int lIterations = 10;
		int lCounter = 0;
		lStartTime = System.currentTimeMillis();
		final long lSleepInterval = 100;
		while (lCounter < lIterations) {
			try {
				Thread.sleep(lSleepInterval);
			} catch (InterruptedException lEx) {
			}
			lReqSent++;
			Token lToken = TokenFactory.createToken("somecompany.service.clock", "getTime");
			lToken.setString("username", "admin");
			lToken.setString("password", "admin");
			// and send it to the gateway (which is was the source of the message)
			//lJWSEndPoint.sendToken("SomeCompany.Service.Node1", lToken,
			lJWSEndPoint.sendToken(lJWSEndPoint.getGatewayId(), lToken,
					new JWSResponseTokenListener() {

						@Override
						public void onTimeout() {
							lResponseReceived++;
						}

						@Override
						public void onFailure(Token aReponse) {
						}

						@Override
						public void onSuccess(Token aReponse) {
							lResponseReceived++;
							lEndTime = System.currentTimeMillis();

							System.out.println(lResponseReceived + ", " + (((lEndTime - lStartTime) - lReqSent * lSleepInterval) / 1000));
							
							if (lResponseReceived == lIterations){
								lJWSEndPoint.shutdown();
							}
						}
					}, 1000 * 60);

			lCounter++;
		}

	}
}
