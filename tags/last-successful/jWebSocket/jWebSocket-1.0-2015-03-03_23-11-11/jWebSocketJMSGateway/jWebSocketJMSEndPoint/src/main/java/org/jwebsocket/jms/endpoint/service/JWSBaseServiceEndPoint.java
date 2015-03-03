//	---------------------------------------------------------------------------
//	jWebSocket - JMS Gateway Base Service Endpoint (Community Edition, CE)
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
package org.jwebsocket.jms.endpoint.service;

import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jwebsocket.jms.endpoint.JWSAutoSelectAuthenticator;
import org.jwebsocket.jms.endpoint.JWSEndPoint;
import org.jwebsocket.jms.endpoint.JWSLoadBalancerCpuUpdater;
import org.jwebsocket.jms.endpoint.JWSMessageListener;
import org.jwebsocket.jms.endpoint.JWSResponseTokenListener;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * Base class for server endpoint developers facilities.
 *
 * @author kyberneees
 */
public abstract class JWSBaseServiceEndPoint {

	protected final Logger mLog = Logger.getLogger(JWSBaseServiceEndPoint.class);
	private JWSAutoSelectAuthenticator mAuthenticator = new JWSAutoSelectAuthenticator();
	private JWSEndPoint mEndPoint;
	private JWSLoadBalancerCpuUpdater mCpuUpdater;
	private String mServiceNamespace;
	private String mClusterAlias, mClusterPassword;

	public JWSAutoSelectAuthenticator getAuthManager() {
		return mAuthenticator;
	}

	private void setupLog4j() {
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
	}

	public JWSBaseServiceEndPoint(String aServiceNamespace, String aClusterAlias, String aClusterPassword) {
		mServiceNamespace = aServiceNamespace;
		mClusterAlias = aClusterAlias;
		mClusterPassword = aClusterPassword;

		setupLog4j();
	}

	public void setEndPoint(final JWSEndPoint aEndPoint, final String aGatewayUsername,
			final String aGatewayPassword) {
		mEndPoint = aEndPoint;

		// init load balancer CPU updater
		mCpuUpdater = new JWSLoadBalancerCpuUpdater(aEndPoint, aEndPoint.getGatewayId());
		mCpuUpdater.autoStart();

		// capturing welcome event to automatically do login
		aEndPoint.addRequestListener("org.jwebsocket.plugins.system", "welcome", new JWSMessageListener(aEndPoint) {

			@Override
			public void processToken(String aSourceId, Token aToken) {
				// invoking OnWelcome
				OnWelcome();

				Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.system", "login");
				lToken.setString("username", aGatewayUsername);
				lToken.setString("password", aGatewayPassword);

				getEndPoint().sendToken(aEndPoint.getGatewayId(), lToken, new JWSResponseTokenListener() {

					@Override
					public void onSuccess(Token aReponse) {
						// invoking 'OnLogin'
						OnLogin();

						Token lToken = TokenFactory.createToken("org.jwebsocket.plugins.loadbalancer", "registerServiceEndPoint");
						lToken.setString("clusterAlias", mClusterAlias);
						lToken.setString("password", mClusterPassword);

						getEndPoint().sendToken(aEndPoint.getGatewayId(), lToken, new JWSResponseTokenListener() {

							@Override
							public void onSuccess(Token aReponse) {
								OnServiceRegistered();
							}
						});
					}
				});
			}
		});

		// registering service actions listeners
		specifyService();

		// starting endpoint
		aEndPoint.start();
	}

	protected void registerAction(String aActionName, JWSServiceEndPointListener aListener) {
		getEndPoint().addRequestListener(mServiceNamespace, aActionName, aListener);
	}

	public JWSEndPoint getEndPoint() {
		return mEndPoint;
	}

	public Logger getLogger() {
		return mLog;
	}

	public abstract void specifyService();

	public void OnWelcome() {
	}

	public void OnLogin() {

	}

	public void OnServiceRegistered() {

	}

}
