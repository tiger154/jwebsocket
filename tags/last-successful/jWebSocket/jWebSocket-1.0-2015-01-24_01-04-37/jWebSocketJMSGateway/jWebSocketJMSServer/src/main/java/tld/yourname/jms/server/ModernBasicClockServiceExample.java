//	---------------------------------------------------------------------------
//	jWebSocket - JMS Gateway Clock Endpoint demo (Community Edition, CE)
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

import org.jwebsocket.jms.endpoint.JMSEndPoint;
import org.jwebsocket.jms.endpoint.service.JWSBaseServiceEndPoint;
import org.jwebsocket.jms.endpoint.JWSEndPoint;
import org.jwebsocket.jms.endpoint.JWSMemoryAuthenticator;
import org.jwebsocket.jms.endpoint.service.Authenticated;
import org.jwebsocket.jms.endpoint.service.JWSServiceEndPointListener;
import org.jwebsocket.token.Token;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class ModernBasicClockServiceExample {

	public static void main(String[] args) {
		/**
		 * Creating a clock service endpoint (basic example). The service respond to 'getTime'
		 * action by sending the endpoint local time.
		 */
		JWSBaseServiceEndPoint lService = new JWSBaseServiceEndPoint(
				"somecompany.service.clock", // the service namespace
				"clock", // the load balancer services's cluster alias
				"s3cr3t-p@ssw0rd") { // the load balancer services's cluster password

					@Override
					public void specifyService() {
						registerAction("getTime", new JWSServiceEndPointListener(this) {

							/**
							 * Here we give support to the 'getTime' action when invoked from the
							 * client.
							 *
							 * @param aSourceId The remote client id
							 * @param aUser The remote client username if authenticated or NULL if
							 * anonymous
							 * @param aRequest The request token
							 * @param aResponse The response token to be populated
							 * @throws Exception
							 */
							@Override
							/**
							 * If Authenticated annotation is present, request tokens require to be
							 * authenticated against the authentication manager. Access denied
							 * message is automatically sent back.
							 */
							@Authenticated
							public void processToken(String aSourceId, String aUser, Token aRequest,
									Token aResponse) throws Exception {
								aResponse.setLong("time", System.currentTimeMillis());
							}
						});
					}

					@Override
					public void OnLogin() {
						getLogger().info("Service authenticated against the gateway!");
					}

					@Override
					public void OnServiceRegistered() {
						getLogger().info("Service registered in gateway the load balancer!");
					}

				};

		try {
			// setting the endpoint service authenticator, clients commonly require 
			// to authenticate againts endpoint services
			JWSMemoryAuthenticator lMemoryAuth = new JWSMemoryAuthenticator();
			lMemoryAuth.addCredentials("admin", "21232f297a57a5a743894a0e4a801fc3"); //admin:admin
			lService.getAuthManager().addAuthenticator(lMemoryAuth);

			// setting the service endpoint
			lService.setEndPoint(JWSEndPoint.getInstance(
					"failover:(tcp://127.0.0.1:61616)?initialReconnectDelay=100&randomize=false",
					"org.jwebsocket.jms.gateway", // gateway topic
					"org.jwebsocket.jms.gateway", // gateway endpoint id
					"SomeCompany.Clock.Service.Node1", // unique node id
					5, // thread pool size, messages being processed concurrently
					JMSEndPoint.TEMPORARY // durable (for servers) or temporary (for clients)

			),
					"root", // gateway username
					"root" // gateway password
			);
		} catch (Exception lEx) {
			lService.getLogger().error("Error during service bootstrap process: ", lEx);
		}
	}
}
