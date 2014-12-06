//	---------------------------------------------------------------------------
//	jWebSocket - LoadBalancerPlugIn (Community Edition, CE)
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
package org.jwebsocket.client.plugins.loadbalancer;

import org.jwebsocket.api.WebSocketTokenClient;
import org.jwebsocket.client.plugins.BaseClientTokenPlugIn;
import org.jwebsocket.client.plugins.sample.SampleServicePlugIn;
import org.jwebsocket.client.token.JWebSocketTokenClient;
import org.jwebsocket.config.JWebSocketClientConstants;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.token.WebSocketResponseTokenListener;

/**
 * Implementation of the LoadBalancerPlugIn class. This client-side plug-in
 * provides the API to access the features of the Load Balancer plug-in on the
 * jWebSocket server.
 *
 * @author Rolando Betancourt Toucet
 */
public class LoadBalancerPlugIn extends BaseClientTokenPlugIn {

	/**
	 *
	 */
	public static String DEFAULT_NS = JWebSocketClientConstants.NS_BASE + ".plugins.loadbalancer";

	/**
	 *
	 * @param aClient
	 * @param aNS
	 */
	public LoadBalancerPlugIn(WebSocketTokenClient aClient, String aNS) {
		super(aClient, aNS);
	}

	/**
	 *
	 * @param aClient
	 */
	public LoadBalancerPlugIn(WebSocketTokenClient aClient) {
		super(aClient, DEFAULT_NS);
	}

	/**
	 * Gets a list (of maps) with the information about all clusters.
	 *
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void clustersInfo(WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "clustersInfo");

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Gets a list of all sticky routes managed by the load balancer.
	 *
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void stickyRoutes(WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "stickyRoutes");

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Changes the type of algorithm used by the load balancer.
	 *
	 * @param aValue
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void changeAlgorithm(int aValue, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "changeAlgorithm");

		lRequest.setInteger("algorithm", aValue);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Registers a new service endpoint in specific cluster.
	 *
	 * @param aPassword
	 * @param aClusterAlias
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void registerServiceEndPoint(String aPassword, String aClusterAlias,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "registerServiceEndPoint");

		lRequest.setString("password", aPassword);
		lRequest.setString("clusterAlias", aClusterAlias);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * De-registers a connected service endpoint.
	 *
	 * @param aPassword
	 * @param aClusterAlias
	 * @param aEndpoinId
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void deregisterServiceEndPoint(String aPassword, String aClusterAlias, String aEndpoinId,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "deregisterServiceEndPoint");

		lRequest.setString("password", aPassword);
		lRequest.setString("clusterAlias", aClusterAlias);
		lRequest.setString("endPointId", aEndpoinId);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Should send a message to the referenced endpoint to gracefully shutdown.
	 *
	 * @param aPassword
	 * @param aClusterAlias
	 * @param aEndpoinId
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void shutdownEndPoint(String aPassword, String aClusterAlias, String aEndpoinId,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "shutdownServiceEndPoint");

		lRequest.setString("password", aPassword);
		lRequest.setString("clusterAlias", aClusterAlias);
		lRequest.setString("endPointId", aEndpoinId);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Create a new sample service endpoint.
	 *
	 * @param aPassword
	 * @param aClusterAlias
	 * @param aEndpointNS
	 * @param aListener
	 * @throws WebSocketException
	 */
	public void sampleService(String aPassword, String aClusterAlias, String aEndpointNS,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		JWebSocketTokenClient lClient = new JWebSocketTokenClient();
		lClient.open(getTokenClient().getURI().toString());
		lClient.login("root", "root");

		SampleServicePlugIn lServiceEndpoint = new SampleServicePlugIn(lClient, aEndpointNS);
		LoadBalancerPlugIn lServiceEndpointPlugIn = new LoadBalancerPlugIn(lServiceEndpoint.getTokenClient());

		lServiceEndpointPlugIn.registerServiceEndPoint(aPassword, aClusterAlias, aListener);
	}
}
