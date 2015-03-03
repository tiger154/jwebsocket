//	---------------------------------------------------------------------------
//	jWebSocket - JMS Gateway Demo Server (Community Edition, CE)
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
package tld.yourname.jms.server;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.jms.JMSException;
import javolution.util.FastMap;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.commons.io.FileUtils;
import org.jwebsocket.jms.endpoint.JMSEndPoint;
import org.jwebsocket.jms.endpoint.JMSEndpointException;
import org.jwebsocket.jms.endpoint.JMSLogging;
import org.jwebsocket.jms.endpoint.JWSAutoSelectAuthenticator;
import org.jwebsocket.jms.endpoint.JWSEndPoint;
import org.jwebsocket.jms.endpoint.JWSLDAPAuthenticator;
import org.jwebsocket.jms.endpoint.JWSMessageListener;
import org.jwebsocket.jms.endpoint.JWSOAuthAuthenticator;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 * JMS Gateway Demo Server
 *
 * @author Alexander Schulze
 */
public class JMSServer {

	static final Logger mLog = Logger.getLogger(JMSServer.class);
	private static JWSEndPoint lJWSEndPoint;
	private static final String mDomain = "yourdomain.tld";

	private void checkADUsername(Token aToken) {
		String lUsername = aToken.getString("username");
		if (null != lUsername) {
			lUsername = lUsername.toLowerCase().endsWith("@" + mDomain) ? lUsername : lUsername + "@" + mDomain;
			aToken.setString("username", lUsername);
		}
	}

	/**
	 *
	 * @param aArgs
	 * @return
	 */
	public JWSEndPoint start(String[] aArgs) {
		// set up log4j logging
		// later this should be read from a shared log4j properties or xml file!
		Properties lProps = new Properties();
		lProps.setProperty("log4j.rootLogger", "INFO, console");
		lProps.setProperty("log4j.logger.org.apache.activemq", "WARN");
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
		lProps.setProperty("log4j.logger.org.jwebsocket", "DEBUG");
		lProps.setProperty("log4j.appender.console.threshold", "DEBUG");
		PropertyConfigurator.configure(lProps);

		final JWSAutoSelectAuthenticator lAuthenticator = new JWSAutoSelectAuthenticator();
		final JWSOAuthAuthenticator lOAuthAuthenticator = new JWSOAuthAuthenticator();
		final JWSLDAPAuthenticator lLDAPAuthenticator = new JWSLDAPAuthenticator();

		mLog.info("jWebSocket JMS Gateway Server Endpoint");
		Configuration lConfig = null;
		boolean lConfigLoaded;
		try {
			// try to load properties files from local folder or jar
			String lPath = "JMSServer.properties";
			mLog.debug("Trying to read properties from: " + lPath);
			lConfig = new PropertiesConfiguration(lPath);
		} catch (ConfigurationException ex) {
		}
		if (null == lConfig) {
			try {
				// try to load properties files from JWEBSOCKET_HOME/conf/JMSPlugIn
				String lPath = Tools.expandEnvVarsAndProps("/private/JMSServer.properties");
				// String lPath = Tools.expandEnvVarsAndProps("${JWEBSOCKET_HOME}conf/JMSPlugIn/JMSServer.properties");
				mLog.debug("Tring to read properties from: " + lPath);
				lConfig = new PropertiesConfiguration(lPath);
			} catch (ConfigurationException ex) {
			}
		}
		if (null == lConfig) {
			mLog.error("Configuration file could not be opened.");
			return null;
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

		// set up OAuth Authenticator
		boolean lUseOAuth = lConfig.getBoolean("UseOAuth", false);

		String lOAuthHost = lConfig.getString("OAuthHost");
		String lOAuthAppId = lConfig.getString("OAuthAppId");
		String lOAuthAppSecret = lConfig.getString("OAuthAppSecret");
		String lOAuthUsername = lConfig.getString("OAuthUsername");
		String lOAuthPassword = lConfig.getString("OAuthPassword");
		long lOAuthTimeout = lConfig.getLong("OAuthTimeout", 5000);

		lUseOAuth = lUseOAuth
				&& null != lOAuthHost
				&& null != lOAuthAppId
				&& null != lOAuthAppSecret
				&& null != lOAuthUsername
				&& null != lOAuthPassword;

		if (lUseOAuth) {
			lOAuthAuthenticator.init(
					lOAuthHost,
					lOAuthAppId,
					lOAuthAppSecret,
					lOAuthTimeout
			);
			lAuthenticator.addAuthenticator(lOAuthAuthenticator);
		}

		// set up LDAP Authenticator
		boolean lUseLDAP = lConfig.getBoolean("UseLDAP", false);

		String lLDAPURL = lConfig.getString("LDAPURL");
		String lBaseDNGroups = lConfig.getString("BaseDNGroups");
		String lBaseDNUsers = lConfig.getString("BaseDNUsers");

		if (lUseLDAP) {
			lLDAPAuthenticator.init(
					lLDAPURL,
					lBaseDNGroups,
					lBaseDNUsers
			);
			lAuthenticator.addAuthenticator(lLDAPAuthenticator);
		}

		// TODO: Validate config data here!
		lConfigLoaded = true;

		if (!lConfigLoaded) {
			mLog.error("Config not loaded.");
			System.exit(1);
		}

		mLog.info("Using: "
				+ lBrokerURL + ", "
				+ "topic: " + lGatewayTopic + ", "
				+ "gateway-id: " + lGatewayId + ", "
				+ "endpoint-id: " + lEndPointId);

		// todo: Comment that for production purposes
		JMSLogging.setFullTextLogging(lFullTextLogging);

		// instantiate a new jWebSocket JMS Gateway Client
		try {
			lJWSEndPoint = JWSEndPoint.getInstance(
					lBrokerURL,
					lGatewayTopic, // gateway topic
					lGatewayId, // gateway endpoint id
					lEndPointId, // unique node id
					5, // thread pool size, messages being processed concurrently
					JMSEndPoint.TEMPORARY // durable (for servers) or temporary (for clients)
			);
		} catch (JMSException lEx) {
			mLog.fatal("JMSEndpoint could not be instantiated: " + lEx.getMessage());
			System.exit(0);
		}

		lJWSEndPoint.addRequestListener(
				"org.jwebsocket.jms.gateway", "welcome", new JWSMessageListener(lJWSEndPoint) {
					@Override
					public void processToken(String aSourceId, Token aToken) {
						mLog.info("Received 'welcome', authenticating against jWebSocket...");
						Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.system", "login");
						lToken.setString("username", lJWSUsername);
						lToken.setString("password", lJWSPassword);
						sendToken(aSourceId, lToken);
					}
				}
		);

		// on response of the login...
		lJWSEndPoint.addResponseListener(
				"org.jwebsocket.plugins.system", "login", new JWSMessageListener(lJWSEndPoint) {
					@Override
					public void processToken(String aSourceId, Token aToken
					) {
						int lCode = aToken.getInteger("code", -1);
						if (0 == lCode) {
							if (mLog.isInfoEnabled()) {
								mLog.info("Authentication against jWebSocket successful.");
							}
						} else {
							mLog.error("Authentication against jWebSocket failed!");
						}
					}
				}
		);

		// on response of the login...
		lJWSEndPoint.addRequestListener(
				"org.jwebsocket.jms.demo", "getUser", new JWSMessageListener(lJWSEndPoint) {
					@Override
					public void processToken(String aSourceId, Token aToken
					) {
						String lPayload = aToken.getString("payload");
						if (mLog.isInfoEnabled()) {
							mLog.info("Processing 'getUser'...");
						}
					}
				}
		);

		// on response of the login...
		lJWSEndPoint.addRequestListener(
				"org.jwebsocket.jms.demo", "echo", new JWSMessageListener(lJWSEndPoint) {
					@Override
					public void processToken(String aSourceId, Token aToken
					) {
						String lPayload = aToken.getString("payload");
						if (mLog.isInfoEnabled()) {
							mLog.info("Processing 'demo1 with Payload '" + lPayload + "'");
						}
						Map<String, Object> lAdditionalResults = new FastMap<String, Object>();
						lAdditionalResults.putAll(aToken.getMap());
						// lAdditionalResults.remove("sourceId");
						lAdditionalResults.remove("payload");
						lJWSEndPoint.respondPayload(
								aToken,
								0, // return code
								"Ok", // return message
								lAdditionalResults,
								aToken.getString("payload"));
					}
				}
		);

		// on response of the login...
		lJWSEndPoint.addRequestListener(
				"org.jwebsocket.jms.demo", "testProgress", new JWSMessageListener(lJWSEndPoint) {
					@Override
					@SuppressWarnings("SleepWhileInLoop")
					public void processToken(String aSourceId, Token aToken) {
						int lMax = 10;
						for (int lIdx = 0; lIdx < lMax; lIdx++) {
							mLog.debug("Progress iteration " + lIdx + "...");
							try {
								Thread.sleep(333);
							} catch (InterruptedException lEx) {
							}
							lJWSEndPoint.sendProgress(
									aToken,
									((lIdx + 1.0) / lMax) * 100, 0,
									"Iteration #" + lIdx, null);
						}
						lJWSEndPoint.respondPayload(
								aToken,
								0, // return code
								"Ok", // return message
								null,
								aToken.getString("payload"));
					}
				}
		);

		// ...
		lJWSEndPoint.addRequestListener(
				"org.jwebsocket.jms.demo", "testCaughtException", new JWSMessageListener(lJWSEndPoint) {
					@Override
					public void processToken(String aSourceId, Token aToken) {
						mLog.debug("Testing caught exception...");
						// provoke null pointer exception and DO catch it for test purposes
						int a = 1;
						int b = 0;
						try {
							int c = a / b;
							lJWSEndPoint.respondPayload(
									aToken,
									0, // return code
									"Ok", // return message
									null,
									aToken.getString("payload"));
						} catch (Exception Ex) {
							lJWSEndPoint.respondPayload(
									aToken,
									-1, // return code
									Ex.getClass().getSimpleName() + ": "
									+ Ex.getMessage(), // return message
									null,
									aToken.getString("payload"));
						}
					}
				}
		);

		// ...
		lJWSEndPoint.addRequestListener(
				"org.jwebsocket.jms.demo", "testUncaughtException", new JWSMessageListener(lJWSEndPoint) {
					@Override
					public void processToken(String aSourceId, Token aToken) {
						mLog.debug("Testing uncaught exception...");
						// provoke null pointer exception and do NOT catch it for test purposes
						int a = 1;
						int b = 0;
						int c = a / b;

						lJWSEndPoint.respondPayload(
								aToken,
								0, // return code
								"Ok", // return message
								null,
								aToken.getString("payload"));
					}
				}
		);

		// test for the OAuth interface
		lJWSEndPoint.addRequestListener(
				"org.jwebsocket.jms.demo", "testOAuth", new JWSMessageListener(lJWSEndPoint) {
					@Override
					public void processToken(String aSourceId, Token aToken) {

						Map<String, Object> lArgs = new FastMap<String, Object>();

						String lUsername;
						int lCode = 0;
						String lMessage = "Ok";
						try {
							lUsername = lOAuthAuthenticator.authToken(aToken);
							if (null == lUsername) {
								lCode = -1;
								lMessage = "User could not be authenticated!";
							} else {
								lArgs.put("username", lUsername);
							}
						} catch (JMSEndpointException ex) {
							lCode = -1;
							lMessage = ex.getClass().getSimpleName()
							+ " on authentication: " + ex.getMessage();
						}

						lJWSEndPoint.respondPayload(
								aToken,
								lCode, // return code
								lMessage, // return message
								lArgs, // additional result fields
								null); // payload
					}
				}
		);

		// test for the LDAP interface
		lJWSEndPoint.addRequestListener(
				"org.jwebsocket.jms.demo", "testLDAP", new JWSMessageListener(lJWSEndPoint) {
					@Override
					public void processToken(String aSourceId, Token aToken) {

						Map<String, Object> lArgs = new FastMap<String, Object>();

						String lUsername;
						int lCode = 0;
						String lMessage = "Ok";
						try {
							lUsername = lLDAPAuthenticator.authToken(aToken);
							if (null == lUsername) {
								lCode = -1;
								lMessage = "User could not be authenticated!";
							} else {
								lArgs.put("username", lUsername);
							}
						} catch (JMSEndpointException ex) {
							lCode = -1;
							lMessage = ex.getClass().getSimpleName()
							+ " on authentication: " + ex.getMessage();
						}

						lJWSEndPoint.respondPayload(
								aToken,
								lCode, // return code
								lMessage, // return message
								lArgs, // additional result fields
								null); // payload
					}
				}
		);

		// test for the auto authentication interface
		lJWSEndPoint.addRequestListener(
				"org.jwebsocket.jms.demo", "testAuth", new JWSMessageListener(lJWSEndPoint) {
					@Override
					public void processToken(String aSourceId, Token aToken) {

						mLog.debug("Testing auto authenticator...");
						Map<String, Object> lArgs = new FastMap<String, Object>();

						String lUsername;
						int lCode = 0;
						String lMessage = "Ok";
						try {
							checkADUsername(aToken);
							lUsername = lAuthenticator.authToken(aToken);
							if (null == lUsername) {
								lCode = -1;
								lMessage = "User could not be authenticated!";
							} else {
								lArgs.put("username", lUsername);
							}
						} catch (JMSEndpointException lEx) {
							lCode = -1;
							lMessage = lEx.getClass().getSimpleName()
							+ " on auto authentication: " + lEx.getMessage();
						}

						lJWSEndPoint.respondPayload(
								aToken,
								lCode, // return code
								lMessage, // return message
								lArgs, // additional result fields
								null); // payload
					}
				}
		);

		lJWSEndPoint.addRequestListener(
				"tld.yourname.jms", "transferFile", new JWSMessageListener(lJWSEndPoint) {
					@Override
					public void processToken(String aSourceId, Token aToken
					) {
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
				}
		);

		// add a high level listener to listen in coming messages
		lJWSEndPoint.addRequestListener(
				"org.jwebsocket.jms.demo", "helloWorld", new JWSMessageListener(lJWSEndPoint) {
					@Override
					public void processToken(String aSourceId, Token aToken
					) {
						mLog.info("Received 'helloWorld'...");
						lJWSEndPoint.respondPayload(
								aToken.getString("sourceId"),
								aToken,
								0, // return code
								"Ok", // return message
								null, // here you can add additional results beside the payload
								"Hello World!");
					}
				}
		);

		// start the endpoint all all listener have been assigned
		lJWSEndPoint.start();
		return lJWSEndPoint;
	}

	/**
	 *
	 */
	public void shutdown() {
		if (lJWSEndPoint != null) {
			lJWSEndPoint.shutdown();
		}
	}

	/**
	 *
	 * @return
	 */
	public JMSEndPoint getJMSEndPoint() {
		return lJWSEndPoint;
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

		mLog.info("JMS Server Endpoint properly shutdown.");
	}
}
