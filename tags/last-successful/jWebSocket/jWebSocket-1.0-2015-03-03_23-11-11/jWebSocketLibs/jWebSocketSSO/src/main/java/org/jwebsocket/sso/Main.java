//	---------------------------------------------------------------------------
//	jWebSocket OAuth demo for Java (Community Edition, CE)
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
package org.jwebsocket.sso;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author Alexander Schulze
 */
public class Main {

	static final Logger mLog = Logger.getLogger(Main.class);

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {

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
		lProps.setProperty("log4j.appender.console.layout.ConversionPattern",
				// "%p: %m%n"
				"%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p - %C{1}: %m%n"
		);
		// set here the jWebSocket log level:
		lProps.setProperty("log4j.logger.org.jwebsocket", "DEBUG");
		lProps.setProperty("log4j.appender.console.threshold", "DEBUG");
		PropertyConfigurator.configure(lProps);

		mLog.info("jWebSocket SSO (OAuth) Demo Client");

		Configuration lConfig = null;
		boolean lConfigLoaded;
		try {
			// loading properties files
			lConfig = new PropertiesConfiguration("/private/SSO.properties");
		} catch (ConfigurationException ex) {
		}

		if (null == lConfig) {
			mLog.info("Configuration file could not be opened.");
			return;
		}

		final String lSMHost = lConfig.getString("SMHost");
		final String lOAuthHost = lConfig.getString("OAuthHost");
		final String lOAuthAppId = lConfig.getString("OAuthAppId");
		final String lOAuthAppSecret = lConfig.getString("OAuthAppSecret");
		final String lOAuthUsername = lConfig.getString("OAuthUsername");
		final String lOAuthPassword = lConfig.getString("OAuthPassword");
		long lOAuthTimeout = lConfig.getLong("OAuthTimeout", 5000);

		// TODO: Validate config data here!
		lConfigLoaded = true;

		if (!lConfigLoaded) {
			mLog.error("Config not loaded.");
			System.exit(1);
		}

		OAuth lOAuth = new OAuth();

		lOAuth.setOAuthHost(lOAuthHost);
		lOAuth.setOAuthAppId(lOAuthAppId);
		lOAuth.setOAuthAppSecret(lOAuthAppSecret);

//		SiteMinder.setSMHost(lSMHost);
//		String lSMSession = SiteMinder.getSSOSession(5000);
//		mLog.info("Getting SM Session: " + lSMSession);
//
//		if (true) {
//			return;
//		}
		int lMaxTotalProcesses = 1;
		int lMaxUserRequests = 2;
		int lLoopDelay = 1;
		int lRequestDelay = 1;
		Map<String, Object> lJSON;
		for (int lCount = 0; lCount < lMaxTotalProcesses; lCount++) {
			mLog.info("================== " + (lCount + 1) + "/" + lMaxTotalProcesses + " ==================");
			String lSessionCookie = lOAuth.getSSOSession(lOAuthUsername, lOAuthPassword, 5000);
			mLog.info("Getting Session Cookie: " + (null == lSessionCookie ? "[null]"
					: lSessionCookie.replace("\r", "\\r").replace("\n", "\\n")));
			try {
				lJSON = lOAuth.parseJSON(lSessionCookie);
			} catch (IOException ex) {
				mLog.error(ex.getMessage());
				break;
			}
			String lAuthSession = lOAuth.authSession(lOAuth.getSessionId(), 5000);
			mLog.info("Authenticate Session: " + (null == lAuthSession ? "[null]"
					: lAuthSession.replace("\r", "\\r").replace("\n", "\\n")));
			try {
				lJSON = lOAuth.parseJSON(lAuthSession);
			} catch (IOException ex) {
				mLog.error(ex.getMessage());
				break;
			}
			// mLog.info("JSON Direct Authentication: " + lOAuth.authDirect(lOAuthUsername, lOAuthPassword));
			String lAccessToken = lOAuth.getAccessToken();
			mLog.info("JSON Obtaining Bearer Token: " + lAccessToken);
			for (int lGetUserIdx = 0; lGetUserIdx < lMaxUserRequests; lGetUserIdx++) {
				// intentionally cause a failure at last loop iteration
				if (lMaxUserRequests == lGetUserIdx + 1) {
					lAccessToken += "xx";
				}
				try {
					String lUsername = lOAuth.getUser(lAccessToken);
					mLog.info((lGetUserIdx + 1) + " of " + lMaxUserRequests
							+ ": JSON User from Access Token: "
							+ (null == lUsername ? "[null]"
									: lUsername.replace("\r", "\\r").replace("\n", "\\n")));
					try {
						lJSON = lOAuth.parseJSON(lUsername);
					} catch (IOException ex) {
						mLog.error("Parsing JSON: " + ex.getMessage());
						break;
					}
					
					/*
					try {
						String lRes = lOAuth.refreshAccessToken(lOAuthTimeout);
						mLog.info("JSON Refresh Access Token: "
								+ (null == lRes ? "[null]"
										: lRes.replace("\r", "\\r").replace("\n", "\\n")));
					} catch (Exception ex) {
						mLog.error("Refreshing Access Token: " + ex.getMessage());
						break;
					}
					*/
					try {
						Thread.currentThread().sleep(lRequestDelay);
					} catch (InterruptedException lEx) {
					}
				} catch (Exception ex) {
					mLog.error(ex.getMessage());
					break;
				}
			}
		}
	}

}
