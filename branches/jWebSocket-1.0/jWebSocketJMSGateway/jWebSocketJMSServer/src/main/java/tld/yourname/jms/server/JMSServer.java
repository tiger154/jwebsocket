//  ---------------------------------------------------------------------------
//  jWebSocket - JMS Gateway Demo Client (Community Edition, CE)
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
package tld.yourname.jms.server;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.commons.io.FileUtils;
import org.jwebsocket.jms.endpoint.JMSEndPoint;
import org.jwebsocket.jms.endpoint.JWSEndPointMessageListener;
import org.jwebsocket.jms.endpoint.JWSEndPointSender;
import org.jwebsocket.jms.endpoint.JWSMessageListener;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.sso.OAuth;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 * JMS Gateway Demo Client
 *
 * @author Alexander Schulze
 */
public class JMSServer {

	static final Logger mLog = Logger.getLogger(JMSServer.class);
	private JMSEndPoint lJMSEndPoint;

	/**
	 *
	 * @param aArgs
	 * @return
	 */
	public JMSEndPoint start(String[] aArgs) {
		// set up log4j logging
		// later this should be read from a shared log4j properties or xml file!
		Properties lProps = new Properties();
		lProps.setProperty("log4j.rootLogger", "DEBUG, console");
		lProps.setProperty("log4j.logger.org.jwebsocket", "DEBUG");
		lProps.setProperty("log4j.logger.org.apache.activemq", "WARN");
		lProps.setProperty("log4j.logger.org.springframework", "WARN");
		lProps.setProperty("log4j.logger.org.apache.xbean", "WARN");
		lProps.setProperty("log4j.logger.org.apache.camel", "INFO");
		lProps.setProperty("log4j.logger.org.eclipse.jetty", "WARN");
		lProps.setProperty("log4j.appender.console", "org.apache.log4j.ConsoleAppender");
		lProps.setProperty("log4j.appender.console.layout", "org.apache.log4j.PatternLayout");
		lProps.setProperty("log4j.appender.console.layout.ConversionPattern", "%p: %m%n");
		lProps.setProperty("log4j.appender.console.threshold", "DEBUG");
		PropertyConfigurator.configure(lProps);

		String lBrokerURL = "failover:(tcp://0.0.0.0:61616,tcp://127.0.0.1:61616)?initialReconnectDelay=100&randomize=false";
		// the name of the JMD Gateway topic
		String lGatewayTopic = "org.jwebsocket.jms.gateway"; // topic name of JMS Gateway
		String lGatewayId = "org.jwebsocket.jms.gateway"; // endpoint id of JMS Gateway
		String lEndPointId = UUID.randomUUID().toString();

		String lOAuthHostURL = "https://hqdvpngpoc01.nvidia.com/as/token.oauth2";
		String lOAuthSecret = "2Federate";

		// tcp://172.20.116.68:61616 org.jwebsocket.jws2jms org.jwebsocket.jms2jws aschulze-dt
		// failover:(tcp://0.0.0.0:61616,tcp://127.0.0.1:61616)?initialReconnectDelay=100&randomize=false org.jwebsocket.jws2jms org.jwebsocket.jms2jws aschulze-dt
		mLog.info("jWebSocket JMS Gateway Server Endpoint");

		if (null != aArgs && aArgs.length >= 3) {
			lBrokerURL = aArgs[0];
			lGatewayTopic = aArgs[1];
			lGatewayId = aArgs[2];
			if (aArgs.length >= 4) {
				lEndPointId = aArgs[3];
			}
			mLog.info("Using: "
					+ lBrokerURL + ", "
					+ lGatewayTopic + ", "
					+ lGatewayId + ", "
					+ lEndPointId);
		} else {
			mLog.info("Usage: java -jar jWebSocketJMSServer-<ver>.jar URL gateway-topic gateway-id [node-id]");
			mLog.info("Example: java -jar jWebSocketJMSServerBundle-1.0.jar tcp://172.20.116.68:61616 " + lGatewayTopic + " " + lGatewayId + " [your node id]");
			System.exit(1);
		}

		// instantiate a new jWebSocket JMS Gateway Client
		lJMSEndPoint = new JMSEndPoint(
				lBrokerURL,
				lGatewayTopic, // gateway topic
				lGatewayId, // gateway endpoint id
				lEndPointId, // unique node id
				5, // thread pool size, messages being processed concurrently
				JMSEndPoint.TEMPORARY // durable (for servers) or temporary (for clients)
		);

		JWSEndPointMessageListener lListener = new JWSEndPointMessageListener(lJMSEndPoint);
		final JWSEndPointSender lSender = new JWSEndPointSender(lJMSEndPoint);

		// integrate OAuth library 
		final OAuth lOAuth = new OAuth(lOAuthHostURL, lOAuthSecret);

		// on welcome message from jWebSocket, authenticate against jWebSocket
		lListener.addRequestListener("org.jwebsocket.jms.gateway", "welcome", new JWSMessageListener(lSender) {
			@Override
			public void processToken(String aSourceId, Token aToken) {
				mLog.info("Received 'welcome', authenticating against jWebSocket...");
				Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.system", "login");
				lToken.setString("username", "root");
				lToken.setString("password", "root");
				sendToken(aSourceId, lToken);
			}
		});

		// on response of the login...
		lListener.addResponseListener("org.jwebsocket.plugins.system", "login", new JWSMessageListener(lSender) {
			@Override
			public void processToken(String aSourceId, Token aToken) {
				int lCode = aToken.getInteger("code", -1);
				if (0 == lCode) {
					if (mLog.isInfoEnabled()) {
						mLog.info("Authentication against jWebSocket successful.");
					}
				} else {
					mLog.error("Authentication against jWebSocket failed!");
				}
			}
		});

		// on response of the login...
		lListener.addRequestListener("org.jwebsocket.jms.demo", "getUser", new JWSMessageListener(lSender) {
			@Override
			public void processToken(String aSourceId, Token aToken) {
				String lPayload = aToken.getString("payload");
				if (mLog.isInfoEnabled()) {
					mLog.info("Processing 'getUser'...");
				}
				Map<String, Object> lAdditionalResults = new FastMap();
				String lAccessToken = aToken.getString("accessToken");
				String lJSON = lOAuth.getUser(lAccessToken);
				String lUsername = lOAuth.getUsername();
				lAdditionalResults.put("username", lUsername);
				lSender.respondPayload(
						aToken.getString("sourceId"),
						aToken,
						lOAuth.getReturnCode(), // return code
						lOAuth.getReturnMsg(), // return message
						lAdditionalResults, // here you can add additional results beside the payload
						"{}");
			}
		});

		// on response of the login...
		lListener.addRequestListener(
				"org.jwebsocket.jms.demo", "echo", new JWSMessageListener(lSender) {
					@Override
					public void processToken(String aSourceId, Token aToken) {
						String lPayload = aToken.getString("payload");
						if (mLog.isInfoEnabled()) {
							mLog.info("Processing 'demo1 with Payload '" + lPayload + "'");
						}
						Map<String, Object> lAdditionalResults = new FastMap();
						lAdditionalResults.putAll(aToken.getMap());
						// lAdditionalResults.remove("sourceId");
						lAdditionalResults.remove("payload");
						lSender.respondPayload(
								aToken.getString("sourceId"),
								aToken,
								0, // return code
								"Ok", // return message
								lAdditionalResults,
								aToken.getString("payload"));
					}
				});

		lListener.addRequestListener(
				"tld.yourname.jms", "transferFile", new JWSMessageListener(lSender) {
					@Override
					public void processToken(String aSourceId, Token aToken) {
						// here you can get the additional arguments
						mLog.info("Received 'transferFile' with additional args"
								+ " (arg1=" + aToken.getString("arg1")
								+ " (arg2=" + aToken.getString("arg2") + ")...");
						// here you get the payload from the requester
						String lPayload = aToken.getString("payload");
						// parse the JSON payload into a Token (for simpler processing)
						Token lToken = JSONProcessor.JSONStringToToken(lPayload);
				// extract the base64 and compressed file contents into Strings 
						// (it's a text message)
						// String lBase64Encoded = lToken.getString("fileAsBase64");
						String lBase64Zipped = lToken.getString("fileAsZip");

						// specify the target file
						File lFile = new File("Apache License 2.0 (copy).txt");
						try {
							// take the zipped version of the file... 
							byte[] lBA = Tools.unzip(lBase64Zipped.getBytes("UTF-8"), Boolean.TRUE);
							// and save it to the hard disk
							FileUtils.writeByteArrayToFile(lFile, lBA);
						} catch (Exception lEx) {
							mLog.error("Demo file " + lFile.getAbsolutePath() + " could not be saved!");
						}
					}
				});

		// add a high level listener to listen in coming messages
		lListener.addRequestListener(
				"org.jwebsocket.jms.demo", "helloWorld", new JWSMessageListener(lSender) {
					@Override
					public void processToken(String aSourceId, Token aToken) {
						mLog.info("Received 'helloWorld'...");
						lSender.respondPayload(
								aToken.getString("sourceId"),
								aToken,
								0, // return code
								"Ok", // return message
								null, // here you can add additional results beside the payload
								"Hello World!");
					}
				});

		// add a high level listener to listen in coming messages
		lJMSEndPoint.addListener(lListener);

		// start the endpoint all all listener have been assigned
		lJMSEndPoint.start();
		return lJMSEndPoint;
	}

	/**
	 *
	 */
	public void shutdown() {
		if (lJMSEndPoint != null) {
			lJMSEndPoint.shutdown();
		}
	}

	/**
	 *
	 * @return
	 */
	public JMSEndPoint getJMSEndPoint() {
		return lJMSEndPoint;
	}

	/**
	 *
	 * @param aArgs
	 */
	@SuppressWarnings("SleepWhileInLoop")
	public static void main(String[] aArgs) {
		JMSServer lJMSServer = new JMSServer();
		lJMSServer.start(aArgs);
		// add a primitive listener to listen in coming messages
		// this one is deprecated, only left for reference purposes!
		// lJMSClient.addListener(new JMSServerMessageListener(lJMSClient));
		// this is a console app demo
		// so wait in a thread loop until the client get shut down
		JMSEndPoint lEndpoint = lJMSServer.getJMSEndPoint();
		try {
			while (!lEndpoint.isShutdown()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException lEx) {
			// ignore a potential exception here
		}

		// check if JMS client has already been shutdown by logic
		if (!lEndpoint.isShutdown()) {
			// if not yet done...
			mLog.info("Shutting down JMS Server Endpoint...");
			// shut the client properly down
			lEndpoint.shutdown();
		}
		// and show final status message in the console

		mLog.info(
				"JMS Server Endpoint properly shutdown.");
	}
}
